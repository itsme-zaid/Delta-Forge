package com.zaid.Delta_Forge.features.json;


import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.MissingNode;

import java.util.Objects;

public sealed interface ValueSlot permits ValueSlot.Missing, ValueSlot.Present {

    record Missing() implements ValueSlot {}

    record Present(JsonNode node) implements ValueSlot {
        public Present {
            Objects.requireNonNull(node, "Present must wrap a non-null JsonNode");
            if (node.isMissingNode()) {
                throw new IllegalArgumentException("Present cannot wrap MissingNode");
            }
        }
    }

    /** The singleton missing value. */
    ValueSlot MISSING = new Missing();

    static ValueSlot missing() {
        return MISSING;
    }

    static ValueSlot present(JsonNode node) {
        return new Present(node);
    }

    static ValueSlot of(JsonNode node) {
        if (node == null || node.isMissingNode()) return MISSING;
        return new Present(node);
    }
}