package ru.tsaplev.app.ASTreeNodes

class BindingPaths {
    var paths: MutableMap<String, List<Int>> = mutableMapOf()

    private var recStack = mutableListOf<Int>()

    fun push(index: Int) {
        recStack.add(index)
    }

    fun pop() {
        recStack.removeLast()
    }

    fun addBinding(name: String) {
        paths[name] = recStack.toList()
    }

}