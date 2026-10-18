package ru.tsaplev.app.ASTreeNodes

import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTVisitor

class ASTArray(val elems: List<ASTNode>): ASTNode {
    override fun visit(visitor: ASTVisitor) {
        elems.forEach { it.visit(visitor) }
        visitor.visit(this)
    }
}