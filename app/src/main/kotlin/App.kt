package ru.tsaplev.app

import ru.tsaplev.app.JSONToAST.GetJsonToAST
import ru.tsaplev.app.JSONToAST.JsonToAssign
import ru.tsaplev.app.JSONToAST.JsonToBinop
import ru.tsaplev.app.JSONToAST.JsonToConst
import ru.tsaplev.app.JSONToAST.JsonToWrite
import ru.tsaplev.app.JSONToAST.JsonToRead
import ru.tsaplev.app.JSONToAST.JsonToSeq
import ru.tsaplev.app.JSONToAST.JsonToVar
import ru.tsaplev.app.execution.ExecutionVisitor
import java.io.File

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main(args: Array<String>) {
    val text = File(args[0]).readText(Charsets.UTF_8)
    val jsonToASTer = GetJsonToAST()

    val ast = jsonToASTer.startParse(text);

    if (ast == null) {
        print("Error: faulty JSON")
        return
    }
    val visitor = ExecutionVisitor();

    ast.visit(visitor)
}
