package com.zaid.Delta_Forge.features.vcs_core.dto.merge;

import tools.jackson.databind.JsonNode;

public record ConflictDto(
        String path,
        String type,
        boolean basePresent,
        JsonNode base,
        boolean oursPresent,
        JsonNode ours,
        boolean theirsPresent,
        JsonNode theirs
) {}