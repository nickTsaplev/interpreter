package ru.tsaplev.app.ASTreeNodes

import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTVisitor

class ASTSwitch(val scrutinee: ASTNode, val branches: List<Pair<MatchPattern, ASTNode>>): ASTNode {
    override fun visit(visitor: ASTVisitor) = visitor.visit(this)
}
