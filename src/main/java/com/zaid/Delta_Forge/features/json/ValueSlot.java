package com.zaid.Delta_Forge.features.json;


import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.MissingNode;

import java.util.Objects;

public sealed interface ValueSlot
        permits ValueSlot.Missing, ValueSlot.Present {

    record Missing() implements ValueSlot {
        public static final Missing INSTANCE = new Missing();
    }

    /** The path exists; here is its value. */
    record Present(JsonNode node) implements ValueSlot {
        public Present {
            Objects.requireNonNull(node, "Present must wrap a non-null JsonNode");
            if (node.isMissingNode()) {
                throw new IllegalArgumentException(
                        "Present cannot wrap MissingNode; use Missing.INSTANCE");
            }
        }
    }
    static ValueSlot of(JsonNode node) {
        if (node == null || node.isMissingNode()) return Missing.INSTANCE;
        return new Present(node);
    }

    static ValueSlot missing() {
        return Missing.INSTANCE;
    }

    static ValueSlot present(JsonNode node) {
        return new Present(node);
    }

    // ---- convenience queries ----

    default boolean isMissing() {
        return this instanceof Missing;
    }

    default boolean isPresent() {
        return this instanceof Present;
    }

    /** Returns the wrapped node if present, or MissingNode if absent. */
    default JsonNode toNodeOrMissing() {
        return switch (this) {
            case Missing() -> MissingNode.getInstance();
            case Present(JsonNode n) -> n;
        };
    }
}