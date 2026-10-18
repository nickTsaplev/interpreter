package ru.tsaplev.app.JSONToAST

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.ASTArray

class JsonToArray: JsonToASTer() {
    override fun parse(
        parser: JsonToASTer,
        text: JsonElement
    ): ASTNode? {
        val jsonObject = text as? JsonObject ?: return next?.parse(parser, text)
        if (jsonObject.hasNodeTag("array")) {
            val elems = jsonObject["array"]?.jsonArray?.map { parser.parse(parser, it) ?: return null } ?: return null
            return ASTArray(elems)
        }
        return next?.parse(parser, text)
    }
}