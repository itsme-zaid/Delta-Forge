package com.zaid.Delta_Forge.UnitTesting;

import com.zaid.Delta_Forge.features.json.JsonDocumentService;
import com.zaid.Delta_Forge.features.json.ValueSlot;
import com.zaid.Delta_Forge.features.merge.ConflictType;
import com.zaid.Delta_Forge.features.merge.MergeConflict;
import com.zaid.Delta_Forge.features.merge.MergeEngine;
import com.zaid.Delta_Forge.features.merge.MergeResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
    @Test
    public void both_modified_ours_theirs(){
        ValueSlot ours = ValueSlot.present(IntNode.valueOf(1001));
        ValueSlot base = ValueSlot.present(IntNode.valueOf(100));
        ValueSlot theirs = ValueSlot.present(IntNode.valueOf(1002));
        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
        assertThat(mergeResult).isInstanceOf(MergeResult.MergeWithConflicts.class);
        MergeResult.MergeWithConflicts conflict =
                (MergeResult.MergeWithConflicts) mergeResult;
        assertThat(conflict.mergeConflict().conflictType()).isEqualTo(ConflictType.BOTH_MODIFIED);
    }
    @Test
    public void ours_type_changed_int_to_boolean(){
        ValueSlot base = ValueSlot.present(IntNode.valueOf(100));
        ValueSlot ours = ValueSlot.present(BooleanNode.valueOf(false));
        ValueSlot theirs = ValueSlot.present(IntNode.valueOf(1002));
        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
        assertThat(mergeResult).isInstanceOf(MergeResult.MergeWithConflicts.class);
        MergeResult.MergeWithConflicts conflict =
                (MergeResult.MergeWithConflicts) mergeResult;
        assertThat(conflict.mergeConflict().conflictType()).isEqualTo(ConflictType.TYPE_CHANGED);
    }
    @Test
    public void type_changed_with_same_value_int_to_string(){
        ValueSlot base = ValueSlot.present(IntNode.valueOf(100));
        ValueSlot ours = ValueSlot.present(StringNode.valueOf("100"));
        ValueSlot theirs = ValueSlot.present(IntNode.valueOf(1002));
        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
        assertThat(mergeResult).isInstanceOf(MergeResult.MergeWithConflicts.class);
        MergeResult.MergeWithConflicts conflict =
                (MergeResult.MergeWithConflicts) mergeResult;
        assertThat(conflict.mergeConflict().conflictType()).isEqualTo(ConflictType.TYPE_CHANGED);
    }
    @Test
    public void value_and_type_both_changed_emits_TYPE_CHANGED(){
        ValueSlot base = ValueSlot.present(IntNode.valueOf(100));
        ValueSlot ours = ValueSlot.present(StringNode.valueOf("1001"));
        ValueSlot theirs = ValueSlot.present(IntNode.valueOf(1002));
        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
        assertThat(mergeResult).isInstanceOf(MergeResult.MergeWithConflicts.class);
        MergeResult.MergeWithConflicts conflict =
                (MergeResult.MergeWithConflicts) mergeResult;
        assertThat(conflict.mergeConflict().conflictType()).isEqualTo(ConflictType.TYPE_CHANGED);
    }
}
