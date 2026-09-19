package ru.tsaplev.app

import ru.tsaplev.app.ASTreeNodes.ASTBinOP
import ru.tsaplev.app.ASTreeNodes.ASTConst

interface ASTVisitor {
    fun visit(node: ASTBinOP) {

    }

    fun visit(node: ASTConst) {}
}