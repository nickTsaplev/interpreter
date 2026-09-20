package ru.tsaplev.app.JSONToAST

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.ASTRead

class JsonToRead: JsonToASTer() {
    override fun parse(
        parser: JsonToASTer,
        text: JsonElement
    ): ASTNode? {
        val jsonObject = text as? JsonObject ?: return next?.parse(parser, text)
        if (jsonObject.hasNodeTag("read")) {
            val variableName = jsonObject["read"]?.asIdentifierOrNull() ?: return null
            return ASTRead(variableName)
        }
        return next?.parse(parser, text)
    }
}
