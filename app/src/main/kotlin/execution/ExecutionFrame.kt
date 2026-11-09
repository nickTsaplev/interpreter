package ru.tsaplev.app.execution

import java.util.Stack

public class ExecutionFrame(private val name: String? = null) {
    private val unnamedStack = Stack<DataValue>()
    private val namedStack = mutableMapOf<String, DataValue>()
    public fun getVar(identifier: String): DataValue? {
        return namedStack[identifier];
    }

    public fun setVar(identifier: String, value: DataValue) {
        namedStack[identifier] = value;
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

    public fun getTopNameless(): DataValue? {
        if(unnamedStack.empty())
            return null
        return unnamedStack.pop()
    }

    fun popNameless(): DataValue {
        if(unnamedStack.empty())
            throw IllegalStateException("Execution stack empty")
        return unnamedStack.pop()
    }

    public fun pushNameless(value: DataValue) {
        unnamedStack.push(value)
    }
}