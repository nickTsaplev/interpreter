package ru.tsaplev.app.ASTreeNodes

import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTVisitor

class ASTSeq(private val left: ASTNode, private val right: ASTNode): ASTNode {
    override fun visit(visitor: ASTVisitor) {
        left.visit(visitor)
        right.visit(visitor)
    }
}