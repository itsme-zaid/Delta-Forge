package com.zaid.Delta_Forge.features.vcs_core.dto.branch;

import java.time.Instant;
import java.util.UUID;

public record BranchResponse(
        UUID id,
        String name,
        UUID headCommitId,
        Instant createdAt,
        Instant updatedAt
) {}