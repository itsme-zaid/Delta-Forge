package com.zaid.Delta_Forge.features.vcs_core.ai;

import com.zaid.Delta_Forge.features.json.ValueSlot;
import com.zaid.Delta_Forge.features.resolution.Resolution;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ResolutionResponse(
        String conflictId, ValueSlot resolvedValue, Resolution.ResolutionSource source,
        String resolvedBy, OffsetDateTime resolvedAt, String explanation
) {}