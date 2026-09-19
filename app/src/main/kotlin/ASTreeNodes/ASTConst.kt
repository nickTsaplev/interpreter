package ru.tsaplev.app.ASTreeNodes

import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTVisitor
import ru.tsaplev.app.DataValue

class ASTConst(val value: DataValue): ASTNode {
    override fun visit(visitor: ASTVisitor) {
        visitor.visit(this);
    }
}