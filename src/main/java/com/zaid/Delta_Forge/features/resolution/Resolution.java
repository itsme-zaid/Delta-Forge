package com.zaid.Delta_Forge.features.resolution;

import com.zaid.Delta_Forge.features.json.ValueSlot;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Objects;

public record Resolution(
        String conflictId,
        ValueSlot resolvedValue,
        ResolutionSource source,
        String resolvedBy,
        OffsetDateTime resolvedAt,
        String explanation
) {
    public Resolution {
        Objects.requireNonNull(conflictId,"conflictId");
        Objects.requireNonNull(resolvedValue, "resolvedValue");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(resolvedBy, "resolvedBy");
        Objects.requireNonNull(resolvedAt, "resolvedAt");
        Objects.requireNonNull(explanation, "explanation");
    }

    public enum ResolutionSource {
        MANUAL,          // user typed/selected the value directly
        AI_ACCEPTED,     // user accepted an AI suggestion verbatim
        AI_EDITED        // user accepted an AI suggestion after editing it
    }
}
