package com.zaid.Delta_Forge.features.vcs_core.branch;

import java.util.Objects;

public record BranchName(String branch_name) {
    public BranchName {
        Objects.requireNonNull(branch_name, "branch_name");
        if (branch_name.isBlank()) throw new IllegalArgumentException("branch name must not be blank");
    }
}