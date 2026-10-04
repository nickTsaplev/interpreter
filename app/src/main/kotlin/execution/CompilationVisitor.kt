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
    private val activeLegacyIfEnds = mutableSetOf<String>()

    override fun visit(node: ASTBinOP) {
        instructions.add(MachineInstruction.BINOP(node.binop))
    }

    override fun visit(node: ASTConst) {
        instructions.add(MachineInstruction.CONST(node.value.get()))
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
        node.legacyLabels?.let { labels ->
            val elseLabel = labels.elseLabel
            val endLabel = labels.end
            val ownsEndLabel = activeLegacyIfEnds.add(endLabel)
            instructions.add(MachineInstruction.JZ(elseLabel))
            node.thenBranch.visit(this)
            instructions.add(MachineInstruction.JMP(endLabel))
            instructions.add(MachineInstruction.LABEL(elseLabel))
            node.elseBranch?.visit(this)
            if (ownsEndLabel && !endLabel.startsWith("L_while_cond_")) {
                instructions.add(MachineInstruction.LABEL(endLabel))
            }
            if (ownsEndLabel) activeLegacyIfEnds.remove(endLabel)
            return
        }
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
        node.legacyLabels?.let { labels ->
            val bodyLabel = "L_while_cody_${labels.body}"
            val conditionLabel = "L_while_cond_${labels.condition}"
            instructions.add(MachineInstruction.JMP(conditionLabel))
            instructions.add(MachineInstruction.LABEL(bodyLabel))
            node.body.visit(this)
            instructions.add(MachineInstruction.LABEL(conditionLabel))
            node.cond.visit(this)
            instructions.add(MachineInstruction.JNZ(bodyLabel))
            return
        }
        val loopStartLabel = "WHILE-START ${labelCount++}"
        val loopEndLabel = "WHILE-END ${labelCount++}"

        instructions.add(MachineInstruction.LABEL(loopStartLabel))
        node.cond.visit(this)
        instructions.add(MachineInstruction.JZ(loopEndLabel))
        node.body.visit(this)
        instructions.add(MachineInstruction.JMP(loopStartLabel))
        instructions.add(MachineInstruction.LABEL(loopEndLabel))
    }

    override fun visit(node: ASTDo) {
        node.legacyLabels?.let { labels ->
            val bodyLabel = "L_while_cody_${labels.body}"
            val conditionLabel = "L_while_cond_${labels.condition}"
            instructions.add(MachineInstruction.LABEL(bodyLabel))
            node.body.visit(this)
            instructions.add(MachineInstruction.LABEL(conditionLabel))
            node.cond.visit(this)
            instructions.add(MachineInstruction.JNZ(bodyLabel))
            return
        }
        val loopStartLabel = "DO-WHILE-START ${labelCount++}"

        instructions.add(MachineInstruction.LABEL(loopStartLabel))
        node.body.visit(this)
        node.cond.visit(this)
        instructions.add(MachineInstruction.JNZ(loopStartLabel))
    }

    override fun visit(node: ASTSkip) {

    }
}
