package com.zaid.Delta_Forge.features.vcs_core.commit;

import com.zaid.Delta_Forge.features.vcs_core.repository.RepositoryId;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface CommitRepository extends CrudRepository<Commit,CommitId> {
    List<Commit> findByRepositoryId(RepositoryId repositoryId);
    List<Commit> findByRepositoryIdOrderByCreatedAtDesc(RepositoryId repositoryId);
    Optional<Commit> findByIdAndRepositoryId(CommitId id, RepositoryId repositoryId);
}
