package ru.tsaplev.app.JSONToAST

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.ASTConst
import ru.tsaplev.app.execution.DataValue

class JsonToConst: JsonToASTer() {
    override fun parse(
        parser: JsonToASTer,
        text: JsonElement
    ): ASTNode? {
        val jsonObject = text as? JsonObject ?: return next?.parse(parser, text)
        if (jsonObject.hasNodeTag("const")) {
            val value = (jsonObject["const"] as? JsonPrimitive)
                ?.takeUnless { it.isString }
                ?.content
                ?.toIntOrNull()
                ?: return null
            return ASTConst(DataValue(value))
        }
        return next?.parse(parser, text)
    }
}
