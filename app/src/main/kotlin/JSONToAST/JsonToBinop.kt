package ru.tsaplev.app.JSONToAST

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.ASTBinOP

class JsonToBinop: JsonToASTer() {
    override fun parse(parser: JsonToASTer, text: JsonElement): ASTNode? {
        val jsonObject = text as? JsonObject ?: return next?.parse(parser, text)
        if (jsonObject.hasNodeTag("binop")) {
            val operation = jsonObject["binop"]?.asStringOrNull()
                ?.takeIf { it in binaryOperators }
                ?: return null
            val left = jsonObject["left"]?.let { parser.parse(parser, it) } ?: return null
            val right = jsonObject["right"]?.let { parser.parse(parser, it) } ?: return null
            return ASTBinOP(operation, left, right)
        }
        return next?.parse(parser, text)
    }
}
