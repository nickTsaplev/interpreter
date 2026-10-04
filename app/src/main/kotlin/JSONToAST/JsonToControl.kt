package ru.tsaplev.app.JSONToAST
import kotlinx.serialization.json.*
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.*

class JsonToControl: JsonToASTer() {
    private fun JsonObject.intLabel(name: String): Int =
        this[name]?.jsonPrimitive?.intOrNull
            ?: throw IllegalArgumentException("Missing or invalid label: $name")

    private fun JsonObject.loopLabels(): LegacyLoopLabels? {
        val labels = this["labels"] ?: return null
        val objectLabels = labels as? JsonObject
            ?: throw IllegalArgumentException("Labels must be an object")
        return LegacyLoopLabels(
            body = objectLabels.intLabel("body"),
            condition = objectLabels.intLabel("condition")
        )
    }

    private fun JsonObject.ifLabels(): LegacyIfLabels? {
        val labels = this["labels"] ?: return null
        val objectLabels = labels as? JsonObject
            ?: throw IllegalArgumentException("Labels must be an object")
        return LegacyIfLabels(
            end = objectLabels["end"]?.jsonPrimitive?.content
                ?: throw IllegalArgumentException("Missing or invalid label: end"),
            elseLabel = objectLabels["else"]?.jsonPrimitive?.content
                ?: throw IllegalArgumentException("Missing or invalid label: else")
        )
    }

    override fun parse(parser: JsonToASTer, text: JsonElement): ASTNode? {
        val jsonObject = text as? JsonObject ?: return next?.parse(parser, text)
        if (jsonObject.hasNodeTag("while")) {
            val loop = jsonObject["while"] as? JsonObject ?: return null
            val cond = loop["cond"]?.let { parser.parse(parser, it) } ?: return null
            val body = loop["body"]?.let { parser.parse(parser, it) } ?: return null
            return ASTWhile(cond, body, loop.loopLabels())
        }
        if (jsonObject.hasNodeTag("do")) {
            val loop = jsonObject["do"] as? JsonObject ?: return null
            val body = loop["body"]?.let { parser.parse(parser, it) } ?: return null
            val cond = loop["cond"]?.let { parser.parse(parser, it) } ?: return null
            return ASTDo(body, cond, loop.loopLabels())
        }
        if (jsonObject.hasNodeTag("if")) {
            val conditional = jsonObject["if"] as? JsonObject ?: return null
            val cond = conditional["cond"]?.let { parser.parse(parser, it) } ?: return null
            val thenBranch = conditional["then"]?.let { parser.parse(parser, it) } ?: return null
            val elseBranch = conditional["else"]?.let { parser.parse(parser, it) ?: return null }
            return ASTIf(cond, thenBranch, elseBranch, conditional.ifLabels())
        }
        return next?.parse(parser, text)
    }
}
