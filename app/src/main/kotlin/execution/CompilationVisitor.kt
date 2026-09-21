package ru.tsaplev.app.execution

import ru.tsaplev.app.ASTVisitor
import ru.tsaplev.app.ASTreeNodes.ASTAssign
import ru.tsaplev.app.ASTreeNodes.ASTBinOP
import ru.tsaplev.app.ASTreeNodes.ASTConst
import ru.tsaplev.app.ASTreeNodes.ASTDo
import ru.tsaplev.app.ASTreeNodes.ASTFuncCall
import ru.tsaplev.app.ASTreeNodes.ASTIf
import ru.tsaplev.app.ASTreeNodes.ASTRead
import ru.tsaplev.app.ASTreeNodes.ASTSkip
import ru.tsaplev.app.ASTreeNodes.ASTVar
import ru.tsaplev.app.ASTreeNodes.ASTWhile
import ru.tsaplev.app.execution.stackMachine.MachineInstruction

class CompilationVisitor: ASTVisitor {
    val instructions = mutableListOf<MachineInstruction>()
    var labelCount = 0

    override fun visit(node: ASTBinOP) {
        instructions.add(MachineInstruction.BINOP(node.binop))
    }

    override fun visit(node: ASTConst) {
        instructions.add(MachineInstruction.CONST(node.value.get()))
    }

    override fun visit(node: ASTFuncCall) {
        if (node.name == "write") {
            instructions.add(MachineInstruction.WRITE())
        }
    }

    override fun visit(node: ASTRead) {
        instructions.add(MachineInstruction.READ())
        instructions.add(MachineInstruction.ST(node.varName))
    }

    override fun visit(node: ASTAssign) {
        instructions.add(MachineInstruction.ST(node.name))
    }

    override fun visit(node: ASTVar) {
        instructions.add(MachineInstruction.LD(node.name))
    }

    override fun visit(node: ASTIf) {
        node.cond.visit(this)
        val elseLabelName = "IF-ELSE {$labelCount++}"
        val endLabelName = "IF-END {$labelCount++}"

        instructions.add(MachineInstruction.JNZ(elseLabelName))
        node.thenBranch.visit(this)
        instructions.add(MachineInstruction.JMP(endLabelName))
        instructions.add(MachineInstruction.LABEL(elseLabelName))
        node.elseBranch?.visit(this)
        instructions.add(MachineInstruction.LABEL(endLabelName))
    }

    override fun visit(node: ASTWhile) {
        val loopStartLabel = "WHILE-START {$labelCount++}"
        val loopEndLabel = "WHILE-END {$labelCount++}"

        instructions.add(MachineInstruction.LABEL(loopStartLabel))
        node.cond.visit(this)
        instructions.add(MachineInstruction.JZ(loopEndLabel))
        node.body.visit(this)
        instructions.add(MachineInstruction.JMP(loopStartLabel))
        instructions.add(MachineInstruction.LABEL(loopEndLabel))
    }

    override fun visit(node: ASTDo) {
        val loopStartLabel = "DO-WHILE-START {$labelCount++}"

        instructions.add(MachineInstruction.LABEL(loopStartLabel))
        node.body.visit(this)
        node.cond.visit(this)
        instructions.add(MachineInstruction.JNZ(loopStartLabel))
    }

    override fun visit(node: ASTSkip) {

    }
}