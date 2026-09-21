import kotlin.test.Test

class ConditionalTests: ProgramTest() {
    @Test
    fun ifWithoutElseTest() {
        val text = """
            {
              "if": {
                "cond": { "const": 0 },
                "then": { "write": { "const": 42 } }
              }
            }
        """.trimIndent()

        testProgram(text, listOf(), listOf())
    }
}
