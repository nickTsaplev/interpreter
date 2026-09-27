import ru.tsaplev.app.JSONToAST.GetJsonToAST
import ru.tsaplev.app.JSONToStackInstructions
import ru.tsaplev.app.StackInstructionsToJSON
import ru.tsaplev.app.ASTreeNodes.ASTAssign
import ru.tsaplev.app.ASTreeNodes.ASTBinOP
import ru.tsaplev.app.ASTreeNodes.ASTConst
import ru.tsaplev.app.ASTreeNodes.ASTDo
import ru.tsaplev.app.ASTreeNodes.ASTFuncCall
import ru.tsaplev.app.ASTreeNodes.ASTIf
import ru.tsaplev.app.ASTreeNodes.ASTSeq
import ru.tsaplev.app.ASTreeNodes.ASTSkip
import ru.tsaplev.app.ASTreeNodes.ASTVar
import ru.tsaplev.app.ASTreeNodes.ASTWhile
import ru.tsaplev.app.execution.CompilationVisitor
import ru.tsaplev.app.execution.DataValue
import ru.tsaplev.app.execution.ExecutionVisitor
import ru.tsaplev.app.execution.IntIO
import ru.tsaplev.app.execution.stackMachine.MachineInstruction
import ru.tsaplev.app.execution.stackMachine.StackMachine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

private class StackIOMock(private val input: List<Int>) : IntIO {
    val output = mutableListOf<Int?>()
    private var index = 0

    override fun read(): Int = input[index++]

    override fun write(output: Int?) {
        this.output.add(output)
    }
}

class StackMachineTests {
    @Test
    fun executesFirstInstructionAndKeepsOperandOrder() {
        val io = StackIOMock(listOf())
        val instructions = listOf(
            MachineInstruction.CONST(7),
            MachineInstruction.CONST(2),
            MachineInstruction.BINOP("-"),
            MachineInstruction.WRITE()
        )

        StackMachine().run(instructions, io)

        assertEquals(listOf<Int?>(5), io.output)
    }

    @Test
    fun readsAndWritesJsonCommands() {
        val io = StackIOMock(listOf(13))
        StackMachine().run(JSONToStackInstructions("""["READ", "WRITE"]"""), io)

        assertEquals(listOf<Int?>(13), io.output)
        assertFailsWith<IllegalArgumentException> {
            JSONToStackInstructions("""["UNKNOWN"]""")
        }
    }

    @Test
    fun compiledProgramMatchesAstInterpreter() {
        val text = """
            {
              "seq": {
                "left": { "read": "x" },
                "right": {
                  "write": {
                    "binop": "*",
                    "left": { "var": "x" },
                    "right": { "const": 3 }
                  }
                }
              }
            }
        """.trimIndent()
        val ast = GetJsonToAST().startParse(text)
        assertNotNull(ast)

        val directIO = StackIOMock(listOf(4))
        ast.visit(ExecutionVisitor(directIO))

        val compiler = CompilationVisitor()
        ast.visit(compiler)
        val instructions = JSONToStackInstructions(StackInstructionsToJSON(compiler.instructions).toString())
        val compiledIO = StackIOMock(listOf(4))
        StackMachine().run(instructions, compiledIO)

        assertEquals(directIO.output, compiledIO.output)
    }

    @Test
    fun consecutiveConditionsHaveDistinctLabels() {
        val ast = ASTSeq(
            ASTIf(ASTConst(DataValue(0)), ASTSkip(), ASTFuncCall("write", ASTConst(DataValue(1)))),
            ASTIf(ASTConst(DataValue(0)), ASTSkip(), ASTFuncCall("write", ASTConst(DataValue(2))))
        )
        val compiler = CompilationVisitor()
        ast.visit(compiler)
        val labels = compiler.instructions.filterIsInstance<MachineInstruction.LABEL>().map { it.label }

        assertEquals(labels.size, labels.toSet().size)

        val io = StackIOMock(listOf())
        StackMachine().run(compiler.instructions, io)
        assertEquals(listOf<Int?>(1, 2), io.output)
    }

    @Test
    fun compiledWhileAndDoLoopsRun() {
        val ast = ASTSeq(
            ASTAssign("i", ASTConst(DataValue(0))),
            ASTSeq(
                ASTWhile(
                    ASTBinOP("<", ASTVar("i"), ASTConst(DataValue(3))),
                    ASTAssign("i", ASTBinOP("+", ASTVar("i"), ASTConst(DataValue(1))))
                ),
                ASTDo(ASTFuncCall("write", ASTVar("i")), ASTConst(DataValue(0)))
            )
        )
        val compiler = CompilationVisitor()
        ast.visit(compiler)
        val io = StackIOMock(listOf())

        StackMachine().run(compiler.instructions, io)

        assertEquals(listOf<Int?>(3), io.output)
    }
}
