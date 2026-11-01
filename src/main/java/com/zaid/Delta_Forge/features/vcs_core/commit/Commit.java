package com.zaid.Delta_Forge.features.vcs_core.commit;

import com.zaid.Delta_Forge.features.vcs_core.blob.SnapshotHash;
import com.zaid.Delta_Forge.features.vcs_core.repository.RepositoryId;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record Commit(
        CommitId id,
        RepositoryId repositoryId,
        SnapshotHash snapshotHash,
        List<CommitId> parents,        // 0, 1, or 2 elements
        String author,
        String message,
        Instant createdAt
) {
    public Commit {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(repositoryId, "repositoryId");
        Objects.requireNonNull(snapshotHash, "snapshotHash");
        Objects.requireNonNull(author, "author");
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(createdAt, "createdAt");
        parents = List.copyOf(parents);
        if (parents.size() > 2) {
            throw new IllegalArgumentException("commit cannot have more than 2 parents");
        }
        if (parents.stream().anyMatch(p -> p.equals(id))) {
            throw new IllegalArgumentException("commit cannot be its own parent");
        }
    }

    public boolean isMergeCommit() { return parents.size() == 2; }
    public boolean isRootCommit()  { return parents.isEmpty(); }
    public CommitId firstParent()  { return parents.getFirst(); }   // throws if empty
}