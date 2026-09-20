import kotlin.test.Test

class FirstTest: ProgramTest() {
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

        testProgram(text, listOf(4), listOf(10, 14, 16))
    }
}