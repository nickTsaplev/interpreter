package ru.tsaplev.app

interface JsonToASTer {
    fun parse(text: String): ASTNode
}