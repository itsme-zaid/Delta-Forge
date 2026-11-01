## Workflow (design — implementation on Day 41+)

Given: `MergeResult.MergeConflicted(partialTree, conflicts)` from `mergeObject`.

1. Persist `MergeRequest` with: pinned commit IDs (base/ours/theirs), partial tree,
   conflict list (with derived conflictIds), status = OPEN, revision = 0.

2. Build `ConflictReport` from the persisted conflicts + empty resolutions:
   each `ConflictEntry` has `Optional<Resolution>` empty.

3. For each conflict (in any order):
   a. If user resolves manually:
   POST /merge-requests/{id}/resolutions { conflictId, resolvedValue, explanation }
   → create Resolution(status=MANUALLY_RESOLVED, source=MANUAL, ...)
   → attach to the ConflictEntry
   → bump revision
   b. If user requests AI suggestion:
   POST /conflicts/{cid}/suggest
   → AiSuggestion stored separately (NOT a Resolution)
   → returned to user (status shows AI_SUGGESTED, derived from suggestion presence)
   c. If user accepts suggestion:
   POST /conflicts/{cid}/accept
   → create Resolution(status=USER_ACCEPTED, source=AI_ACCEPTED, ...)
   → attach to the ConflictEntry
   → bump revision
   d. If user rejects:
   POST /conflicts/{cid}/reject
   → discard suggestion; conflict remains unresolved

4. When all ConflictEntries have a non-empty Optional<Resolution>:
   POST /merge-requests/{id}/apply { expectedRevision }
   → verify revision matches (CAS)
   → verify target branch head still matches oursCommitId (STALE_PREVIEW check)
   → materialize: partialTree + setAt/removeAt per resolution
   → validate (schema, invariants)
   → create merge commit (two parents)
   → move target branch head
   → status = APPLIED, appliedAt = now

5. On apply failure:
    - revision mismatch → 409 CONFLICT
    - branch head moved → 409 STALE_PREVIEW
    - validation failed → status = VALIDATION_FAILED, return error, no commit