package ru.tsaplev.app.JSONToAST

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonArray
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.ASTVarDecl
import ru.tsaplev.app.ASTreeNodes.ASTFuncDecl

class JsonToDecl : JsonToASTer() {
    override fun parse(parser: JsonToASTer, text: JsonElement): ASTNode? {
        val obj = text as? JsonObject ?: return next?.parse(parser, text)
        if (!obj.hasNodeTag("decl")) return next?.parse(parser, text)
        if (obj.size != 1) return null
        val decl = obj["decl"] as? JsonObject ?: return null
        val name = decl["name"]?.asStringOrNull()?.takeIf { it.isNotBlank() } ?: return null
        if ("params" in decl || "body" in decl) {
            if (decl.keys != setOf("name", "params", "body")) return null
            val params = decl["params"] as? JsonArray ?: return null
            val names = params.map { it.asStringOrNull()?.takeIf { name -> name.isNotBlank() } ?: return null }
            if (names.distinct().size != names.size) return null
            val body = decl["body"]?.let { parser.parse(parser, it) } ?: return null
            return ASTFuncDecl(name, names, body)
        }
        if (decl.keys != setOf("name", "value")) return null
        val value = decl["value"]?.let { parser.parse(parser, it) } ?: return null
        return ASTVarDecl(name, value)
    }
}
