package ru.tsaplev.app

import ru.tsaplev.app.JSONToAST.GetJsonToAST
import ru.tsaplev.app.execution.CompilationVisitor
import ru.tsaplev.app.execution.ExecutionVisitor
import ru.tsaplev.app.execution.StandardDataIO
import ru.tsaplev.app.execution.optimize
import ru.tsaplev.app.execution.stackMachine.StackMachine
import ru.tsaplev.app.source.SourceParser
import java.io.File

fun main(args: Array<String>) {
    var optimize = false
    val mode = when {
        args.size == 1 -> "ast"
        args.size == 2 && (args[0] == "ast" || args[0] == "stack") -> args[0]
        args.size == 3 && args[0] == "compile" -> "compile"
        args.size == 4 && args[0] == "compile" -> {
            optimize = (args[1] == "optimize")
            "compile"
        }
        else -> {
            System.err.println("Usage: interpreter <program.json|program.in> | ast <program> | stack <stack.json> | compile <program> <output.json>")
            return
        }
    }
    val inputPath = if (args.size == 1) args[0] else if (args.size == 4) args[2] else args[1]

    if (mode == "ast") {
        val text = File(inputPath).readText(Charsets.UTF_8)
        val ast = parseProgram(inputPath, text)

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

    if (mode == "stack") {
        val text = File(inputPath).readText(Charsets.UTF_8)

        val commandList = try {
            JSONToStackInstructions(text)
        } catch (e: IllegalArgumentException) {
            print("Argument error while parsing JSON: ${e.message}")
            return
        }

        val machine = StackMachine()
        try {
            machine.run(commandList, StandardDataIO())
        } catch (e: IllegalStateException) {
            print("State error while running: ${e.message}")
        } catch (e: IllegalArgumentException) {
            print("Argument error while running: ${e.message}")
        }
    }

    if (mode == "compile") {
        val text = File(inputPath).readText(Charsets.UTF_8)
        val ast = parseProgram(inputPath, text)

        if (ast == null) {
            print("Error: faulty JSON")
            return
        }

        val visitor = CompilationVisitor()
        try {
            ast.visit(visitor)
        } catch (e: IllegalArgumentException) {
            print("Argument error while compiling: ${e.message}")
            return
        }

        val instructions = if (optimize) optimize(visitor.instructions) else visitor.instructions

        val outputFileName = if (args.size == 3) args[2] else args[3]

        File(outputFileName).writeText(StackInstructionsToJSON(instructions).toString())
    }
}

private fun parseProgram(path: String, text: String): ASTNode? =
    if (path.endsWith(".in")) SourceParser(text).parse() else GetJsonToAST().startParse(text)
