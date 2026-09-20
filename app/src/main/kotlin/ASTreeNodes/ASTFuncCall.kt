package ru.tsaplev.app.ASTreeNodes

import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTVisitor

class ASTFuncCall(val name: String,
                  private val inner: ASTNode): ASTNode {
    override fun visit(visitor: ASTVisitor) {
        inner.visit(visitor)
        visitor.visit(this)
    }
}