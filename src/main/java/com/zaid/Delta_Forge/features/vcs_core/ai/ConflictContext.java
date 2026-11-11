package com.zaid.Delta_Forge.features.vcs_core.ai;

import tools.jackson.databind.JsonNode;

import java.util.List;

public record ConflictContext(
        String conflictId,
        String path,                    // e.g. "/service/timeout"
        String conflictType,            // BOTH_MODIFIED, DELETE_MODIFY, etc.
        JsonNode baseValue,              // null if missing
        JsonNode oursValue,              // null if missing
        JsonNode theirsValue,            // null if missing
        JsonNode parentContext,          // the surrounding object, trimmed
        List<String> commitMessages,     // untrusted — see §5
        String mergePolicy,              // trusted — from repository config
        JsonNode schemaConstraints,      // trusted — schema for this path
        List<String> crossFieldRules     // trusted — e.g. minReplicas <= maxReplicas
) {}