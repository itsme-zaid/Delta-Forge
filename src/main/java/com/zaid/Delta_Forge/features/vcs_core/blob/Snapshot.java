package com.zaid.Delta_Forge.features.vcs_core.blob;

import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.Objects;

public record Snapshot(
        SnapshotHash hash,
        JsonNode document,            // parsed tree
        String normalizationVersion,  // "normalization-v1"
        Instant createdAt
) {
    public Snapshot {
        Objects.requireNonNull(hash, "hash");
        Objects.requireNonNull(document, "document");
        Objects.requireNonNull(normalizationVersion, "normalizationVersion");
        Objects.requireNonNull(createdAt, "createdAt");
    }
}