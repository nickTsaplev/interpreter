package ru.tsaplev.app

import ru.tsaplev.app.JSONToAST.GetJsonToAST
import ru.tsaplev.app.execution.DataIO
import ru.tsaplev.app.execution.DataValue
import ru.tsaplev.app.execution.ExecutionVisitor
import ru.tsaplev.app.execution.CompilationVisitor
import ru.tsaplev.app.execution.stackMachine.StackMachine
import kotlin.test.*

class DynamicScopeInterpreterTest {
    private class IO(input: List<Int> = emptyList()) : DataIO {
        private val input = ArrayDeque(input)
        val output = mutableListOf<DataValue>()
        var reads = 0
        override fun read(): DataValue {
            reads++
            return DataValue.IntValue(input.removeFirst())
        }
        override fun write(output: DataValue?, end: String) {
            this.output += requireNotNull(output)
        }
    }

    private fun parse(json: String) = assertNotNull(GetJsonToAST().startParse(json), json)
    private fun run(json: String, io: IO = IO()): IO {
        parse(json).visit(ExecutionVisitor(io))
        return io
    }
    private fun seq(vararg nodes: String): String = nodes.reduceRight { left, right ->
        """{"seq":{"left":$left,"right":$right}}"""
    }
    private fun block(vararg nodes: String) = """{"block":[${nodes.joinToString(",")}]}"""
    private fun c(value: Int) = """{"const":$value}"""
    private fun v(name: String) = """{"var":"$name"}"""
    private fun decl(name: String, value: String) = """{"decl":{"name":"$name","value":$value}}"""
    private fun assign(name: String, value: String) = """{"assn":{"dst":"$name","src":$value}}"""
    private fun write(value: String) = """{"write":$value}"""
    private fun function(name: String, params: List<String>, body: String): String {
        val names = params.joinToString(",") { "\"$it\"" }
        return """{"decl":{"name":"$name","params":[$names],"body":$body}}"""
    }
    private fun call(name: String, vararg args: String, statement: Boolean = false): String {
        val tag = if (statement) "callStmt" else "call"
        return """{"$tag":{"name":"$name","args":[${args.joinToString(",")}]}}"""
    }
    private fun ret(value: String = "null") = """{"return":$value}"""
    private fun bin(op: String, left: String, right: String) =
        """{"binop":"$op","left":$left,"right":$right}"""
    private fun expect(io: IO, vararg values: Int) =
        assertEquals<List<DataValue>>(values.map { DataValue.IntValue(it) }, io.output)

    @Test
    fun declarationAndAssignmentAreSeparateOperations() {
        expect(run(seq(decl("x", c(1)), assign("x", c(7)), write(v("x")))), 7)
        val visitor = ExecutionVisitor(IO())
        assertFailsWith<IllegalStateException> { parse(assign("missing", c(2))).visit(visitor) }
        assertFailsWith<IllegalStateException> { parse(write(v("missing"))).visit(visitor) }
    }

    @Test
    fun duplicateDeclarationDoesNotOverwriteExistingValue() {
        val io = IO()
        val visitor = ExecutionVisitor(io)
        parse(decl("x", c(4))).visit(visitor)
        assertFailsWith<IllegalStateException> { parse(decl("x", c(9))).visit(visitor) }
        parse(write(v("x"))).visit(visitor)
        expect(io, 4)
    }

    @Test
    fun blocksShadowNamesAndAssignmentsUpdateNearestDeclaration() {
        expect(run(seq(
            decl("x", c(1)),
            block(decl("x", c(2)), block(assign("x", c(3))), write(v("x"))),
            write(v("x")), block(assign("x", c(8))), write(v("x"))
        )), 3, 1, 8)
    }

    @Test
    fun blockScopeIsRemovedAfterFailure() {
        val io = IO()
        val visitor = ExecutionVisitor(io)
        parse(decl("x", c(1))).visit(visitor)
        assertFailsWith<IllegalStateException> {
            parse(block(decl("x", c(2)), write(v("missing")))).visit(visitor)
        }
        parse(write(v("x"))).visit(visitor)
        assertFailsWith<IllegalStateException> {
            parse(seq(block(decl("local", c(5))), write(v("local")))).visit(visitor)
        }
        expect(io, 1)
    }

    @Test
    fun readUpdatesDynamicBindingAndRejectsUndeclaredDestinationBeforeReading() {
        val io = IO(listOf(17))
        val visitor = ExecutionVisitor(io)
        assertFailsWith<IllegalStateException> { parse("""{"read":"missing"}""").visit(visitor) }
        assertEquals(0, io.reads)
        parse(seq(decl("x", c(0)), block("""{"read":"x"}"""), write(v("x")))).visit(visitor)
        expect(io, 17)
    }

