package com.zaid.Delta_Forge.UnitTesting;

import com.zaid.Delta_Forge.features.json.JsonDocumentService;
import com.zaid.Delta_Forge.features.json.ValueSlot;
import com.zaid.Delta_Forge.features.merge.ConflictType;
import com.zaid.Delta_Forge.features.merge.MergeConflict;
import com.zaid.Delta_Forge.features.merge.MergeEngine;
import com.zaid.Delta_Forge.features.merge.MergeResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.*;

import static org.assertj.core.api.Assertions.assertThat;

public class MergeEngineTest {
    JsonDocumentService service;
    MergeEngine mergeEngine;

    @BeforeEach
    public void set_up(){
        this.service = new JsonDocumentService(JsonMapper.builder().build());
        this.mergeEngine = new MergeEngine(service);
    }

    @Test
    public void no_change_emits_merge_success(){
        ValueSlot ours = ValueSlot.present(FloatNode.valueOf(1.0f));
        ValueSlot base = ValueSlot.present(IntNode.valueOf(1));
        ValueSlot theirs = ValueSlot.present(IntNode.valueOf(1));
//        assertThat(mergeEngine.mergePrimitive(null,ours,theirs,base);
        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
        assertThat(mergeResult).isInstanceOf(MergeResult.MergeSuccess.class);
        MergeResult.MergeSuccess success =
                (MergeResult.MergeSuccess) mergeResult;
        assertThat(success.mergedOutcome()).isEqualTo(base).isEqualTo(theirs);
    }
    @Test
    public void ours_only_changed_emits_success(){
        ValueSlot ours = ValueSlot.present(StringNode.valueOf("string2"));
        ValueSlot base = ValueSlot.present(StringNode.valueOf("string1"));
        ValueSlot theirs = ValueSlot.present(StringNode.valueOf("string1"));
        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
        assertThat(mergeResult).isInstanceOf(MergeResult.MergeSuccess.class);
        MergeResult.MergeSuccess success =
                (MergeResult.MergeSuccess) mergeResult;
        assertThat(success.mergedOutcome()).isEqualTo(ours);
    }
    @Test
    public void theirs_only_changed_emits_success(){
        ValueSlot ours = ValueSlot.present(StringNode.valueOf("string1"));
        ValueSlot base = ValueSlot.present(StringNode.valueOf("string1"));
        ValueSlot theirs = ValueSlot.present(StringNode.valueOf("string2"));
//        assertThat(mergeEngine.mergePrimitive(null,ours,theirs,base);
        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
        assertThat(mergeResult).isInstanceOf(MergeResult.MergeSuccess.class);
        MergeResult.MergeSuccess success =
                (MergeResult.MergeSuccess) mergeResult;
        assertThat(success.mergedOutcome()).isEqualTo(theirs);
    }
    @Test
    public void both_changed_to_same_value_emits_success(){
        ValueSlot ours = ValueSlot.present(StringNode.valueOf("string2"));
        ValueSlot base = ValueSlot.present(StringNode.valueOf("string1"));
        ValueSlot theirs = ValueSlot.present(StringNode.valueOf("string2"));
//        assertThat(mergeEngine.mergePrimitive(null,ours,theirs,base);
        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
        assertThat(mergeResult).isInstanceOf(MergeResult.MergeSuccess.class);
        MergeResult.MergeSuccess success =
                (MergeResult.MergeSuccess) mergeResult;
        assertThat(success.mergedOutcome()).isEqualTo(ours);
    }
    @Test
    public void base_value_deleted_theirs_unchanged(){
        ValueSlot ours = ValueSlot.present(StringNode.valueOf("string2"));
        ValueSlot base = ValueSlot.missing();
        ValueSlot theirs = ValueSlot.present(StringNode.valueOf("string2"));
//        assertThat(mergeEngine.mergePrimitive(null,ours,theirs,base);
        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
        assertThat(mergeResult).isInstanceOf(MergeResult.MergeSuccess.class);
        MergeResult.MergeSuccess success =
                (MergeResult.MergeSuccess) mergeResult;
        assertThat(success.mergedOutcome()).isEqualTo(ours);
    }
//
    @Test
    public void type_changed_both_sides_same(){
        ValueSlot ours = ValueSlot.present(IntNode.valueOf(100));
        ValueSlot base = ValueSlot.present(StringNode.valueOf("100"));
        ValueSlot theirs = ValueSlot.present(IntNode.valueOf(100));
//        assertThat(mergeEngine.mergePrimitive(null,ours,theirs,base);
        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
        assertThat(mergeResult).isInstanceOf(MergeResult.MergeSuccess.class);
        MergeResult.MergeSuccess success =
                (MergeResult.MergeSuccess) mergeResult;
        assertThat(success.mergedOutcome()).isEqualTo(ours);
    }
//
    @Test
    public void one_side_set_to_null_from_int_node(){
        ValueSlot ours = ValueSlot.present(IntNode.valueOf(100));
        ValueSlot base = ValueSlot.present(IntNode.valueOf(100));
        ValueSlot theirs = ValueSlot.present(NullNode.getInstance());
//        assertThat(mergeEngine.mergePrimitive(null,ours,theirs,base);
        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
        assertThat(mergeResult).isInstanceOf(MergeResult.MergeSuccess.class);
        MergeResult.MergeSuccess success =
                (MergeResult.MergeSuccess) mergeResult;
        assertThat(success.mergedOutcome()).isInstanceOf(NullNode.class);
    }
//
    @Test
    public void both_side_set_to_null_from_int_node(){
        ValueSlot ours = ValueSlot.present(NullNode.getInstance());
        ValueSlot base = ValueSlot.present(IntNode.valueOf(100));
        ValueSlot theirs = ValueSlot.present(NullNode.getInstance());
        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
        assertThat(mergeResult).isInstanceOf(MergeResult.MergeSuccess.class);
        MergeResult.MergeSuccess success =
                (MergeResult.MergeSuccess) mergeResult;
        assertThat(success.mergedOutcome()).isInstanceOf(NullNode.class);
    }
//
//
//    // now conflicting test cases here:
//    @Test
//    public void both_modified_ours_theirs(){
//        ValueSlot ours = ValueSlot.present(IntNode.valueOf(1001));
//        ValueSlot base = ValueSlot.present(IntNode.valueOf(100));
//        ValueSlot theirs = ValueSlot.present(IntNode.valueOf(1002));
//        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
//        assertThat(mergeResult).isInstanceOf(MergeResult.MergeWithConflicts.class);
//        MergeResult.MergeWithConflicts conflict =
//                (MergeResult.MergeWithConflicts) mergeResult;
//        assertThat(conflict.mergeConflict().conflictType()).isEqualTo(ConflictType.BOTH_MODIFIED);
//    }
//    @Test
//    public void ours_type_changed_int_to_boolean(){
//        ValueSlot base = ValueSlot.present(IntNode.valueOf(100));
//        ValueSlot ours = ValueSlot.present(BooleanNode.valueOf(false));
//        ValueSlot theirs = ValueSlot.present(IntNode.valueOf(1002));
//        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
//        assertThat(mergeResult).isInstanceOf(MergeResult.MergeWithConflicts.class);
//        MergeResult.MergeWithConflicts conflict =
//                (MergeResult.MergeWithConflicts) mergeResult;
//        assertThat(conflict.mergeConflict().conflictType()).isEqualTo(ConflictType.TYPE_CHANGED);
//    }
//    @Test
//    public void type_changed_with_same_value_int_to_string(){
//        ValueSlot base = ValueSlot.present(IntNode.valueOf(100));
//        ValueSlot ours = ValueSlot.present(StringNode.valueOf("100"));
//        ValueSlot theirs = ValueSlot.present(IntNode.valueOf(1002));
//        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
//        assertThat(mergeResult).isInstanceOf(MergeResult.MergeWithConflicts.class);
//        MergeResult.MergeWithConflicts conflict =
//                (MergeResult.MergeWithConflicts) mergeResult;
//        assertThat(conflict.mergeConflict().conflictType()).isEqualTo(ConflictType.TYPE_CHANGED);
//    }
//    @Test
//    public void value_and_type_both_changed_emits_TYPE_CHANGED(){
//        ValueSlot base = ValueSlot.present(IntNode.valueOf(100));
//        ValueSlot ours = ValueSlot.present(StringNode.valueOf("1001"));
//        ValueSlot theirs = ValueSlot.present(IntNode.valueOf(1002));
//        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
//        assertThat(mergeResult).isInstanceOf(MergeResult.MergeWithConflicts.class);
//        MergeResult.MergeWithConflicts conflict =
//                (MergeResult.MergeWithConflicts) mergeResult;
//        assertThat(conflict.mergeConflict().conflictType()).isEqualTo(ConflictType.TYPE_CHANGED);
//    }

