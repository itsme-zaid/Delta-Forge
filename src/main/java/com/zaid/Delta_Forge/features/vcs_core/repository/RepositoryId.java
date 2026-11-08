package com.zaid.Delta_Forge.features.vcs_core.repository;

import java.util.Objects;
import java.util.UUID;

public record RepositoryId(UUID repository_id) {
    public RepositoryId {
        Objects.requireNonNull(repository_id, "value");
    }

    public static RepositoryId random() {
        return new RepositoryId(UUID.randomUUID());
    }
}
