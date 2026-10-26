package com.zaid.Delta_Forge.features.json;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Comparator;


@Service
public class JsonDocumentService {

    private final JsonMapper mapper;

    public JsonDocumentService(JsonMapper mapper){
        this.mapper = mapper;
    }

    public JsonNode parse(String json) {
        try {
            return mapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("Invalid JSON: " + e.getMessage(), e);
        }
    }
    // to make sure that 1 is equal to 1.0 ;;;
    private static final Comparator<JsonNode> NUMERIC_AWARE =
            (a, b) -> {

                if (a.isNumber() && b.isNumber()) {
                    return a.decimalValue().compareTo(b.decimalValue());
                }
                return a.equals(b) ? 0 : -1;
    };

    public boolean structurallyEqual(JsonNode left, JsonNode right) {
        return left.equals(NUMERIC_AWARE, right);
    }
    public JsonNode deepCopy(JsonNode node){
        return node.deepCopy();
    }
    public String serialize(JsonNode node) {
        try {
            return mapper.writeValueAsString(node);
        } catch (JacksonException e) {
            throw new IllegalStateException("Unserializable node", e);
        }
    }
}
