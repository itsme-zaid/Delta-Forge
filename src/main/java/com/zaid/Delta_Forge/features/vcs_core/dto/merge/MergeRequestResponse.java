package com.zaid.Delta_Forge.features.vcs_core.dto.merge;

import com.zaid.Delta_Forge.features.vcs_core.branch.BranchName;
import com.zaid.Delta_Forge.features.vcs_core.commit.CommitId;
import com.zaid.Delta_Forge.features.vcs_core.dto.ConflictWithId;
import tools.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record MergeRequestResponse(
        UUID id,
        UUID repositoryId,
        UUID targetBranchId,
        UUID sourceBranchId,
        UUID baseCommitId,
        UUID oursCommitId,
        UUID theirsCommitId,
        JsonNode partialTree,
        List<ConflictWithId> conflicts,
        String status,
        int revision,
        OffsetDateTime createdAt,
        OffsetDateTime appliedAt
) {


}