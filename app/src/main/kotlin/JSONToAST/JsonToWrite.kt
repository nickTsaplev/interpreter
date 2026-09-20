package ru.tsaplev.app.JSONToAST

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.ASTFuncCall

class JsonToWrite: JsonToASTer() {
    override fun parse(
        parser: JsonToASTer,
        text: JsonElement
    ): ASTNode? {
        val jsonObject = text as? JsonObject ?: return next?.parse(parser, text)
        if (jsonObject.hasNodeTag("write")) {
            val value = jsonObject["write"]?.let { parser.parse(parser, it) } ?: return null
            return ASTFuncCall("write", value)
        }
        return next?.parse(parser, text)
    }
}
