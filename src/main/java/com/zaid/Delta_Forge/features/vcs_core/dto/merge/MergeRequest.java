package com.zaid.Delta_Forge.features.vcs_core.dto.merge;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MergeRequest(
        @NotBlank String source,
        @NotBlank String target,
        @NotNull UUID expectedTargetHead
) {}