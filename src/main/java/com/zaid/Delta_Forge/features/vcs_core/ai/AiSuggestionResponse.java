package com.zaid.Delta_Forge.features.vcs_core.ai;

import tools.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AiSuggestionResponse(
        UUID id, String conflictId, String action, JsonNode proposedValue,
        String explanation, String status, String provider, OffsetDateTime createdAt
) {}