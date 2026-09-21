package ru.tsaplev.app

import ru.tsaplev.app.JSONToAST.GetJsonToAST
import ru.tsaplev.app.execution.ExecutionVisitor
import ru.tsaplev.app.execution.IntIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class InterpreterProgramTest {
    private fun resourceText(directory: String, fileName: String): String =
        assertNotNull(
            javaClass.getResource("/$directory/$fileName"),
            "Missing test resource: $directory/$fileName"
        ).readText(Charsets.UTF_8)

    private fun assertProgram(programName: String, input: List<Int>, expectedOutput: List<Int>) {
        val source = resourceText("programs", "$programName.in")
        val json = resourceText("json", "$programName.json")
        val io = TestIntIO(input)
        val ast = assertNotNull(GetJsonToAST().startParse(json), "Invalid AST: json/$programName.json")

        assertTrue(source.isNotBlank(), "Empty source program: programs/$programName.in")
        ast.visit(ExecutionVisitor(io))

        assertEquals(expectedOutput, io.output, "Unexpected output for program $programName")
        assertTrue(io.inputConsumed, "Unused input for program $programName: ${io.remainingInput}")
    }

    @Test
    fun `program conditional if without else skips false branch`() {
        val input = emptyList<Int>()
        val expectedOutput = emptyList<Int>()

        assertProgram("conditional_if_without_else", input, expectedOutput)
    }

    @Test
    fun `program io arithmetic combines assignment read and write`() {
        val input = listOf(4)
        val expectedOutput = listOf(10, 14, 16)

        assertProgram("io_arithmetic", input, expectedOutput)
    }

    @Test
    fun `program logical or evaluates true and false cases`() {
        val input = emptyList<Int>()
        val expectedOutput = listOf(1, 0)

        assertProgram("logical_or", input, expectedOutput)
    }

    @Test
    fun `program while loop produces modular sequence`() {
        val input = emptyList<Int>()
        val expectedOutput = listOf(4, 3, 5, 7, 6, 1, 3, 2, 4, 6, 5, 7, 2, 1, 3, 5, 4, 6, 8, 0)

        assertProgram("while_modular_sequence", input, expectedOutput)
    }

    @Test
    fun `program do while executes once before false condition`() {
        val input = emptyList<Int>()
        val expectedOutput = listOf(42)

        assertProgram("do_while_once", input, expectedOutput)
    }

    @Test
    fun `program prime search finds primes below forty`() {
        val input = emptyList<Int>()
        val expectedOutput = listOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37)

        assertProgram("prime_numbers_below_forty", input, expectedOutput)
    }

    @Test
    fun `program arithmetic evaluates operators and nested expressions`() {
        val input = emptyList<Int>()
        val expectedOutput = listOf(12, 2, 35, 3, 2, 30, -3)

        assertProgram("arithmetic", input, expectedOutput)
    }

    @Test
    fun `program comparisons evaluates true and false results`() {
        val input = emptyList<Int>()
        val expectedOutput = listOf(1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0)

        assertProgram("comparisons", input, expectedOutput)
    }

    @Test
    fun `program logic evaluates truth tables and nonzero values`() {
        val input = emptyList<Int>()
        val expectedOutput = listOf(0, 0, 0, 1, 0, 1, 1, 1, 1)

        assertProgram("logic", input, expectedOutput)
    }

    @Test
    fun `program assignments reads variables and applies compound operators`() {
        val input = listOf(20, 3)
        val expectedOutput = listOf(23, 25, 23, 69, 17, 2)

        assertProgram("assignments", input, expectedOutput)
    }

    @Test
    fun `program branches selects then branch`() {
        val input = listOf(11)
        val expectedOutput = listOf(100)

        assertProgram("branches", input, expectedOutput)
    }

    @Test
    fun `program branches selects elif branch`() {
        val input = listOf(10)
        val expectedOutput = listOf(200)

        assertProgram("branches", input, expectedOutput)
    }

    @Test
    fun `program branches selects else branch`() {
        val input = listOf(9)
        val expectedOutput = listOf(300)

        assertProgram("branches", input, expectedOutput)
    }

    @Test
    fun `program while factorial repeats until condition is false`() {
        val input = listOf(5)
        val expectedOutput = listOf(120)

        assertProgram("while_factorial", input, expectedOutput)
    }

    @Test
    fun `program while factorial handles zero iterations`() {
        val input = listOf(0)
        val expectedOutput = listOf(1)

        assertProgram("while_factorial", input, expectedOutput)
    }

    @Test
    fun `program do while repeats its body`() {
        val input = listOf(3)
        val expectedOutput = listOf(3, 2, 1)

        assertProgram("do_while", input, expectedOutput)
    }

    @Test
    fun `program do while executes once for zero input`() {
        val input = listOf(0)
        val expectedOutput = listOf(0)

        assertProgram("do_while", input, expectedOutput)
    }

    @Test
    fun `program for sum iterates and accumulates values`() {
        val input = listOf(5)
        val expectedOutput = listOf(0, 1, 2, 3, 4, 10)

        assertProgram("for_sum", input, expectedOutput)
    }

    @Test
    fun `program for sum handles zero iterations`() {
        val input = listOf(0)
        val expectedOutput = listOf(0)

        assertProgram("for_sum", input, expectedOutput)
    }

    @Test
    fun `program nested control traverses grid with conditions`() {
        val input = listOf(3, 4)
        val expectedOutput = listOf(6)

        assertProgram("nested_control", input, expectedOutput)
    }

    @Test
    fun `program nested control handles empty outer loop`() {
        val input = listOf(0, 5)
        val expectedOutput = listOf(0)

        assertProgram("nested_control", input, expectedOutput)
    }

    @Test
    fun `program optional syntax supports comments semicolons and missing else`() {
        val input = emptyList<Int>()
        val expectedOutput = listOf(1)

        assertProgram("optional_syntax", input, expectedOutput)
    }

    @Test
    fun `program blocks and skip preserve nested execution order`() {
        val input = emptyList<Int>()
        val expectedOutput = listOf(2)

        assertProgram("blocks_skip", input, expectedOutput)
    }

    @Test
    fun `program stress collatz tracks long trajectory statistics`() {
        val input = listOf(27)
        val expectedOutput = listOf(82, 137, 1336, 2158, 577, 160, 111, 9232, 41, 70, 7518)

        assertProgram("stress_collatz", input, expectedOutput)
    }

    @Test
    fun `program stress collatz handles finished trajectory`() {
        val input = listOf(1)
        val expectedOutput = listOf(0, 1, 0, 0, 0)

        assertProgram("stress_collatz", input, expectedOutput)
    }

    @Test
    fun `program stress primes calculates primes statistics and checksum`() {
        val input = listOf(100)
        val expectedOutput = listOf(2, 11, 31, 41, 61, 71, 25, 1060, 64660)

        assertProgram("stress_primes", input, expectedOutput)
    }

    @Test
    fun `program stress primes handles empty range`() {
        val input = listOf(1)
        val expectedOutput = listOf(0, 0, 0)

        assertProgram("stress_primes", input, expectedOutput)
    }

    @Test
    fun `program stress state machine follows seed forty two path`() {
        val input = listOf(42)
        val expectedOutput = listOf(46, 435, 642, 833, 1008, 1231, 22, 1539, 13, 17)

        assertProgram("stress_state_machine", input, expectedOutput)
    }

    @Test
    fun `program stress state machine follows zero seed path`() {
        val input = listOf(0)
        val expectedOutput = listOf(22, 143, 518, 605, 848, 1053, 42, 1371, 15, 15)

        assertProgram("stress_state_machine", input, expectedOutput)
    }

    @Test
    fun `program stress grid traverses seven by six cells`() {
        val input = listOf(7, 6)
        val expectedOutput = listOf(11, 27, 126, 126, 208, 234, 248, 260, 260, 17, 14)

        assertProgram("stress_grid", input, expectedOutput)
    }

    @Test
    fun `program stress grid handles empty outer dimension`() {
        val input = listOf(0, 8)
        val expectedOutput = listOf(0, 0, 0)

        assertProgram("stress_grid", input, expectedOutput)
    }

    @Test
    fun `program stress numeric pipeline combines all loop forms`() {
        val input = listOf(7, 126)
        val expectedOutput = listOf(5040, 9, 405, 4, 126, 1)

        assertProgram("stress_numeric_pipeline", input, expectedOutput)
    }

    @Test
    fun `program stress numeric pipeline reaches category two`() {
        val input = listOf(4, 18)
        val expectedOutput = listOf(24, 6, 42, 2, 6, 2)

        assertProgram("stress_numeric_pipeline", input, expectedOutput)
    }

    @Test
    fun `program stress numeric pipeline handles zero bound`() {
        val input = listOf(0, 17)
        val expectedOutput = listOf(1, 1, 1, 1, 1, 1)

        assertProgram("stress_numeric_pipeline", input, expectedOutput)
    }

    private class TestIntIO(input: List<Int>) : IntIO {
        private val inputQueue = ArrayDeque(input)
        val output = mutableListOf<Int>()

        val inputConsumed: Boolean
            get() = inputQueue.isEmpty()

        val remainingInput: List<Int>
            get() = inputQueue.toList()

        override fun write(output: Int?) {
            this.output.add(requireNotNull(output) { "Interpreter attempted to write null" })
        }

        override fun read(): Int =
            inputQueue.removeFirstOrNull()
                ?: error("Interpreter requested more input than the test provides")
    }
}
