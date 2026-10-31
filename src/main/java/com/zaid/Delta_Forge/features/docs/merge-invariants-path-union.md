# Merge Invariants — Path-Union Properties

## Purpose

This document records two path-level properties of the three-way merge
engine, the reasoning that proves them, and the property-based tests that
enforce them (Day 27).

The properties answer the question: **"What can the merge engine do to the
set of changed paths?"** The answer is a clean two-sided bound:
- The merge never **invents** a path (subset).
- For non-conflicting merges, the merge never **drops** a path (union equality).

Together these pin down the strongest correct statement about path-level
merge behavior.

---

## Definitions

For two documents `A` and `B`:

- `paths(diff(A, B))` = the set of `JsonPointer`s at which `A` and `B` differ.
  Computed by `DiffEngine.diff(A, B)` and extracting `DiffOperation::path`.
- "Changed path" means: the value at this pointer differs between `A` and `B`
  under `structurallyEqual` (D1–D5 aware).

For a merge `M = merge(base, ours, theirs)`:

- If `M` is `MergeSuccess(merged)`: `result = merged`.
- If `M` is `MergeConflicted(partialTree, conflicts)`: `result = partialTree`
  (the tree with unresolved keys omitted).

---

## Property 1 — Subset (no invented paths)

> **Any path changed in the merge result was changed by at least one side
> relative to base.**

Formally:

### Why it holds (for all merges, including conflicted)

The merge engine at every path does exactly one of:

1. **Takes ours** — when `theirs == base` (or `ours == theirs != base`).
   The path's changed-ness in the result equals ours' changed-ness.
   So if it appears in the result diff, it appears in `diff(base, ours)`.

2. **Takes theirs** — symmetric. The path appears in `diff(base, theirs)`.

3. **Takes the common value** — when `ours == theirs` (or both `== base`).
   If it differs from base, the path appears in **both** side diffs.
   If it equals base, the path appears in **neither** diff and not in the result.

4. **Conflicts** — the path is **omitted** from the partial tree.
   Since base has the key and the partial tree doesn't, `diff(base, partial)`
   shows this path as a REMOVE. But **both side diffs also show this path**
   (one side changed it, the other changed it differently, or one deleted it
   while the other edited) — so the path is in the union.

In every case, a path in the result diff is also in at least one side's diff.
**The merge never invents a path.**

### Status

- Holds for **all** merges: `MergeSuccess` and `MergeConflicted`.
- For `MergeConflicted`, `result` is the **partial tree** (not the final document).
- This is invariant **I7** in `docs/MERGE_INVARIANTS.md`.

---

## Property 2 — Union equality (no dropped paths)

> **For non-conflicting merges, the merge result changes exactly the paths
> that at least one side changed.**

Formally:

### Why it holds (for non-conflicting merges only)

A non-conflicting merge means: at every path, the merge engine took one of
the three "no conflict" branches:

- `ours == base` (only theirs changed)
- `theirs == base` (only ours changed)
- `ours == theirs` (both changed to the same value, or both unchanged)

At any such path, **at most one side is different from base** (or both sides
are identically different):

- If `ours == base` and `theirs != base`: path is in `diff(base, theirs)`, in
  the union, and in the result diff (because the merge takes theirs). ✓
- If `theirs == base` and `ours != base`: symmetric. ✓
- If `ours == theirs != base`: path is in **both** side diffs, in the union,
  and in the result diff. ✓
- If `ours == theirs == base`: path is in neither side diff, neither union,
  nor result diff. ✓

In every case, the path's presence in the result diff matches its presence in
the union. There is **no case** where a path appears in the union but not the
result, because the merge never reverts a non-conflicting change to base.

**The merge never drops a non-conflicting change.**

### Why it does NOT hold for conflicting merges

A conflicting key is **omitted from the partial tree**. Base has the key,
the partial tree does not, so `diff(base, partial)` shows it as a REMOVE
even though both sides showed it as an EDIT. The path set is still correct
(it's in the union), but the **operation** has changed from EDIT to REMOVE.

So:
- **Path-set equality** happens to hold for conflicting merges too (the path
  is in both the union and the result diff, as a REMOVE). But this is a
  coincidence of path-set comparison, not a semantic property.
- **Operation-set equality** (comparing `ADD/REMOVE/REPLACE` + values) fails
  for conflicting merges, because the partial tree's REMOVE is a placeholder,
  not a decision.

The test uses `assumeThat(MergeSuccess)` to restrict to the case where the
property is semantically meaningful.

### Status

- Holds only for `MergeSuccess` merges.
- Requires the same **path granularity** across diffs (see Caveat below).
- Not listed in `MERGE_INVARIANTS.md` yet; should be added as **I11** after
  Day 27 passes.

---

## Caveat — path granularity must be consistent

Both properties assume the diff engine reports **leaf-level paths**
consistently across all three diffs. If the diff engine ever does
**rationalization** (collapsing child paths into a single parent REPLACE),
the path sets would diverge:

- `diff(base, ours)` might report `/service` as REPLACE.
- `diff(base, merged)` might report `/service/timeout` as REPLACE.

Same change, different granularity — the path sets no longer match, and
Property 2 would fail even though the merge is correct.

The diff engine does **not** rationalize (see `docs/json-diff.md`). This
caveat is recorded so that a future change to the diff engine (adding
rationalization for display) doesn't accidentally weaken these properties.

**Rule:** merge invariants are tested against the **engine-level diff**
(no rationalization), never against a display-level diff.

---

## The tests (Day 27)

### Test 1 — subset (all merges)

```java
@Property
void no_unrelated_path_is_modified(@ForAll("mergeTriples") MergeTriple t) {
    MergeResult r = engine.mergeObject(JsonPointer.empty(), t.base(), t.ours(), t.theirs());

    JsonNode result = (r instanceof MergeResult.MergeSuccess s)
            ? ((ValueSlot.Present) s.mergedOutcome()).node()
            : ((MergeResult.MergeConflicted) r).partialMergedOutcome();

    Set<JsonPointer> resultPaths = diffPaths(t.base(), result);
    Set<JsonPointer> union = union(diffPaths(t.base(), t.ours()),
                                   diffPaths(t.base(), t.theirs()));

    assertThat(resultPaths).isSubsetOf(union);
}