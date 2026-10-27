package com.zaid.Delta_Forge.UnitTesting;

import com.zaid.Delta_Forge.features.diff.DiffEngineImpl;
import com.zaid.Delta_Forge.features.diff.DiffFormatter;
import com.zaid.Delta_Forge.features.diff.DiffOperation;
import com.zaid.Delta_Forge.features.json.JsonDocumentService;
import com.zaid.Delta_Forge.features.json.ValueSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.BooleanNode;
import tools.jackson.databind.node.IntNode;
import tools.jackson.databind.node.StringNode;

import java.util.List;

import static com.zaid.Delta_Forge.features.diff.DiffOperationType.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DiffFormatterTest {
    private DiffFormatter formatter;
    private JsonDocumentService service;

    private DiffEngineImpl diffEngine;
    @BeforeEach
    public void setup(){
        JsonMapper mapper = JsonMapper.builder().build();
        service = new JsonDocumentService(mapper);
        formatter = new DiffFormatter(service);
        diffEngine = new DiffEngineImpl(service);
    }
    @Test
    public void check_(){
        List<DiffOperation> ops = diffEngine.diff(
                service.parse("{\"service\":{\"database\":{\"host\":\"db1\",\"host11\":\"db1\"}}}"),
                service.parse("{\"service\":{\"database\":{\"host11\":\"db1\",\"host\":\"db1\"}}}"));
        System.out.println(formatter.formatText(ops));
        assertThat(formatter.formatText(ops)).isNotEmpty();
    }

    @Test
    void empty_operations_renders_no_changes() {
        assertThat(formatter.formatText(List.of())).isEqualTo("(no changes)");
    }
    @Test
    void add_renders_plus_prefix() {
        var ops = List.of(new DiffOperation(ADD, JsonPointer.compile("/host"),
                ValueSlot.MISSING, ValueSlot.present(StringNode.valueOf("localhost"))));
        assertThat(formatter.formatText(ops)).isEqualTo("+ /host = \"localhost\"");
    }
    @Test
    void remove_renders_minus_prefix() {
        var ops = List.of(new DiffOperation(REMOVE, JsonPointer.compile("/debug"),
                ValueSlot.present(BooleanNode.valueOf(true)),ValueSlot.missing()));
        assertThat(formatter.formatText(ops)).isEqualTo("- /debug = true");
    }

    @Test
    void replace_renders_tilde_prefix_with_arrow() {
        var ops = List.of(new DiffOperation(REPLACE, JsonPointer.compile("/port"),
                ValueSlot.present(IntNode.valueOf(8080)), ValueSlot.present((IntNode.valueOf(9090)))));
        assertThat(formatter.formatText(ops)).isEqualTo("~ /port: 8080 -> 9090");
    }
    @Test
    void root_path_renders_as_root_label() {
        var ops = List.of(new DiffOperation(REPLACE, JsonPointer.empty(),
                ValueSlot.present(IntNode.valueOf(1)), ValueSlot.present((IntNode.valueOf(2)))));
        assertThat(formatter.formatText(ops)).contains("(root)");
    }

//    @Test.present(  void missing_renders_as_missing_label() {
//        var ops = List.of(new DiffOperation(ADD, JsonPointer.compile("/x"),
//                Missing.INSTANCE, ValueSlot.present(StringNode.valueOf("v"))));
//        assertThat(formatter.formatText(ops)).doesNotContain("(missing)");
//        // newValue is present; (missing) only appears for oldValue on ADD
//    }

    @Test
    void json_format_omits_absent_slots() {
        var ops = List.of(new DiffOperation(ADD, JsonPointer.compile("/x"),
                ValueSlot.missing(), ValueSlot.present(IntNode.valueOf(1))));
        String json = formatter.formatJson(ops);
        assertThat(json).doesNotContain("oldValue");
        assertThat(json).contains("\"newValue\":1");
    }
}
