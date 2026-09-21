package ru.tsaplev.app.execution.stackMachine

sealed class MachineInstruction {
    class READ: MachineInstruction()
    class WRITE: MachineInstruction()
    class LD(val varname: String): MachineInstruction()
    class ST(val varname: String): MachineInstruction()
    class CONST(val value: Int): MachineInstruction()
    class BINOP(val op: String): MachineInstruction()
    class LABEL(val label: String): MachineInstruction()
    class JMP(val label: String): MachineInstruction()
    class JZ(val label: String): MachineInstruction()
    class JNZ(val label: String): MachineInstruction()
}