package ru.tsaplev.app.ASTreeNodes

import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTVisitor

class ASTLength(val value: ASTNode): ASTNode {
    override fun visit(visitor: ASTVisitor) {
        value.visit(visitor)
        visitor.visit(this)
    }
}
