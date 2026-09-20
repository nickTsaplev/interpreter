package ru.tsaplev.app.JSONToAST
import kotlinx.serialization.json.*
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.*

class JsonToControl: JsonToASTer() {
    override fun parse(parser: JsonToASTer, text: JsonElement): ASTNode? {
        val jsonObject = text as? JsonObject ?: return next?.parse(parser, text)
        if (jsonObject.hasNodeTag("while")) {
            val loop = jsonObject["while"] as? JsonObject ?: return null
            val cond = loop["cond"]?.let { parser.parse(parser, it) } ?: return null
            val body = loop["body"]?.let { parser.parse(parser, it) } ?: return null
            return ASTWhile(cond, body)
        }
        if (jsonObject.hasNodeTag("do")) {
            val loop = jsonObject["do"] as? JsonObject ?: return null
            val body = loop["body"]?.let { parser.parse(parser, it) } ?: return null
            val cond = loop["cond"]?.let { parser.parse(parser, it) } ?: return null
            return ASTDo(body, cond)
        }
        if (jsonObject.hasNodeTag("if")) {
            val conditional = jsonObject["if"] as? JsonObject ?: return null
            val cond = conditional["cond"]?.let { parser.parse(parser, it) } ?: return null
            val thenBranch = conditional["then"]?.let { parser.parse(parser, it) } ?: return null
            val elseBranch = conditional["else"]?.let { parser.parse(parser, it) ?: return null }
            return ASTIf(cond, thenBranch, elseBranch)
        }
        return next?.parse(parser, text)
    }
}
