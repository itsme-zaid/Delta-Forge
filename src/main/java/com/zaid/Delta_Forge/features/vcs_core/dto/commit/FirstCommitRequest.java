package com.zaid.Delta_Forge.features.vcs_core.dto.commit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.JsonNode;

import java.util.UUID;

public record FirstCommitRequest(
        @NotBlank String branchName,
        @NotNull JsonNode document,
        @NotBlank String message
) {
}
