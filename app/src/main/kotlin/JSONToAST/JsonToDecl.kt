package ru.tsaplev.app.JSONToAST

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.ASTVarDecl

class JsonToDecl : JsonToASTer() {
    override fun parse(parser: JsonToASTer, text: JsonElement): ASTNode? {
        val obj = text as? JsonObject ?: return next?.parse(parser, text)
        if (!obj.hasNodeTag("decl")) return next?.parse(parser, text)
        if (obj.size != 1) return null
        val decl = obj["decl"] as? JsonObject ?: return null
        val name = decl["name"]?.asStringOrNull()?.takeIf { it.isNotBlank() } ?: return null
        if (decl.keys != setOf("name", "value")) return null
        val value = decl["value"]?.let { parser.parse(parser, it) } ?: return null
        return ASTVarDecl(name, value)
    }
}
