package ru.tsaplev.app.ASTreeNodes

import ru.tsaplev.app.execution.DataValue

class MatchPattern private constructor(
    private val matches: (DataValue, MutableMap<String, DataValue>) -> Boolean
) {
    fun match(value: DataValue): Map<String, DataValue>? {
        val bindings = mutableMapOf<String, DataValue>()
        return if (matches(value, bindings)) bindings else null
    }

    companion object {
        fun wildcard() = MatchPattern { _, _ -> true }

        fun literal(expected: DataValue) = MatchPattern { value, _ -> value == expected }

        fun type(name: String): MatchPattern? = when (name) {
            "int" -> MatchPattern { value, _ -> value is DataValue.IntValue }
            "string" -> MatchPattern { value, _ -> value is DataValue.StringValue }
            "array" -> MatchPattern { value, _ -> value is DataValue.ArrayValue }
            "sexp" -> MatchPattern { value, _ -> value is DataValue.SExpressionValue }
            else -> null
        }

        fun bind(name: String, pattern: MatchPattern) = MatchPattern { value, bindings ->
            if (pattern.matches(value, bindings)) {
                bindings[name] = value
                true
            } else false
        }

        fun array(patterns: List<MatchPattern>) = MatchPattern { value, bindings ->
            value is DataValue.ArrayValue && matchItems(patterns, value.elems, bindings)
        }

        fun constructor(name: String, patterns: List<MatchPattern>) = MatchPattern { value, bindings ->
            value is DataValue.SExpressionValue && value.name == name &&
                matchItems(patterns, value.elems, bindings)
        }

        private fun matchItems(
            patterns: List<MatchPattern>,
            values: List<DataValue>,
            bindings: MutableMap<String, DataValue>
        ): Boolean = patterns.size == values.size && patterns.indices.all {
            patterns[it].matches(values[it], bindings)
        }
    }
}
