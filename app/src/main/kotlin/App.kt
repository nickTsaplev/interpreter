package ru.tsaplev.app

import ru.tsaplev.app.JSONToAST.GetJsonToAST
import ru.tsaplev.app.JSONToAST.JsonToAssign
import ru.tsaplev.app.JSONToAST.JsonToBinop
import ru.tsaplev.app.JSONToAST.JsonToConst
import ru.tsaplev.app.JSONToAST.JsonToWrite
import ru.tsaplev.app.JSONToAST.JsonToRead
import ru.tsaplev.app.JSONToAST.JsonToSeq
import ru.tsaplev.app.JSONToAST.JsonToSkip
import ru.tsaplev.app.JSONToAST.JsonToControl
import ru.tsaplev.app.JSONToAST.JsonToVar
import ru.tsaplev.app.JSONToAST.JsonToASTer
import ru.tsaplev.app.execution.ExecutionVisitor
import java.io.File

fun createParser(): JsonToASTer = JsonToBinop()
    .addNext(JsonToConst())
    .addNext(JsonToWrite())
    .addNext(JsonToSeq())
    .addNext(JsonToAssign())
    .addNext(JsonToVar())
    .addNext(JsonToRead())
    .addNext(JsonToSkip())
    .addNext(JsonToControl())

fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("Usage: interpreter <ast.json>")
        return
    }

    val text = File(args[0]).readText(Charsets.UTF_8)
    val ast = createParser().startParse(text)

    if (ast == null) {
        print("Error: faulty JSON")
        return
    }
    try {
        ast.visit(ExecutionVisitor())
    } catch(e : IllegalStateException) {
        print("State error while running: ${e.message}")
    } catch(e : IllegalArgumentException) {
        print("Argument error while running: ${e.message}")
    }
}
