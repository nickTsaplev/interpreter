# Interpreter functions implementation plan

> **For agentic workers:** Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add dynamic-scope functions and explicit variable declarations to the AST interpreter.

**Architecture:** Reuse ExecutionStack's active frame chain for variable and function lookup. Declarations bind in the current frame, assignment searches outward. Functions evaluate arguments in the caller, enter a fresh frame, bind parameters, and unwind with finally; explicit return transfers an optional value. Blocks and switch bindings enter temporary scopes. Calls explicitly specify expression or statement use.

**Tech Stack:** Kotlin, Gradle, kotlin.test, kotlinx.serialization.json.

**Spec:** User request in this session (2026-11-22); all work stays local on feat/interpreter-functions-dynamic-scope based on origin/s-expressions.

## Global constraints

- No push, remote mutations, PR changes, destructive Git operations or rewriting existing commits.
- No user-function implementation in CompilationVisitor or the stack machine. New unsupported AST nodes fail explicitly for compilation.
- Assignment and read require an existing declaration; duplicate declarations in a frame fail; inner declarations shadow outer names.
- Existing stack-machine setVar semantics remain available for its existing instruction set.

## Review focus

- Arguments evaluate left to right in the caller, before parameter bindings exist.
- Function return and exceptions unwind every block and function scope.
- A statement call discards its result; a value call must produce exactly one result.
- Switch bindings shadow caller names and disappear after the chosen branch.
- Malformed JSON, duplicate parameters, wrong arity, missing functions and undeclared variables fail clearly.

## Tasks

- [x] 1. Add failing runtime tests for declarations, dynamic lookup and errors. Implement declareVar/assignVar; add ASTVarDecl and ASTBlock; route execution variable access through the chain. Keep stack-machine setVar compatibility. Run targeted tests.
- [x] 2. Add failing function tests (recursion, dynamic caller lookup, mutation, argument order, arity, statement/value calls, returns and cleanup). Add ASTFuncDecl/ASTReturn, parameter/function bindings and calls. Run targeted tests.
- [x] 3. Add failing JSON integration/validation and compiler rejection tests. Extend GetJsonToAST with decl, block, call/callStmt and return parsers. Preserve legacy write construction. Run targeted tests.
- [x] 4. Migrate existing AST fixtures/examples and direct tests to explicit declarations without weakening assignment behavior. Variable declarations compile to existing ST for shared fixtures; functions/blocks/return stay unsupported in compilation. Update README with schema, semantics and local PR instructions. Run full test/build, review diff and report changed files.

## Decisions / progress

- User explicitly requested autonomous implementation after a brief plan; no additional design approval gate.
- Work in the current checkout on the requested new branch; preserve all pre-existing untracked files.
- Functions and variables use separate namespaces; write is reserved. Declarations take effect at execution time (no hoisting).
- Use explicit return; falling off a function is void, an expression call to a void function fails. Bare return outside a function fails.
- Canonical JSON: decl {name, value} for variables; decl {name, params, body} for functions; block contains a statement array; call/callStmt contain {name, args}; return contains an expression or null.
- Baseline: ./gradlew test passed before changes.
- Ruling: JSON parsing was implemented with each runtime step so tests could exercise the public JSON-to-execution path without depending on missing Kotlin types.
- Task 1: six new tests failed on missing declaration/block parsing and implicit read creation, then all six passed.
- Task 2: ten new function tests failed on missing function parsing, then all sixteen declaration/function tests passed.
- Task 3: additional switch-scope and malformed-node tests failed on branch-local leakage and ambiguous tags; both corrected. Compiler rejection tests confirm no body/argument instructions are emitted for unsupported nodes.
- Task 4: 16 legacy JSON/source fixture pairs and the nested-array example now declare variables explicitly; direct AST tests use declarations for initial bindings. Full ./gradlew test build passed; git diff --check passed. Independent whole-change review completed; switch-expression regression fixed and rechecked.
- Review: one Important regression found: new switch frame dropped expression-branch results. Two regression tests reproduced failure; successful branch results now transfer to caller after scope exit. Return/error still unwind without transferring partial results.
- Minor deferred: old JsonToBinop accepts mixed-tag objects before new parsers inspect them. General strict validation for legacy AST forms is outside this change; new forms reject mixed tags when reached.

## Final verification

- Initial `./gradlew test build`: BUILD SUCCESSFUL (87 tests). After adding file-backed programs, `./gradlew test`: BUILD SUCCESSFUL; app: 97 tests, utils: 2 tests; zero failures, errors or skipped tests.
- `git diff --check`: passed.
- Review: no remaining Critical/Important findings.
- Branch: `feat/interpreter-functions-dynamic-scope`, based on `origin/s-expressions` (`3bc8482`); no commits added, changes remain in the working tree.
- Future PR: base=`s-expressions`, head=`feat/interpreter-functions-dynamic-scope` while PR #11 is open. No remote changes were performed.
- Existing untracked files were preserved; the knowledge graph was refreshed in its existing untracked `.codebase-memory/` directory.

## File-backed program follow-up

- Added 11 .in/.json program pairs and 12 runner checks (factorial has inputs 6 and 0).
- Tests were first observed failing on missing program resources, then passing once the real files were added.
- Six positive programs cover dynamic scope, recursion, argument ordering, scoped declarations, statement/value calls, early return and switch binding visibility.
- Five negative programs cover duplicate declaration, undeclared assignment/read, escaping block locals and void expressions. Expected outputs/errors are explicit literals in InterpreterProgramTest.

