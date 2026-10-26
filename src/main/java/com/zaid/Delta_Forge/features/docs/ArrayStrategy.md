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