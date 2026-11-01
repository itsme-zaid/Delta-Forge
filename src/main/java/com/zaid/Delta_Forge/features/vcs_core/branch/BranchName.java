package com.zaid.Delta_Forge.features.vcs_core.branch;

import java.util.Objects;

public record BranchName(String value) {
    public BranchName {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) throw new IllegalArgumentException("branch name must not be blank");
        // Optional: validate against git-style rules (no leading /, no .., no control chars)
    }
}