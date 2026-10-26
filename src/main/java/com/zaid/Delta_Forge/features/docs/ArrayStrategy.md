 Array semantics are domain-dependent. An array can be an ordered list (["prod", "staging"]), a set (["admin", "user"]), or a collection of entities ([{"id": "u1"}, {"id": "u2"}]). JSON alone doesn't tell you which. Merging ["a", "b"] and ["b", "c"] as sets gives ["a", "b", "c"]; as ordered lists, it's a conflict. You cannot pick the right strategy without knowing the domain.
<br>
 
Atomic replacement is deterministic and correct. It never produces false positives. REPLACE /items applied to the base gives exactly the target. The apply(diff(A, B), A) == B property holds.
<br>

 It matches the merge engine's AtomicArrayStrategy. When you build the merge engine on Day 17, arrays will be treated atomically there too. If diff recursed into arrays but merge didn't, the two engines would disagree about what changed — breaking the "diff and merge use the same equality" contract from Day 08.
 <br>

LCS/Myers for arrays is a future-work item. A future LcsArrayStrategy could match elements by identity (e.g., id field) and merge per-entity. But that requires: (a) an identity function, (b) alignment across three arrays (base, ours, theirs), not two, and (c) a threshold for what counts as "the same element." That's a week of work, not a day.

### Arrays are atomic in v1 because their merge semantics depend on domain knowledge that JSON does not carry. Atomic replacement is deterministic, correct, and matches the merge engine's behavior