package ru.tsaplev.app

import ru.tsaplev.app.JSONToAST.GetJsonToAST
import ru.tsaplev.app.execution.DataIO
import ru.tsaplev.app.execution.DataValue
import ru.tsaplev.app.execution.ExecutionVisitor
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

}
