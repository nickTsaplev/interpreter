package ru.tsaplev.app.JSONToAST

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.ASTAssign
import ru.tsaplev.app.ASTreeNodes.ASTBinOP

class JSONToAssign: JsonToASTer() {
    override fun parse(
        parser: JsonToASTer,
        text: JsonElement
    ): ASTNode? {
        val jsonObject: JsonObject = Json.decodeFromJsonElement(text)
        if(jsonObject.keys.contains("assn")) {
            val left = jsonObject["assn"]?.let { it.jsonObject["dst"]?.jsonPrimitive?.content } ?: return null
            val src = jsonObject["assn"]?.let { it.jsonObject["src"]?.let {parser.parse(parser, it)} } ?: return null

            return ASTAssign(
                left, src
            )
        }
        return next?.parse(parser, text)
    }
}