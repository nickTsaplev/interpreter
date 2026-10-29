package ru.tsaplev.app.ASTreeNodes

import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTVisitor

class ASTElem(val elem: ASTNode, val index: ASTNode): ASTNode {
    override fun visit(visitor: ASTVisitor) {
        elem.visit(visitor)
        index.visit(visitor)
        visitor.visit(this)
    }
}