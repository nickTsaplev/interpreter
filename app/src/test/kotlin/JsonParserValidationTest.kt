package ru.tsaplev.app

import ru.tsaplev.app.JSONToAST.GetJsonToAST
import kotlin.test.Test
import kotlin.test.assertNull

class JsonParserValidationTest {
    @Test
    fun `parser rejects malformed and unknown AST nodes`() {
        val invalidJsonValues = listOf(
            "{",
            "{\"unknown\":1}",
            "{\"while\":{\"cond\":{\"const\":1}}}",
            "{\"read\":42}",
            "{\"const\":\"1\"}"
        )

        invalidJsonValues.forEach { json ->
            val parsedNode = runCatching { GetJsonToAST().startParse(json) }.getOrNull()

            assertNull(parsedNode, "Unexpectedly accepted invalid AST: $json")
        }
    }
}
