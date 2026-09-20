package ru.tsaplev.app.ASTreeNodes

import kotlinx.serialization.Serializable
import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTVisitor

@Serializable
class ASTBinOP(val binop: String,
               private val left: ASTNode,
    private val right: ASTNode): ASTNode {
    override fun visit(visitor: ASTVisitor) {
        left.visit(visitor)
        right.visit(visitor)
        visitor.visit(this)
    }
}