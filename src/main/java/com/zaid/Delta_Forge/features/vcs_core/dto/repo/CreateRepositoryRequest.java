package com.zaid.Delta_Forge.features.vcs_core.dto.repo;

import com.zaid.Delta_Forge.features.vcs_core.repository.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateRepositoryRequest(
        @NotBlank @Size(min = 1, max = 100) String name,
        @NotBlank @Size(min = 1, max = 100) @Pattern(regexp = "[A-Za-z0-9_\\-/]+") String defaultBranch,
        @NotBlank Visibility visibility
) {}