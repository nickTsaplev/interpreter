package ru.tsaplev.app

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
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
        "ST" -> MachineInstruction.ST(field)
        "LD" -> MachineInstruction.LD(field)
        else -> null
    }
}

fun JSONToStackInstructions(text: String): List<MachineInstruction> {
    val array: JsonArray = Json.decodeFromString(text)
    val ans = mutableListOf<MachineInstruction>()
    for (command in array) {
        if (command is JsonPrimitive) {
            if (command.content == "READ")
                ans.add(MachineInstruction.READ())
            if (command.content == "WRITE")
                ans.add(MachineInstruction.READ())
        }

        if (command is JsonObject) {
            val key = command.keys.first()
            val field = command[key]?.jsonPrimitive?.content
            if (field != null) {
                toCommandWithArg(key, field)?.also {
                    ans.add(it)
                }
            }
        }
    }
    return ans
}
