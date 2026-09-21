package ru.tsaplev.app.execution.stackMachine

import ru.tsaplev.app.execution.DataValue
import ru.tsaplev.app.execution.ExecutionFrame
import ru.tsaplev.app.execution.IntIO
import ru.tsaplev.app.execution.binaryOperation

class StackMachine {
    private val mem = ExecutionFrame("main")

    private fun findLabel(commands: List<MachineInstruction>, label: String): Int {
        commands.forEachIndexed { index, instruction ->
            if (instruction is MachineInstruction.LABEL && instruction.label == label)
                return index
        }
        throw IllegalArgumentException("$label not found")
    }

    fun run(commands: List<MachineInstruction>, io: IntIO) {
        var pc = 1

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