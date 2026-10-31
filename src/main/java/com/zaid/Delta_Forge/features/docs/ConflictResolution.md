# Conflict Resolution Design

## Three concepts, three types

1. **MergeConflict** — observation. Produced by the merge engine. Immutable.
2. **Resolution** — decision. Produced by a human (or by accepting an AI
   suggestion). Immutable, append-only. Carries conflictId, resolvedValue,
   source, resolvedBy, resolvedAt, explanation.
3. **ConflictReport** — view. A snapshot of conflicts + current resolutions.
   Derived; not persisted. Served by the API (Day 41), rendered by the UI (Day 45).

## The merge runs exactly once

When `POST /merge` is called, `mergeObject` runs once and produces
`MergeConflicted(partialTree, conflicts)`. The partial tree and conflicts are
**stored** on the merge request. They are never recomputed.

Resolutions are **annotations** on the stored conflicts, not inputs to a re-merge.
Submitting a resolution:
- inserts a row in `resolutions` (keyed by conflictId)
- does NOT call mergeObject
- does NOT recompute conflicts

Applying the merge:
- loads the stored partial tree + resolutions
- materializes (setAt/removeAt per resolution)
- validates (schema, invariants)
- commits (two parents)
- does NOT call mergeObject

**The only time mergeObject runs again is on a NEW merge request, when the
user explicitly re-initiates after a stale-preview rejection.**

## Workflow phases

### Phase 1 — Merge run

POST /merge → engine runs → MergeSuccess | MergeConflicted.
If MergeConflicted: store merge request (status = OPEN, revision = 0),
return 409 with ConflictReport (status = UNRESOLVED).

### Phase 2 — Resolution

User submits resolutions via:
POST /merge-requests/{id}/resolutions
body: { conflictId, resolvedValue, explanation }
requires: expected revision (optimistic concurrency)

Each submission:
- checks revision matches (else 409 stale client)
- checks conflict is not already applied
- inserts Resolution row
- bumps merge request revision
- returns updated ConflictReport

AI suggestions (Day 49) go through a separate path:
POST /merge-requests/{id}/conflicts/{cid}/suggest
→ suggestion stored in ai_suggestions, NOT in resolutions
→ ConflictReport still shows UNRESOLVED for that conflict
→ user must explicitly accept/reject/edit → creates Resolution

### Phase 3 — Apply

POST /merge-requests/{id}/apply
body: { expected revision }

Server:
1. canApply() check (all resolved? already applied?)
2. stale check (target branch head == pinned ours commit? else STALE_PREVIEW)
3. materialize(partialTree, resolutions) → final document
4. validate final document (schema, invariants)
    - if invalid: status = VALIDATION_FAILED, return 422 with details
    - do NOT commit
5. create merge commit (two parents)
6. status = APPLIED, store merge commit id
7. return 200 with merge commit id

## State transitions

MergeRequest.status:
OPEN       — created, conflicts may or may not exist
RESOLVED   — all conflicts have resolutions (derived; may auto-advance)
APPLIED    — merge commit created
STALE      — target branch moved; user must create a new merge request
VALIDATION_FAILED — materialized doc failed validation; user must re-resolve
CANCELLED  — user abandoned; main unchanged

Allowed transitions:
OPEN → RESOLVED (last resolution submitted)
RESOLVED → APPLIED (apply succeeds)
RESOLVED → VALIDATION_FAILED (apply fails validation)
VALIDATION_FAILED → RESOLVED (user submits new resolution)
OPEN → CANCELLED
RESOLVED → CANCELLED
* → STALE (target branch moves at any point before APPLIED)

Forbidden:
APPLIED → anything (terminal)
RESOLVED → OPEN (can't un-resolve; only add new resolutions)
* → mergeObject re-run in place

## What does NOT happen

- The AI never creates a Resolution without human action.
- A merge commit is never created with unresolved conflicts.
- A merge commit is never created with validation failures.
- Resolutions are never modified after creation; corrections are new rows.
- The merge engine is never re-invoked from resolution/apply handlers.

## Why no re-merge

Re-merging would:
- produce the same conflicts again (deterministic, same inputs)
- discard accumulated resolutions unless re-applied
- silently absorb new target-branch commits if the branch moved
  (violating the "pinned commit IDs" contract from Day 20)

The only correct response to a moved target is: reject as STALE, require a
new merge request. That's one new merge run, not a re-merge in place.