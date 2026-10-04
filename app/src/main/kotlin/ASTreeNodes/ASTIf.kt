package ru.tsaplev.app.ASTreeNodes

import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTVisitor

class ASTIf(
    val cond: ASTNode,
    val thenBranch: ASTNode,
    val elseBranch: ASTNode?,
    val legacyLabels: LegacyIfLabels? = null
) : ASTNode {
    override fun visit(visitor: ASTVisitor) {
        visitor.visit(this)
    }
}
