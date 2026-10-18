package ru.tsaplev.app

import java.io.File
import ru.tsaplev.app.ASTreeNodes.ASTArray
import ru.tsaplev.app.ASTreeNodes.ASTAssign
import ru.tsaplev.app.ASTreeNodes.ASTBinOP
import ru.tsaplev.app.ASTreeNodes.ASTConst
import ru.tsaplev.app.ASTreeNodes.ASTElem
import ru.tsaplev.app.ASTreeNodes.ASTFuncCall
import ru.tsaplev.app.ASTreeNodes.ASTLength
import ru.tsaplev.app.ASTreeNodes.ASTSeq
import ru.tsaplev.app.ASTreeNodes.ASTSet
import ru.tsaplev.app.ASTreeNodes.ASTString
import ru.tsaplev.app.ASTreeNodes.ASTVar
import ru.tsaplev.app.JSONToAST.GetJsonToAST
import ru.tsaplev.app.execution.CompilationVisitor
import ru.tsaplev.app.execution.DataIO
import ru.tsaplev.app.execution.DataValue
import ru.tsaplev.app.execution.ExecutionVisitor
import ru.tsaplev.app.execution.parseDataValue
import ru.tsaplev.app.execution.stackMachine.StackMachine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

private class DataIOMock(input: List<DataValue> = emptyList()): DataIO {
    private val inputQueue = ArrayDeque(input)
    val output = mutableListOf<DataValue>()

    override fun write(output: DataValue?, end: String) {
        this.output.add(requireNotNull(output))
    }

    override fun read(): DataValue = inputQueue.removeFirst()
}

class StringArrayInterpreterTest {
    @Test
    fun stringsAndArraysSupportLanguageOperations() {
        val program = sequence(
            ASTAssign(
                "values",
                ASTArray(listOf(ASTConst(int(1)), ASTString("old")))
            ),
            ASTSet(ASTVar("values"), ASTString("new"), ASTConst(int(1))),
            ASTFuncCall("write", ASTElem(ASTVar("values"), ASTConst(int(1)))),
            ASTFuncCall("write", ASTLength(ASTVar("values"))),
            ASTFuncCall("write", ASTLength(ASTString("hello"))),
            ASTFuncCall("write", ASTBinOP("+", ASTString("hello"), ASTString(" world"))),
            ASTFuncCall(
                "write",
                ASTBinOP(
                    "+",
                    ASTVar("values"),
                    ASTArray(listOf(ASTConst(int(3))))
                )
            ),
            ASTFuncCall("write", ASTBinOP("<", ASTString("abc"), ASTString("abd")))
        )

        val io = DataIOMock()
        program.visit(ExecutionVisitor(io))

        assertEquals(
            listOf(
                string("new"),
                int(2),
                int(5),
                string("hello world"),
                array(int(1), string("new"), int(3)),
                int(1)
            ),
            io.output
        )
    }

    @Test
    fun nestedInputParserHandlesArraysStringsAndNegativeNumbers() {
        assertEquals(
            array(
                int(-2),
                array(int(3), string("a; b")),
                string("plain text")
            ),
            parseDataValue("[-2; [3, \"a; b\"]; plain text]")
        )
    }

    @Test
    fun compilerAndStackMachineSupportStringsAndArrays() {
        val program = sequence(
            ASTAssign("values", ASTArray(listOf(ASTString("a"), ASTString("b")))),
            ASTSet(ASTVar("values"), ASTString("changed"), ASTConst(int(0))),
            ASTFuncCall("write", ASTElem(ASTVar("values"), ASTConst(int(0)))),
            ASTFuncCall("write", ASTLength(ASTVar("values")))
        )
        val directIO = DataIOMock()
        program.visit(ExecutionVisitor(directIO))

        val compiler = CompilationVisitor()
        program.visit(compiler)
        val serialized = StackInstructionsToJSON(compiler.instructions).toString()
        val restoredInstructions = JSONToStackInstructions(serialized)
        val compiledIO = DataIOMock()
        StackMachine().run(restoredInstructions, compiledIO)

        assertEquals(directIO.output, compiledIO.output)
        assertEquals(listOf(string("changed"), int(2)), compiledIO.output)
    }

    @Test
    fun nestedArraySumExampleWorksDirectlyAndCompiled() {
        val source = repositoryExample("nested_array_sum.in")
        val json = repositoryExample("nested_array_sum.json")
        val ast = assertNotNull(GetJsonToAST().startParse(json))
        assertNotNull(source.takeIf { it.isNotBlank() })

        val directIO = DataIOMock()
        ast.visit(ExecutionVisitor(directIO))

        val compiler = CompilationVisitor()
        ast.visit(compiler)
        val compiledIO = DataIOMock()
        StackMachine().run(compiler.instructions, compiledIO)

        assertEquals(listOf<DataValue>(int(15)), directIO.output)
        assertEquals(directIO.output, compiledIO.output)
    }

    private fun repositoryExample(name: String): String {
        val workingDirectory = File(System.getProperty("user.dir"))
        val file = sequenceOf(
            File(workingDirectory, "examples/$name"),
            File(workingDirectory.parentFile, "examples/$name")
        ).firstOrNull(File::isFile)
        return assertNotNull(file, "Missing example: examples/$name").readText(Charsets.UTF_8)
    }

    private fun sequence(vararg nodes: ASTNode): ASTNode =
        nodes.reduceRight { node, rest -> ASTSeq(node, rest) }

    private fun int(value: Int) = DataValue.IntValue(value)
    private fun string(value: String) = DataValue.StringValue(value)
    private fun array(vararg values: DataValue) = DataValue.ArrayValue(values.toMutableList())
}
