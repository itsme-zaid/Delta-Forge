
# VCS Domain Model

## Entities and responsibilities

- **Repository** — namespace. Owns commits, snapshots, branches.
- **Snapshot** — content-addressed JSON document. Identity = SHA-256 of canonical
  form (`normalization-v1`). Immutable. Deduplicated by hash.
- **Commit** — immutable history node. Identity = UUID. Holds a snapshot hash
  and 0, 1, or 2 ordered parents.
- **CommitParent** — association table. `(commit_id, parent_id, position)`.
  Position 0 = first parent (mainline); position 1 = second parent (merge source).
- **Branch** — mutable pointer. `(repository_id, name)` unique. Head updated by CAS.
- **MergeRequest** — merge workflow state. Pins commit IDs; stores partial tree
  and conflicts; tracks status/revision.
- **Resolution** — a per-conflict decision. Immutable, audited.

## Key decisions

1. **One document per repository.** A snapshot is one JSON blob, not a tree.
   Simplifies commit → snapshot to a single hop.
2. **Snapshots are content-addressed; commits are UUIDs.** Content-addressing
   gives dedup + integrity for documents. UUIDs for commits because timestamps
   make content-addressed commits rarely deduplicable, and UUIDs are easier to
   reason about. Trade-off documented.
3. **Parent edges are authoritative; children derivable.** `commit_parents` is
   the source of truth. Descendant queries computed by reverse traversal when
   needed; add a `commit_children` index in v2 only if descendant queries become
   hot.
4. **Commit parents are ordered (0 = first parent, 1 = second).** Enables
   `--first-parent`-style history and identifies the target branch's previous
   head for a merge commit.
5. **No HEAD entity in v1.** Every API call names a branch explicitly. HEAD can
   be added as a per-session pointer in v2 without schema changes.
6. **Merge requests store conflicts as JSONB; resolutions as rows.** Conflicts
   are read-only observations (never updated). Resolutions are decisions (get
   updated, audited). Different access patterns → different storage.
7. **`resolutions.resolved_present` preserves D4 at the DB layer.** A resolution
   can be "delete the key" (present=false) or "set to null" (present=true,
   value=null). Without the boolean, the DB would lose the distinction.
8. **Every commit/snapshot FK is within a repository.** Enforced by composite
   FKs. Prevents cross-repo reference bugs.

## DAG operations

- **Ancestor check:** BFS from head following parent edges; check if target is reached.
- **Best common ancestor:** ancestor-set intersection + remove-non-best; reject
  if multiple (criss-cross merge).
- **History:** walk parents recursively; ordered by parent position (first parent first).
- **Reachability:** same as ancestor check, different use case.