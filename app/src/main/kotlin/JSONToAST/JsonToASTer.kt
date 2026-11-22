package ru.tsaplev.app.JSONToAST

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.ASTFuncCall

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

    fun startParse(text: String): ASTNode? =
        parseStatement(Json.parseToJsonElement(text))

    fun parseStatement(text: JsonElement): ASTNode? {
        val node = parse(this, text) ?: return null
        val call = (text as? JsonObject)?.get("call") as? JsonObject
        // The reference frontend uses the same call node in both contexts.
        // Legacy name/callStmt nodes keep their explicit expression/statement mode.
        return if (node is ASTFuncCall && call != null && "fun" in call) {
            ASTFuncCall(node.name, node.arguments, asExpression = false)
        } else node
    }

    protected fun JsonElement.asStringOrNull(): String? {
        val primitive = this as? JsonPrimitive ?: return null
        return primitive.takeIf { it.isString }?.content
    }

    protected fun JsonObject.hasNodeTag(tag: String): Boolean = tag in this
}
