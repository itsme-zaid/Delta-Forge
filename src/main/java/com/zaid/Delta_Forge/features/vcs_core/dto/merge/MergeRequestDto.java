package com.zaid.Delta_Forge.features.vcs_core.dto.merge;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MergeRequestDto(
        @NotBlank String source, //source branch
        @NotBlank String target, // target branch
        @NotNull UUID expectedTargetHead
) {}