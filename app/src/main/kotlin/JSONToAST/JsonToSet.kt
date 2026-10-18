package ru.tsaplev.app.JSONToAST

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.ASTSet

class JsonToSet : JsonToASTer() {
    override fun parse(
        parser: JsonToASTer,
        text: JsonElement
    ): ASTNode? {
        val jsonObject = text as? JsonObject ?: return next?.parse(parser, text)
        if (jsonObject.hasNodeTag("set")) {
            val assignment = jsonObject["set"] as? JsonObject ?: return null
            val destination = assignment["dst"]?.let { parser.parse(parser, it) } ?: return null
            val source = assignment["value"]?.let { parser.parse(parser, it) } ?: return null
            val index = assignment["idx"]?.let { parser.parse(parser, it) } ?: return null

            return ASTSet(destination, source, index)
        }
        return next?.parse(parser, text)
    }
}
