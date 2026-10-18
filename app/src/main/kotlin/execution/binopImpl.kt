package ru.tsaplev.app.execution

fun binaryOperation(operand: String, left: Int, right: Int): Int {
    return when (operand) {
        "+" -> left + right
        "-" -> left - right
        "*" -> left * right
        "/" -> left / right
        "%" -> left % right
        "<" -> (left < right).toIntBoolean()
        "<=" -> (left <= right).toIntBoolean()
        ">" -> (left > right).toIntBoolean()
        ">=" -> (left >= right).toIntBoolean()
        "==" -> (left == right).toIntBoolean()
        "!=" -> (left != right).toIntBoolean()
        "&&" -> (left != 0 && right != 0).toIntBoolean()
        "!!", "||" -> (left != 0 || right != 0).toIntBoolean()
        else -> throw IllegalArgumentException("Unknown binary operation: $operand")
    }
}

private fun Boolean.toIntBoolean(): Int = if (this) 1 else 0
