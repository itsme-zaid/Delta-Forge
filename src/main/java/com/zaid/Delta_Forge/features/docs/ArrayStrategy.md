 Array semantics are domain-dependent. An array can be an ordered list (["prod", "staging"]), a set (["admin", "user"]), or a collection of entities ([{"id": "u1"}, {"id": "u2"}]). JSON alone doesn't tell you which. Merging ["a", "b"] and ["b", "c"] as sets gives ["a", "b", "c"]; as ordered lists, it's a conflict. You cannot pick the right strategy without knowing the domain.
<br>
 
Atomic replacement is deterministic and correct. It never produces false positives. REPLACE /items applied to the base gives exactly the target. The apply(diff(A, B), A) == B property holds.
<br>

 It matches the merge engine's AtomicArrayStrategy. When you build the merge engine on Day 17, arrays will be treated atomically there too. If diff recursed into arrays but merge didn't, the two engines would disagree about what changed — breaking the "diff and merge use the same equality" contract from Day 08.
 <br>

LCS/Myers for arrays is a future-work item. A future LcsArrayStrategy could match elements by identity (e.g., id field) and merge per-entity. But that requires: (a) an identity function, (b) alignment across three arrays (base, ours, theirs), not two, and (c) a threshold for what counts as "the same element." That's a week of work, not a day.

### Arrays are atomic in v1 because their merge semantics depend on domain knowledge that JSON does not carry. Atomic replacement is deterministic, correct, and matches the merge engine's behavior

## Array of Objects Behaviour:
- Day11: did multiple tests and was able to find out that a array of objects has the same behavior in diffEngine as a scalar Array has i.e <B> any change (even in order) results in one operation on the entire array /REPLACE
</B>


- <b>What does NOT trigger a diff:</b>
- Key reordering *inside* an object element (D1: key order is not meaningful)
- Equal elements in the same order (fast path via structurallyEqual)

<b>What DOES trigger a diff (as one REPLACE on the array):</b>
- Element value change
- Element addition or removal
- Element reordering (D2: array order is meaningful)

### Cost of atomicity

For a 1000-element array with one changed element, the diff output contains the
whole old array and the whole new array. This is the primary cost of the atomic
strategy: it is *correct* (deterministic, matches the merge engine), but the
output granularity is coarse.

### What would fix it (deferred to v2)

A future `EntityArrayStrategy` would:
1. Take a configured identity field (e.g., "id")
2. Match elements across base/ours/theirs by that field
3. Diff / merge per matched element
4. Report unmatched elements as ADD/REMOVE
5. Detect conflicts when the same entity is edited differently on both branches

This is a week of work (alignment algorithm + strategy configuration + tests
against false positives) and is out of scope for v1. The atomic strategy is
the correct default for arrays whose semantics are unknown.



# Array Merge Strategy (v2)

## Purpose

This document records the design, behaviour, and v1/v2 scope boundary for
how StructMerge (Delta_Forge) merges JSON arrays.

It exists because **JSON arrays are syntactically one thing and semantically
several things**, and the merge engine cannot tell which without domain
knowledge. Every decision below follows from that single fact.

---

## 1. The problem

A JSON array is an ordered list of values. JSON itself gives no signal about
what the list *means*. The same syntax `["admin", "user"]` can be:

1. **Ordered sequence** — steps in a build pipeline, log entries.
   Order matters; duplicates are meaningful.

2. **Set** — permissions, tags, feature flags.
   Order is irrelevant; duplicates are meaningless.

3. **Entity collection** — records identified by a field (`id`).
   Order is usually irrelevant; identity is in a field.

4. **Priority list** — routing rules, override chains.
   Order matters *and* identity matters.

5. **Configuration sequence** — allowed origins, feature lists.
   Semantically a set, behaviourally iterated in order.

Each meaning demands a **different correct merge rule**:

| Meaning | Correct merge | Example |
|---|---|---|
| Ordered sequence | Both reorder → conflict. One reorders, other unchanged → take reordered | `["a","b"]` vs `["b","a"]` |
| Set | Union of additions; no conflict unless both removed the same element differently | `["user"]` + `["admin"]` → `["user","admin"]` |
| Entity collection | Match by `id`, merge per-entity, conflict per-entity | `[{id:u1,role:user}]` + `[{id:u1,role:admin}]` |
| Priority list | Hardest. Needs order + identity + interaction analysis | Rules that shadow each other |
| Config sequence | Depends on framework; union may be unsafe | Iterated at first-match |

**The merge engine cannot choose between these from the array alone.**

---

## 2. v1 decision: atomic replacement

v1 ships exactly one strategy: `AtomicArrayStrategy`. It treats the array as
an **opaque value**: any difference at all produces one `REPLACE` (diff) or one
conflict (merge), at the array's path. No recursion into elements.

### Why atomic is the correct default

1. **It is deterministically correct.** Applying `REPLACE /items [old] → [new]`
   to the base document produces the target document, exactly. The
   `apply(diff(A, B), A) == B` property holds unconditionally.

2. **It never produces false positives.** If the arrays differ, there
   *is* a difference. Reporting it is honest. A merge strategy that guesses
   "this is a set, union it" can be silently wrong for sequences, priority
   lists, and config lists.

3. **It is consistent with the diff engine.** The diff engine (Day 11) also
   treats arrays as atomic. Diff and merge agree on granularity, which
   preserves the invariant from Day 08: *diff and merge must use the same
   notion of "changed."* If merge recursed into arrays but diff did not, the
   two engines would produce different answers to "did this array change?" —
   which is exactly the failure mode the architecture is designed to prevent.

4. **It makes no assumptions about semantics.** Any non-atomic strategy
   requires the user to tell the system *what kind of array this is*. That
   configuration layer is not part of v1.

Atomic is not "the best strategy." Atomic is **the only strategy that is
never silently wrong without domain knowledge.**

### Behaviour

| Case | Result |
|---|---|
| `ours == theirs` | Take that value (convergent edit) |
| `ours == base` | Take theirs |
| `theirs == base` | Take ours |
| Both changed differently | `ARRAY_MODIFIED` conflict at the array's path |

This is the same truth table as `mergePrimitive`, because **an atomic array is
semantically a scalar value**. The strategy reuses the primitive merge rules
rather than duplicating them.

Note the conflict type is `ARRAY_MODIFIED`, not `BOTH_MODIFIED`. The strategy
emits the array-specific conflict kind so downstream consumers (conflict
report, UI, audit trail) can label it distinctly.

---

## 3. The extension point

```java
public interface ArrayMergeStrategy {
    MergeResult mergeArray(JsonPointer path,
                           JsonNode base,
                           JsonNode ours,
                           JsonNode theirs,
                           MergeEngine engine);
}