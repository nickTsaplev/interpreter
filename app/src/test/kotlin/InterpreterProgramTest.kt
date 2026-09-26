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
    fun conditionalIfWithoutElseSkipsFalseBranch() {
        val input = emptyList<Int>()
        val expectedOutput = emptyList<Int>()

        assertProgram("conditional_if_without_else", input, expectedOutput)
    }

    @Test
    fun ioArithmeticCombinesAssignmentReadAndWrite() {
        val input = listOf(4)
        val expectedOutput = listOf(10, 14, 16)

        assertProgram("io_arithmetic", input, expectedOutput)
    }

    @Test
    fun logicalOrEvaluatesTrueAndFalseCases() {
        val input = emptyList<Int>()
        val expectedOutput = listOf(1, 0)

        assertProgram("logical_or", input, expectedOutput)
    }

    @Test
    fun whileLoopProducesModularSequence() {
        val input = emptyList<Int>()
        val expectedOutput = listOf(4, 3, 5, 7, 6, 1, 3, 2, 4, 6, 5, 7, 2, 1, 3, 5, 4, 6, 8, 0)

        assertProgram("while_modular_sequence", input, expectedOutput)
    }

    @Test
    fun doWhileExecutesOnceBeforeFalseCondition() {
        val input = emptyList<Int>()
        val expectedOutput = listOf(42)

        assertProgram("do_while_once", input, expectedOutput)
    }

    @Test
    fun primeSearchFindsPrimesBelowForty() {
        val input = emptyList<Int>()
        val expectedOutput = listOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37)

        assertProgram("prime_numbers_below_forty", input, expectedOutput)
    }

    @Test
    fun arithmeticEvaluatesOperatorsAndNestedExpressions() {
        val input = emptyList<Int>()
        val expectedOutput = listOf(12, 2, 35, 3, 2, 30, -3)

        assertProgram("arithmetic", input, expectedOutput)
    }

    @Test
    fun comparisonsEvaluateTrueAndFalseResults() {
        val input = emptyList<Int>()
        val expectedOutput = listOf(1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0)

        assertProgram("comparisons", input, expectedOutput)
    }

    @Test
    fun logicEvaluatesTruthTablesAndNonzeroValues() {
        val input = emptyList<Int>()
        val expectedOutput = listOf(0, 0, 0, 1, 0, 1, 1, 1, 1)

        assertProgram("logic", input, expectedOutput)
    }

    @Test
    fun assignmentsReadVariablesAndApplyCompoundOperators() {
        val input = listOf(20, 3)
        val expectedOutput = listOf(23, 25, 23, 69, 17, 2)

        assertProgram("assignments", input, expectedOutput)
    }

    @Test
    fun branchesSelectThenBranch() {
        val input = listOf(11)
        val expectedOutput = listOf(100)

        assertProgram("branches", input, expectedOutput)
    }

    @Test
    fun branchesSelectElifBranch() {
        val input = listOf(10)
        val expectedOutput = listOf(200)

        assertProgram("branches", input, expectedOutput)
    }

    @Test
    fun branchesSelectElseBranch() {
        val input = listOf(9)
        val expectedOutput = listOf(300)

        assertProgram("branches", input, expectedOutput)
    }

    @Test
    fun whileFactorialRepeatsUntilConditionIsFalse() {
        val input = listOf(5)
        val expectedOutput = listOf(120)

        assertProgram("while_factorial", input, expectedOutput)
    }

    @Test
    fun whileFactorialHandlesZeroIterations() {
        val input = listOf(0)
        val expectedOutput = listOf(1)

        assertProgram("while_factorial", input, expectedOutput)
    }

    @Test
    fun doWhileRepeatsItsBody() {
        val input = listOf(3)
        val expectedOutput = listOf(3, 2, 1)

        assertProgram("do_while", input, expectedOutput)
    }

    @Test
    fun doWhileExecutesOnceForZeroInput() {
        val input = listOf(0)
        val expectedOutput = listOf(0)

        assertProgram("do_while", input, expectedOutput)
    }

    @Test
    fun forSumIteratesAndAccumulatesValues() {
        val input = listOf(5)
        val expectedOutput = listOf(0, 1, 2, 3, 4, 10)

        assertProgram("for_sum", input, expectedOutput)
    }

    @Test
    fun forSumHandlesZeroIterations() {
        val input = listOf(0)
        val expectedOutput = listOf(0)

        assertProgram("for_sum", input, expectedOutput)
    }

    @Test
    fun nestedControlTraversesGridWithConditions() {
        val input = listOf(3, 4)
        val expectedOutput = listOf(6)

        assertProgram("nested_control", input, expectedOutput)
    }

    @Test
    fun nestedControlHandlesEmptyOuterLoop() {
        val input = listOf(0, 5)
        val expectedOutput = listOf(0)

        assertProgram("nested_control", input, expectedOutput)
    }

    @Test
    fun optionalSyntaxSupportsCommentsSemicolonsAndMissingElse() {
        val input = emptyList<Int>()
        val expectedOutput = listOf(1)

        assertProgram("optional_syntax", input, expectedOutput)
    }

    @Test
    fun blocksAndSkipPreserveNestedExecutionOrder() {
        val input = emptyList<Int>()
        val expectedOutput = listOf(2)

        assertProgram("blocks_skip", input, expectedOutput)
    }

    @Test
    fun stressCollatzTracksLongTrajectoryStatistics() {
        val input = listOf(27)
        val expectedOutput = listOf(82, 137, 1336, 2158, 577, 160, 111, 9232, 41, 70, 7518)

        assertProgram("stress_collatz", input, expectedOutput)
    }

    @Test
    fun stressCollatzHandlesFinishedTrajectory() {
        val input = listOf(1)
        val expectedOutput = listOf(0, 1, 0, 0, 0)

        assertProgram("stress_collatz", input, expectedOutput)
    }

    @Test
    fun stressPrimesCalculatesPrimesStatisticsAndChecksum() {
        val input = listOf(100)
        val expectedOutput = listOf(2, 11, 31, 41, 61, 71, 25, 1060, 64660)

        assertProgram("stress_primes", input, expectedOutput)
    }

    @Test
    fun stressPrimesHandlesEmptyRange() {
        val input = listOf(1)
        val expectedOutput = listOf(0, 0, 0)

        assertProgram("stress_primes", input, expectedOutput)
    }

    @Test
    fun stressStateMachineFollowsSeedFortyTwoPath() {
        val input = listOf(42)
        val expectedOutput = listOf(46, 435, 642, 833, 1008, 1231, 22, 1539, 13, 17)

        assertProgram("stress_state_machine", input, expectedOutput)
    }

    @Test
    fun stressStateMachineFollowsZeroSeedPath() {
        val input = listOf(0)
        val expectedOutput = listOf(22, 143, 518, 605, 848, 1053, 42, 1371, 15, 15)

        assertProgram("stress_state_machine", input, expectedOutput)
    }

    @Test
    fun stressGridTraversesSevenBySixCells() {
        val input = listOf(7, 6)
        val expectedOutput = listOf(11, 27, 126, 126, 208, 234, 248, 260, 260, 17, 14)

        assertProgram("stress_grid", input, expectedOutput)
    }

    @Test
    fun stressGridHandlesEmptyOuterDimension() {
        val input = listOf(0, 8)
        val expectedOutput = listOf(0, 0, 0)

        assertProgram("stress_grid", input, expectedOutput)
    }

    @Test
    fun stressNumericPipelineCombinesAllLoopForms() {
        val input = listOf(7, 126)
        val expectedOutput = listOf(5040, 9, 405, 4, 126, 1)

        assertProgram("stress_numeric_pipeline", input, expectedOutput)
    }

    @Test
    fun stressNumericPipelineReachesCategoryTwo() {
        val input = listOf(4, 18)
        val expectedOutput = listOf(24, 6, 42, 2, 6, 2)

        assertProgram("stress_numeric_pipeline", input, expectedOutput)
    }

    @Test
    fun stressNumericPipelineHandlesZeroBound() {
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
