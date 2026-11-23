package ru.tsaplev.app.JSONToAST

import kotlinx.serialization.json.*
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.ASTConst
import ru.tsaplev.app.ASTreeNodes.ASTFuncDecl
import ru.tsaplev.app.ASTreeNodes.ASTScope
import ru.tsaplev.app.ASTreeNodes.ASTVarDecl
import ru.tsaplev.app.execution.DataValue

class JsonToScope : JsonToASTer() {
    override fun parse(parser: JsonToASTer, text: JsonElement): ASTNode? {
        val obj = text as? JsonObject ?: return next?.parse(parser, text)
        if (!obj.hasNodeTag("scope")) return next?.parse(parser, text)
        if (obj.size != 1) return null
        val scope = obj["scope"] as? JsonObject ?: return null
        if (scope.keys != setOf("decls", "body")) return null
        val declarations = scope["decls"] as? JsonArray ?: return null
        val nodes = declarations.map { declaration(parser, it) ?: return null }
        val body = scope["body"]?.let { parser.parseStatement(it) } ?: return null
        return ASTScope(nodes, body)
    }

    private fun declaration(parser: JsonToASTer, text: JsonElement): ASTNode? {
        val node = text as? JsonObject ?: return null
        if (node.keys != setOf("name", "spec")) return null
        val name = node["name"]?.asStringOrNull()?.takeIf { it.isNotBlank() } ?: return null
        val spec = node["spec"] ?: return null
        if (spec.asStringOrNull() == "var") {
            return ASTVarDecl(name, ASTConst(DataValue.IntValue(0)))
        }
        val functionSpec = spec as? JsonObject ?: return null
        if (functionSpec.keys != setOf("fun")) return null
        val function = functionSpec["fun"] as? JsonObject ?: return null
        if (function.keys != setOf("args", "body")) return null
        val args = function["args"] as? JsonArray ?: return null
        val names = args.map { it.asStringOrNull()?.takeIf(String::isNotBlank) ?: return null }
        if (names.distinct().size != names.size) return null
        val body = function["body"]?.let { parser.parseStatement(it) } ?: return null
        return ASTFuncDecl(name, names, body)
    }
}
