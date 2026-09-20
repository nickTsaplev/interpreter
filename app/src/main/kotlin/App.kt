package ru.tsaplev.app

import ru.tsaplev.app.JSONToAST.JSONToAssign
import ru.tsaplev.app.JSONToAST.JSONToBinop
import ru.tsaplev.app.JSONToAST.JSONToConst
import ru.tsaplev.app.JSONToAST.JSONToWrite
import ru.tsaplev.app.JSONToAST.JsonToSeq
import ru.tsaplev.app.JSONToAST.JsonToVar
import ru.tsaplev.app.execution.ExecutionVisitor
import ru.tsaplev.utils.Printer
import java.io.File

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main(args: Array<String>) {
    val text = File(args[0]).readText(Charsets.UTF_8)
    val jsonToASTer = JSONToBinop()
        .addNext(JSONToConst())
        .addNext(JSONToWrite())
        .addNext(JsonToSeq())
        .addNext(JSONToAssign())
        .addNext(JsonToVar())

    val ast = jsonToASTer.startParse(text);

    if (ast == null) {
        print("Error: faulty JSON")
        return
    }
    val visitor = ExecutionVisitor();

    ast.visit(visitor)
}
