package com.zaid.Delta_Forge.features.vcs_core.branch;

import com.zaid.Delta_Forge.features.vcs_core.commit.CommitId;
import com.zaid.Delta_Forge.features.vcs_core.repository.RepositoryId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record Branch(
        UUID id,
        RepositoryId repositoryId,
        BranchName name,
        CommitId headCommitId,
        Instant createdAt,
        Instant updatedAt
) {
    public Branch {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(repositoryId, "repositoryId");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(headCommitId, "headCommitId");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(updatedAt, "updatedAt");
    }
}
