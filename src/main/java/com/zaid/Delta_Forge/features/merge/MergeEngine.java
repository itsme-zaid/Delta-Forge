package com.zaid.Delta_Forge.features.merge;

import com.zaid.Delta_Forge.features.json.JsonDocumentService;
import com.zaid.Delta_Forge.features.json.ValueSlot;
import com.zaid.Delta_Forge.features.merge.array_merge.ArrayMergeStrategy;
import com.zaid.Delta_Forge.features.merge.array_merge.AtomicArrayStrategy;
import org.springframework.stereotype.Service;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.*;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

@Service
public class MergeEngine {
    JsonDocumentService jsonService;
    public MergeEngine(JsonDocumentService jsonService){
        this.jsonService = jsonService;
    }
    AtomicArrayStrategy arrayStrategy = new AtomicArrayStrategy();
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
        if (equalsSlot(ours, base)) {
            return MergeResult.success(theirs);
        }
        if (equalsSlot(theirs, base)) {
            return MergeResult.success(ours);
        }
        if (equalsSlot(ours, theirs)) {
            return MergeResult.success(theirs);
        }

        ConflictType kind = determineConflictType(ours, theirs);
        return MergeResult.conflicted(null, List.of(
                new MergeConflict(path, ours, theirs, base, kind)));
    }
    public MergeResult mergeObject(JsonPointer path,
                                    ObjectNode base,
                                    ObjectNode ours,
                                    ObjectNode theirs) {

        ObjectNode merged = JsonNodeFactory.instance.objectNode();
        List<MergeConflict> conflicts = new ArrayList<>();

        // Union of keys, sorted for determinism.
        TreeSet<String> keys = new TreeSet<>();
        base.propertyNames().forEach(keys::add);
        ours.propertyNames().forEach(keys::add);
        theirs.propertyNames().forEach(keys::add);

        for (String key : keys) {
            JsonPointer childPath = path.appendProperty(key);

            ValueSlot b = ValueSlot.of(base.path(key));       // Missing if absent
            ValueSlot o = ValueSlot.of(ours.path(key));
            ValueSlot t = ValueSlot.of(theirs.path(key));

            MergeResult childResult = mergeNodes(childPath, b, o, t);

            if (childResult instanceof MergeResult.MergeSuccess s) {
                // Resolved. Add to merged tree if present.
                if (s.mergedOutcome() instanceof ValueSlot.Present(JsonNode n)) {
                    merged.set(key, n);
                }
                // If null: this key resolved to "missing" (clean delete or both-absent).
                // Simply don't add it to the merged object.
            } else if (childResult instanceof MergeResult.MergeConflicted c) {
                conflicts.addAll(c.conflicts());
                // If the child produced a partial tree, we could merge it in,
                // but for v1 we drop it and let the user resolve.
                // The conflict record preserves what ours/theirs/base were.
            }
        }

        if (conflicts.isEmpty()) {
            return MergeResult.success(new ValueSlot.Present(merged));
        }
        return MergeResult.conflicted(merged, conflicts);
    }
    public MergeResult mergeObject(JsonPointer path,
                                   JsonNode base,
                                   JsonNode ours,
                                   JsonNode theirs) {

        ObjectNode merged = JsonNodeFactory.instance.objectNode();
        if(!(base instanceof ObjectNode) || !(ours instanceof ObjectNode) || !(theirs instanceof ObjectNode)) mergeNodes(path,ValueSlot.of(base),ValueSlot.of(ours),ValueSlot.of(theirs));
        List<MergeConflict> conflicts = new ArrayList<>();

        // Union of keys, sorted for determinism.
        TreeSet<String> keys = new TreeSet<>();
        base.propertyNames().forEach(keys::add);
        ours.propertyNames().forEach(keys::add);
        theirs.propertyNames().forEach(keys::add);

        for (String key : keys) {
            JsonPointer childPath = path.appendProperty(key);

            ValueSlot b = ValueSlot.of(base.path(key));       // Missing if absent
            ValueSlot o = ValueSlot.of(ours.path(key));
            ValueSlot t = ValueSlot.of(theirs.path(key));

            MergeResult childResult = mergeNodes(childPath, b, o, t);

            if (childResult instanceof MergeResult.MergeSuccess s) {
                // Resolved. Add to merged tree if present.
                if (s.mergedOutcome() instanceof ValueSlot.Present(JsonNode n)) {
                    merged.set(key, n);
                }
                // If null: this key resolved to "missing" (clean delete or both-absent).
                // Simply don't add it to the merged object.
            } else if (childResult instanceof MergeResult.MergeConflicted c) {
                conflicts.addAll(c.conflicts());
                // If the child produced a partial tree, we could merge it in,
                // but for v1 we drop it and let the user resolve.
                // The conflict record preserves what ours/theirs/base were.
            }
        }

        if (conflicts.isEmpty()) {
            return MergeResult.success(new ValueSlot.Present(merged));
        }
        return MergeResult.conflicted(merged, conflicts);
    }
    private MergeResult mergeNodes(JsonPointer path, ValueSlot b, ValueSlot o, ValueSlot t) {
        if (o instanceof ValueSlot.Present(JsonNode on)
                && t instanceof ValueSlot.Present(JsonNode tn)
                && b instanceof ValueSlot.Present(JsonNode bn)) {
            if (on.isObject() && tn.isObject() && bn.isObject()) {
                return mergeObject(path, (ObjectNode) bn, (ObjectNode) on, (ObjectNode) tn);
            }
            if (on.isArray() && tn.isArray() && bn.isArray()) {
                return arrayStrategy.merge(bn,on,tn,path);
            }
        }
        return mergePrimitive(path, o, t, b);
    }
    private ConflictType determineConflictType(ValueSlot ours, ValueSlot theirs) {
        boolean oursMissing   = ours   instanceof ValueSlot.Missing;
        boolean theirsMissing = theirs instanceof ValueSlot.Missing;

        // Delete-vs-edit: one side deleted, the other edited
        if (oursMissing && !theirsMissing) return ConflictType.DELETE_MODIFY;
        if (theirsMissing && !oursMissing) return ConflictType.DELETE_MODIFY;
        // Both missing can't reach here (equalsSlot catches it)

        // Both present, differing types
        JsonNode o = ((ValueSlot.Present) ours).node();
        JsonNode t = ((ValueSlot.Present) theirs).node();
        if (o.getNodeType() != t.getNodeType()) return ConflictType.TYPE_CHANGED;

        return ConflictType.BOTH_MODIFIED;
    }
}
