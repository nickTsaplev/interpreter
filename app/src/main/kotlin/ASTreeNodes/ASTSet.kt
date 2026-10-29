package ru.tsaplev.app.ASTreeNodes

import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTVisitor

class ASTSet(val destination: ASTNode, val source: ASTNode, val index: ASTNode): ASTNode {
    override fun visit(visitor: ASTVisitor) {
        destination.visit(visitor)
        index.visit(visitor)
        source.visit(visitor)
        visitor.visit(this)
    }
}