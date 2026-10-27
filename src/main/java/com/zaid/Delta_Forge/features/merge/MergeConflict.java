package com.zaid.Delta_Forge.features.merge;

import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;


public record MergeConflict(
        JsonPointer path,
        JsonNode ours,
        JsonNode theirs,
        JsonNode base,
        ConflictType conflictType
) {}
