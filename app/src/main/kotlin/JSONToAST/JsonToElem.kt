package ru.tsaplev.app.JSONToAST

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.ASTElem

class JsonToElem: JsonToASTer() {
    override fun parse(parser: JsonToASTer, text: JsonElement): ASTNode? {
        val jsonObject = text as? JsonObject ?: return next?.parse(parser, text)
        if (jsonObject.hasNodeTag("elem")) {
            val elem = jsonObject["elem"]?.let { parser.parse(parser, it) } ?: return null
            val index = jsonObject["index"]?.let { parser.parse(parser, it) } ?: return null
            return ASTElem(elem, index)
        }
        return next?.parse(parser, text)
    }
}