package ru.tsaplev.app

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import ru.tsaplev.app.execution.stackMachine.MachineInstruction

private fun toCommandWithArg(name: String, field: String): MachineInstruction? {
    return when(name) {
        "JNZ" -> MachineInstruction.JNZ(field)
        "JZ" -> MachineInstruction.JZ(field)
        "JMP" -> MachineInstruction.JMP(field)
        "LABEL" -> MachineInstruction.LABEL(field)
        "BINOP" -> MachineInstruction.BINOP(field)
        "CONST" -> MachineInstruction.CONST(field.toInt())
        "STRING" -> MachineInstruction.STRING(field)
        "ARRAY" -> MachineInstruction.ARRAY(field.toInt())
        "ST" -> MachineInstruction.ST(field)
        "LD" -> MachineInstruction.LD(field)
        "PATT" -> MachineInstruction.PATT(field)
        else -> null
    }
}

fun JSONToStackInstructions(text: String): List<MachineInstruction> {
    val array: JsonArray = Json.decodeFromString(text)
    val ans = mutableListOf<MachineInstruction>()
    for (command in array) {
        if (command is JsonPrimitive) {
            when (command.content) {
                "READ" -> ans.add(MachineInstruction.READ())
                "WRITE" -> ans.add(MachineInstruction.WRITE())
                "ELEM" -> ans.add(MachineInstruction.ELEM())
                "SET" -> ans.add(MachineInstruction.SET())
                "LENGTH" -> ans.add(MachineInstruction.LENGTH())
                "DROP" -> ans.add(MachineInstruction.DROP())
                "DUP" -> ans.add(MachineInstruction.DUP())
                "SCOPE" -> ans.add(MachineInstruction.SCOPE())
                "ENTER" -> ans.add(MachineInstruction.ENTER())
                "LEAVE" -> ans.add(MachineInstruction.LEAVE())
                "MF" -> ans.add(MachineInstruction.MF())

                else -> throw IllegalArgumentException("Unknown stack instruction: ${command.content}")
            }
        } else if (command is JsonObject) {
            require(command.size == 1) { "Expected one stack instruction per object" }
            val key = command.keys.first()
            when(key)  {
                "SEXP" -> {
                    val field = command[key]?.jsonArray ?: throw IllegalArgumentException("Missing argument for $key")
                    ans.add(
                        MachineInstruction.SEXP(
                            field[0].jsonPrimitive.content,
                            field[1].jsonPrimitive.content.toInt()
                        )
                    )
                }
                "PATT" -> {
                    val field = command[key]
                    if (field is JsonPrimitive) {
                        ans.add(toCommandWithArg(key, field.content) ?:
                            throw IllegalArgumentException("Unknown stack instruction: $key"))
                    } else {
                        val name = field?.jsonObject["tag"]?.jsonObject["name"]?.jsonPrimitive?.content
                        val len = field?.jsonObject["tag"]?.jsonObject["length"]?.jsonPrimitive?.content
                        ans.add(MachineInstruction.PATT("Tag ${name} ${len}"))
                    }

                }
                else -> {
                    val field = command[key]?.jsonPrimitive?.content
                        ?: throw IllegalArgumentException("Missing argument for $key")
                    ans.add(
                        toCommandWithArg(key, field)
                            ?: throw IllegalArgumentException("Unknown stack instruction: $key")
                    )
                }
            }
        } else {
            throw IllegalArgumentException("Invalid stack instruction: $command")
        }
    }
    return ans
}
