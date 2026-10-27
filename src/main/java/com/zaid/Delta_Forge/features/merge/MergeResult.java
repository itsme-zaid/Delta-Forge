package com.zaid.Delta_Forge.features.merge;

import tools.jackson.databind.JsonNode;


public sealed interface MergeResult permits MergeResult.MergeSuccess,MergeResult.MergeWithConflicts{
    record MergeSuccess(JsonNode ours, JsonNode theirs, JsonNode mergedOutcome) implements MergeResult {}
    record MergeWithConflicts(MergeConflict mergeConflict) implements MergeResult{}

    static MergeResult success(JsonNode ours,JsonNode theirs,JsonNode mergedOutcome){
        return new MergeSuccess(ours,theirs,mergedOutcome);
    }

}
