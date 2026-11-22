package ru.tsaplev.app.ASTreeNodes

import ru.tsaplev.app.execution.DataValue

sealed class MatchPattern private constructor() {
    fun match(value: DataValue): Map<String, DataValue>? {
        val bindings = mutableMapOf<String, DataValue>()
        return if (matches(value, bindings)) bindings else null
    }

    abstract fun matches(value: DataValue,
                         bindings: MutableMap<String, DataValue>): Boolean

    class Wirdcard(): MatchPattern() {
        override fun matches(
            value: DataValue,
            bindings: MutableMap<String, DataValue>
        ): Boolean = true
    }

    class Literal(val expected: DataValue): MatchPattern() {
        override fun matches(
            value: DataValue,
            bindings: MutableMap<String, DataValue>
        ): Boolean {
            return (value == expected)
        }
    }

    class Type(val name: String): MatchPattern() {
        override fun matches(
            value: DataValue,
            bindings: MutableMap<String, DataValue>
        ): Boolean = when (name) {
            "int" -> value is DataValue.IntValue
            "string" -> value is DataValue.StringValue
            "array" -> value is DataValue.ArrayValue
            "sexp" -> value is DataValue.SExpressionValue
            else -> true
        }
    }

    class Bind(val name: String, val pattern: MatchPattern): MatchPattern() {
        override fun matches(
            value: DataValue,
            bindings: MutableMap<String, DataValue>
        ): Boolean {
            return if (pattern.matches(value, bindings)) {
                bindings[name] = value
                true
            } else false
        }
    }

    class Array(val patterns: List<MatchPattern>) : MatchPattern() {
        override fun matches(
            value: DataValue,
            bindings: MutableMap<String, DataValue>
        ): Boolean {
            return value is DataValue.ArrayValue && value.elems.indices.all {
                patterns[it].matches(value.elems[it], bindings)
            }
        }
    }

    class SExpression(val name: String, val patterns: List<MatchPattern>): MatchPattern() {
        override fun matches(
            value: DataValue,
            bindings: MutableMap<String, DataValue>
        ): Boolean {
            return value is DataValue.SExpressionValue && value.name == name && value.elems.indices.all {
                patterns[it].matches(value.elems[it], bindings)
            }
        }
    }
}
