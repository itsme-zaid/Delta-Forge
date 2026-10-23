package com.zaid.Delta_Forge.features.json;

import tools.jackson.databind.JsonNode;

public final class JsonEquality {

    private JsonEquality() {}

    public static boolean areEqual(JsonNode left, JsonNode right) {
        return left.equals(right);
    }
}