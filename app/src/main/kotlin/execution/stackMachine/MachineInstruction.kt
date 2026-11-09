package ru.tsaplev.app.execution.stackMachine

import ru.tsaplev.app.execution.MatchPattern

sealed class MachineInstruction {
    class READ: MachineInstruction()
    class WRITE: MachineInstruction()
    class LD(val varname: String): MachineInstruction()
    class ST(val varname: String): MachineInstruction()
    class CONST(val value: Int): MachineInstruction()
    class STRING(val value: String): MachineInstruction()
    class ARRAY(val size: Int): MachineInstruction()
    class ELEM: MachineInstruction()
    class SET: MachineInstruction()
    class LENGTH: MachineInstruction()
    class BINOP(val op: String): MachineInstruction()
    class LABEL(val label: String): MachineInstruction()
    class JMP(val label: String): MachineInstruction()
    class JZ(val label: String): MachineInstruction()
    class JNZ(val label: String): MachineInstruction()

    class MATCH(val pattern: MatchPattern): MachineInstruction()
    class ENDMATCH: MachineInstruction()
    class MATCHFAIL: MachineInstruction()
    class SEXP(val name: String, val size: Int): MachineInstruction()
}
