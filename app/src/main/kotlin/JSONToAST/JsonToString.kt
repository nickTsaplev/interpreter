package ru.tsaplev.app.JSONToAST

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.ASTString

class JsonToString: JsonToASTer() {
    override fun parse(
        parser: JsonToASTer,
        text: JsonElement
    ): ASTNode? {
        val jsonObject = text as? JsonObject ?: return next?.parse(parser, text)
        if (jsonObject.hasNodeTag("string")) {
            val value = (jsonObject["string"] as? JsonPrimitive)
                ?.takeIf { it.isString }
                ?.content
                ?: return null
            return ASTString(value)
        }
        return next?.parse(parser, text)
    }
}