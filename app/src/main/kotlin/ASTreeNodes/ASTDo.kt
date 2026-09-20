package ru.tsaplev.app.ASTreeNodes

import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTVisitor

class ASTDo(val body: ASTNode, val cond: ASTNode) : ASTNode {
    override fun visit(visitor: ASTVisitor) {
        visitor.visit(this)
    }
}
