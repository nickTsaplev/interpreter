package ru.tsaplev.app.JSONToAST

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.ASTConst
import ru.tsaplev.app.ASTreeNodes.ASTVar
import ru.tsaplev.app.execution.DataValue

class JsonToVar: JsonToASTer() {
    override fun parse(
        parser: JsonToASTer,
        text: JsonElement
    ): ASTNode? {
        val jsonObject: JsonObject = Json.decodeFromJsonElement(text)
        if(jsonObject.keys.contains("var")) {
            val value = jsonObject["var"]?.jsonPrimitive?.content ?: return null
            return ASTVar(value)
        }
        return next?.parse(parser, text)
    }
}