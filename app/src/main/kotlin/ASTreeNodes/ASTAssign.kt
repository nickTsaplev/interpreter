package ru.tsaplev.app.ASTreeNodes

import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTVisitor

class ASTAssign(val name: String, private val rvalue: ASTNode): ASTNode {
    override fun visit(visitor: ASTVisitor) {
        rvalue.visit(visitor)
        visitor.visit(this)
    }
}