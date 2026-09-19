package ru.tsaplev.app.JSONToAST

import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.ASTBinOP
import ru.tsaplev.app.JsonToASTer

class JSONToBinop: JsonToASTer {
    override fun parse(text: String): ASTNode {
        return ASTBinOP("0", )
    }
}