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
            "{\"binop\":\"power\",\"left\":{\"const\":2},\"right\":{\"const\":3}}",
            "{\"read\":42}",
            "{\"read\":\"X\"}",
            "{\"const\":\"1\"}",
            "{\"const\":1,\"var\":\"x\"}"
        )

        invalidJsonValues.forEach { json ->
            assertNull(GetJsonToAST().startParse(json), "Unexpectedly accepted invalid AST: $json")
        }
    }
}
