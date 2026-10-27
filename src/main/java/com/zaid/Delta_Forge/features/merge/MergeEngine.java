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
    private boolean equalsSlot(ValueSlot a, ValueSlot b) {
        return switch (a) {
            case ValueSlot.Missing() -> b instanceof ValueSlot.Missing;
            case ValueSlot.Present(JsonNode an) -> b instanceof ValueSlot.Present(JsonNode bn)
                    && jsonService.structurallyEqual(an, bn);
        };
    }
    public MergeResult mergePrimitive(JsonPointer path,
                                      ValueSlot ours,
                                      ValueSlot theirs,
                                      ValueSlot base) {
        if (equalsSlot(ours, base))   return MergeResult.success(ours, theirs, theirs);
        if (equalsSlot(theirs, base)) return MergeResult.success(ours, theirs, ours);
        if (equalsSlot(ours, theirs)) return MergeResult.success(ours, theirs, theirs);

        ConflictType kind = determineConflictType(ours, theirs);
        return MergeResult.conflict(new MergeConflict(path, ours, theirs, base, kind));
    }

    private ConflictType determineConflictType(ValueSlot ours, ValueSlot theirs) {
        boolean bothPresent = ours instanceof ValueSlot.Present
                && theirs instanceof ValueSlot.Present;
        if (bothPresent) {
            JsonNode o = ((ValueSlot.Present) ours).node();
            JsonNode t = ((ValueSlot.Present) theirs).node();
            if (o.getNodeType() != t.getNodeType()) return ConflictType.TYPE_CHANGED;
        }
        return ConflictType.BOTH_MODIFIED;
    }
}