    @Test
    void independent_changes_merge_cleanly() {
        ObjectNode base   =  JsonNodeFactory.instance.objectNode();
        base.put("a",10);
        base.put("b",20);
        ObjectNode ours   =  JsonNodeFactory.instance.objectNode();
        ours.put("a",10);
        ours.put("b",2);
        ObjectNode theirs   =  JsonNodeFactory.instance.objectNode();
        theirs.put("a",1);
        theirs.put("b",20);

        MergeResult r = mergeEngine.mergeObject(JsonPointer.empty(), base, ours, theirs);

        assertThat(r).isInstanceOf(MergeResult.MergeSuccess.class);
        ValueSlot p = ((MergeResult.MergeSuccess) r).mergedOutcome();
        ValueSlot.Present merged = (ValueSlot.Present) p;
        assertThat(merged.node().get("a").asInt()).isEqualTo(1);
        assertThat(merged.node().get("b").asInt()).isEqualTo(2);
    }
    @Test
    void same_key_added_on_both_sides_with_same_value_emits_success() {
        ObjectNode base   =  JsonNodeFactory.instance.objectNode();
        base.put("a",10);
        base.put("b",20);
        ObjectNode ours   =  JsonNodeFactory.instance.objectNode();
        ours.put("a",10);
        ours.put("b",2);
        ours.put("c",5);
        ObjectNode theirs   =  JsonNodeFactory.instance.objectNode();
        theirs.put("a",1);
        theirs.put("b",20);
        theirs.put("c",5);

        MergeResult r = mergeEngine.mergeObject(JsonPointer.empty(), base, ours, theirs);

        assertThat(r).isInstanceOf(MergeResult.MergeSuccess.class);
        ValueSlot p = ((MergeResult.MergeSuccess) r).mergedOutcome();
        ValueSlot.Present merged = (ValueSlot.Present) p;
        assertThat(merged.node().get("a").asInt()).isEqualTo(1);
        assertThat(merged.node().get("b").asInt()).isEqualTo(2);
        assertThat(merged.node().get("c").asInt()).isEqualTo(5);
    }
    @Test
    void same_key_added_on_both_sides_with_different_value_emits_conflict() {
        ObjectNode base   =  JsonNodeFactory.instance.objectNode();
        base.put("a",10);
        base.put("b",20);
        ObjectNode ours   =  JsonNodeFactory.instance.objectNode();
        ours.put("a",10);
        ours.put("b",2);
        ours.put("c",5);
        ObjectNode theirs   =  JsonNodeFactory.instance.objectNode();
        theirs.put("a",1);
        theirs.put("b",20);
        theirs.put("c",51);

        MergeResult r = mergeEngine.mergeObject(JsonPointer.empty(), base, ours, theirs);

        assertThat(r).isInstanceOf(MergeResult.MergeConflicted.class);
        MergeResult.MergeConflicted c = (MergeResult.MergeConflicted) r;
        assertThat(c.conflicts()).hasSize(1);
        assertThat(c.conflicts().getFirst().conflictType()).isEqualTo(ConflictType.BOTH_MODIFIED);
    }
    @Test
    void key_added_on_one_side_emits_success() {
        ObjectNode base   =  JsonNodeFactory.instance.objectNode();
        base.put("a",10);
        base.put("b",20);
        ObjectNode ours   =  JsonNodeFactory.instance.objectNode();
        ours.put("a",10);
        ours.put("b",2);
        ours.put("c",5);
        ObjectNode theirs   =  JsonNodeFactory.instance.objectNode();
        theirs.put("a",1);
        theirs.put("b",20);

        MergeResult r = mergeEngine.mergeObject(JsonPointer.empty(), base, ours, theirs);

        assertThat(r).isInstanceOf(MergeResult.MergeSuccess.class);
        ValueSlot p = ((MergeResult.MergeSuccess) r).mergedOutcome();
        ValueSlot.Present merged = (ValueSlot.Present) p;
        assertThat(merged.node().get("a").asInt()).isEqualTo(1);
        assertThat(merged.node().get("b").asInt()).isEqualTo(2);
        assertThat(merged.node().get("c").asInt()).isEqualTo(5);
    }
    @Test
    void key_deleted_on_one_side_emits_success_with_other_unchanged() {
        ObjectNode base   =  JsonNodeFactory.instance.objectNode();
        base.put("a",10);
        base.put("b",20);
        base.put("c",5);
        ObjectNode ours   =  JsonNodeFactory.instance.objectNode();
        ours.put("a",10);
        ours.put("b",2);
        ours.put("c",5);
        ObjectNode theirs   =  JsonNodeFactory.instance.objectNode();
        theirs.put("a",1);
        theirs.put("b",20);


        MergeResult r = mergeEngine.mergeObject(JsonPointer.empty(), base, ours, theirs);

        assertThat(r).isInstanceOf(MergeResult.MergeSuccess.class);
        ValueSlot p = ((MergeResult.MergeSuccess) r).mergedOutcome();
        ValueSlot.Present merged = (ValueSlot.Present) p;
        assertThat(merged.node().get("a").asInt()).isEqualTo(1);
        assertThat(merged.node().get("b").asInt()).isEqualTo(2);
        assertThat(merged.node().has("c")).isFalse();
    }
    @Test
    void key_deleted_on_one_side_and_modified_on_other() {
        ObjectNode base   =  JsonNodeFactory.instance.objectNode();
        base.put("a",10);
        base.put("b",20);
        base.put("c",5);
        ObjectNode ours   =  JsonNodeFactory.instance.objectNode();
        ours.put("a",10);
        ours.put("b",2);
        ours.put("c",52);
        ObjectNode theirs   =  JsonNodeFactory.instance.objectNode();
        theirs.put("a",1);
        theirs.put("b",20);


        MergeResult r = mergeEngine.mergeObject(JsonPointer.empty(), base, ours, theirs);

        assertThat(r).isInstanceOf(MergeResult.MergeConflicted.class);
        MergeResult.MergeConflicted p = ((MergeResult.MergeConflicted) r);
        JsonNode partialMergedOutcome = p.partialMergedOutcome();
        System.out.println(p);
        assertThat(p.conflicts().getFirst().conflictType()).isEqualTo(ConflictType.DELETE_MODIFY);
    }
    @Test
    void key_deleted_on_one_side_and_deleted_on_another() {
        ObjectNode base   =  JsonNodeFactory.instance.objectNode();
        base.put("a",10);
        base.put("b",20);
        base.put("c",5);
        ObjectNode ours   =  JsonNodeFactory.instance.objectNode();
        ours.put("a",10);
        ours.put("b",2);
        ours.put("c",52);
        ours.put("d",52);
        ObjectNode theirs   =  JsonNodeFactory.instance.objectNode();
        theirs.put("a",1);
        theirs.put("b",20);
//        theirs.put("c",52);


        MergeResult r = mergeEngine.mergeObject(JsonPointer.empty(), base, ours, theirs);

        assertThat(r).isInstanceOf(MergeResult.MergeConflicted.class);
        MergeResult.MergeConflicted p = ((MergeResult.MergeConflicted) r);
//        JsonNode partialMergedOutcome = p.partialMergedOutcome();
        System.out.println(p);
//        assertThat(p.conflicts().getFirst().conflictType()).isEqualTo(ConflictType.DELETE_MODIFY);
    }
    @Test
    void different_keys_added_on_both_sides() {
        ObjectNode base   =  JsonNodeFactory.instance.objectNode();
        base.put("a",10);
        base.put("b",20);
        base.put("c",5);
        ObjectNode ours   =  JsonNodeFactory.instance.objectNode();
        ours.put("a",10);
        ours.put("b",2);
        ours.put("c",52);
        ours.put("d",52);
        ObjectNode theirs   =  JsonNodeFactory.instance.objectNode();
        theirs.put("a",1);
        theirs.put("b",20);
        theirs.put("e",52);
        theirs.put("c",52);
//        theirs.put("c",52);


        MergeResult r = mergeEngine.mergeObject(JsonPointer.empty(), base, ours, theirs);

        assertThat(r).isInstanceOf(MergeResult.MergeSuccess.class);
//        MergeResult.MergeConflicted p = ((MergeResult.MergeConflicted) r);
//        JsonNode partialMergedOutcome = p.partialMergedOutcome();
//        System.out.println(p);
//        assertThat(p.conflicts().getFirst().conflictType()).isEqualTo(ConflictType.DELETE_MODIFY);
    }