## Changed files (78 task files)

- `README.md`
- `app/src/main/kotlin/ASTVisitor.kt`
- `app/src/main/kotlin/ASTreeNodes/ASTBlock.kt`
- `app/src/main/kotlin/ASTreeNodes/ASTFuncCall.kt`
- `app/src/main/kotlin/ASTreeNodes/ASTFuncDecl.kt`
- `app/src/main/kotlin/ASTreeNodes/ASTReturn.kt`
- `app/src/main/kotlin/ASTreeNodes/ASTVarDecl.kt`
- `app/src/main/kotlin/JSONToAST/GetJsonToAST.kt`
- `app/src/main/kotlin/JSONToAST/JsonToBlock.kt`
- `app/src/main/kotlin/JSONToAST/JsonToDecl.kt`
- `app/src/main/kotlin/JSONToAST/JsonToFunctionControl.kt`
- `app/src/main/kotlin/execution/CompilationVisitor.kt`
- `app/src/main/kotlin/execution/ExecutionFrame.kt`
- `app/src/main/kotlin/execution/ExecutionStack.kt`
- `app/src/main/kotlin/execution/ExecutionVisitor.kt`
- `app/src/test/kotlin/DynamicScopeInterpreterTest.kt`
- `app/src/test/kotlin/FunctionAstValidationTest.kt`
- `app/src/test/kotlin/InterpreterProgramTest.kt`
- `app/src/test/kotlin/JumpOptimizationTest.kt`
- `app/src/test/kotlin/StackMachineTests.kt`
- `app/src/test/kotlin/StringArrayInterpreterTest.kt`
- `app/src/test/resources/json/assignments.json`
- `app/src/test/resources/json/blocks_skip.json`
- `app/src/test/resources/json/branches.json`
- `app/src/test/resources/json/do_while.json`
- `app/src/test/resources/json/for_sum.json`
- `app/src/test/resources/json/functions_argument_order.json`
- `app/src/test/resources/json/functions_dynamic_scope.json`
- `app/src/test/resources/json/functions_recursive_factorial.json`
- `app/src/test/resources/json/functions_statement_return.json`
- `app/src/test/resources/json/functions_switch_scope.json`
- `app/src/test/resources/json/functions_void_expression.json`
- `app/src/test/resources/json/io_arithmetic.json`
- `app/src/test/resources/json/nested_control.json`
- `app/src/test/resources/json/optional_syntax.json`
- `app/src/test/resources/json/prime_numbers_below_forty.json`
- `app/src/test/resources/json/stress_collatz.json`
- `app/src/test/resources/json/stress_grid.json`
- `app/src/test/resources/json/stress_numeric_pipeline.json`
- `app/src/test/resources/json/stress_primes.json`
- `app/src/test/resources/json/stress_state_machine.json`
- `app/src/test/resources/json/variable_block_escape.json`
- `app/src/test/resources/json/variable_block_scope.json`
- `app/src/test/resources/json/variable_duplicate_declaration.json`
- `app/src/test/resources/json/variable_undeclared_assignment.json`
- `app/src/test/resources/json/variable_undeclared_read.json`
- `app/src/test/resources/json/while_factorial.json`
- `app/src/test/resources/json/while_modular_sequence.json`
- `app/src/test/resources/programs/assignments.in`
- `app/src/test/resources/programs/blocks_skip.in`
- `app/src/test/resources/programs/branches.in`
- `app/src/test/resources/programs/do_while.in`
- `app/src/test/resources/programs/for_sum.in`
- `app/src/test/resources/programs/functions_argument_order.in`
- `app/src/test/resources/programs/functions_dynamic_scope.in`
- `app/src/test/resources/programs/functions_recursive_factorial.in`
- `app/src/test/resources/programs/functions_statement_return.in`
- `app/src/test/resources/programs/functions_switch_scope.in`
- `app/src/test/resources/programs/functions_void_expression.in`
- `app/src/test/resources/programs/io_arithmetic.in`
- `app/src/test/resources/programs/nested_control.in`
- `app/src/test/resources/programs/optional_syntax.in`
- `app/src/test/resources/programs/prime_numbers_below_forty.in`
- `app/src/test/resources/programs/stress_collatz.in`
- `app/src/test/resources/programs/stress_grid.in`
- `app/src/test/resources/programs/stress_numeric_pipeline.in`
- `app/src/test/resources/programs/stress_primes.in`
- `app/src/test/resources/programs/stress_state_machine.in`
- `app/src/test/resources/programs/variable_block_escape.in`
- `app/src/test/resources/programs/variable_block_scope.in`
- `app/src/test/resources/programs/variable_duplicate_declaration.in`
- `app/src/test/resources/programs/variable_undeclared_assignment.in`
- `app/src/test/resources/programs/variable_undeclared_read.in`
- `app/src/test/resources/programs/while_factorial.in`
- `app/src/test/resources/programs/while_modular_sequence.in`
- `docs/superpowers/plans/2026-11-22-interpreter-functions.md`
- `examples/nested_array_sum.in`
- `examples/nested_array_sum.json`
