package ru.tsaplev.app

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.encodeToJsonElement
import ru.tsaplev.app.execution.stackMachine.MachineInstruction

fun StackInstructionsToJSON(instructions: List<MachineInstruction>) = JsonArray(instructions.map {
    when(it) {
        is MachineInstruction.BINOP -> Json.encodeToJsonElement(mapOf("BINOP" to it.op))
        is MachineInstruction.CONST -> Json.encodeToJsonElement(mapOf("CONST" to it.value))
        is MachineInstruction.JMP -> Json.encodeToJsonElement(mapOf("JMP" to it.label))
        is MachineInstruction.JNZ -> Json.encodeToJsonElement(mapOf("JNZ" to it.label))
        is MachineInstruction.JZ -> Json.encodeToJsonElement(mapOf("JZ" to it.label))
        is MachineInstruction.LABEL -> Json.encodeToJsonElement(mapOf("LABEL" to it.label))
        is MachineInstruction.LD -> Json.encodeToJsonElement(mapOf("LD" to it.varname))
        is MachineInstruction.READ -> Json.encodeToJsonElement("READ")
        is MachineInstruction.ST -> Json.encodeToJsonElement(mapOf("ST" to it.varname))
        is MachineInstruction.WRITE -> Json.encodeToJsonElement("WRITE")
    }
})