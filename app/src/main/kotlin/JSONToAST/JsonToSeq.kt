package ru.tsaplev.app.JSONToAST

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.ASTBinOP
import ru.tsaplev.app.ASTreeNodes.ASTSeq

class JsonToSeq: JsonToASTer() {
    override fun parse(
        parser: JsonToASTer,
        text: JsonElement
    ): ASTNode? {
        val jsonObject: JsonObject = Json.decodeFromJsonElement(text)
        if(jsonObject.keys.contains("seq")) {
            val left = jsonObject["seq"]?.let { it.jsonObject["left"]?.let{ parser.parse(parser, it) }} ?: return null
            val right = jsonObject["seq"]?.let { it.jsonObject["right"]?.let{ parser.parse(parser, it) }} ?: return null

            return ASTSeq(left, right)
        }
        return next?.parse(parser, text)
    }
}