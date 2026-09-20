package ru.tsaplev.app.execution

import ru.tsaplev.app.ASTVisitor
import ru.tsaplev.app.ASTreeNodes.ASTAssign
import ru.tsaplev.app.ASTreeNodes.ASTBinOP
import ru.tsaplev.app.ASTreeNodes.ASTConst
import ru.tsaplev.app.ASTreeNodes.ASTFuncCall
import ru.tsaplev.app.ASTreeNodes.ASTRead
import ru.tsaplev.app.ASTreeNodes.ASTVar

class ExecutionVisitor: ASTVisitor {
    private val executionStack = ExecutionStack()
    private val errors = mutableListOf<String>()

    private fun binaryOperation(operand: String, left: Int, right: Int): Int {
        when (operand) {
            "+" -> return left+right
            "-" -> return left-right
            "*" -> return left*right
            "/" -> return left/right
            "<" -> return if (left < right) 1 else 0
            "<=" -> return if (left <= right) 1 else 0
            ">" -> return if (left > right) 1 else 0
            ">=" -> return if (left >= right) 1 else 0
            "==" -> return if (left == right) 1 else 0
            "!=" -> return if (left != right) 1 else 0
            "&&" -> return if ((left == 1) && (right == 1)) 1 else 0
            "||" -> return if ((left == 1) || (right == 1)) 1 else 0
            "%" -> return (left % right)
        }
        return 0
    }

    override fun visit(node: ASTBinOP) {
        val right = executionStack.current().getTopNameless()
        val left = executionStack.current().getTopNameless()

        if (right == null || left == null) {
            errors.add("Binary operation " + node.binop + " missing operands")
            return
        }

        executionStack.current().pushNameless(
            DataValue(binaryOperation(node.binop, left.get(), right.get()))
        )
    }

    override fun visit(node: ASTConst) {
        executionStack.current().pushNameless(node.value)
    }

    override fun visit(node: ASTFuncCall) {
        if (node.name == "write") {
            println(executionStack.current().getTopNameless()?.get())
            return
        }
    }

    override fun visit(node: ASTRead) {
        val value = readln().toInt()
        executionStack.current().setVar(node.varName, DataValue(value))
    }

    override fun visit(node: ASTAssign) {
        val value = executionStack.current().getTopNameless()
        if (value == null) {
            errors.add("Assignment left part not found")
            return
        }
        executionStack.current().setVar(node.name, value)
    }

    override fun visit(node: ASTVar) {
        val value = executionStack.current().getVar(node.name)
        if (value == null) {
            errors.add("Var not found: ${node.name}")
            return
        }
        executionStack.current().pushNameless(value)
    }
}