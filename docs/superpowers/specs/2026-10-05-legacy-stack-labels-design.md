# Legacy stack-label compatibility

## Goal

Compile AST JSON into the existing `stack-json` fixtures without changing
their legacy `L_*` labels or control-flow layout.

## AST JSON contract

Control nodes may include an optional `labels` object:

```json
{
  "while": {
    "labels": { "body": 5, "condition": 6 },
    "cond": { "...": "..." },
    "body": { "...": "..." }
  }
}
```

`do` uses the same `body` and `condition` fields. `if` uses:

```json
"labels": { "end": 0, "else": 2 }
```

Each field must be an integer. Omitting `labels` preserves the current
compiler output and makes the addition backward compatible.

## Compilation

When label metadata is present, the compiler emits the legacy instructions:

- `while`: jump to `L_while_cond_N`, emit `L_while_cody_M`, emit the body,
  emit `L_while_cond_N`, emit the condition, then `JNZ L_while_cody_M`.
- `do`: emit `L_while_cody_M`, the body, `L_while_cond_N`, the condition,
  then `JNZ L_while_cody_M`.
- `if`: use `L_end_N` and `L_else_M`; nested else-if nodes may reuse the
  same `end` identifier explicitly.

Without metadata, retain the existing `IF-*`, `WHILE-*`, and
`DO-WHILE-*` generation.

## Fixtures and verification

Annotate every control node in the 22 `ast-json` fixtures using the labels
already specified in the paired `stack-json` fixture. `CompilerProgramTest`
compares parsed JSON structures, so whitespace does not affect results.

Verification will run all compiler tests (22 expected passes) and the full
Gradle test suite.
