package com.zaid.Delta_Forge.features.vcs_core.ai.AiSuggestion;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AiSuggestionRepository extends CrudRepository<AiSuggestion, UUID> {
    List<AiSuggestion> findByMergeRequestId(UUID mergeRequestId);
    Optional<AiSuggestion> findByMergeRequestIdAndConflictId(UUID mergeRequestId, String conflictId);
}