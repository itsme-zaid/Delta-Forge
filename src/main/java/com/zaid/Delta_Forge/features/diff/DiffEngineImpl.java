package com.zaid.Delta_Forge.features.diff;

import com.zaid.Delta_Forge.features.json.JsonDocumentService;
import com.zaid.Delta_Forge.features.json.ValueSlot;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class DiffEngineImpl implements DiffEngine{
    public static final Comparator<DiffOperation> ORDER =
            Comparator.comparing((DiffOperation op) -> op.path().toString())
                    .thenComparing(DiffOperation::diffOperationType);

    private final JsonDocumentService jsonDocumentService;
    public DiffEngineImpl(JsonDocumentService jsonDocumentService) { this.jsonDocumentService = jsonDocumentService;}
    @Override
    public List<DiffOperation> diff(JsonNode base, JsonNode target) {
        List<DiffOperation> out = new ArrayList<>();
        diffNode(base, target, JsonPointer.empty(), out);
        // Day 08 contract: stable order — sort by path, then by type
        out.sort(ORDER);
        return List.copyOf(out);
    }
    private void diffNode(JsonNode base,JsonNode target, JsonPointer path, List<DiffOperation> out){
        ;
        if(jsonDocumentService.structurallyEqual(base,target)) return;
        // Decision 2: both objects → recurse per key.
        if (base.isObject() && target.isObject()) {
            ObjectNode b = (ObjectNode) base;
            ObjectNode t = (ObjectNode) target;

            // Union of keys, sorted for deterministic output.
            TreeSet<String> keys = new TreeSet<>();
            b.properties().forEach(entry -> keys.add(entry.getKey()));
            t.properties().forEach(entry -> keys.add(entry.getKey()));

            for (String key : keys) {
                JsonNode bv = b.path(key);   // MissingNode if key absent
                JsonNode tv = t.path(key);   // MissingNode if key absent
                JsonPointer childPath = path.appendProperty(key);

                if (bv.isMissingNode()) {
                    out.add(new DiffOperation(
                            DiffOperationType.ADD, childPath,
                            ValueSlot.missing(), ValueSlot.of(tv)));
                } else if (tv.isMissingNode()) {
                    out.add(new DiffOperation(
                            DiffOperationType.REMOVE, childPath,
                            ValueSlot.of(bv), ValueSlot.missing()));
                } else {
                    diffNode(bv, tv, childPath, out);
                }
            }

            return;
        }
        out.add(new DiffOperation(
                DiffOperationType.REPLACE,
                path,
                ValueSlot.of(base),
                ValueSlot.of(target)
        ));
    }
}
