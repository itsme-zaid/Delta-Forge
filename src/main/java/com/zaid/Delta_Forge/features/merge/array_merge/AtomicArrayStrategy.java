package com.zaid.Delta_Forge.features.merge.array_merge;

import com.zaid.Delta_Forge.features.json.JsonDocumentService;
import com.zaid.Delta_Forge.features.json.ValueSlot;
import com.zaid.Delta_Forge.features.merge.MergeEngine;
import com.zaid.Delta_Forge.features.merge.MergeResult;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

public class AtomicArrayStrategy implements ArrayMergeStrategy{
    @Override
    public MergeResult merge(JsonNode base, JsonNode ours, JsonNode theirs, JsonPointer path) {
    MergeEngine engine = new MergeEngine(new JsonDocumentService(JsonMapper.builder().build()));
        return engine.mergePrimitive(
                path,
                ValueSlot.present(ours),
                ValueSlot.present(theirs),
                ValueSlot.present(base)
        );
//        return null;
    }
}
