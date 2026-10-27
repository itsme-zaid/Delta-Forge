package com.zaid.Delta_Forge.UnitTesting;

import com.zaid.Delta_Forge.features.diff.DiffEngineImpl;
import com.zaid.Delta_Forge.features.diff.DiffOperation;
import com.zaid.Delta_Forge.features.diff.DiffOperationType;
import com.zaid.Delta_Forge.features.json.JsonDocumentService;
import com.zaid.Delta_Forge.features.json.ValueSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DiffEngineImplTest {
    private DiffEngineImpl diffEngine;
    private JsonDocumentService service;

    @BeforeEach
    void setUp() {
        JsonMapper mapper = JsonMapper.builder().build();
        service = new JsonDocumentService(mapper);
        diffEngine = new DiffEngineImpl(service);
    }
    @Test
    public void debug_test(){
//        List<DiffOperation> ops = diffEngine.diff(
//                service.parse("{\"service\":{\"database\":{\"host\":\"db1\"}}}"),
//                service.parse("{\"service\":{\"databasse\":{\"host\":\"db2\"}}}"));
//        for(DiffOperation d : ops){
//            System.out.println(d.diffOperationType() + " " + d.path().toString() + " " + d.oldVal() + " -> " + d.newVal());
//        }
////        assertThat(ops).hasSize(1);
//        DiffOperation op = ops.getFirst();
        //assertThat(op.path().toString()).isEqualTo("/port");
        //assertThat(((ValueSlot.Present) op.oldVal()).node().asInt()).isEqualTo(8080);
        //assertThat(((ValueSlot.Present) op.newVal()).node().asInt()).isEqualTo(9090);
    }
    @Test
    void identical_objects_produce_no_operations() {
        List<DiffOperation> ops = diffEngine.diff(
                service.parse("{\"a\":1,\"b\":2}"),
                service.parse("{\"a\":1,\"b\":2}"));
        assertThat(ops).isEmpty();
    }
    @Test
    void reordered_keys_produce_no_operations() {
        // D1: key order is not meaningful
        List<DiffOperation> ops = diffEngine.diff(
                service.parse("{\"a\":1,\"b\":2}"),
                service.parse("{\"b\":2,\"a\":1}"));
        assertThat(ops).isEmpty();
    }
    @Test
    void key_only_in_target_emits_add() {
        List<DiffOperation> ops = diffEngine.diff(
                service.parse("{\"a\":1}"),
                service.parse("{\"a\":1,\"b\":2}"));

        assertThat(ops).hasSize(1);
        DiffOperation op = ops.get(0);
        assertThat(op.diffOperationType()).isEqualTo(DiffOperationType.ADD);
        assertThat(op.path().toString()).isEqualTo("/b");
        assertThat(op.oldVal()).isInstanceOf(ValueSlot.Missing.class);
        assertThat(((ValueSlot.Present) op.newVal()).node().asInt()).isEqualTo(2);
    }

    @Test
    void key_only_in_base_emits_add() {
        List<DiffOperation> ops = diffEngine.diff(
                service.parse("{\"a\":1,\"b\":2}"),
                service.parse("{\"a\":1}"));

        assertThat(ops).hasSize(1);
        DiffOperation op = ops.get(0);
        assertThat(op.diffOperationType()).isEqualTo(DiffOperationType.REMOVE);
        assertThat(op.path().toString()).isEqualTo("/b");
        assertThat(op.newVal()).isInstanceOf(ValueSlot.Missing.class);
    }
    @Test
    void changed_value_emits_single_replace_at_key_path() {
        List<DiffOperation> ops = diffEngine.diff(
                service.parse("{\"a\":1,\"b\":2}"),
                service.parse("{\"a\":10,\"b\":2}"));

        assertThat(ops).hasSize(1);
        assertThat(ops.getFirst().path().toString()).isEqualTo("/a");
        assertThat(ops.getFirst().diffOperationType()).isEqualTo(DiffOperationType.REPLACE);
    }
    // multiple keys but in same sorted order
    @Test
    void multiple_changes_are_sorted_by_path() {
        List<DiffOperation> ops = diffEngine.diff(
                service.parse("{\"z\":1,\"a\":1,\"m\":1}"),
                service.parse("{\"z\":2,\"a\":2,\"m\":2}"));

        assertThat(ops).extracting(op -> op.path().toString())
                .containsExactly("/a", "/m", "/z");
    }

    @Test
    void nested_object_changes_use_full_path() {
        List<DiffOperation> ops = diffEngine.diff(
                service.parse("{\"service\":{\"database\":{\"host\":\"db1\"}}}"),
                service.parse("{\"service\":{\"database\":{\"host\":\"db2\"}}}"));

        assertThat(ops).hasSize(1);
        assertThat(ops.getFirst().path().toString()).isEqualTo("/service/database/host");
    }
    @Test
    void conflict_free_sibling_survives_alongside_changed_sibling() {
        List<DiffOperation> ops = diffEngine.diff(
                service.parse("{\"a\":1,\"b\":2}"),
                service.parse("{\"a\":10,\"b\":2}"));

        // /a changed; /b untouched. Only /a appears.
        assertThat(ops).hasSize(1);
        assertThat(ops.get(0).path().toString()).isEqualTo("/a");
    }

    // add and replace both in the same object
    @Test
    void add_and_replace_in_same_object() {
        List<DiffOperation> ops = diffEngine.diff(
                service.parse("{\"port\":8080}"),
                service.parse("{\"port\":9090,\"host\":\"localhost\"}"));

        assertThat(ops).hasSize(2);
        assertThat(ops).extracting(op -> op.path().toString())
                .containsExactly("/host", "/port");
        assertThat(ops.get(0).diffOperationType()).isEqualTo(DiffOperationType.ADD);      // /host
        assertThat(ops.get(1).diffOperationType()).isEqualTo(DiffOperationType.REPLACE);  // /port
    }
    // first type change : object to scalar
    @Test
    void object_replaced_by_scalar_emits_one_replace() {
        List<DiffOperation> ops = diffEngine.diff(
                service.parse("{\"config\":{\"port\":8080}}"),
                service.parse("{\"config\":\"disabled\"}"));

        assertThat(ops).hasSize(1);
        DiffOperation op = ops.get(0);
        assertThat(op.diffOperationType()).isEqualTo(DiffOperationType.REPLACE);
        assertThat(op.path().toString()).isEqualTo("/config");
        assertThat(((ValueSlot.Present) op.oldVal()).node().isObject()).isTrue();
        assertThat(((ValueSlot.Present) op.newVal()).node().asString()).isEqualTo("disabled");
    }
    @Test
    void scalar_replaced_by_object_emits_one_replace() {
        List<DiffOperation> ops = diffEngine.diff(
                service.parse("{\"config\":\"disabled\"}"),
                service.parse("{\"config\":{\"port\":8080}}"));

        assertThat(ops).hasSize(1);
        assertThat(ops.getFirst().diffOperationType()).isEqualTo(DiffOperationType.REPLACE);
        assertThat(ops.getFirst().path().toString()).isEqualTo("/config");
    }
    @Test
    void differing_array_emits_one_replace() {
        List<DiffOperation> ops = diffEngine.diff(
                service.parse("{\"items\":[1,2,3]}"),
                service.parse("{\"items\":[1,2,4]}"));

        assertThat(ops).hasSize(1);
        assertThat(ops.get(0).diffOperationType()).isEqualTo(DiffOperationType.REPLACE);
        assertThat(ops.get(0).path().toString()).isEqualTo("/items");
    }

    @Test
    void same_array_emits_nothing(){
        List<DiffOperation> ops = diffEngine.diff(
                service.parse("{\"items\":[1,2,3]}"),
                service.parse("{\"items\":[1,2,3]}")
        );
        assertThat(ops).hasSize(0);
    }

    @Test
    void same_inputs_produce_same_output_twice() {
        JsonNode base = service.parse("{\"b\":1,\"a\":2,\"c\":3}");
        JsonNode target = service.parse("{\"b\":10,\"a\":20,\"c\":30}");

        assertThat(diffEngine.diff(base, target))
                .isEqualTo(diffEngine.diff(base, target));
    }

    // ---------- Immutability ----------

    @Test
    void diff_does_not_mutate_inputs() {
        JsonNode base = service.parse("{\"a\":1}");
        JsonNode target = service.parse("{\"a\":2,\"b\":3}");
        String b = base.toString();
        String t = target.toString();

        diffEngine.diff(base, target);

        assertThat(base.toString()).isEqualTo(b);
        assertThat(target.toString()).isEqualTo(t);
    }

    //day 12: array of object behavior
    @Test
    void array_of_object_same_size(){
        JsonNode base = service.parse("{\"user\":[{\"id\":1}]}");
        JsonNode target = service.parse("{\"user\":[{\"id\":11}]}");
        List<DiffOperation> ops = diffEngine.diff(base,target);
        assertThat(ops).hasSize(1);
        assertThat(ops.getFirst().diffOperationType()).isEqualTo(DiffOperationType.REPLACE);
    }
    @Test
    void array_of_object_array_key_element_added(){
        JsonNode base = service.parse("{\"user\":[{\"id\":1}]}");
        JsonNode target = service.parse("{\"user\":[{\"id\":11},{\"id\":121}]}");
        List<DiffOperation> ops = diffEngine.diff(base,target);
        assertThat(ops).hasSize(1);
        assertThat(ops.getFirst().diffOperationType()).isEqualTo(DiffOperationType.REPLACE);
    }
    @Test
    void array_of_object_array_key_element_removed(){
        JsonNode base = service.parse("{\"user\":[{\"id\":11},{\"id\":121}]}");
        JsonNode target = service.parse("{\"user\":[{\"id\":1}]}");
        List<DiffOperation> ops = diffEngine.diff(base,target);
        assertThat(ops).hasSize(1);
        assertThat(ops.getFirst().diffOperationType()).isEqualTo(DiffOperationType.REPLACE);
    }
    @Test
    void array_of_object_array_order_changed(){
        JsonNode base = service.parse("{\"user\":[{\"id\":11},{\"id\":121}]}");
        JsonNode target = service.parse("{\"user\":[{\"id\":121},{\"id\":11}]}");
        List<DiffOperation> ops = diffEngine.diff(base,target);
        assertThat(ops).hasSize(1);
        assertThat(ops.getFirst().diffOperationType()).isEqualTo(DiffOperationType.REPLACE);
    }
}
