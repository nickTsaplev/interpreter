package ru.tsaplev.app

import java.util.Stack

public class ExecutionStack {
    private val stack = Stack<ExecutionFrame>()

    constructor() {
        stack.push(ExecutionFrame("main"))
    }

    public fun start(name: String? = null) {
        stack.push(ExecutionFrame(name))
    }

    public fun pop() {
        stack.pop()
    }

    public fun current(): ExecutionFrame {
        return stack.peek()
    }
}