package ru.tsaplev.app.JSONToAST

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import ru.tsaplev.app.ASTreeNodes.MatchPattern
import ru.tsaplev.app.execution.DataValue

private fun string(json: JsonElement?): String? =
    (json as? JsonPrimitive)?.takeIf { it.isString }?.content

private fun items(json: JsonElement?): List<MatchPattern>? =
    (json as? JsonArray)?.map { parsePattern(it) ?: return null }

internal fun parsePattern(json: JsonElement): MatchPattern? {
    if (json is JsonPrimitive) {
        if (!json.isString) {
            val number = json.intOrNull ?: return null
            return MatchPattern.literal(DataValue.IntValue(number))
        }
        if (json.content.startsWith("#")) {
            MatchPattern.type(json.content.drop(1))?.let { return it }
        }
        return MatchPattern.literal(DataValue.StringValue(json.content))
    }
    val node = json as? JsonObject ?: return null
    return when {
        "wildcard" in node -> MatchPattern.wildcard()
        "literal" in node -> {
            val literal = string(node["literal"]) ?: return null
            MatchPattern.literal(DataValue.StringValue(literal))
        }
        "bind" in node -> {
            val bind = node["bind"] as? JsonObject ?: return null
            val name = string(bind["name"]) ?: return null
            val pattern = bind["pattern"]?.let(::parsePattern) ?: return null
            MatchPattern.bind(name, pattern)
        }
        "array" in node -> MatchPattern.array(items(node["array"]) ?: return null)
        "sexp" in node -> {
            val name = string(node["sexp"]) ?: return null
            MatchPattern.constructor(name, items(node["args"]) ?: return null)
        }
        else -> null
    }
}
