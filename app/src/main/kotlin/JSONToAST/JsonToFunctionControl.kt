package ru.tsaplev.app.JSONToAST

import kotlinx.serialization.json.*
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.ASTFuncCall
import ru.tsaplev.app.ASTreeNodes.ASTReturn

class JsonToFunctionControl : JsonToASTer() {
    override fun parse(parser: JsonToASTer, text: JsonElement): ASTNode? {
        val obj = text as? JsonObject ?: return next?.parse(parser, text)
        if ("return" in obj) {
            if (obj.size != 1) return null
            val value = obj.getValue("return")
            if (value == JsonNull || value.asStringOrNull() == "") return ASTReturn()
            if (value is JsonObject && "expr" in value) {
                if (value.keys != setOf("expr")) return null
                return ASTReturn(parser.parse(parser, value.getValue("expr")) ?: return null)
            }
            return ASTReturn(parser.parse(parser, value) ?: return null)
        }
        val tag = when {
            "call" in obj -> "call"
            "callStmt" in obj -> "callStmt"
            else -> return next?.parse(parser, text)
        }
        if (obj.size != 1) return null
        val call = obj[tag] as? JsonObject ?: return null
        val nameKey = if ("fun" in call) "fun" else "name"
        if (call.keys != setOf(nameKey, "args")) return null
        val name = call[nameKey]?.asStringOrNull()?.takeIf { it.isNotBlank() } ?: return null
        val args = call["args"] as? JsonArray ?: return null
        return ASTFuncCall(name, args.map { parser.parse(parser, it) ?: return null }, tag == "call")
    }
}
