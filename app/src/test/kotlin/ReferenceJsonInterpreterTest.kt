package ru.tsaplev.app

import ru.tsaplev.app.JSONToAST.GetJsonToAST
import ru.tsaplev.app.execution.CompilationVisitor
import ru.tsaplev.app.execution.ExecutionVisitor
import ru.tsaplev.app.execution.stackMachine.StackMachine
import kotlin.test.*

class ReferenceJsonInterpreterTest {
    private fun parse(json: String) = assertNotNull(GetJsonToAST().startParse(json), json)
    private fun execute(json: String, vararg expected: Int) {
        val io = TestIntIO(emptyList())
        parse(json).visit(ExecutionVisitor(io))
        assertEquals(expected.toList(), io.output)
    }

    @Test
    fun referenceScopesDeclareDefaultValuesAndShadowOuterNames() {
        execute("""
            {"scope":{"decls":[{"name":"x","spec":"var"}],"body":{"seq":{
              "left":{"assn":{"dst":"x","src":{"const":1}}},
              "right":{"seq":{
                "left":{"scope":{"decls":[{"name":"x","spec":"var"}],"body":{"seq":{
                  "left":{"write":{"var":"x"}},
                  "right":{"seq":{"left":{"assn":{"dst":"x","src":{"const":9}}},
                                  "right":{"write":{"var":"x"}}}}
                }}}},
                "right":{"write":{"var":"x"}}
              }}
            }}}}
        """.trimIndent(), 0, 9, 1)
    }

    @Test
    fun referenceFunctionDeclarationsAndReturnWrappersUseDynamicScope() {
        execute("""
            {"scope":{"decls":[
              {"name":"x","spec":"var"},
              {"name":"get","spec":{"fun":{"args":[],"body":{"return":{"expr":{"var":"x"}}}}}},
              {"name":"caller","spec":{"fun":{"args":["x"],"body":{"return":{"expr":{"call":{"fun":"get","args":[]}}}}}}}
            ],"body":{"seq":{
              "left":{"assn":{"dst":"x","src":{"const":1}}},
              "right":{"seq":{"left":{"write":{"call":{"fun":"caller","args":[{"const":42}]}}},
                              "right":{"write":{"var":"x"}}}}
            }}}}
        """.trimIndent(), 42, 1)
    }

    @Test
    fun referenceCallsInStatementBodiesDiscardResults() {
        execute("""
            {"scope":{"decls":[
              {"name":"answer","spec":{"fun":{"args":[],"body":{"return":{"expr":{"const":42}}}}}},
              {"name":"noop","spec":{"fun":{"args":[],"body":{"return":""}}}}
            ],"body":{"seq":{
              "left":{"call":{"fun":"answer","args":[]}},
              "right":{"seq":{
                "left":{"if":{"cond":{"const":1},"then":{"call":{"fun":"noop","args":[]}},"else":"skip"}},
                "right":{"write":{"binop":"+","left":{"const":3},"right":{"call":{"fun":"answer","args":[]}}}}
              }}
            }}}}
        """.trimIndent(), 45)
    }

    @Test
    fun referenceStatementCallsInsideLoopsStillHaveEffects() {
        execute("""
            {"scope":{"decls":[
              {"name":"x","spec":"var"},
              {"name":"step","spec":{"fun":{"args":[],"body":{"assn":{"dst":"x","src":{
                "binop":"+","left":{"var":"x"},"right":{"const":1}
              }}}}}}
            ],"body":{"seq":{
              "left":{"while":{"cond":{"binop":"<","left":{"var":"x"},"right":{"const":2}},
                               "body":{"call":{"fun":"step","args":[]}}}},
              "right":{"seq":{"left":{"do":{"body":{"call":{"fun":"step","args":[]}},"cond":{"const":0}}},
                              "right":{"write":{"var":"x"}}}}
            }}}}
        """.trimIndent(), 3)
    }

    @Test
    fun referenceVoidAndFallthroughReturnZeroInExpressions() {
        execute("""
            {"scope":{"decls":[
              {"name":"empty","spec":{"fun":{"args":[],"body":{"return":""}}}},
              {"name":"fallthrough","spec":{"fun":{"args":[],"body":"skip"}}}
            ],"body":{"seq":{
              "left":{"write":{"call":{"fun":"empty","args":[]}}},
              "right":{"write":{"call":{"fun":"fallthrough","args":[]}}}
            }}}}
        """.trimIndent(), 0, 0)
    }

    @Test
    fun duplicateReferenceDeclarationsAreRejectedBeforeBodyRuns() {
        val ast = parse("""
            {"scope":{"decls":[{"name":"x","spec":"var"},{"name":"x","spec":"var"}],
                      "body":{"write":{"const":99}}}}
        """.trimIndent())
        val io = TestIntIO(emptyList())
        assertFailsWith<IllegalStateException> { ast.visit(ExecutionVisitor(io)) }
        assertTrue(io.output.isEmpty())
    }

    @Test
    fun malformedReferenceNodesAreRejected() {
        val invalid = listOf(
            """{"scope":{"decls":{},"body":"skip"}}""",
            """{"scope":{"decls":[]}}""",
            """{"scope":{"decls":[{"name":"x","spec":"unknown"}],"body":"skip"}}""",
            """{"scope":{"decls":[{"name":"f","spec":{"fun":{"args":["x","x"],"body":"skip"}}}],"body":"skip"}}""",
            """{"scope":{"decls":[{"name":"f","spec":{"fun":{"args":[1],"body":"skip"}}}],"body":"skip"}}""",
            """{"scope":{"decls":[{"name":"","spec":"var"}],"body":"skip"}}""",
            """{"call":{"fun":"f","args":{}}}""",
            """{"call":{"fun":"f","name":"g","args":[]}}""",
            """{"return":{"expr":{"unknown":1}}}""",
            """{"return":"not empty"}"""
        )
        invalid.forEach { assertNull(GetJsonToAST().startParse(it), it) }
    }

    @Test
    fun compilerKeepsOuterVariableOnlyReferenceProgramsWorking() {
        val node = parse("""
            {"scope":{"decls":[{"name":"x","spec":"var"}],
                      "body":{"seq":{"left":{"assn":{"dst":"x","src":{"const":7}}},
                                     "right":{"write":{"var":"x"}}}}}}
        """.trimIndent())
        val compiler = CompilationVisitor()
        node.visit(compiler)
        val io = TestIntIO(emptyList())
        StackMachine().run(compiler.instructions, io)
        assertEquals(listOf(7), io.output)
    }

    @Test
    fun compilerRejectsReferenceFunctionsAndNestedDeclaredScopes() {
        for (json in listOf(
            """{"scope":{"decls":[{"name":"f","spec":{"fun":{"args":[],"body":"skip"}}}],"body":"skip"}}""",
            """{"scope":{"decls":[],"body":{"scope":{"decls":[{"name":"x","spec":"var"}],"body":"skip"}}}}"""
        )) {
            assertFailsWith<UnsupportedOperationException> { parse(json).visit(CompilationVisitor()) }
        }
    }
}
