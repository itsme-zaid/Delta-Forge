package com.zaid.Delta_Forge.features.vcs_core.commit;

import com.zaid.Delta_Forge.features.vcs_core.blob.SnapshotHash;
import com.zaid.Delta_Forge.features.vcs_core.repository.RepositoryId;
import com.zaid.Delta_Forge.features.vcs_core.user.UserId;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Table("commits")
public record Commit(
        @Id CommitId id,
        @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY) RepositoryId repositoryId,
        @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY, prefix = "snapshot_") SnapshotHash snapshotHash,
        @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY) UserId authorId,
        String message,
        @Column("created_at") OffsetDateTime createdAt
) {
    public Commit {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(repositoryId, "repositoryId");
        Objects.requireNonNull(snapshotHash, "snapshotHash");
        Objects.requireNonNull(authorId, "authorId");
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(createdAt, "createdAt");

    }
}