package com.zaid.Delta_Forge.features.vcs_core.blob;

import java.util.Objects;

public record SnapshotHash(String hash) {
    public SnapshotHash {
        Objects.requireNonNull(hash, "hex");
        if (hash.length() != 64) {
            throw new IllegalArgumentException("sha-256 hex must be 64 chars, got " + hash.length());
        }
        if (!hash.chars().allMatch(c -> Character.digit(c, 16) >= 0)) {
            throw new IllegalArgumentException("snapshot hash must be lowercase hex");
        }
    }
}
