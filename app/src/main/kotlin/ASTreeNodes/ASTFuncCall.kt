package ru.tsaplev.app.ASTreeNodes

import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTVisitor

class ASTFuncCall(
    val name: String,
    val arguments: List<ASTNode>,
    val asExpression: Boolean = true
) : ASTNode {
    // Compatibility with the original single-argument statement (write).
    constructor(name: String, inner: ASTNode) : this(name, listOf(inner), false)

    override fun visit(visitor: ASTVisitor) = visitor.visit(this)
}
