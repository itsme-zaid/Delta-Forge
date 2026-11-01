package com.zaid.Delta_Forge.features.vcs_core.repository;

import java.time.Instant;
import java.util.Objects;

public record Repository(
        RepositoryId id,
        String name,
        String owner,
        String defaultBranch,         // e.g. "main"
        Instant createdAt
) {
    public Repository {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(defaultBranch, "defaultBranch");
        Objects.requireNonNull(createdAt, "createdAt");
        if (name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (defaultBranch.isBlank()) throw new IllegalArgumentException("defaultBranch must not be blank");
    }
}