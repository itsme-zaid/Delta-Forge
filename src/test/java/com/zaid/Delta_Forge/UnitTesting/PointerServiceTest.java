package com.zaid.Delta_Forge.UnitTesting;

import com.zaid.Delta_Forge.features.json.JsonDocumentService;
import com.zaid.Delta_Forge.features.pointer.PointerService;
import com.zaid.Delta_Forge.features.pointer.Pointers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.IntNode;
import tools.jackson.databind.node.StringNode;

import static org.assertj.core.api.Assertions.assertThat;

public class PointerServiceTest {
    private PointerService service = new PointerService();
    private JsonMapper mapper;
    private JsonDocumentService jsonDocumentService;

    @BeforeEach
    void setUp(){
        mapper = JsonMapper.builder().build();
        jsonDocumentService = new JsonDocumentService(mapper);
    }

    @Test
    void set_existing_object_key(){
        JsonNode doc = jsonDocumentService.parse("{\"a\":1}");
        service.setAt(doc, Pointers.parse("/a"), new IntNode(2));
        assertThat(doc.at(Pointers.parse("/a")).asInt()).isEqualTo(2);
    }

    @Test
    void setAt_creates_missing_parents(){
        JsonNode doc = jsonDocumentService.parse("{}");
        service.setAt(doc,Pointers.parse("/service/database/host"), StringNode.valueOf("sam"));

        assertThat(doc.at(Pointers.parse("/service/database/host")).toString()).isEqualTo("sam");
    }

    @Test
    void setAt_on_array_elements(){
        JsonNode arr = jsonDocumentService.parse("{\"items\":[\"x\",\"y\"]}");
        service.setAt(arr, Pointers.parse("/items/1"), StringNode.valueOf("z"));
        assertThat(arr.at(Pointers.parse("/items/1")).asText()).isEqualTo("z");

    }
}
