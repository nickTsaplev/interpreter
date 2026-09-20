package ru.tsaplev.app

import ru.tsaplev.app.JSONToAST.GetJsonToAST
import ru.tsaplev.app.execution.ExecutionVisitor
import java.io.File

fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("Usage: interpreter <ast.json>")
        return
    }

    val text = File(args[0]).readText(Charsets.UTF_8)
    val ast = GetJsonToAST().startParse(text)

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
