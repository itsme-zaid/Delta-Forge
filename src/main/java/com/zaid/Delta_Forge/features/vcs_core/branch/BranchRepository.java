package com.zaid.Delta_Forge.features.vcs_core.branch;

import com.zaid.Delta_Forge.features.vcs_core.repository.RepositoryId;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BranchRepository extends CrudRepository<Branch, UUID> {
    Optional<Branch> findByRepositoryIdAndName(RepositoryId repositoryId, BranchName name);
    List<Branch> findByRepositoryId(RepositoryId repositoryId);
}
