import ru.tsaplev.app.JSONToAST.GetJsonToAST
import ru.tsaplev.app.execution.ExecutionVisitor
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

abstract class ProgramTest {
    fun testProgram(text: String, input: List<Int>, expectedOutput: List<Int>) {
        val jsonToASTer = GetJsonToAST()

        val ast = jsonToASTer.startParse(text)

        assertNotNull(ast)

        val io = ListIOMock(input)
        val visitor = ExecutionVisitor(io)

        ast.visit(visitor)

        assertEquals<List<Int?>>(io.output, expectedOutput)
    }
}