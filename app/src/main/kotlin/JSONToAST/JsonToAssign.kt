package ru.tsaplev.app.JSONToAST

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.ASTAssign

class JsonToAssign: JsonToASTer() {
    override fun parse(
        parser: JsonToASTer,
        text: JsonElement
    ): ASTNode? {
        val jsonObject = text as? JsonObject ?: return next?.parse(parser, text)
        if (jsonObject.hasNodeTag("assn")) {
            val assignment = jsonObject["assn"] as? JsonObject ?: return null
            val destination = assignment["dst"]?.asIdentifierOrNull() ?: return null
            val source = assignment["src"]?.let { parser.parse(parser, it) } ?: return null
            return ASTAssign(destination, source)
        }
        return next?.parse(parser, text)
    }
}
