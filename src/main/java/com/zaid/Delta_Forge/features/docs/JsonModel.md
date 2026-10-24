# JSON Model

## Purpose

This document captures decisions about what a JSON document *is* for the
purposes of StructMerge. These decisions determine the diff engine (Phase 1),
the three-way merge engine (Phase 2), and the version-control storage
(Phase 4). They are not preferences — they are contracts, and every test in
`src/test/java/.../json/` and `src/test/java/.../merge/` enforces them.

## Node kinds

A JSON document is one of exactly six node kinds (RFC 8259 §2-§4):

1. **Object** — unordered map of string keys to JSON values
2. **Array** — ordered list of JSON values
3. **String** — UTF-8, may contain escape sequences
4. **Number** — no NaN, no Infinity, arbitrary precision permitted
5. **Boolean** — true / false
6. **Null** — the single value `null`

Internal nodes are Object or Array. Leaves are String, Number, Boolean, Null.
This is the entire model.

## Decisions

### D1 — Object key order is NOT meaningful

**Decision:** Two objects with the same keys and values in different orders
are equal.

**Consequence for canonicalization:** When serializing to canonical form
(for hashing on Day 15), keys are sorted lexicographically by Unicode
code point (Java `String.compareTo`).

**Do not use:** locale-aware collation (`Collator`) — sorts differ per
machine, breaking determinism.

### D2 — Array order IS meaningful

**Decision:** `[1,2]` and `[2,1]` are not equal. The default merge strategy
for arrays is atomic-with-order-preserved.

**Consequence:** Arrays whose semantics are "set" or "entity collection"
need an explicit `ArrayMergeStrategy` (Days 22-24). Not the default.

**Do not do in v1:** auto-detect set-ness from array contents or from a
heuristic. The user must opt in explicitly.

### D3 — Duplicate object keys are REJECTED

**Decision:** `parse("{\"a\": 1, \"a\": 2}")` throws.

**Consequence:** The parser (Day 4) must configure Jackson to reject
duplicate keys rather than last-value-wins.

**Rationale:** RFC 8259 §8.3 leaves this to the implementation. Rejecting
is the only choice that doesn't silently accept documents that different
parsers interpret differently.

### D4 — Missing vs. Null are DISTINCT

**Decision:** `{"a": null}` has key `a` present with value `null`.
`{}` does not have key `a`. These are different documents.

**Consequence:** Every "value at a path" representation must distinguish
presence from value. On Day 16 this becomes a `Slot`/`Present`/`Missing`
type; until then it is a written contract.

**Rationale:** If missing == null, then "delete a key" and "set it to null"
are indistinguishable in the merge engine, and the conflict report can't
show which one happened.

**Worked example:** base=`{"a": 1}`, ours=`{"a": null}`, theirs=`{}`.
This is a DELETE_VS_EDIT conflict, not a "both deleted" no-op.
The conflict report shows `ours = null (present)`, `theirs = missing`.

### D5 — Numeric equality is by `BigDecimal.compareTo`

**Decision:** `1`, `1.0`, `1e0`, `10e-1` are all equal. `-0` equals `0`
for equality and hashing; the original text of the number is preserved
in the JSON node for display.

**Consequence for hashing (Day 15):** Numbers are canonicalized to
`BigDecimal.stripTrailingZeros().toPlainString()` before hashing.
Equality and hashing must agree — this is non-negotiable, or
`HashMap`/`HashSet` will silently produce duplicates.

**Do not use:** Jackson's default `IntNode(1).equals(DoubleNode(1.0))`
— that returns `false` and will produce spurious merge conflicts.

## Sample files

Located at `src/test/resources/sample-json/`. Each file exists to exercise
a specific decision or edge case:

| File | What it exercises |
|---|---|
| `objects/nested.json` | D1 (order ignored), nested traversal |
| `arrays/primitives.json` | D2 (order preserved) |
| `arrays/of-objects.json` | D2 + entity-array discussion (Day 22) |
| `edge-cases/null-and-missing.json` | D4 (null vs missing) |
| `edge-cases/unicode-keys.json` | RFC 6901 escaping |
| `edge-cases/large-numbers.json` | D5 (numeric policy) |
| `edge-cases/duplicate-keys.json` | D3 (rejection) |
| `edge-cases/valid-empty.json` | root `{}` |
| `edge-cases/valid-null-root.json` | root `null` |
| `edge-cases/root-array.json` | root array (not an object) |
| `edge-cases/root-string.json` | root string |

## Reading

- RFC 8259 (JSON), §1-§6 and §8
- RFC 6901 (JSON Pointer), all 5 pages
- RFC 6902 (JSON Patch), §4 (operations) — for diff vocabulary

## Status

- [x] Decisions made (D1-D5)
- [ ] Implemented in `JsonDocumentService` (Day 4)
- [ ] Enforced by tests (Day 4)
- [ ] Enforced by canonicalization (Day 15)


- <B>setAt policy:</B> Creates missing object parents recursively. Does not create missing array elements; array indices must be within bounds. This matches JSON Patch's add behavior for objects and avoids the ambiguity of "how many nulls to insert" for arrays. The Materializer (Day 10) always operates on a deep copy of the merged tree, so mutation is safe.