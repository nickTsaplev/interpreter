package ru.tsaplev.app.execution.stackMachine

import ru.tsaplev.app.execution.DataValue
import ru.tsaplev.app.execution.ExecutionFrame
import ru.tsaplev.app.execution.IntIO
import ru.tsaplev.app.execution.binaryOperation

class StackMachine {
    private val mem = ExecutionFrame("main")
    private var labelTable = mapOf<String, Int>()

    private fun findLabel(commands: List<MachineInstruction>, label: String): Int {
        return labelTable[label] ?: throw IllegalArgumentException("$label not found")
    }

    private fun createLabelTable(commands: List<MachineInstruction>) {
        val newLabels = mutableMapOf<String, Int>()
        commands.forEachIndexed { index, instruction ->
            if (instruction is MachineInstruction.LABEL)
                newLabels[instruction.label] = index
        }
        labelTable = newLabels
    }

    fun run(commands: List<MachineInstruction>, io: IntIO) {
        var pc = 0

        createLabelTable(commands)

        while (pc < commands.size) {
            val command = commands[pc++]
            when(command) {
                is MachineInstruction.READ -> mem.pushNameless(DataValue(io.read()))
                is MachineInstruction.BINOP -> {
                    val right = mem.popNameless()
                    val left = mem.popNameless()

                    mem.pushNameless(DataValue(binaryOperation(command.op, left.get(), right.get())))
                }
                is MachineInstruction.CONST -> mem.pushNameless(DataValue(command.value))
                is MachineInstruction.JMP -> pc = findLabel(commands, command.label)
                is MachineInstruction.JNZ -> {
                    val zero = mem.popNameless()
                    if (zero.get() != 0) {
                        pc = findLabel(commands, command.label)
                    }
                }
                is MachineInstruction.JZ -> {
                    val zero = mem.popNameless()
                    if (zero.get() == 0) {
                        pc = findLabel(commands, command.label)
                    }
                }
                is MachineInstruction.LABEL -> {}
                is MachineInstruction.LD -> {
                    val value = mem.getVar(command.varname)
                        ?: throw IllegalArgumentException("Variable ${command.varname} not found")
                    mem.pushNameless(value)
                }
                is MachineInstruction.ST -> mem.setVar(command.varname, mem.popNameless())
                is MachineInstruction.WRITE -> io.write(mem.popNameless().get())
            }
        }
    }
}
