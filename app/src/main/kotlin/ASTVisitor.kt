package ru.tsaplev.app

import ru.tsaplev.app.ASTreeNodes.ASTAssign
import ru.tsaplev.app.ASTreeNodes.ASTBinOP
import ru.tsaplev.app.ASTreeNodes.ASTConst
import ru.tsaplev.app.ASTreeNodes.ASTFuncCall
import ru.tsaplev.app.ASTreeNodes.ASTRead
import ru.tsaplev.app.ASTreeNodes.ASTVar
import ru.tsaplev.app.ASTreeNodes.ASTIf
import ru.tsaplev.app.ASTreeNodes.ASTWhile
import ru.tsaplev.app.ASTreeNodes.ASTDo
import ru.tsaplev.app.ASTreeNodes.ASTSkip

interface ASTVisitor {
    fun visit(node: ASTBinOP)

    fun visit(node: ASTConst)

    fun visit(node: ASTFuncCall)

    fun visit(node: ASTRead)

    fun visit(node: ASTAssign)

    fun visit(node: ASTVar)

    fun visit(node: ASTIf)
    fun visit(node: ASTWhile)
    fun visit(node: ASTDo)
    fun visit(node: ASTSkip)
}