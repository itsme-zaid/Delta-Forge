package com.zaid.Delta_Forge.features.merge;

import com.zaid.Delta_Forge.features.json.ValueSlot;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;


public record MergeConflict(
        JsonPointer path,
        ValueSlot ours,
        ValueSlot theirs,
        ValueSlot base,
        ConflictType conflictType
) {}
