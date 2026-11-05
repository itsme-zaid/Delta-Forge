package com.zaid.Delta_Forge.features.vcs_core.blob;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class SnapshotHasher {
    private SnapshotHasher() {
    }

    /**
     * Hash the canonical UTF-8 bytes of a snapshot.
     * <p>
     * The input MUST be the canonical form produced by normalization-v1
     * (sorted object keys, normalized numbers). Hashing raw JSON bytes would
     * break deduplication: {"a":1,"b":2} and {"b":2,"a":1} would hash
     * differently even though they are the same document per D1.
     */
    public static SnapshotHash hash(String canonicalJson) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(canonicalJson.getBytes(StandardCharsets.UTF_8));
            return new SnapshotHash(HexFormat.of().formatHex(hashBytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
