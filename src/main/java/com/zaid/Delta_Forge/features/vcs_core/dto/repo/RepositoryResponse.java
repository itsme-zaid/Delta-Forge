package com.zaid.Delta_Forge.features.vcs_core.dto.repo;
import java.time.Instant;
import java.util.UUID;

public record RepositoryResponse(
        UUID id,
        String name,
        String defaultBranch,
        String visibility,
        Instant createdAt
) {}