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
import java.util.List;
import java.util.Objects;
@Table("commits")
public record Commit(
        @Id CommitId id,
        @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY) RepositoryId repositoryId,
        @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY) SnapshotHash snapshotHash,
        @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY) UserId authorId,
        @MappedCollection(idColumn = "commit_id", keyColumn = "position")
        List<CommitId> parents,
        String message,
        @Column("created_at") Instant createdAt
) {
    public Commit {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(repositoryId, "repositoryId");
        Objects.requireNonNull(snapshotHash, "snapshotHash");
        Objects.requireNonNull(authorId, "authorId");
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

    public boolean isMergeCommit() {
        return parents.size() == 2;
    }

    public boolean isRootCommit() {
        return parents.isEmpty();
    }

    public CommitId firstParent() {
        return parents.getFirst();
    }   // throws if empty
}