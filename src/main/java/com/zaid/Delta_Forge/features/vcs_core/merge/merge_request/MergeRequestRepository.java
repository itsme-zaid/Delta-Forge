package com.zaid.Delta_Forge.features.vcs_core.merge.merge_request;

import com.zaid.Delta_Forge.features.vcs_core.dto.merge.MergeResponse;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MergeRequestRepository extends CrudRepository<MergeRequest, UUID> {
    Optional<MergeRequest> findByIdAndRepositoryId(UUID id, UUID repositoryId);
    List<MergeRequest> findByRepositoryId(UUID repositoryId);
    List<MergeRequest> findByRepositoryIdAndStatus(UUID repositoryId, String status);
}
