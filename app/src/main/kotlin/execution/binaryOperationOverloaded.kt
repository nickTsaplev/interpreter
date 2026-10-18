package ru.tsaplev.app.execution

import kotlin.math.min

private fun Boolean.toLanguageBoolean(): DataValue = if (this) DataValue.IntValue(1) else DataValue.IntValue(0)

fun cmpDV(left: DataValue, right: DataValue): Int {
    if (left is DataValue.IntValue && right is DataValue.IntValue) {
        return left.value - right.value
    }

    if (left is DataValue.StringValue && right is DataValue.StringValue) {
        if (left.value < right.value) {
            return -1
        }
        if (left.value == right.value) {
            return 0
        }
        return 1
    }

    if (left is DataValue.ArrayValue && right is DataValue.ArrayValue) {
        for (index in 0..<min(left.elems.size, right.elems.size)) {
            val cmpRs = cmpDV(left.elems[index], right.elems[index])
            if (cmpRs != 0)
                return cmpRs
        }

        return (left.elems.size - right.elems.size)
    }

    throw IllegalArgumentException()
}

fun binaryOperationOverloaded(operand: String, left: DataValue, right: DataValue): DataValue {
    when (operand) {
        "<" -> (cmpDV(left,right) < 0).toLanguageBoolean()
        "<=" -> (cmpDV(left,right) <= 0).toLanguageBoolean()
        ">" -> (cmpDV(left,right) > 0).toLanguageBoolean()
        ">=" -> (cmpDV(left,right) >= 0).toLanguageBoolean()
        "==" -> (cmpDV(left,right) == 0).toLanguageBoolean()
        "!=" -> (cmpDV(left,right) != 0).toLanguageBoolean()
    }

    if (left is DataValue.IntValue && right is DataValue.IntValue) {
        return DataValue.IntValue(binaryOperation(operand, left.value, right.value))
    }

    if (left is DataValue.StringValue && right is DataValue.StringValue) {
        return when (operand) {
            "+" -> DataValue.StringValue(left.value + right.value)
            else -> throw IllegalArgumentException("Unknown binary string operation: $operand")
        }
    }

    if (left is DataValue.ArrayValue && right is DataValue.ArrayValue) {
        return when (operand) {
            "+" -> DataValue.ArrayValue((left.elems + right.elems).toMutableList())
            else -> throw IllegalArgumentException("Unknown binary string operation: $operand")
        }
    }

    throw IllegalArgumentException("Differing types: $left and $right")
}