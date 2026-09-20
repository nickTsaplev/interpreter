package ru.tsaplev.app.JSONToAST

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import ru.tsaplev.app.ASTNode

abstract class JsonToASTer {
    protected var next: JsonToASTer? = null
    abstract fun parse(parser: JsonToASTer, text: JsonElement): ASTNode?
    fun addNext(parser: JsonToASTer): JsonToASTer {
        if (next == null)
            next = parser
        else
            next?.addNext(parser);
        return this;
    }

    fun startParse(text: String): ASTNode?
        = parse(this, Json.decodeFromString<JsonElement>(text))
}