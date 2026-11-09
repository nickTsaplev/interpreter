package ru.tsaplev.app.JSONToAST

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.ASTSwitch

class JsonToSwitch: JsonToASTer() {
    override fun parse(parser: JsonToASTer, text: JsonElement): ASTNode? {
        val jsonObject = text as? JsonObject ?: return next?.parse(parser, text)
        if (!jsonObject.hasNodeTag("switch")) return next?.parse(parser, text)
        val node = jsonObject["switch"] as? JsonObject ?: return null
        val scrutinee = node["scrutinee"]?.let { parser.parse(parser, it) } ?: return null
        val branches = node["branches"] as? JsonArray ?: return null
        if (branches.isEmpty()) return null
        return ASTSwitch(scrutinee, branches.map {
            val entry = it as? JsonObject ?: return null
            val pattern = entry["pattern"]?.let(::parsePattern) ?: return null
            val branch = entry["branch"]?.let { body -> parser.parse(parser, body) } ?: return null
            pattern to branch
        })
    }
}
