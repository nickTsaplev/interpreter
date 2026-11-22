package ru.tsaplev.app

import ru.tsaplev.app.ASTreeNodes.ASTArray
import ru.tsaplev.app.ASTreeNodes.ASTAssign
import ru.tsaplev.app.ASTreeNodes.ASTBinOP
import ru.tsaplev.app.ASTreeNodes.ASTConst
import ru.tsaplev.app.ASTreeNodes.ASTFuncCall
import ru.tsaplev.app.ASTreeNodes.ASTRead
import ru.tsaplev.app.ASTreeNodes.ASTVar
import ru.tsaplev.app.ASTreeNodes.ASTIf
import ru.tsaplev.app.ASTreeNodes.ASTWhile
import ru.tsaplev.app.ASTreeNodes.ASTDo
import ru.tsaplev.app.ASTreeNodes.ASTElem
import ru.tsaplev.app.ASTreeNodes.ASTSeq
import ru.tsaplev.app.ASTreeNodes.ASTSet
import ru.tsaplev.app.ASTreeNodes.ASTSkip
import ru.tsaplev.app.ASTreeNodes.ASTString
import ru.tsaplev.app.ASTreeNodes.ASTLength
import ru.tsaplev.app.ASTreeNodes.ASTSwitch
import ru.tsaplev.app.ASTreeNodes.ASTSExp
import ru.tsaplev.app.ASTreeNodes.ASTVarDecl
import ru.tsaplev.app.ASTreeNodes.ASTBlock
import ru.tsaplev.app.ASTreeNodes.ASTFuncDecl
import ru.tsaplev.app.ASTreeNodes.ASTReturn
import ru.tsaplev.app.ASTreeNodes.ASTScope

interface ASTVisitor {
    fun visit(node: ASTScope) {
        throw UnsupportedOperationException("Reference scopes are not supported by this visitor")
    }

    fun visit(node: ASTFuncDecl) {
        throw UnsupportedOperationException("User functions are only supported by the AST interpreter")
    }

    fun visit(node: ASTReturn) {
        throw UnsupportedOperationException("Return is only supported by the AST interpreter")
    }

    fun visit(node: ASTVarDecl) {
        throw UnsupportedOperationException("Variable declarations are not supported by this visitor")
    }

    fun visit(node: ASTBlock) {
        throw UnsupportedOperationException("Scoped blocks are only supported by the AST interpreter")
    }

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
    fun visit(node: ASTSeq)

    fun visit(node: ASTString)
    fun visit(node: ASTArray)
    fun visit(node: ASTElem)
    fun visit(node: ASTSet)
    fun visit(node: ASTLength)

    fun visit(node: ASTSwitch)
    fun visit(node: ASTSExp)
}
