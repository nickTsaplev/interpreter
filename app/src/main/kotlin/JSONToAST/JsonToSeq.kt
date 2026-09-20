package ru.tsaplev.app.JSONToAST

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.ASTSeq

class JsonToSeq: JsonToASTer() {
    override fun parse(
        parser: JsonToASTer,
        text: JsonElement
    ): ASTNode? {
        val jsonObject = text as? JsonObject ?: return next?.parse(parser, text)
        if (jsonObject.hasNodeTag("seq")) {
            val sequence = jsonObject["seq"] as? JsonObject ?: return null
            val left = sequence["left"]?.let { parser.parse(parser, it) } ?: return null
            val right = sequence["right"]?.let { parser.parse(parser, it) } ?: return null
            return ASTSeq(left, right)
        }
        return next?.parse(parser, text)
    }
}
