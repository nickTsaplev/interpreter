package ru.tsaplev.app.execution

import ru.tsaplev.app.ASTVisitor
import ru.tsaplev.app.ASTreeNodes.ASTArray
import ru.tsaplev.app.ASTreeNodes.ASTAssign
import ru.tsaplev.app.ASTreeNodes.ASTBinOP
import ru.tsaplev.app.ASTreeNodes.ASTConst
import ru.tsaplev.app.ASTreeNodes.ASTDo
import ru.tsaplev.app.ASTreeNodes.ASTElem
import ru.tsaplev.app.ASTreeNodes.ASTFuncCall
import ru.tsaplev.app.ASTreeNodes.ASTIf
import ru.tsaplev.app.ASTreeNodes.ASTRead
import ru.tsaplev.app.ASTreeNodes.ASTSeq
import ru.tsaplev.app.ASTreeNodes.ASTSet
import ru.tsaplev.app.ASTreeNodes.ASTSkip
import ru.tsaplev.app.ASTreeNodes.ASTString
import ru.tsaplev.app.ASTreeNodes.ASTLength
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
        val value = node.value
        if (value is DataValue.IntValue)
            instructions.add(MachineInstruction.CONST(value.value))
        else
            throw IllegalArgumentException("Expected int value, got ${node.value}")
    }

    override fun visit(node: ASTFuncCall) {
        if (node.name == "write") {
            instructions.add(MachineInstruction.WRITE())
        } else {
            throw IllegalArgumentException("Unknown function: ${node.name}")
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
        val elseLabelName = "IF-ELSE ${labelCount++}"
        val endLabelName = "IF-END ${labelCount++}"

        instructions.add(MachineInstruction.JZ(elseLabelName))
        node.thenBranch.visit(this)
        instructions.add(MachineInstruction.JMP(endLabelName))
        instructions.add(MachineInstruction.LABEL(elseLabelName))
        node.elseBranch?.visit(this)
        instructions.add(MachineInstruction.LABEL(endLabelName))
    }

    override fun visit(node: ASTWhile) {
        val loopStartLabel = "WHILE-START ${labelCount++}"
        val loopConditionLabel = "WHILE-CONDITION ${labelCount++}"

        instructions.add(MachineInstruction.JMP(loopConditionLabel))

        instructions.add(MachineInstruction.LABEL(loopStartLabel))
        node.body.visit(this)

        instructions.add(MachineInstruction.LABEL(loopConditionLabel))
        node.cond.visit(this)
        instructions.add(MachineInstruction.JNZ(loopStartLabel))

    }

    override fun visit(node: ASTDo) {
        val loopStartLabel = "DO-WHILE-START ${labelCount++}"

        instructions.add(MachineInstruction.LABEL(loopStartLabel))
        node.body.visit(this)
        node.cond.visit(this)
        instructions.add(MachineInstruction.JNZ(loopStartLabel))
    }

    override fun visit(node: ASTSkip) {

    }

    override fun visit(node: ASTSeq) {
        // Already visited
    }

    override fun visit(node: ASTString) {
        instructions.add(MachineInstruction.STRING(node.value))
    }

    override fun visit(node: ASTArray) {
        instructions.add(MachineInstruction.ARRAY(node.elems.size))
    }

    override fun visit(node: ASTElem) {
        instructions.add(MachineInstruction.ELEM())
    }

    override fun visit(node: ASTSet) {
        instructions.add(MachineInstruction.SET())
    }

    override fun visit(node: ASTLength) {
        instructions.add(MachineInstruction.LENGTH())
    }
}
