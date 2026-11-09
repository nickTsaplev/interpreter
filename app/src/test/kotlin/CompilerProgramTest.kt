package ru.tsaplev.app

import kotlinx.serialization.json.Json
import ru.tsaplev.app.JSONToAST.GetJsonToAST
import ru.tsaplev.app.execution.CompilationVisitor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CompilerProgramTest {
    private fun resourceText(directory: String, fileName: String): String =
        assertNotNull(
            javaClass.getResource("/$directory/$fileName"),
            "Missing test resource: $directory/$fileName"
        ).readText(Charsets.UTF_8)

    private fun assertCompilerProgram(programName: String, stackProgramName: String = programName) {
        val inputJson = resourceText("ast-json", "$programName.json")
        val expectedOutputJson = resourceText("stack-json", "$stackProgramName.json")

        assertTrue(
            inputJson.isNotBlank(),
            "Empty inputJson: ast-json/$programName.json"
        )

        assertTrue(
            expectedOutputJson.isNotBlank(),
            "Empty expectedOutputJson: stack-json/$programName.json"
        )
        val ast = GetJsonToAST().startParse(inputJson)

        assertNotNull(
            ast,
            "Null AST JSON: ast-json/$programName.json"
        )

        val visitor = CompilationVisitor()
        ast.visit(visitor)

        assertEquals(
            Json.parseToJsonElement(expectedOutputJson),
            StackInstructionsToJSON(visitor.instructions),
            "Unexpected compiled JSON for program $programName"
        )
    }

    @Test
    fun compilerArithmetic() {
        assertCompilerProgram("arithmetic")
    }

    @Test
    fun compilerAssignments() {
        assertCompilerProgram("assignments")
    }

    @Test
    fun compilerBlocksSkip() {
        assertCompilerProgram("blocks_skip", "blocks-skip")
    }

    @Test
    fun compilerBranches() {
        assertCompilerProgram("branches")
    }

    @Test
    fun compilerComparisons() {
        assertCompilerProgram("comparisons")
    }

    @Test
    fun compilerConditionalIfWithoutElse() {
        assertCompilerProgram("conditional_if_without_else")
    }

    @Test
    fun compilerDoWhile() {
        assertCompilerProgram("do_while")
    }

    @Test
    fun compilerDoWhileOnce() {
        assertCompilerProgram("do_while_once")
    }

    @Test
    fun compilerForSum() {
        assertCompilerProgram("for_sum")
    }

    @Test
    fun compilerIoArithmetic() {
        assertCompilerProgram("io_arithmetic")
    }

    @Test
    fun compilerLogic() {
        assertCompilerProgram("logic")
    }

    @Test
    fun compilerLogicalOr() {
        assertCompilerProgram("logical_or")
    }

    @Test
    fun compilerNestedControl() {
        assertCompilerProgram("nested_control")
    }

    @Test
    fun compilerOptionalSyntax() {
        assertCompilerProgram("optional_syntax")
    }

    @Test
    fun compilerPrimeNumbersBelowForty() {
        assertCompilerProgram("prime_numbers_below_forty")
    }

    @Test
    fun compilerStressCollatz() {
        assertCompilerProgram("stress_collatz")
    }

    @Test
    fun compilerStressGrid() {
        assertCompilerProgram("stress_grid")
    }

    @Test
    fun compilerStressNumericPipeline() {
        assertCompilerProgram("stress_numeric_pipeline")
    }

    @Test
    fun compilerStressPrimes() {
        assertCompilerProgram("stress_primes")
    }

    @Test
    fun compilerStressStateMachine() {
        assertCompilerProgram("stress_state_machine")
    }

    @Test
    fun compilerWhileFactorial() {
        assertCompilerProgram("while_factorial")
    }

    @Test
    fun compilerWhileModularSequence() {
        assertCompilerProgram("while_modular_sequence")
    }
}