    @Test
    fun sequenceDoesNotCreateAnImplicitScope() {
        expect(run(seq(seq(decl("x", c(6)), """{"skip":null}"""), write(v("x")))), 6)
    }

    @Test
    fun functionsReadAndModifyTheCallersScopeRatherThanTheDefinitionScope() {
        expect(run(seq(
            decl("x", c(1)),
            function("bump", emptyList(), seq(assign("x", bin("+", v("x"), c(1))), ret(v("x")))),
            function("caller", listOf("x"), ret(call("bump"))),
            write(call("caller", c(40))), write(v("x"))
        )), 41, 1)
    }

    @Test
    fun recursiveCallsHaveIndependentParametersAndReturnValues() {
        val body = """{"if":{"cond":${bin("<=", v("n"), c(1))},"then":${ret(c(1))},"else":${ret(
            bin("*", v("n"), call("fact", bin("-", v("n"), c(1))))
        )}}}"""
        expect(run(seq(function("fact", listOf("n"), body), write(call("fact", c(6))))), 720)
    }

    @Test
    fun argumentsEvaluateLeftToRightInCallerBeforeParameterBinding() {
        expect(run(seq(
            decl("x", c(5)), decl("counter", c(0)),
            function("next", emptyList(), seq(
                assign("counter", bin("+", v("counter"), c(1))), ret(v("counter"))
            )),
            function("pair", listOf("x", "y"), ret(bin("+", bin("*", v("x"), c(10)), v("y")))),
            write(call("pair", c(2), v("x"))),
            write(call("pair", call("next"), call("next")))
        )), 25, 12)
    }

    @Test
    fun statementCallsDiscardResultsAndVoidFunctionsCannotBeUsedAsValues() {
        val io = IO()
        val visitor = ExecutionVisitor(io)
        parse(seq(
            function("answer", emptyList(), ret(c(42))),
            function("void", emptyList(), """{"skip":null}"""),
            call("answer", statement = true), call("void", statement = true),
            write(bin("+", c(3), call("answer")))
        )).visit(visitor)
        assertFailsWith<IllegalStateException> { parse(write(call("void"))).visit(visitor) }
        parse(write(call("answer"))).visit(visitor)
        expect(io, 45, 42)
    }

    @Test
    fun wrongArityAndUnknownFunctionsFailBeforeEvaluatingArguments() {
        val io = IO()
        val visitor = ExecutionVisitor(io)
        parse(seq(decl("count", c(0)), function("one", listOf("x"), ret(v("x"))),
            function("next", emptyList(), seq(assign("count", c(9)), ret(c(1)))))).visit(visitor)
        for (invalid in listOf(call("one"), call("one", call("next"), c(2)),
            call("missing", call("next")), call("write"))) {
            assertFailsWith<IllegalArgumentException> { parse(invalid).visit(visitor) }
        }
        parse(write(v("count"))).visit(visitor)
        expect(io, 0)
    }

    @Test
    fun returnUnwindsLoopsBlocksAndSkipsRemainingStatements() {
        val loop = """{"while":{"cond":${c(1)},"body":${block(decl("x", c(8)), ret(v("x")))}}}"""
        expect(run(seq(decl("x", c(1)), function("early", emptyList(),
            seq(loop, assign("x", c(99)))), write(call("early")), write(v("x")))), 8, 1)
    }

    @Test
    fun failureUnwindsFunctionParametersAndLocalDeclarations() {
        val io = IO()
        val visitor = ExecutionVisitor(io)
        parse(seq(decl("x", c(1)), function("broken", listOf("x"),
            block(decl("local", c(9)), write(v("missing")))))).visit(visitor)
        assertFailsWith<IllegalStateException> { parse(call("broken", c(2))).visit(visitor) }
        parse(write(v("x"))).visit(visitor)
        assertFailsWith<IllegalStateException> { parse(write(v("local"))).visit(visitor) }
        expect(io, 1)
    }

    @Test
    fun localFunctionsAreDynamicallyResolvedAndDoNotEscapeBlocks() {
        val io = IO()
        val visitor = ExecutionVisitor(io)
        parse(seq(function("f", emptyList(), ret(c(1))),
            function("invoke", emptyList(), ret(call("f"))),
            block(function("f", emptyList(), ret(c(2))), write(call("invoke")),
                function("local", emptyList(), ret(c(3)))),
            write(call("invoke"))
        )).visit(visitor)
        assertFailsWith<IllegalArgumentException> { parse(call("local")).visit(visitor) }
        expect(io, 2, 1)
    }

