package ru.tsaplev.app

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

    public fun getTopNameless(): DataValue? {
        if(unnamedStack.empty())
            return null
        return unnamedStack.pop()
    }
}