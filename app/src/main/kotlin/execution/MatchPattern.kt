package ru.tsaplev.app.execution

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull

class MatchPattern private constructor(
    val json: JsonElement,
    private val matches: (DataValue, MutableMap<String, DataValue>) -> Boolean
) {
    fun match(value: DataValue): Map<String, DataValue>? {
        val bindings = mutableMapOf<String, DataValue>()
        return if (matches(value, bindings)) bindings else null
    }

    companion object {
        private fun string(json: JsonElement?): String? =
            (json as? JsonPrimitive)?.takeIf { it.isString }?.content

        private fun items(json: JsonElement?): List<MatchPattern>? =
            (json as? JsonArray)?.map { parse(it) ?: return null }

        private fun matchItems(
            patterns: List<MatchPattern>,
            values: List<DataValue>,
            bindings: MutableMap<String, DataValue>
        ): Boolean = patterns.size == values.size && patterns.indices.all {
            patterns[it].matches(values[it], bindings)
        }

        fun parse(json: JsonElement): MatchPattern? {
            if (json is JsonPrimitive) {
                if (!json.isString) {
                    val number = json.intOrNull ?: return null
                    return MatchPattern(json) { value, _ -> value is DataValue.IntValue && value.value == number }
                }
                return when (json.content) {
                    "#int" -> MatchPattern(json) { value, _ -> value is DataValue.IntValue }
                    "#string" -> MatchPattern(json) { value, _ -> value is DataValue.StringValue }
                    "#array" -> MatchPattern(json) { value, _ -> value is DataValue.ArrayValue }
                    "#sexp" -> MatchPattern(json) { value, _ -> value is DataValue.SExpressionValue }
                    else -> MatchPattern(json) { value, _ -> value is DataValue.StringValue && value.value == json.content }
                }
            }
            val node = json as? JsonObject ?: return null
            return when {
                "wildcard" in node -> MatchPattern(json) { _, _ -> true }
                "literal" in node -> {
                    val literal = string(node["literal"]) ?: return null
                    MatchPattern(json) { value, _ -> value is DataValue.StringValue && value.value == literal }
                }
                "bind" in node -> {
                    val bind = node["bind"] as? JsonObject ?: return null
                    val name = string(bind["name"]) ?: return null
                    val pattern = bind["pattern"]?.let(::parse) ?: return null
                    MatchPattern(json) { value, bindings ->
                        if (pattern.matches(value, bindings)) {
                            bindings[name] = value
                            true
                        } else false
                    }
                }
                "array" in node -> {
                    val patterns = items(node["array"]) ?: return null
                    MatchPattern(json) { value, bindings ->
                        value is DataValue.ArrayValue && matchItems(patterns, value.elems, bindings)
                    }
                }
                "sexp" in node -> {
                    val name = string(node["sexp"]) ?: return null
                    val patterns = items(node["args"]) ?: return null
                    MatchPattern(json) { value, bindings ->
                        value is DataValue.SExpressionValue && value.name == name &&
                            matchItems(patterns, value.elems, bindings)
                    }
                }
                else -> null
            }
        }
    }
}
