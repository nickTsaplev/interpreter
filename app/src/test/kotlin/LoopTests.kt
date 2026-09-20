import kotlin.test.Test

class LoopTests: ProgramTest() {
    @Test
    fun whileTest() {
        val text = """
            {
              "seq": {
                "left": { "assn": { "dst": "a", "src": { "const": 2 } } },
                "right": {
                  "while": {
                    "cond": {
                      "binop": "<",
                      "left": { "var": "a" },
                      "right": { "const": 22 }
                    },
                    "body": {
                      "seq": {
                        "left": {
                          "write": {
                            "binop": "+",
                            "left": {
                              "binop": "%",
                              "left": { "var": "a" },
                              "right": { "const": 7 }
                            },
                            "right": {
                              "binop": "%",
                              "left": { "var": "a" },
                              "right": { "const": 3 }
                            }
                          }
                        },
                        "right": {
                          "assn": {
                            "dst": "a",
                            "src": {
                              "binop": "+",
                              "left": { "var": "a" },
                              "right": { "const": 1 }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
        """.trimIndent()

        testProgram(text, listOf(), listOf(4, 3, 5, 7, 6, 1, 3, 2, 4, 6, 5, 7, 2, 1, 3, 5, 4, 6, 8, 0))
    }

    @Test
    fun forTest() {
        val text = """
            {
              "seq": {
                "left": { "assn": { "dst": "n", "src": { "const": 40 } } },
                "right": {
                  "seq": {
                    "left": { "assn": { "dst": "i", "src": { "const": 2 } } },
                    "right": {
                      "while": {
                        "cond": {
                          "binop": "<",
                          "left": { "var": "i" },
                          "right": { "var": "n" }
                        },
                        "body": {
                          "seq": {
                            "left": {
                              "seq": {
                                "left": {
                                  "assn": { "dst": "is_prime", "src": { "const": 1 } }
                                },
                                "right": {
                                  "seq": {
                                    "left": {
                                      "seq": {
                                        "left": {
                                          "assn": { "dst": "j", "src": { "const": 2 } }
                                        },
                                        "right": {
                                          "while": {
                                            "cond": {
                                              "binop": "<=",
                                              "left": {
                                                "binop": "*",
                                                "left": { "var": "j" },
                                                "right": { "var": "j" }
                                              },
                                              "right": { "var": "i" }
                                            },
                                            "body": {
                                              "seq": {
                                                "left": {
                                                  "assn": {
                                                    "dst": "is_prime",
                                                    "src": {
                                                      "binop": "&&",
                                                      "left": { "var": "is_prime" },
                                                      "right": {
                                                        "binop": "!=",
                                                        "left": {
                                                          "binop": "%",
                                                          "left": { "var": "i" },
                                                          "right": { "var": "j" }
                                                        },
                                                        "right": { "const": 0 }
                                                      }
                                                    }
                                                  }
                                                },
                                                "right": {
                                                  "assn": {
                                                    "dst": "j",
                                                    "src": {
                                                      "binop": "+",
                                                      "left": { "var": "j" },
                                                      "right": { "const": 1 }
                                                    }
                                                  }
                                                }
                                              }
                                            }
                                          }
                                        }
                                      }
                                    },
                                    "right": {
                                      "if": {
                                        "cond": { "var": "is_prime" },
                                        "then": { "write": { "var": "i" } },
                                        "else": "skip"
                                      }
                                    }
                                  }
                                }
                              }
                            },
                            "right": {
                              "assn": {
                                "dst": "i",
                                "src": {
                                  "binop": "+",
                                  "left": { "var": "i" },
                                  "right": { "const": 1 }
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
            }
        """.trimIndent()

        testProgram(text, listOf(), listOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37))
    }
}