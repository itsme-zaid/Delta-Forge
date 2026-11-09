package com.zaid.Delta_Forge.features.vcs_core.dto.commit;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CommitResponse(
        UUID id,
        String branch,
        String snapshotHash,
        UUID authorId,
        String message,
        Instant createdAt
) {}