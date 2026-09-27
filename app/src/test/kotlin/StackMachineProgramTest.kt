package ru.tsaplev.app

import ru.tsaplev.app.execution.IntIO
import ru.tsaplev.app.execution.stackMachine.StackMachine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StackMachineProgramTest {
    private fun resourceText(directory: String, fileName: String): String =
        assertNotNull(
            javaClass.getResource("/$directory/$fileName"),
            "Missing test resource: $directory/$fileName"
        ).readText(Charsets.UTF_8)

    private fun assertProgram(
        programName: String,
        input: List<Int>,
        expectedOutput: List<Int>
    ) {
        val source = resourceText("programs", "$programName.in")
        val json = resourceText("stack-json", "$programName.json")
        val io = TestIntIO(input)
        val commands = JSONToStackInstructions(json)

        assertTrue(
            source.isNotBlank(),
            "Empty source program: programs/$programName.in"
        )

        StackMachine().run(commands, io)

        assertEquals(
            expectedOutput,
            io.output,
            "Unexpected output for program $programName"
        )

        assertTrue(
            io.inputConsumed,
            "Unused input for program $programName: ${io.remainingInput}"
        )
    }

    @Test
    fun arithmeticEvaluatesOperatorsAndNestedExpressions() {
        val input = emptyList<Int>()
        val expectedOutput = listOf(12, 2, 35, 3, 2, 30, -3)

        assertProgram("arithmetic", input, expectedOutput)
    }

    @Test
    fun ioArithmeticCombinesAssignmentReadAndWrite() {
        val input = listOf(4)
        val expectedOutput = listOf(10, 14, 16)

        assertProgram("io_arithmetic", input, expectedOutput)
    }

    @Test
    fun branchesSelectCorrectBranch() {
        assertProgram("branches", listOf(11), listOf(100))
    }

    @Test
    fun branchesSelectElseIfBranch() {
        assertProgram("branches", listOf(10), listOf(200))
    }

    @Test
    fun branchesSelectElseBranch() {
        assertProgram("branches", listOf(9), listOf(300))
    }

    @Test
    fun doWhileRepeatsUntilConditionIsFalse() {
        assertProgram("do_while", listOf(3), listOf(3, 2, 1))
    }

    @Test
    fun doWhileExecutesBodyBeforeCheckingFalseCondition() {
        assertProgram("do_while", listOf(0), listOf(0))
    }

    @Test
    fun whileLoopAccumulatesFactorial() {
        assertProgram("while_factorial", listOf(5), listOf(120))
    }

    @Test
    fun whileLoopSkipsBodyWhenConditionIsInitiallyFalse() {
        assertProgram("while_factorial", listOf(0), listOf(1))
    }

    private class TestIntIO(input: List<Int>) : IntIO {
        private val inputQueue = ArrayDeque(input)
        val output = mutableListOf<Int>()

        val inputConsumed: Boolean
            get() = inputQueue.isEmpty()

        val remainingInput: List<Int>
            get() = inputQueue.toList()

        override fun write(output: Int?) {
            this.output.add(
                requireNotNull(output) {
                    "Interpreter attempted to write null"
                }
            )
        }

        override fun read(): Int =
            inputQueue.removeFirstOrNull()
                ?: error("Interpreter requested more input than the test provides")
    }
}
