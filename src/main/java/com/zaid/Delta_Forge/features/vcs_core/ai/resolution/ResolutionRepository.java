package com.zaid.Delta_Forge.features.vcs_core.ai.resolution;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResolutionRepository extends CrudRepository<ResolutionRow, UUID> {
    List<ResolutionRow> findByMergeRequestId(UUID mergeRequestId);
    Optional<ResolutionRow> findByMergeRequestIdAndConflictId(UUID mergeRequestId, String conflictId);
}