import ru.tsaplev.app.JSONToAST.JsonToAssign
import ru.tsaplev.app.JSONToAST.JsonToBinop
import ru.tsaplev.app.JSONToAST.JsonToConst
import ru.tsaplev.app.JSONToAST.JsonToRead
import ru.tsaplev.app.JSONToAST.JsonToSeq
import ru.tsaplev.app.JSONToAST.JsonToVar
import ru.tsaplev.app.JSONToAST.JsonToWrite
import ru.tsaplev.app.execution.ExecutionVisitor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FirstTest {
    @Test
    fun IOAndArithTest1() {
        val text = """
            {
              "seq": {
                "left": {
                  "write": {
                    "binop": "+",
                    "left": {
                      "binop": "+",
                      "left": {
                        "binop": "+",
                        "left": { "const": 2 },
                        "right": { "const": 2 }
                      },
                      "right": { "const": 4 }
                    },
                    "right": {
                      "binop": "/",
                      "left": { "const": 5 },
                      "right": { "const": 2 }
                    }
                  }
                },
                "right": {
                  "seq": {
                    "left": {
                      "assn": {
                        "dst": "a",
                        "src": {
                          "binop": "+",
                          "left": {
                            "binop": "+",
                            "left": { "const": 2 },
                            "right": { "const": 2 }
                          },
                          "right": { "const": 3 }
                        }
                      }
                    },
                    "right": {
                      "seq": {
                        "left": {
                          "write": {
                            "binop": "*",
                            "left": { "var": "a" },
                            "right": { "const": 2 }
                          }
                        },
                        "right": {
                          "seq": {
                            "left": { "read": "b" },
                            "right": {
                              "write": {
                                "binop": "*",
                                "left": { "var": "b" },
                                "right": { "const": 4 }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }

        """
        val jsonToASTer = JsonToBinop()
            .addNext(JsonToConst())
            .addNext(JsonToWrite())
            .addNext(JsonToSeq())
            .addNext(JsonToAssign())
            .addNext(JsonToVar())
            .addNext(JsonToRead())

        val ast = jsonToASTer.startParse(text)

        assertNotNull(ast)

        val io = ListIOMock(mutableListOf(4))
        val visitor = ExecutionVisitor(io)

        ast.visit(visitor)

        assertEquals<List<Int?>>(io.output, listOf(10, 14, 16))
    }
}