package com.zaid.Delta_Forge.features.vcs_core.blob;

import java.util.Objects;

public record SnapshotHash(String hex) {
    public SnapshotHash {
        Objects.requireNonNull(hex, "hex");
        if (hex.length() != 64) {
            throw new IllegalArgumentException("sha-256 hex must be 64 chars, got " + hex.length());
        }
        if (!hex.chars().allMatch(c -> Character.digit(c, 16) >= 0)) {
            throw new IllegalArgumentException("snapshot hash must be lowercase hex");
        }
    }
}
