package com.zaid.Delta_Forge.features.merge.array_merge;

import com.zaid.Delta_Forge.features.merge.MergeResult;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;

public interface ArrayMergeStrategy {
    /**
     * Decides how to merge three arrays (base, ours, theirs) at a path.
     * v1 ships only AtomicArrayStrategy. v2 extends with SetArrayStrategy and
     * EntityArrayStrategy, which require per-path configuration (schema layer).
     */
    MergeResult merge(
            JsonNode base,
            JsonNode ours,
            JsonNode theirs,
            JsonPointer path
    );
}
