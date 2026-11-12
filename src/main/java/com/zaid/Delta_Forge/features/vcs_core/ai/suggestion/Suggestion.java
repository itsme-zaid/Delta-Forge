package com.zaid.Delta_Forge.features.vcs_core.ai.suggestion;

import org.springframework.ai.chat.metadata.Usage;
import tools.jackson.databind.JsonNode;

public record Suggestion(
        String conflictId,
        Action action,
        JsonNode proposedValue,          // only for CUSTOM_JSON
        String explanation,
        Usage usage                      // token counts, if reported
) {}