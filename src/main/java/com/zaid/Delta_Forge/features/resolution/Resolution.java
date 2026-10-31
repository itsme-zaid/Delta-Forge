package com.zaid.Delta_Forge.features.resolution;

import com.zaid.Delta_Forge.features.json.ValueSlot;

import java.time.Instant;
import java.util.Objects;

public record Resolution(
        ValueSlot resolvedValue,
        ResolutionSource source,
        String resolvedBy,
        Instant resolvedAt,
        String explanation
) {
    public Resolution {
        Objects.requireNonNull(resolvedValue, "resolvedValue");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(resolvedBy, "resolvedBy");
        Objects.requireNonNull(resolvedAt, "resolvedAt");
    }

    public enum ResolutionSource {
        MANUAL,          // user typed/selected the value directly
        AI_ACCEPTED,     // user accepted an AI suggestion verbatim
        AI_EDITED        // user accepted an AI suggestion after editing it
    }
}
