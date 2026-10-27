package com.zaid.Delta_Forge.features.merge;

import com.zaid.Delta_Forge.features.json.ValueSlot;
import tools.jackson.databind.JsonNode;


public sealed interface MergeResult permits MergeResult.MergeSuccess,MergeResult.MergeWithConflicts{
    record MergeSuccess(ValueSlot ours, ValueSlot theirs, ValueSlot mergedOutcome) implements MergeResult {}
    record MergeWithConflicts(MergeConflict mergeConflict) implements MergeResult{}
    static MergeResult success(ValueSlot ours, ValueSlot theirs, ValueSlot mergedOutcome){
        return new MergeSuccess(ours,theirs,mergedOutcome);
    }
    static MergeWithConflicts conflict(MergeConflict mergeConflict){
        return new MergeWithConflicts(mergeConflict);
    }
}
