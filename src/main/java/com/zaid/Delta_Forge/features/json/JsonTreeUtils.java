package com.zaid.Delta_Forge.features.json;

import tools.jackson.databind.JsonNode;

public final class JsonTreeUtils {

    private JsonTreeUtils() {}

    public static ValueSlot getSlot(JsonNode object, String key) {
        JsonNode value = object.get(key);

        if (value == null) {
            return new ValueSlot.Missing();
        }

        return new ValueSlot.Present(value);
    }
}
