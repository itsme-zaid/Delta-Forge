package com.zaid.Delta_Forge.UnitTesting;

import com.zaid.Delta_Forge.features.json.JsonDocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonDocumentServiceTest {
    private JsonMapper mapper;
    private JsonDocumentService service;

    @BeforeEach
    void setUp(){
        mapper = JsonMapper.builder().build();
        service = new JsonDocumentService(mapper);
    }

    @Test
    void structurallyEqual_treats_1_and_1_point_0_as_equal() throws Exception{
        JsonNode left = mapper.readTree("1");
        JsonNode right = mapper.readTree("1.00");
        assertThat(service.structurallyEqual(left,right)).isTrue();
    }
    @Test
    void structurallyEqual_returnsFalse_forDifferentNumbers() throws Exception {
        JsonNode left = mapper.readTree("1");
        JsonNode right = mapper.readTree("2.0");

        assertThat(service.structurallyEqual(left, right)).isFalse();
    }

    @Test
    void deepCopy_returnsIndependentCopy() throws Exception {
        ObjectNode original = (ObjectNode) mapper.readTree("{\"a\": 1}");
        ObjectNode copy = (ObjectNode) service.deepCopy(original);

        // mutate the copy
        copy.put("a", 999);

        // original should not change
        assertThat(original.get("a").asInt()).isEqualTo(1);
        assertThat(copy.get("a").asInt()).isEqualTo(999);
        assertThat(copy).isNotSameAs(original);
    }
}
