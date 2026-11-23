# Reference JSON AST compatibility

Implement the user's request autonomously, using the actual frontend at
https://dboulytchev.github.io/eval.html as the format authority.
No push, PR changes, reset or rewriting existing commits.

## Findings

- Ten user-provided exports match the frontend output structurally.
- Reference scopes use `scope: {decls: [{name, spec}], body}`.
- Variables use `spec: "var"`; initialized values are separate assignments.
- Functions use `spec: {fun: {args, body}}`.
- Calls use `call: {fun, args}` in both expressions and statements.
- Return uses `return: {expr}` or `return: ""`.
- Actual frontend parsing differs from its displayed grammar. Use actual exports,
  including required control-body blocks, rather than reconstructing JSON manually.

## Implementation and commits

1. Code and focused tests: parse scopes/declarations and reference calls/returns;
   distinguish call contexts, retain legacy JSON compatibility, preserve dynamic
   lookup and finally-based unwinding. Reference-style functions return zero when
   no value is returned. Compile only an outer variable-only reference scope using
   existing instructions; reject nested declared scopes and user functions.
2. Fixtures and source corrections: export all 34 tracked source examples with
   the unmodified reference JavaScript. Preserve the ten existing exports byte
   for byte. Keep expected outputs independent and test errors for duplicate and
   undeclared variables as explicitly required by the original task.
3. Documentation: replace the canonical JSON schema with the reference format,
   state legacy compatibility and supported compilation subset.

## Verification

- Observe focused tests failing before implementation.
- Run full Gradle test/build on the new parser and all exported fixtures.
- Check all 34 reference exports against tracked JSON structurally.
- Review the final diff, preserve unrelated untracked files, and make local
  commits for the three units above.

## Progress

- Baseline has 14 failing app tests because scope JSON was not supported.
- Reference JS downloaded from the frontend (293831 bytes). It was executed
  locally with only the page's DOM fields supplied; AST generation code was not
  patched. JSON is produced before execution, including for negative programs.
- Frontend quirks (duplicate declarations accepted, return inside an infinite
  loop does not exit) do not override the user's explicit declaration errors or
  normal early-return behavior.

- All 34 tracked source programs now parse in the actual frontend; all 34 JSON ASTs are structurally equal to its exports. Ten supplied exports are byte-identical. Eighteen other fixtures required replacement; six were already equal.
- Focused tests: eight failures on unsupported nodes before implementation, then nine tests passed.
- Full `./gradlew test build`: app 106 tests plus utils 2 tests, zero failures/errors/skips.
- Independent review found no Important issues. One minor legacy mixed-format function-body context inconsistency remains harmless: unused return values are discarded with the function frame.
- No compiler/stack-machine user functions were implemented; only the existing outer variable preamble is accepted from reference scopes.
- Reference L0.js SHA256: `305d573fedce2e7bc58ee5bd2954ecdbbd27db9ff7b5111315691fce45ef9cf7`.
