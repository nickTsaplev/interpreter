package ru.tsaplev.app.execution

class StandardDataIO: DataIO {
    override fun write(output: DataValue?, end: String) {
        if (output == null) {
            print(end)
            return
        }

        when (output) {
            is DataValue.ArrayValue -> {
                print('[')
                output.elems.forEachIndexed { index, value ->
                    write(value, if (index < output.elems.size - 1) "; " else "")
                }
                print(']')
            }
            is DataValue.IntValue -> {
                print(output.value)
            }
            is DataValue.StringValue -> {
                print(output.value)
            }
        }
        print(end)
    }

    override fun read(): DataValue {
        return parseDataValue(readln())
    }
}

internal fun parseDataValue(input: String): DataValue = DataValueParser(input).parse()

private class DataValueParser(private val input: String) {
    private var position = 0

    fun parse(): DataValue {
        skipWhitespace()
        val value = parseValue()
        skipWhitespace()
        require(position == input.length) { "Unexpected input at position $position" }
        return value
    }

    private fun parseValue(): DataValue {
        skipWhitespace()
        if (position == input.length) return DataValue.StringValue("")

        return when (input[position]) {
            '[' -> parseArray()
            '"' -> DataValue.StringValue(parseQuotedString())
            else -> parseScalar()
        }
    }

    private fun parseArray(): DataValue.ArrayValue {
        position++
        skipWhitespace()
        val elements = mutableListOf<DataValue>()
        if (consume(']')) return DataValue.ArrayValue(elements)

        while (true) {
            elements.add(parseValue())
            skipWhitespace()
            if (consume(']')) break
            require(consume(';') || consume(',')) { "Expected ',', ';' or ']' at position $position" }
            skipWhitespace()
        }
        return DataValue.ArrayValue(elements)
    }

    private fun parseQuotedString(): String {
        position++
        val result = StringBuilder()
        while (position < input.length) {
            val char = input[position++]
            when (char) {
                '"' -> return result.toString()
                '\\' -> {
                    require(position < input.length) { "Unfinished escape sequence" }
                    result.append(
                        when (val escaped = input[position++]) {
                            'n' -> '\n'
                            'r' -> '\r'
                            't' -> '\t'
                            '"', '\\' -> escaped
                            else -> throw IllegalArgumentException("Unsupported escape sequence: \\$escaped")
                        }
                    )
                }
                else -> result.append(char)
            }
        }
        throw IllegalArgumentException("Unterminated string")
    }

    private fun parseScalar(): DataValue {
        val start = position
        while (position < input.length && input[position] !in charArrayOf(',', ';', ']')) position++
        val token = input.substring(start, position).trim()
        return token.toIntOrNull()?.let(DataValue::IntValue) ?: DataValue.StringValue(token)
    }

    private fun consume(expected: Char): Boolean {
        if (position >= input.length || input[position] != expected) return false
        position++
        return true
    }

    private fun skipWhitespace() {
        while (position < input.length && input[position].isWhitespace()) position++
    }
}
