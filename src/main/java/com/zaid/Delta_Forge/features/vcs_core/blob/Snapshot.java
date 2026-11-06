package com.zaid.Delta_Forge.features.vcs_core.blob;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.Objects;

@Table("snapshots")
public record Snapshot(
        // SnapshotHash is basically the hash of the JSON document wrapped in a record
        @Id SnapshotHash hash,
        JsonNode document,            // parsed tree
        @Column("normalization_version") String normalizationVersion,  // "normalization-v1"
        @Column("normalization_version") Instant createdAt,
        @Column("canonical_bytes") byte[] canonicalBytes
) {
    public Snapshot {
        Objects.requireNonNull(hash, "hash");
        Objects.requireNonNull(document, "document");
        Objects.requireNonNull(normalizationVersion, "normalizationVersion");
        Objects.requireNonNull(createdAt, "createdAt");
    }
}