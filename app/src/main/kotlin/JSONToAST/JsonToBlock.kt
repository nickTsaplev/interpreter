package ru.tsaplev.app.JSONToAST

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.ASTBlock

class JsonToBlock : JsonToASTer() {
    override fun parse(parser: JsonToASTer, text: JsonElement): ASTNode? {
        val obj = text as? JsonObject ?: return next?.parse(parser, text)
        if (!obj.hasNodeTag("block")) return next?.parse(parser, text)
        if (obj.size != 1) return null
        val statements = obj["block"] as? JsonArray ?: return null
        return ASTBlock(statements.map { parser.parseStatement(it) ?: return null })
    }
}
