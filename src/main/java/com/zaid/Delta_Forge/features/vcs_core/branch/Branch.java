package com.zaid.Delta_Forge.features.vcs_core.branch;

import com.zaid.Delta_Forge.features.vcs_core.commit.CommitId;
import com.zaid.Delta_Forge.features.vcs_core.repository.RepositoryId;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
@Table("branches")
public record Branch(
        @Id UUID id,
        @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY) RepositoryId repositoryId,
        @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY) BranchName name,
        @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY, prefix = "head_") CommitId headCommitId,
        @Column("created_at") OffsetDateTime createdAt,
        @Column("updated_at") OffsetDateTime updatedAt
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
