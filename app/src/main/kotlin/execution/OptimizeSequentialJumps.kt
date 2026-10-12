package ru.tsaplev.app.execution

import ru.tsaplev.app.execution.stackMachine.MachineInstruction

fun optimizeSequentialJumps(instructions: List<MachineInstruction>): List<MachineInstruction> {
    val replaceTable = mutableMapOf<String, String>()
    for (i in 0..(instructions.size - 2)) {
        if (instructions[i] is MachineInstruction.LABEL && instructions[i + 1] is MachineInstruction.JMP) {
            val fromLabel = (instructions[i] as MachineInstruction.LABEL).label
            val toLabel = (instructions[i + 1] as MachineInstruction.JMP).label
            replaceTable[fromLabel] = toLabel
        }
    }

    val newInstructions = instructions.toMutableList()
    for (i in newInstructions.indices) {
        if (newInstructions[i] is MachineInstruction.JMP) {
            while ((newInstructions[i] as MachineInstruction.JMP).label in replaceTable.keys) {
                newInstructions[i] = MachineInstruction.JMP(replaceTable[(newInstructions[i] as MachineInstruction.JMP).label]!!)
            }
        }
    }

    return newInstructions.filter {
        if (it is MachineInstruction.LABEL)
            it.label !in replaceTable.keys
        else
            true
    }
}

fun optimizeStepJumps(instructions: List<MachineInstruction>): List<MachineInstruction> {
    return instructions.filterIndexed { index, instruction ->
        !(index < instructions.size - 1 &&
                instruction is MachineInstruction.JMP &&
                instructions[index + 1] is MachineInstruction.LABEL &&
                instruction.label == (instructions[index + 1] as MachineInstruction.LABEL).label)
    }
}

fun removeUnusedLabels(instructions: List<MachineInstruction>): List<MachineInstruction> {
    val usedLabels = mutableSetOf<String>()
    instructions.forEach {
        if (it is MachineInstruction.JMP)
            usedLabels.add(it.label)
        if (it is MachineInstruction.JZ)
            usedLabels.add(it.label)
        if (it is MachineInstruction.JNZ)
            usedLabels.add(it.label)
    }

    return instructions.filter {
        !(it is MachineInstruction.LABEL && it.label !in usedLabels)
    }
}

fun optimize(instructions: List<MachineInstruction>): List<MachineInstruction> {
    return removeUnusedLabels(optimizeStepJumps(optimizeSequentialJumps(instructions)))
}