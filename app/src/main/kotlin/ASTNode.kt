package ru.tsaplev.app

interface ASTNode {
    fun visit(visitor: ASTVisitor)
}