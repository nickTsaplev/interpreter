package ru.tsaplev.app.execution

import java.util.Stack

class ExecutionFrame(private val name: String? = null) {
    private val unnamedStack = Stack<DataValue>()
    private val namedStack = mutableMapOf<String, DataValue>()
    fun getVar(identifier: String): DataValue? {
        return namedStack[identifier]
    }

    fun setVar(identifier: String, value: DataValue) {
        namedStack[identifier] = value
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