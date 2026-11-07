package com.zaid.Delta_Forge.features.vcs_core.commit;

import com.zaid.Delta_Forge.features.vcs_core.repository.RepositoryId;
import org.hibernate.validator.constraints.ParameterScriptAssert;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

public interface CommitRepository extends CrudRepository<Commit,CommitId> {
    List<Commit> findByRepositoryId(RepositoryId repositoryId);
    List<Commit> findByRepositoryIdOrderByCreatedAtDesc(RepositoryId repositoryId);
    Optional<Commit> findByIdAndRepositoryId(CommitId id, RepositoryId repositoryId);
    Boolean existsByIdAndRepositoryId(CommitId id, RepositoryId repositoryId);

    @Query("""
    SELECT c.id
    FROM commits c
    WHERE c.repository_id = :repoId
    AND NOT EXISTS (
        SELECT 1 FROM commit_parents cp WHERE cp.commit_id = c.id
    )
    """)
    List<UUID> findRootCommitIdsByRepo(@Param("repoId") UUID repoId);

    // Step 2: load via findById to trigger parent loading
    default Commit findRootCommit(RepositoryId repoId) {
        List<UUID> ids = findRootCommitIdsByRepo(repoId.value());
        if (ids.isEmpty()) throw new NoSuchElementException("no root commit in repo " + repoId);
        return findById(new CommitId(ids.getFirst())).orElseThrow();
    }
}
