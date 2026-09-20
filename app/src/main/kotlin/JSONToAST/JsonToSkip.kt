package ru.tsaplev.app.JSONToAST

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.ASTSkip

class JsonToSkip : JsonToASTer() {
    override fun parse(parser: JsonToASTer, text: JsonElement): ASTNode? {
        if (text is JsonPrimitive && text.isString && text.content == "skip") {
            return ASTSkip()
        }

        val jsonObject = text as? JsonObject ?: return next?.parse(parser, text)
        return if (jsonObject.hasNodeTag("skip")) ASTSkip() else next?.parse(parser, text)
    }
}
