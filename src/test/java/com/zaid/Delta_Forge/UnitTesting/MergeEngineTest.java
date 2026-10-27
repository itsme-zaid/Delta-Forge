package com.zaid.Delta_Forge.UnitTesting;

import com.zaid.Delta_Forge.features.json.JsonDocumentService;
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
        ValueNode ours = FloatNode.valueOf(1.0f);
        ValueNode base = IntNode.valueOf(1);
        ValueNode theirs = IntNode.valueOf(1);
//        assertThat(mergeEngine.mergePrimitive(null,ours,theirs,base);
        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
        assertThat(mergeResult).isInstanceOf(MergeResult.MergeSuccess.class);
        MergeResult.MergeSuccess success =
                (MergeResult.MergeSuccess) mergeResult;
        assertThat(success.mergedOutcome()).isEqualTo(base).isEqualTo(theirs);
    }
    @Test
    public void ours_only_changed_emits_success(){
        ValueNode ours = StringNode.valueOf("string2");
        ValueNode base = StringNode.valueOf("string1");
        ValueNode theirs = StringNode.valueOf("string1");
        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
        assertThat(mergeResult).isInstanceOf(MergeResult.MergeSuccess.class);
        MergeResult.MergeSuccess success =
                (MergeResult.MergeSuccess) mergeResult;
        assertThat(success.mergedOutcome()).isEqualTo(ours);
    }
    @Test
    public void theirs_only_changed_emits_success(){
        ValueNode ours = StringNode.valueOf("string1");
        ValueNode base = StringNode.valueOf("string1");
        ValueNode theirs = StringNode.valueOf("string2");
//        assertThat(mergeEngine.mergePrimitive(null,ours,theirs,base);
        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
        assertThat(mergeResult).isInstanceOf(MergeResult.MergeSuccess.class);
        MergeResult.MergeSuccess success =
                (MergeResult.MergeSuccess) mergeResult;
        assertThat(success.mergedOutcome()).isEqualTo(theirs);
    }
    @Test
    public void both_changed_to_same_value_emits_success(){
        ValueNode ours = StringNode.valueOf("string2");
        ValueNode base = StringNode.valueOf("string1");
        ValueNode theirs = StringNode.valueOf("string2");
//        assertThat(mergeEngine.mergePrimitive(null,ours,theirs,base);
        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
        assertThat(mergeResult).isInstanceOf(MergeResult.MergeSuccess.class);
        MergeResult.MergeSuccess success =
                (MergeResult.MergeSuccess) mergeResult;
        assertThat(success.mergedOutcome()).isEqualTo(ours);
    }
    @Test
    public void base_value_deleted_theirs_unchanged(){
        ValueNode ours = StringNode.valueOf("string2");
        ValueNode base = StringNode.valueOf("");
        ValueNode theirs = StringNode.valueOf("string2");
//        assertThat(mergeEngine.mergePrimitive(null,ours,theirs,base);
        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
        assertThat(mergeResult).isInstanceOf(MergeResult.MergeSuccess.class);
        MergeResult.MergeSuccess success =
                (MergeResult.MergeSuccess) mergeResult;
        assertThat(success.mergedOutcome()).isEqualTo(ours);
    }

    @Test
    public void type_changed_both_sides_same(){
        ValueNode ours = IntNode.valueOf(100);
        ValueNode base = StringNode.valueOf("100");
        ValueNode theirs = IntNode.valueOf(100);
//        assertThat(mergeEngine.mergePrimitive(null,ours,theirs,base);
        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
        assertThat(mergeResult).isInstanceOf(MergeResult.MergeSuccess.class);
        MergeResult.MergeSuccess success =
                (MergeResult.MergeSuccess) mergeResult;
        assertThat(success.mergedOutcome()).isEqualTo(ours);
    }

    @Test
    public void one_side_set_to_null_from_int_node(){
        ValueNode ours = IntNode.valueOf(100);
        ValueNode base = IntNode.valueOf(100);
        ValueNode theirs = NullNode.getInstance();
//        assertThat(mergeEngine.mergePrimitive(null,ours,theirs,base);
        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
        assertThat(mergeResult).isInstanceOf(MergeResult.MergeSuccess.class);
        MergeResult.MergeSuccess success =
                (MergeResult.MergeSuccess) mergeResult;
        assertThat(success.mergedOutcome()).isInstanceOf(NullNode.class);
    }

    @Test
    public void both_side_set_to_null_from_int_node(){
        ValueNode ours = NullNode.getInstance();
        ValueNode base = IntNode.valueOf(100);
        ValueNode theirs = NullNode.getInstance();
        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
        assertThat(mergeResult).isInstanceOf(MergeResult.MergeSuccess.class);
        MergeResult.MergeSuccess success =
                (MergeResult.MergeSuccess) mergeResult;
        assertThat(success.mergedOutcome()).isInstanceOf(NullNode.class);
    }


    // now conflicting test cases here:
    @Test
    public void both_modified_ours_theirs(){
        ValueNode ours = IntNode.valueOf(1001);
        ValueNode base = IntNode.valueOf(100);
        ValueNode theirs = IntNode.valueOf(1002);
        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
        assertThat(mergeResult).isInstanceOf(MergeResult.MergeWithConflicts.class);
        MergeResult.MergeWithConflicts conflict =
                (MergeResult.MergeWithConflicts) mergeResult;
        assertThat(conflict.mergeConflict().conflictType()).isEqualTo(ConflictType.BOTH_MODIFIED);
    }
    @Test
    public void ours_type_changed_int_to_boolean(){
        ValueNode base = IntNode.valueOf(100);
        ValueNode ours = BooleanNode.valueOf(false);
        ValueNode theirs = IntNode.valueOf(1002);
        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
        assertThat(mergeResult).isInstanceOf(MergeResult.MergeWithConflicts.class);
        MergeResult.MergeWithConflicts conflict =
                (MergeResult.MergeWithConflicts) mergeResult;
        assertThat(conflict.mergeConflict().conflictType()).isEqualTo(ConflictType.TYPE_CHANGED);
    }
    @Test
    public void type_changed_with_same_value_int_to_string(){
        ValueNode base = IntNode.valueOf(100);
        ValueNode ours = StringNode.valueOf("100");
        ValueNode theirs = IntNode.valueOf(1002);
        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
        assertThat(mergeResult).isInstanceOf(MergeResult.MergeWithConflicts.class);
        MergeResult.MergeWithConflicts conflict =
                (MergeResult.MergeWithConflicts) mergeResult;
        assertThat(conflict.mergeConflict().conflictType()).isEqualTo(ConflictType.TYPE_CHANGED);
    }
    @Test
    public void value_and_type_both_changed_emits_TYPE_CHANGED(){
        ValueNode base = IntNode.valueOf(100);
        ValueNode ours = StringNode.valueOf("1001");
        ValueNode theirs = IntNode.valueOf(1002);
        MergeResult mergeResult = mergeEngine.mergePrimitive(null,ours,theirs,base);
        assertThat(mergeResult).isInstanceOf(MergeResult.MergeWithConflicts.class);
        MergeResult.MergeWithConflicts conflict =
                (MergeResult.MergeWithConflicts) mergeResult;
        assertThat(conflict.mergeConflict().conflictType()).isEqualTo(ConflictType.TYPE_CHANGED);
    }
}
