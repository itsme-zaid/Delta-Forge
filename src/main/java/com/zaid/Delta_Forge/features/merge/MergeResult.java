package com.zaid.Delta_Forge.features.merge;

import com.zaid.Delta_Forge.features.json.ValueSlot;
import tools.jackson.databind.JsonNode;

import java.util.List;


public sealed interface MergeResult permits MergeResult.MergeSuccess, MergeResult.MergeConflicted {

    /** No conflicts; the tree is fully resolved. */
    record MergeSuccess(ValueSlot mergedOutcome) implements MergeResult {}

    /** Some keys resolved, some conflicted. Partial tree + the conflicts. */
    record MergeConflicted(JsonNode partialMergedOutcome, List<MergeConflict> conflicts)
            implements MergeResult {}

    static MergeResult success(ValueSlot merged) {
        return new MergeSuccess(merged);
    }

    static MergeResult conflicted(JsonNode partialMerged, List<MergeConflict> conflicts) {
        return new MergeConflicted(partialMerged, List.copyOf(conflicts));
    }
}
