package com.zaid.Delta_Forge.features.vcs_core.repository;

import java.util.Objects;
import java.util.UUID;

public record RepositoryId(UUID value) {
    public RepositoryId {
        Objects.requireNonNull(value, "value");
    }

    public static RepositoryId random() {
        return new RepositoryId(UUID.randomUUID());
    }
}
