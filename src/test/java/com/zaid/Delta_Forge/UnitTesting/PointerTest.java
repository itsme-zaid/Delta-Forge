package com.zaid.Delta_Forge.UnitTesting;

import com.zaid.Delta_Forge.features.json.JsonDocumentService;
import com.zaid.Delta_Forge.features.pointer.Pointers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PointerTest {
    private JsonMapper mapper;
    private JsonDocumentService service;

    @BeforeEach
    void setUp(){
        mapper = JsonMapper.builder().build();
        service = new JsonDocumentService(mapper);
    }
    @Test
    void root_is_empty_string() {
        JsonPointer root = Pointers.root();
        assertThat(root.toString()).isEmpty();
        assertThat(Pointers.isRoot(root)).isTrue();
        assertThat(root.matches()).isTrue();
    }
    @Test
    void parses_simple_key() {
        JsonPointer p = Pointers.parse("/foo");
        assertThat(Pointers.get(service.parse("{\"foo\":42}"), p).asInt()).isEqualTo(42);
    }
    @Test
    void parses_nested_keys() {
        JsonPointer p = Pointers.parse("/service/database/host");
        JsonNode doc = service.parse("{\"service\":{\"database\":{\"host\":\"db\"}}}");
        assertThat(Pointers.get(doc, p).asText()).isEqualTo("db");
    }
    @Test
    void parses_array_index() {
        JsonPointer p = Pointers.parse("/items/1");
        JsonNode doc = service.parse("{\"items\":[\"a\",\"b\",\"c\"]}");
        assertThat(Pointers.get(doc, p).asString()).isEqualTo("b");
    }
    @Test
    void array_index_must_not_have_leading_zeros() {
        // "/items/01" is not a valid array index; Jackson may parse it
        // as a property name. The important thing is that it does NOT match
        // the array element at index 1.
        JsonNode doc = service.parse("{\"items\":[\"a\",\"b\",\"c\"]}");
        JsonNode result = doc.at(Pointers.parse("/items/01"));
        assertThat(result.isMissingNode()).isTrue();
    }
    @Test
    void slash_in_key_is_escaped_as_tilde_one() {
        // key "a/b" → pointer "/a~1b"
        JsonPointer p = Pointers.parse("/a~1b");
        JsonNode doc = service.parse("{\"a/b\":1,\"a\":{\"b\":2}}");
        assertThat(Pointers.get(doc, p).asInt()).isEqualTo(1);   // the a/b key
        assertThat(Pointers.get(doc, Pointers.parse("/a/b")).asInt()).isEqualTo(2);  // the nested path
    }

    @Test
    void child_slash_in_key_is_escaped() {
        JsonPointer p = Pointers.child(Pointers.root(), "a/b");
        assertThat(p.toString()).isEqualTo("/a~1b");
    }

    @Test
    void tilde_in_key_is_escaped_as_tilde_zero() {
        JsonPointer p = Pointers.parse("/m~0n");
        JsonNode doc = service.parse("{\"m~n\":8}");
        assertThat(Pointers.get(doc, p).asInt()).isEqualTo(8);
    }

    @Test
    void child_tilde_in_key_is_escaped() {
        JsonPointer p = Pointers.child(Pointers.root(), "m~n");
        assertThat(p.toString()).isEqualTo("/m~0n");
    }
    @Test
    void escaping_encodes_tilde_before_slash() {
        // Key "~1" — a tilde followed by a one.
        // Encoded: "~0" + "1" = "~01" (NOT "~10", NOT "~1", NOT "~0~1")
        JsonPointer p = Pointers.child(Pointers.root(), "~1");
        assertThat(p.toString()).isEqualTo("/~01");
    }

    @Test
    void unescape_decodes_tilde_one_before_tilde_zero() {
        // Pointer "/~01" must decode to "~1", NOT "/"
        JsonPointer p = Pointers.parse("/~01");
        assertThat(p.getMatchingProperty()).isEqualTo("~1");
    }
    @Test
    void empty_key_is_a_single_empty_segment() {
        // {"":42} — key is the empty string
        JsonPointer p = Pointers.parse("/");
        JsonNode doc = service.parse("{\"\":42}");
        assertThat(Pointers.get(doc, p).asInt()).isEqualTo(42);

        // And child(root, "") produces "/" not ""
        assertThat(Pointers.child(Pointers.root(), "").toString()).isEqualTo("/");
    }
    @Test
    void space_and_unicode_keys_work() {
        JsonPointer p = Pointers.parse("/with spaces/日本語");
        JsonNode doc = service.parse("{\"with spaces\":{\"日本語\":\"こんにちは\"}}");
        assertThat(Pointers.get(doc, p).asString()).isEqualTo("こんにちは");
    }
    @Test
    void malformed_pointer_throws_at_parse() {
        assertThatThrownBy(() -> Pointers.parse("foo"))       // no leading /
                .isInstanceOf(IllegalArgumentException.class);
        // Some Jackson versions accept this; check your version's behavior.
        // If it doesn't throw, your `Pointers.parse` should add a validation.
    }
    @Test
    void root_array_element_accessible() {
        JsonNode doc = service.parse("[10,20,30]");
        assertThat(Pointers.get(doc, Pointers.parse("/0")).asInt()).isEqualTo(10);
        assertThat(Pointers.get(doc, Pointers.parse("/2")).asInt()).isEqualTo(30);
    }
    @Test
    void escaped_slash_does_not_match_nested_object() {
        // A user who has {"a/b": 1} and queries pointer "/a/b" must NOT
        // get the value at {"a":{"b":2}} — the pointer "/a/b" means
        // "key a, then key b", which is a DIFFERENT path from "key a/b".
        JsonNode doc = service.parse("{\"a/b\":1,\"a\":{\"b\":2}}");

        assertThat(Pointers.get(doc, Pointers.parse("/a~1b")).asInt()).isEqualTo(1);
        assertThat(Pointers.get(doc, Pointers.parse("/a/b")).asInt()).isEqualTo(2);

        // And /a/b must not accidentally resolve to /a~1b
        // (this is the "phantom path" bug if you implement split("/") naively)
    }
}
