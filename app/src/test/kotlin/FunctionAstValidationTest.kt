package ru.tsaplev.app

import ru.tsaplev.app.JSONToAST.GetJsonToAST
import ru.tsaplev.app.execution.CompilationVisitor
import kotlin.test.*

class FunctionAstValidationTest {
    @Test
    fun malformedDeclarationsCallsReturnsAndBlocksAreRejected() {
        val invalid = listOf(
            """{"decl":null}""",
            """{"decl":{"name":"x"}}""",
            """{"decl":{"name":"","value":{"const":1}}}""",
            """{"decl":{"name":1,"value":{"const":1}}}""",
            """{"decl":{"name":"f","params":{},"body":"skip"}}""",
            """{"decl":{"name":"f","params":[1],"body":"skip"}}""",
            """{"decl":{"name":"f","params":["x","x"],"body":"skip"}}""",
            """{"decl":{"name":"f","params":[""],"body":"skip"}}""",
            """{"decl":{"name":"f","params":[]}}""",
            """{"decl":{"name":"f","body":"skip"}}""",
            """{"decl":{"name":"f","params":[],"body":"skip","value":{"const":1}}}""",
            """{"decl":{"name":"x","value":{"unknown":1}}}""",
            """{"call":{"name":"f"}}""",
            """{"call":{"name":"f","args":{}}}""",
            """{"call":{"name":"","args":[]}}""",
            """{"callStmt":{"name":"f","args":[{"unknown":1}]}}""",
            """{"return":{"unknown":1}}""",
            """{"block":{}}""",
            """{"block":[{"unknown":1}]}""",
            """{"return":null,"call":{"name":"f","args":[]}}""",
            """{"block":[],"decl":{"name":"x","value":{"const":1}}}"""
        )
        for (json in invalid) assertNull(GetJsonToAST().startParse(json), json)
    }

    @Test
    fun compilerRejectsFunctionsCallsReturnAndScopedBlocks() {
        val unsupported = listOf(
            """{"decl":{"name":"f","params":[],"body":{"write":{"const":1}}}}""",
            """{"call":{"name":"f","args":[{"const":1}]}}""",
            """{"callStmt":{"name":"f","args":[]}}""",
            """{"return":{"const":1}}""",
            """{"block":[{"write":{"const":1}}]}"""
        )
        for (json in unsupported) {
            val compiler = CompilationVisitor()
            val node = assertNotNull(GetJsonToAST().startParse(json))
            assertFails { node.visit(compiler) }
            assertTrue(compiler.instructions.isEmpty(), "Unsupported AST must not compile its body/arguments")
        }
    }
}
