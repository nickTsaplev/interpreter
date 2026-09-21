package ru.tsaplev.app

import ru.tsaplev.app.JSONToAST.GetJsonToAST
import ru.tsaplev.app.execution.ExecutionVisitor
import ru.tsaplev.app.execution.StandardIO
import ru.tsaplev.app.execution.stackMachine.StackMachine
import java.io.File

fun main(args: Array<String>) {
    if (args.size != 2) {
        System.err.println("Usage: interpreter ast/stack <ast.json>")
        return
    }

    if(args[0] == "ast") {
        val text = File(args[1]).readText(Charsets.UTF_8)
        val ast = GetJsonToAST().startParse(text)

        if (ast == null) {
            print("Error: faulty JSON")
            return
        }
        try {
            ast.visit(ExecutionVisitor())
        } catch (e: IllegalStateException) {
            print("State error while running: ${e.message}")
        } catch (e: IllegalArgumentException) {
            print("Argument error while running: ${e.message}")
        }
    }

    if(args[0] == "stack") {
        val text = File(args[1]).readText(Charsets.UTF_8)
        val commandList = JSONToStackInstructions(text)

        val machine = StackMachine()
        try {
            machine.run(commandList, StandardIO())
        } catch (e: IllegalStateException) {
            print("State error while running: ${e.message}")
        } catch (e: IllegalArgumentException) {
            print("Argument error while running: ${e.message}")
        }
    }
}