    @Test
    void type_changed() {
        JsonMapper mapper = JsonMapper.builder().build();
        JsonNode base   =  mapper.readTree("{\"config\":{\"port\":8080}}");
        JsonNode ours   =  mapper.readTree("{\"config\":\"disabled\"}");
        JsonNode theirs   =  mapper.readTree("{\"config\":{\"port\":9090}}");

        MergeResult r = mergeEngine.mergeObject(JsonPointer.empty(), (ObjectNode) base, (ObjectNode) ours,(ObjectNode) theirs);

        assertThat(r).isInstanceOf(MergeResult.MergeConflicted.class);
        MergeResult.MergeConflicted p = ((MergeResult.MergeConflicted) r);
//        JsonNode partialMergedOutcome = p.partialMergedOutcome();
        System.out.println(p);
        assertThat(p.conflicts().getFirst().conflictType()).isEqualTo(ConflictType.TYPE_CHANGED);
    }

    @Test
    void array_to_object_type_changed_one_side_other_same() {
        JsonMapper mapper = JsonMapper.builder().build();
        JsonNode base   =  mapper.readTree("{\"config\":{\"list\":[1,2,3,4]}}");
        JsonNode ours   =  mapper.readTree("{\"config\":{\"list\":[1,2,3,4]}}");
        JsonNode theirs   =  mapper.readTree("{\"config\":{\"list\":9090}}");
        MergeResult r = mergeEngine.mergeObject(JsonPointer.empty(), (ObjectNode) base, (ObjectNode) ours,(ObjectNode) theirs);
        assertThat(r).isInstanceOf(MergeResult.MergeSuccess.class);
    }
    @Test
    void array_to_object_type_changed_one_side_other_value_changed() {
        JsonMapper mapper = JsonMapper.builder().build();
        JsonNode base   =  mapper.readTree("{\"config\":{\"list\":[1,2,3,4]}}");
        JsonNode ours   =  mapper.readTree("{\"config\":{\"list\":[1,2,3,4]}}");
        JsonNode theirs   =  mapper.readTree("{\"config\":{\"list\":null}}");
        MergeResult r = mergeEngine.mergeObject(JsonPointer.empty(), (ObjectNode) base, (ObjectNode) ours,(ObjectNode) theirs);
        assertThat(r).isInstanceOf(MergeResult.MergeSuccess.class);
    }
}
