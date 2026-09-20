package ru.tsaplev.app.execution

import ru.tsaplev.app.ASTVisitor
import ru.tsaplev.app.ASTreeNodes.ASTAssign
import ru.tsaplev.app.ASTreeNodes.ASTBinOP
import ru.tsaplev.app.ASTreeNodes.ASTConst
import ru.tsaplev.app.ASTreeNodes.ASTFuncCall
import ru.tsaplev.app.ASTreeNodes.ASTRead
import ru.tsaplev.app.ASTreeNodes.ASTVar
import ru.tsaplev.app.ASTreeNodes.ASTIf
import ru.tsaplev.app.ASTreeNodes.ASTWhile
import ru.tsaplev.app.ASTreeNodes.ASTDo
import ru.tsaplev.app.ASTreeNodes.ASTSkip

class ExecutionVisitor(val io: IntIO = StandardIO()): ASTVisitor {

    private val executionStack = ExecutionStack()

    private fun binaryOperation(operand: String, left: Int, right: Int): Int {
        return when (operand) {
            "+" -> left + right
            "-" -> left - right
            "*" -> left * right
            "/" -> left / right
            "%" -> left % right
            "<" -> (left < right).toLanguageBoolean()
            "<=" -> (left <= right).toLanguageBoolean()
            ">" -> (left > right).toLanguageBoolean()
            ">=" -> (left >= right).toLanguageBoolean()
            "==" -> (left == right).toLanguageBoolean()
            "!=" -> (left != right).toLanguageBoolean()
            "&&" -> (left != 0 && right != 0).toLanguageBoolean()
            "!!", "||" -> (left != 0 || right != 0).toLanguageBoolean()
            else -> throw IllegalArgumentException("Unknown binary operation: $operand")
        }
    }

    private fun Boolean.toLanguageBoolean(): Int = if (this) 1 else 0

    private fun popValue(context: String): DataValue =
        executionStack.current().getTopNameless()
            ?: throw IllegalStateException("$context did not produce a value")

    override fun visit(node: ASTBinOP) {
        val right = popValue("Right operand of ${node.binop}")
        val left = popValue("Left operand of ${node.binop}")

        executionStack.current().pushNameless(
            DataValue(binaryOperation(node.binop, left.get(), right.get()))
        )
    }

    override fun visit(node: ASTConst) {
        executionStack.current().pushNameless(node.value)
    }

    override fun visit(node: ASTFuncCall) {
        if (node.name == "write") {
            io.write(popValue("write argument").get())
            return
        }
        throw IllegalArgumentException("Unknown function: ${node.name}")
    }

    override fun visit(node: ASTRead) {
        val value = io.read()
        executionStack.current().setVar(node.varName, DataValue(value))
    }

    override fun visit(node: ASTAssign) {
        val value = popValue("Assignment to ${node.name}")
        executionStack.current().setVar(node.name, value)
    }

    override fun visit(node: ASTVar) {
        val value = executionStack.current().getVar(node.name)
            ?: throw IllegalStateException("Variable not found: ${node.name}")
        executionStack.current().pushNameless(value)
    }

    override fun visit(node: ASTIf) {
        node.cond.visit(this)
        val branch = if (popValue("if condition").get() != 0) node.thenBranch else node.elseBranch
        branch?.visit(this)
    }

    override fun visit(node: ASTWhile) {
        while (true) {
            node.cond.visit(this)
            if (popValue("while condition").get() == 0) {
                break
            }
            node.body.visit(this)
        }
    }

    override fun visit(node: ASTDo) {
        do {
            node.body.visit(this)
            node.cond.visit(this)
        } while (popValue("do-while condition").get() != 0)
    }

    override fun visit(node: ASTSkip) {
        // skip does nothing
    }
}
