package ru.tsaplev.app.JSONToAST

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.ASTBinOP

class JsonToBinop: JsonToASTer() {
    override fun parse(parser: JsonToASTer, text: JsonElement): ASTNode? {
        val jsonObject: JsonObject = Json.decodeFromJsonElement(text)
        if(jsonObject.keys.contains("binop")) {
            val left = jsonObject["left"]?.let { parser.parse(parser, it) } ?: return null
            val right = jsonObject["right"]?.let { parser.parse(parser, it) } ?: return null

            return ASTBinOP(
                jsonObject["binop"]!!.jsonPrimitive.content, left, right
            )
        }
        return next?.parse(parser, text)
    }
}