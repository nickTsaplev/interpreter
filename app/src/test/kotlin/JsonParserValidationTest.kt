package ru.tsaplev.app

import ru.tsaplev.app.JSONToAST.GetJsonToAST
import ru.tsaplev.app.ASTreeNodes.ASTWhile
import ru.tsaplev.app.ASTreeNodes.LegacyLoopLabels
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class JsonParserValidationTest {
    @Test
    fun parserReadsAndValidatesControlLabels() {
        val valid = """
            {"while":{"labels":{"body":5,"condition":6},"cond":{"const":1},"body":"skip"}}
        """.trimIndent()
        val loop = assertNotNull(GetJsonToAST().startParse(valid)) as ASTWhile

        assertEquals(LegacyLoopLabels(5, 6), loop.legacyLabels)

        val invalid = """
            {"while":{"labels":{"body":"five"},"cond":{"const":1},"body":"skip"}}
        """.trimIndent()
        assertNull(runCatching { GetJsonToAST().startParse(invalid) }.getOrNull())
    }

    @Test
    fun parserRejectsMalformedAndUnknownAstNodes() {
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
