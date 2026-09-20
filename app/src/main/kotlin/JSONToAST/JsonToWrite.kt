package ru.tsaplev.app.JSONToAST

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.ASTFuncCall

class JsonToWrite: JsonToASTer() {
    override fun parse(
        parser: JsonToASTer,
        text: JsonElement
    ): ASTNode? {
        val jsonObject: JsonObject = Json.decodeFromJsonElement(text)
        if(jsonObject.keys.contains("write")) {
            val value = jsonObject["write"]?.let { parser.parse(parser, it) } ?: return null
            return try {
                ASTFuncCall("write", value)
            } catch(e: NumberFormatException) {
                null
            }
        }
        return next?.parse(parser, text)
    }
}