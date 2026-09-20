package ru.tsaplev.app.ASTreeNodes

import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTVisitor

class ASTVar(val name: String): ASTNode {
    override fun visit(visitor: ASTVisitor) {
        visitor.visit(this)
    }
}