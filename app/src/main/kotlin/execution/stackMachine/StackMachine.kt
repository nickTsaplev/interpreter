package ru.tsaplev.app.execution.stackMachine

import ru.tsaplev.app.execution.DataValue
import ru.tsaplev.app.execution.DataIO
import ru.tsaplev.app.execution.ExecutionFrame
import ru.tsaplev.app.execution.IntIO
import ru.tsaplev.app.execution.binaryOperationOverloaded

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

    fun run(commands: List<MachineInstruction>, io: IntIO) = run(commands, object : DataIO {
        override fun write(output: DataValue?, end: String) {
            io.write((output as? DataValue.IntValue)?.value)
        }

        override fun read(): DataValue = DataValue.IntValue(io.read())
    })

    fun run(commands: List<MachineInstruction>, io: DataIO) {
        var pc = 0

        createLabelTable(commands)

        while (pc < commands.size) {
            val command = commands[pc++]
            when(command) {
                is MachineInstruction.READ -> mem.pushNameless(io.read())
                is MachineInstruction.BINOP -> {
                    val right = mem.popNameless()
                    val left = mem.popNameless()

                    mem.pushNameless((binaryOperationOverloaded(command.op, left, right)))
                }
                is MachineInstruction.CONST -> mem.pushNameless(DataValue.IntValue(command.value))
                is MachineInstruction.STRING -> mem.pushNameless(DataValue.StringValue(command.value))
                is MachineInstruction.ARRAY -> {
                    val elements = MutableList(command.size) { mem.popNameless() }
                    mem.pushNameless(DataValue.ArrayValue(elements.reversed().toMutableList()))
                }
                is MachineInstruction.ELEM -> {
                    val index = (mem.popNameless() as? DataValue.IntValue)?.value
                        ?: throw IllegalArgumentException("Array index is not an integer")
                    val array = mem.popNameless()

                    when(array) {
                        is DataValue.StringValue -> mem.pushNameless(DataValue.IntValue(array.value[index].code))
                        is DataValue.ArrayValue -> mem.pushNameless(array.elems[index])
                        else -> throw IllegalStateException("Indexed value is not an array/string")
                    }
                }
                is MachineInstruction.SET -> {
                    val source = mem.popNameless()
                    val index = (mem.popNameless() as? DataValue.IntValue)?.value
                        ?: throw IllegalArgumentException("Array index is not an integer")
                    val destination = mem.popNameless()

                    when(destination) {
                        is DataValue.StringValue -> {
                            val charValue = (source as? DataValue.IntValue) ?: throw IllegalStateException("string element must be char")
                            destination.value = destination.value.replaceRange(index, index + 1, charValue.value.toChar().toString())
                        }
                        is DataValue.ArrayValue -> destination.elems[index] = source
                        else -> throw IllegalStateException("Set destination is not an array/string")
                    }
                }
                is MachineInstruction.LENGTH -> {
                    val length = when (val value = mem.popNameless()) {
                        is DataValue.ArrayValue -> value.elems.size
                        is DataValue.StringValue -> value.value.length
                        is DataValue.IntValue -> throw IllegalArgumentException("length is only defined for arrays and strings")
                    }
                    mem.pushNameless(DataValue.IntValue(length))
                }
                is MachineInstruction.JMP -> pc = findLabel(commands, command.label)
                is MachineInstruction.JNZ -> {
                    val zero = mem.popNameless()
                    if (zero !is DataValue.IntValue)
                        throw IllegalArgumentException("Condition produced not int")
                    if (zero.value != 0) {
                        pc = findLabel(commands, command.label)
                    }
                }
                is MachineInstruction.JZ -> {
                    val zero = mem.popNameless()
                    if (zero !is DataValue.IntValue)
                        throw IllegalArgumentException("Condition produced not int")
                    if (zero.value == 0) {
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
                is MachineInstruction.WRITE -> io.write(mem.popNameless())
            }
        }
    }
}