    @Test
    fun invalidDeclarationsAndReturnOutsideFunctionAreRejected() {
        val visitor = ExecutionVisitor(IO())
        parse(function("f", emptyList(), ret(c(1)))).visit(visitor)
        assertFailsWith<IllegalStateException> {
            parse(function("f", emptyList(), ret(c(2)))).visit(visitor)
        }
        assertFailsWith<IllegalArgumentException> {
            parse(function("write", emptyList(), ret(c(2)))).visit(visitor)
        }
        assertFailsWith<IllegalStateException> { parse(ret(c(1))).visit(visitor) }
        assertNull(GetJsonToAST().startParse(function("bad", listOf("x", "x"), ret(c(1)))))
    }

    @Test
    fun bareReturnIsVoidAndFunctionDeclarationsDoNotExecuteBodies() {
        expect(run(seq(
            decl("x", c(1)), function("noop", emptyList(), seq(ret(), assign("x", c(9)))),
            function("uninvoked", emptyList(), assign("x", c(7))),
            call("noop", statement = true), write(v("x"))
        )), 1)
    }

    @Test
    fun switchBindingsAreDynamicAndBranchDeclarationsDoNotEscape() {
        val io = IO()
        val visitor = ExecutionVisitor(io)
        val branch = seq(decl("branchLocal", c(7)), write(call("bump")))
        val switch = """{"switch":{"scrutinee":${c(9)},"branches":[
            {"pattern":{"bind":{"name":"x","pattern":{"wildcard":true}}},"branch":$branch}
        ]}}"""
        parse(seq(decl("x", c(1)), function("bump", emptyList(),
            seq(assign("x", bin("+", v("x"), c(1))), ret(v("x")))),
            switch, write(v("x"))
        )).visit(visitor)
        assertFailsWith<IllegalStateException> { parse(write(v("branchLocal"))).visit(visitor) }
        expect(io, 10, 1)
    }

    @Test
    fun returnThroughSwitchRestoresBindings() {
        val switch = """{"switch":{"scrutinee":${c(9)},"branches":[
            {"pattern":{"bind":{"name":"x","pattern":{"wildcard":true}}},"branch":${ret(v("x"))}}
        ]}}"""
        expect(run(seq(decl("x", c(1)), function("select", emptyList(), switch),
            write(call("select")), write(v("x")))), 9, 1)
    }

    @Test
    fun switchExpressionsPreserveBranchValuesAndSurroundingOperands() {
        val switch = """{"switch":{"scrutinee":${c(9)},"branches":[
            {"pattern":{"wildcard":true},"branch":${c(32)}}
        ]}}"""
        val program = parse(write(bin("+", c(10), switch)))
        val directIO = IO()
        program.visit(ExecutionVisitor(directIO))
        val compiler = CompilationVisitor()
        program.visit(compiler)
        val compiledIO = IO()
        StackMachine().run(compiler.instructions, compiledIO)
        expect(directIO, 42)
        assertEquals(compiledIO.output, directIO.output)
    }

    @Test
    fun switchExpressionsCanReturnAFunctionResultWithoutLeakingBindings() {
        val switch = """{"switch":{"scrutinee":${c(32)},"branches":[
            {"pattern":{"bind":{"name":"x","pattern":{"wildcard":true}}},"branch":${call("get")}}
        ]}}"""
        expect(run(seq(decl("x", c(1)), function("get", emptyList(), ret(v("x"))),
            write(bin("+", c(10), switch)), write(v("x")))), 42, 1)
    }

    @Test
    fun statementCallsLeaveNoHiddenValuesAmongExpressionOperands() {
        val argument = seq(call("value", statement = true), c(2))
        expect(run(seq(function("value", emptyList(), ret(c(99))),
            function("id", listOf("x"), ret(v("x"))),
            write(bin("+", c(40), call("id", argument)))
        )), 42)
    }

    @Test
    fun functionsCanReturnStringsArraysAndSExpressions() {
        val io = run(seq(
            function("id", listOf("x"), ret(v("x"))),
            write(call("id", """{"string":"hello"}""")),
            write(call("id", """{"array":[{"const":1},{"const":2}]}""")),
            write(call("id", """{"sexp":"Box","args":[{"const":3}]}"""))
        ))
        assertEquals(listOf<DataValue>(DataValue.StringValue("hello"),
            DataValue.ArrayValue(mutableListOf(DataValue.IntValue(1), DataValue.IntValue(2))),
            DataValue.SExpressionValue("Box", mutableListOf(DataValue.IntValue(3)))), io.output)
    }
}
