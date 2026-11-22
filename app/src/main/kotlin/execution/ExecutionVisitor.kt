package ru.tsaplev.app.execution

import ru.tsaplev.app.ASTVisitor
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.ASTVarDecl
import ru.tsaplev.app.ASTreeNodes.ASTBlock
import ru.tsaplev.app.ASTreeNodes.ASTFuncDecl
import ru.tsaplev.app.ASTreeNodes.ASTReturn
import ru.tsaplev.app.ASTreeNodes.ASTScope
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
import ru.tsaplev.app.ASTreeNodes.ASTSwitch
import ru.tsaplev.app.ASTreeNodes.ASTSExp

class ExecutionVisitor(val io: DataIO = StandardDataIO()): ASTVisitor {

    private val executionStack = ExecutionStack()
    private var functionDepth = 0

    private class ReturnSignal(val value: DataValue?) : RuntimeException(null, null, false, false)

    override fun visit(node: ASTFuncDecl) {
        executionStack.declareFunction(node)
    }

    override fun visit(node: ASTReturn) {
        check(functionDepth > 0) { "Return outside a function" }
        throw ReturnSignal(node.value?.let { evaluate(it) })
    }

    private fun evaluate(node: ASTNode): DataValue {
        val frame = executionStack.current()
        val depth = frame.depth
        try {
            node.visit(this)
            check(frame.depth == depth + 1) { "Expression did not produce exactly one value" }
            return frame.popNameless()
        } finally {
            while (frame.depth > depth) frame.popNameless()
        }
    }

    override fun visit(node: ASTVarDecl) {
        check(!executionStack.current().hasVar(node.name)) {
            "Variable already declared in this scope: ${node.name}"
        }
        executionStack.declareVar(node.name, evaluate(node.initialValue))
    }

    override fun visit(node: ASTBlock) {
        executionStack.start()
        try {
            node.statements.forEach { it.visit(this) }
        } finally {
            executionStack.pop()
        }
    }

    override fun visit(node: ASTScope) {
        executionStack.start()
        try {
            node.declarations.forEach { it.visit(this) }
            node.body.visit(this)
        } finally {
            executionStack.pop()
        }
    }

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
            require(node.arguments.size == 1) { "write expects 1 argument, got ${node.arguments.size}" }
            check(!node.asExpression) { "write does not return a value" }
            io.write(evaluate(node.arguments.single()))
            return
        }
        val function = executionStack.getFunction(node.name)
            ?: throw IllegalArgumentException("Unknown function: ${node.name}")
        require(function.parameters.size == node.arguments.size) {
            "${node.name} expects ${function.parameters.size} arguments, got ${node.arguments.size}"
        }
        // Evaluate all arguments in the caller, before any parameter can shadow it.
        val values = node.arguments.map { evaluate(it) }
        executionStack.start(node.name)
        functionDepth++
        val result = try {
            function.parameters.zip(values).forEach { (name, value) ->
                executionStack.declareVar(name, value)
            }
            try {
                function.body.visit(this)
                null
            } catch (signal: ReturnSignal) {
                signal.value
            }
        } finally {
            functionDepth--
            executionStack.pop()
        }
        if (node.asExpression) {
            executionStack.current().pushNameless(result ?: DataValue.IntValue(0))
        }
    }

    override fun visit(node: ASTRead) {
        check(executionStack.getVar(node.varName) != null) { "Variable not declared: ${node.varName}" }
        val value = io.read()
        executionStack.assignVar(node.varName, value)
    }

    override fun visit(node: ASTAssign) {
        val value = popValue("Assignment to ${node.name}")
        executionStack.assignVar(node.name, value)
    }

    override fun visit(node: ASTVar) {
        val value = executionStack.getVar(node.name)
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

    override fun visit(node: ASTSwitch) {
        node.scrutinee.visit(this)
        val value = popValue("switch scrutinee")
        for ((pattern, branch) in node.branches) {
            val bindings = pattern.match(value) ?: continue
            executionStack.start("match")
            val results = try {
                bindings.forEach { (name, value) -> executionStack.declareVar(name, value) }
                branch.visit(this)
                val frame = executionStack.current()
                List(frame.depth) { frame.popNameless() }.asReversed()
            } finally {
                executionStack.pop()
            }
            // A switch branch may be an expression; its values outlive its local names.
            results.forEach { executionStack.current().pushNameless(it) }
            return
        }
        throw IllegalArgumentException("Match failure on $value")
    }

    override fun visit(node: ASTSExp) {
        val ans = mutableListOf<DataValue>()
        for (i in node.elems.indices) {
            ans.add(popValue("array construction"))
        }
        executionStack.current().pushNameless(DataValue.SExpressionValue(node.name,ans.reversed().toMutableList()))
    }

    override fun visit(node: ASTElem) {
        val index = popIntOrThrow("array index")
        val value = popValue("array value")

        when(value) {
            is DataValue.StringValue -> executionStack.current().pushNameless(DataValue.IntValue(value.value[index].code))
            is DataValue.ArrayValue -> executionStack.current().pushNameless(value.elems[index])
            is DataValue.SExpressionValue -> executionStack.current().pushNameless(value.elems[index])
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
            is DataValue.SExpressionValue -> destination.elems[index] = source
            else -> throw IllegalStateException("$node did not get an array to index")
        }
    }

    override fun visit(node: ASTLength) {
        val value = popValue("length argument")
        val length = when (value) {
            is DataValue.ArrayValue -> value.elems.size
            is DataValue.StringValue -> value.value.length
            is DataValue.SExpressionValue -> value.elems.size
            is DataValue.IntValue -> throw IllegalArgumentException("length is only defined for arrays, strings and S-expressions")
        }
        executionStack.current().pushNameless(DataValue.IntValue(length))
    }
}
