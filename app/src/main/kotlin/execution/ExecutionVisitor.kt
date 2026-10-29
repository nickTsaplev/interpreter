package ru.tsaplev.app.execution

import ru.tsaplev.app.ASTVisitor
import ru.tsaplev.app.ASTreeNodes.ASTArray
import ru.tsaplev.app.ASTreeNodes.ASTAssign
import ru.tsaplev.app.ASTreeNodes.ASTBinOP
import ru.tsaplev.app.ASTreeNodes.ASTConst
import ru.tsaplev.app.ASTreeNodes.ASTFuncCall
import ru.tsaplev.app.ASTreeNodes.ASTRead
import ru.tsaplev.app.ASTreeNodes.ASTVar
import ru.tsaplev.app.ASTreeNodes.ASTIf
import ru.tsaplev.app.ASTreeNodes.ASTWhile
import ru.tsaplev.app.ASTreeNodes.ASTDo
import ru.tsaplev.app.ASTreeNodes.ASTElem
import ru.tsaplev.app.ASTreeNodes.ASTSeq
import ru.tsaplev.app.ASTreeNodes.ASTSet
import ru.tsaplev.app.ASTreeNodes.ASTSkip
import ru.tsaplev.app.ASTreeNodes.ASTString
import ru.tsaplev.app.ASTreeNodes.ASTLength

class ExecutionVisitor(val io: DataIO = StandardDataIO()): ASTVisitor {

    private val executionStack = ExecutionStack()

    private fun popValue(context: String): DataValue =
        executionStack.current().getTopNameless()
            ?: throw IllegalStateException("$context did not produce a value")

    private fun popIntOrThrow(reason: String): Int {
        return (popValue(reason) as? DataValue.IntValue)?.value ?:throw IllegalStateException("$reason did not produce an integer")
    }

    override fun visit(node: ASTBinOP) {
        val right = popValue("Right operand of ${node.binop}")
        val left = popValue("Left operand of ${node.binop}")

        executionStack.current().pushNameless(
            binaryOperationOverloaded(node.binop, left, right)
        )
    }

    override fun visit(node: ASTConst) {
        executionStack.current().pushNameless(node.value)
    }

    override fun visit(node: ASTFuncCall) {
        if (node.name == "write") {
            io.write(popValue("write argument"))
            return
        }
        throw IllegalArgumentException("Unknown function: ${node.name}")
    }

    override fun visit(node: ASTRead) {
        val value = io.read()
        executionStack.current().setVar(node.varName, value)
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
        val branch = if (popIntOrThrow("if condition") != 0) node.thenBranch else node.elseBranch
        branch?.visit(this)
    }

    override fun visit(node: ASTWhile) {
        while (true) {
            node.cond.visit(this)
            if (popIntOrThrow("while condition") == 0) {
                break
            }
            node.body.visit(this)
        }
    }

    override fun visit(node: ASTDo) {
        do {
            node.body.visit(this)
            node.cond.visit(this)
        } while (popIntOrThrow("do-while condition") != 0)
    }

    override fun visit(node: ASTSkip) {
        // skip does nothing
    }

    override fun visit(node: ASTSeq) {
        // Already visited
    }

    override fun visit(node: ASTString) {
        executionStack.current().pushNameless(DataValue.StringValue(node.value))
    }

    override fun visit(node: ASTArray) {
        val ans = mutableListOf<DataValue>()
        for (i in node.elems.indices) {
            ans.add(popValue("array construction"))
        }
        executionStack.current().pushNameless(DataValue.ArrayValue(ans.reversed().toMutableList()))
    }

    override fun visit(node: ASTElem) {
        val index = popIntOrThrow("array index")
        val value = popValue("array value")

        when(value) {
            is DataValue.StringValue -> executionStack.current().pushNameless(DataValue.IntValue(value.value[index].code))
            is DataValue.ArrayValue -> executionStack.current().pushNameless(value.elems[index])
            else -> throw IllegalStateException("$node did not get an array to index")
        }
    }

    override fun visit(node: ASTSet) {
        val source = popValue("array element set value")
        val index = popIntOrThrow("array set index")
        val destination = popValue("array set destination")

        when(destination) {
            is DataValue.StringValue -> {
                val charValue = (source as? DataValue.IntValue) ?: throw IllegalStateException("string element must be char")
                destination.value = destination.value.replaceRange(index, index + 1, charValue.value.toChar().toString())
            }
            is DataValue.ArrayValue -> destination.elems[index] = source
            else -> throw IllegalStateException("$node did not get an array to index")
        }
    }

    override fun visit(node: ASTLength) {
        val value = popValue("length argument")
        val length = when (value) {
            is DataValue.ArrayValue -> value.elems.size
            is DataValue.StringValue -> value.value.length
            is DataValue.IntValue -> throw IllegalArgumentException("length is only defined for arrays and strings")
        }
        executionStack.current().pushNameless(DataValue.IntValue(length))
    }
}
