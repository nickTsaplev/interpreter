package ru.tsaplev.app.execution

class ExecutionStack {
    private val stack: MutableList<ExecutionFrame> = mutableListOf(ExecutionFrame("main"))



    fun start(name: String? = null) {
        stack.add(ExecutionFrame(name))
    }

    fun pop() {
        check(stack.size > 1) { "Cannot remove the root scope" }
        stack.removeLast()
    }

    fun current(): ExecutionFrame {
        return stack[stack.lastIndex]
    }

    fun getVar(identifier: String): DataValue? {
        stack.reversed().forEach {
            if (it.hasVar(identifier)) {
                return it.getVar(identifier)
            }
        }
        return null
    }

    // Legacy stack-machine ST permits implicit creation; AST execution uses assignVar.
    fun setVar(identifier: String, value: DataValue) {
        stack.reversed().forEach {
            if (it.hasVar(identifier)) {
                return it.setVar(identifier, value)
            }
        }
        current().setVar(identifier, value)
    }

    fun declareVar(identifier: String, initialValue: DataValue) =
        current().declareVar(identifier, initialValue)

    fun assignVar(identifier: String, value: DataValue) {
        val frame = stack.asReversed().firstOrNull { it.hasVar(identifier) }
            ?: throw IllegalStateException("Variable not declared: $identifier")
        frame.setVar(identifier, value)
    }

    fun getTopNameless(): DataValue? = stack[0].getTopNameless()

    fun popNameless(): DataValue = stack[0].popNameless()

    fun pushNameless(value: DataValue) = stack[0].pushNameless(value)

    val unnamedDepth
        get() = stack[0].depth
}
