package ru.tsaplev.app.execution

import kotlin.text.trim

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

    private fun parseInput(rawInput: String): DataValue {
        val input = rawInput.trim()

        if (input == "") {
            return DataValue.StringValue("")
        }

        if (input[0] == '[') {
            return DataValue.ArrayValue(input.replace(',', ';').split(';').map {
                parseInput(it)
            }.toMutableList())
        }

        if (input[0].isDigit()) {
            return try {
                DataValue.IntValue(input.toInt())
            } catch (e: NumberFormatException) {
                DataValue.StringValue(input)
            }
        }
        return DataValue.StringValue(input)
    }

    override fun read(): DataValue {
        val input = readln()
        return parseInput(input)
    }
}