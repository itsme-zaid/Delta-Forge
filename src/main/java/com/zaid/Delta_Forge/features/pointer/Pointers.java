package com.zaid.Delta_Forge.features.pointer;

import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;

public final class Pointers {
    private Pointers() {}

    /** Parse a pointer string; throws on malformed. */
    public static JsonPointer parse(String s) {
        return JsonPointer.compile(s);
    }

    /** The root pointer (empty string). */
    public static JsonPointer root() {
        return JsonPointer.empty();
    }

    /** Child pointer under an object key. */
    public static JsonPointer child(JsonPointer parent, String key) {
        // Jackson's appendWithPropertyName handles escaping correctly.
        return parent.appendProperty(key);
    }

    /** Child pointer under an array index. */
    public static JsonPointer child(JsonPointer parent, int index) {
        return parent.appendIndex(index);
    }

    /** Is this pointer the root? */
    public static boolean isRoot(JsonPointer p) {
        return p.matches();
    }

    /** Return the value at this pointer, or MissingNode if not present. */
    public static JsonNode get(JsonNode root, JsonPointer p) {
        return root.at(p);
    }

    /** Human-readable form for logs and errors (identical to toString). */
    public static String display(JsonPointer p) {
        return isRoot(p) ? "(root)" : p.toString();
    }
}
