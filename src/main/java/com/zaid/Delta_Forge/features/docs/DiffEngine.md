# JSON Diff Engine

## Purpose

Computes the list of operations that transform one JSON document into another.
Consumed by: the CLI (Day 13), the conflict report (Day 25), the audit trail
(Day 50), and the demo (Day 65).

## Operation model

Three types (RFC 6902 subset):
- ADD: path exists in target, not in base
- REMOVE: path exists in base, not in target
- REPLACE: path exists in both, values differ

`DiffOperation(type, path, oldValue, newValue)` where oldValue/newValue are
`ValueSlot` (Present | Missing), not raw JsonNode. This preserves the D4
(missing ≠ null) distinction.

## Interface contract

`List<DiffOperation> diff(JsonNode base, JsonNode target)`

- No side effects; inputs are never mutated.
- Stable output order: sorted by path (ascending), then by type (ADD < REMOVE < REPLACE).
- `diff(A, A)` returns `[]`.
- `apply(diff(A, B), A) == B` for all A, B (property test, Day 11).

## Design decisions

### Q1: Arrays are atomic
If arrays differ at all, emit a single REPLACE at the array's path.
Do not recurse. Matches `AtomicArrayStrategy` in the merge engine.

### Q2: Object key order is not a diff
Reordering keys produces no operations. Iterate keys in sorted order
for determinism.

### Q3: Root path is the empty pointer ""
`diff({}, [])` emits `REPLACE ""` (path is empty string, not "/").

### Q4: structurallyEqual documents produce no operations
Fast path at every recursion level. Uses the same `JsonEquality`
as the merge engine, including D5 numeric normalization.

## Shared equality

Diff and merge MUST use the same `structurallyEqual`. If they disagree,
a document that diff reports unchanged might merge as changed. This is
enforced by the `diff(A, A) == []` property test on Day 11.


## The absence of operation will be the representation of equality
- thats why there would be no "skip or keep" operation

# NOTE:
<p> <b> Diff operations are at the granularity of JSON nodes. A string, number, or boolean value either changes (one REPLACE) or doesn't (no operation). Character-level or token-level diffing (Myers) is not used in the diff engine's contract because (a) it would require a character-level apply to satisfy apply(diff(A, B), A) == B, and (b) it would diverge from the merge engine's value-level comparison, breaking the "diff and merge agree on what changed" property. Character-level highlighting is a display concern and may be added to DiffFormatter on Day 13 without affecting the operation model. </b></p> 

### we won't be using myers/naive lcs for actual diffing but only as a representation to user about what changed among values;

[//]: # (## Status)

[//]: # ()
[//]: # (- [x] Operation model designed &#40;Day 08&#41;)

[//]: # (- [ ] Primitive diff implemented &#40;Day 09&#41;)

[//]: # (- [ ] Object diff implemented &#40;Day 10&#41;)

[//]: # (- [ ] Array diff implemented &#40;Day 11&#41;)

[//]: # (- [ ] Formatting &#40;Day 13&#41;)

[//]: # (- [ ] 40+ test matrix &#40;Day 14&#41;)