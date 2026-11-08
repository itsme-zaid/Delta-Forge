package com.zaid.Delta_Forge.features.vcs_core.dto.branch;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
public record CreateBranchRequest(
        @NotBlank @Size(min = 1, max = 100) @Pattern(regexp = "[A-Za-z0-9_\\-/]+") String name,
        @NotNull UUID fromCommitId
) {}
