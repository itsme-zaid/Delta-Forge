package com.zaid.Delta_Forge.features.vcs_core.merge;

import com.zaid.Delta_Forge.features.merge.MergeConflict;
import com.zaid.Delta_Forge.features.vcs_core.commit.CommitId;
import com.zaid.Delta_Forge.features.vcs_core.repository.RepositoryId;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MergeRequest(
        UUID id,
        RepositoryId repositoryId,
        UUID targetBranchId,
        UUID sourceBranchId,
        CommitId baseCommitId,         // common ancestor (null if no common ancestor)
        CommitId oursCommitId,         // target head at preview time
        CommitId theirsCommitId,       // source head at preview time
        JsonNode partialTree,          // keys that merged cleanly; omitted on conflict
        List<MergeConflict> conflicts, // observations, no resolutions yet
        MergeRequestStatus status,     // OPEN | APPLIED | STALE | CANCELLED
        int revision,                  // optimistic concurrency for resolution updates
        Instant createdAt,
        Instant appliedAt              // null until APPLIED
) {
}