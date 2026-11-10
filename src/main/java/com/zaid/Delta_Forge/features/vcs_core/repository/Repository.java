package com.zaid.Delta_Forge.features.vcs_core.repository;

import com.zaid.Delta_Forge.features.vcs_core.user.UserId;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Objects;

@Table("repositories")
public record Repository(
        @Id RepositoryId id,
        String name,
        @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY) UserId ownerId,
        @Column("default_branch") String defaultBranch,         // e.g. "main"
        @Column("visibility") String visibility,
        @Column("created_at") OffsetDateTime createdAt
) {
    public Repository {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(ownerId, "ownerId");
        Objects.requireNonNull(defaultBranch, "defaultBranch");
        Objects.requireNonNull(createdAt, "createdAt");
        if (name.isBlank()) throw new IllegalArgumentException("name must not be blank");
        if (defaultBranch.isBlank()) throw new IllegalArgumentException("defaultBranch must not be blank");
    }
}