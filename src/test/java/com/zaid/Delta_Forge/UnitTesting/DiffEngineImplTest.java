package com.zaid.Delta_Forge.UnitTesting;

import com.zaid.Delta_Forge.features.diff.DiffEngineImpl;
import com.zaid.Delta_Forge.features.diff.DiffOperation;
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
    public void different_scalar_value(){
        List<DiffOperation> ops = diffEngine.diff(service.parse("{\"port\": 8080}"), service.parse("{\"port\": 9090}"));
        for(DiffOperation d : ops){
            System.out.println(d.diffOperationType() + " " + d.path().toString() + " " + d.oldVal() + " -> " + d.newVal());
        }
        assertThat(ops).hasSize(1);
        DiffOperation op = ops.getFirst();
        //assertThat(op.path().toString()).isEqualTo("/port");
        //assertThat(((ValueSlot.Present) op.oldVal()).node().asInt()).isEqualTo(8080);
        //assertThat(((ValueSlot.Present) op.newVal()).node().asInt()).isEqualTo(9090);
    }

}
