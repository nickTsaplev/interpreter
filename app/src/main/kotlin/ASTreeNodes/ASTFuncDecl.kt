package ru.tsaplev.app.ASTreeNodes

import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTVisitor

class ASTFuncDecl(val name: String, val parameters: List<String>, val body: ASTNode) : ASTNode {
    init {
        require(name.isNotBlank()) { "Function name must not be blank" }
        require(parameters.all { it.isNotBlank() }) { "Parameter names must not be blank" }
        require(parameters.distinct().size == parameters.size) { "Duplicate parameter name in $name" }
    }

    override fun visit(visitor: ASTVisitor) = visitor.visit(this)
}
