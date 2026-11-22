package ru.tsaplev.app.execution

import java.util.Stack
import ru.tsaplev.app.ASTreeNodes.ASTFuncDecl

class ExecutionFrame(private val name: String? = null) {
    private val unnamedStack = Stack<DataValue>()
    private val namedStack = mutableMapOf<String, DataValue>()
    private val functions = mutableMapOf<String, ASTFuncDecl>()

    fun declareFunction(function: ASTFuncDecl) {
        require(function.name != "write") { "Cannot redefine built-in function: write" }
        check(function.name !in functions) { "Function already declared in this scope: ${function.name}" }
        functions[function.name] = function
    }

    fun getFunction(identifier: String): ASTFuncDecl? = functions[identifier]

    fun getVar(identifier: String): DataValue? {
        return namedStack[identifier]
    }

    fun setVar(identifier: String, value: DataValue) {
        namedStack[identifier] = value
    }

    fun declareVar(identifier: String, initialValue: DataValue) {
        check(!hasVar(identifier)) { "Variable already declared in this scope: $identifier" }
        namedStack[identifier] = initialValue
    }

    fun hasVar(identifier: String): Boolean {
        return namedStack.keys.contains(identifier)
    }

    internal fun bind(values: Map<String, DataValue>): Map<String, DataValue?> {
        val previous = values.keys.associateWith { namedStack[it] }
        namedStack.putAll(values)
        return previous
    }

    internal fun restore(previous: Map<String, DataValue?>) {
        previous.forEach { (name, value) ->
            if (value == null) namedStack.remove(name)
            else namedStack[name] = value
        }
    }

    fun getTopNameless(): DataValue? {
        if(unnamedStack.empty())
            return null
        return unnamedStack.pop()
    }

    fun popNameless(): DataValue {
        if(unnamedStack.empty())
            throw IllegalStateException("Execution stack empty")
        return unnamedStack.pop()
    }

    fun pushNameless(value: DataValue) {
        unnamedStack.push(value)
    }

    val depth: Int
        get() = unnamedStack.size
}
