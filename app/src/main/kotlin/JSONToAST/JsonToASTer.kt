package ru.tsaplev.app.JSONToAST

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import ru.tsaplev.app.ASTNode

abstract class JsonToASTer {
    protected var next: JsonToASTer? = null
    abstract fun parse(parser: JsonToASTer, text: JsonElement): ASTNode?
    fun addNext(parser: JsonToASTer): JsonToASTer {
        if (next == null)
            next = parser
        else
            next?.addNext(parser)
        return this
    }

    fun startParse(text: String): ASTNode? = try {
        parse(this, Json.parseToJsonElement(text))
    } catch (_: Exception) {
        null
    }

    protected fun JsonElement.asStringOrNull(): String? {
        val primitive = this as? JsonPrimitive ?: return null
        return primitive.takeIf { it.isString }?.content
    }

    protected fun JsonElement.asIdentifierOrNull(): String? =
        asStringOrNull()?.takeIf(identifierPattern::matches)

    protected fun JsonObject.hasNodeTag(tag: String): Boolean =
        tag in this && keys.count { it in nodeTags } == 1

    companion object {
        private val identifierPattern = Regex("[a-z][a-zA-Z_'0-9]*")
        private val nodeTags = setOf("read", "write", "assn", "while", "do", "if", "seq", "skip", "binop", "var", "const")
        val binaryOperators = setOf("!!", "||", "&&", "==", "!=", "<=", "<", ">=", ">", "+", "-", "*", "/", "%")
    }
}
