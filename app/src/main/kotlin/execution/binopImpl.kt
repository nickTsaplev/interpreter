package ru.tsaplev.app.execution

fun binaryOperation(operand: String, left: Int, right: Int): Int {
    return when (operand) {
        "+" -> left + right
        "-" -> left - right
        "*" -> left * right
        "/" -> left / right
        "%" -> left % right
        "<" -> (left < right).toLanguageBoolean()
        "<=" -> (left <= right).toLanguageBoolean()
        ">" -> (left > right).toLanguageBoolean()
        ">=" -> (left >= right).toLanguageBoolean()
        "==" -> (left == right).toLanguageBoolean()
        "!=" -> (left != right).toLanguageBoolean()
        "&&" -> (left != 0 && right != 0).toLanguageBoolean()
        "!!", "||" -> (left != 0 || right != 0).toLanguageBoolean()
        else -> throw IllegalArgumentException("Unknown binary operation: $operand")
    }
}

private fun Boolean.toLanguageBoolean(): Int = if (this) 1 else 0
