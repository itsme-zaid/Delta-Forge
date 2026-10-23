package com.zaid.Delta_Forge.features.json;


import tools.jackson.databind.JsonNode;

public sealed interface ValueSlot
        permits ValueSlot.Missing, ValueSlot.Present {

    record Missing() implements ValueSlot {}

    record Present(JsonNode value) implements ValueSlot {
        public Present {
            if (value == null) {
                throw new IllegalArgumentException(
                        "Present cannot contain Java null"
                );
            }
        }
    }
}