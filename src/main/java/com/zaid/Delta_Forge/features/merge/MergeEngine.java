package com.zaid.Delta_Forge.features.merge;

import com.zaid.Delta_Forge.features.json.JsonDocumentService;
import com.zaid.Delta_Forge.features.json.ValueSlot;
import org.springframework.stereotype.Service;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.StringNode;
import tools.jackson.databind.node.ValueNode;

import java.time.LocalDateTime;

@Service
public class MergeEngine {
    JsonDocumentService jsonService;
    public MergeEngine(JsonDocumentService jsonService){
        this.jsonService = jsonService;
    }
    public MergeResult mergePrimitive(JsonPointer path, JsonNode ours, JsonNode theirs, JsonNode base){
        if(jsonService.structurallyEqual(ours,base)) return new MergeResult.MergeSuccess(ours,theirs,theirs);
        else if(jsonService.structurallyEqual(theirs,base)) return new MergeResult.MergeSuccess(ours,theirs,ours);
        else if (jsonService.structurallyEqual(ours,theirs)) return new MergeResult.MergeSuccess(ours,theirs,theirs);
        else{
            // for now as of d17 only primitives so only type and modified conflicts
            if(ours.getNodeType() != theirs.getNodeType()) return new MergeResult.MergeWithConflicts(new MergeConflict(path,ours,theirs,base,ConflictType.TYPE_CHANGED));

            return new MergeResult.MergeWithConflicts(new MergeConflict(path,ours,theirs,base,ConflictType.BOTH_MODIFIED));
        }
    }
}
