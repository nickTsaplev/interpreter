package ru.tsaplev.app.JSONToAST

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.ASTSExp


class JsonToSExp: JsonToASTer() {
    override fun parse(
        parser: JsonToASTer,
        text: JsonElement
    ): ASTNode? {
        val jsonObject = text as? JsonObject ?: return next?.parse(parser, text)
        if (jsonObject.hasNodeTag("sexp")) {
            val name = jsonObject["sexp"]?.jsonPrimitive?.content ?: return null
            val elems = (jsonObject["args"] as? JsonArray )?.map { parser.parse(parser, it) ?: return null } ?: return null
            return ASTSExp(name, elems)
        }
        return next?.parse(parser, text)
    }
}