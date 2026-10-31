package com.zaid.Delta_Forge.property_testing;

import com.zaid.Delta_Forge.features.diff.DiffEngine;
import com.zaid.Delta_Forge.features.diff.DiffEngineImpl;
import com.zaid.Delta_Forge.features.diff.DiffOperation;
import com.zaid.Delta_Forge.features.json.JsonDocumentService;
import com.zaid.Delta_Forge.features.json.ValueSlot;
import com.zaid.Delta_Forge.features.merge.MergeEngine;
import com.zaid.Delta_Forge.features.merge.MergeResult;
import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeProperty;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MergeInvariantsTest {
    JsonDocumentService service;
    MergeEngine engine;
    DiffEngine diffEngine;
    @BeforeProperty
    public void set_up(){
        this.service = new JsonDocumentService(JsonMapper.builder().build());
        this.engine = new MergeEngine(service);
        this.diffEngine = new DiffEngineImpl(service);
    }


    private Arbitrary<ObjectNode> wrapInObject(Arbitrary<ObjectNode> inner) {
        return inner.map(child -> {
            ObjectNode obj = JsonNodeFactory.instance.objectNode();
            obj.set("k", child);
            return obj;
        });
    }
    @Provide
    Arbitrary<ObjectNode> documents() {
        return Arbitraries.recursive(
                () -> Arbitraries.just(JsonNodeFactory.instance.objectNode()),  // base: {}
                this::wrapInObject,
                1, 4
        );
    }
    @Property
    void only_theirs_changed_returns_theirs(@ForAll("documents") ObjectNode base,
                                            @ForAll("documents") ObjectNode theirs) {
        // precondition: ours == base (only theirs changed)
        MergeResult r = engine.mergeObject(JsonPointer.compile(""), base, base, theirs);
        assertThat(((MergeResult.MergeSuccess) r).mergedOutcome())
                .isEqualTo(ValueSlot.present(theirs));
    }

    @Property
    void identical_docs_returns_the_same_doc(@ForAll("documents") ObjectNode base,
                                            @ForAll("documents") ObjectNode theirs) {
        // precondition: ours == base (only theirs changed)
        MergeResult r = engine.mergeObject(JsonPointer.compile(""), base, base, base);
        assertThat(((MergeResult.MergeSuccess) r).mergedOutcome())
                .isEqualTo(ValueSlot.present(base));
    }
    @Property
    void only_base_changed_returns_base(@ForAll("documents") ObjectNode base,
                                            @ForAll("documents") ObjectNode theirs) {
        // precondition: ours == base (only theirs changed)
        MergeResult r = engine.mergeObject(JsonPointer.compile(""), theirs, base, base);
        assertThat(((MergeResult.MergeSuccess) r).mergedOutcome())
                .isEqualTo(ValueSlot.present(base));
    }
    @Property
    void symmetrically_deterministic(@ForAll("documents") ObjectNode base,
                                     @ForAll("documents") ObjectNode theirs) {
        // precondition: ours == base (only theirs changed)
        MergeResult r = engine.mergeObject(JsonPointer.compile(""), base, base, theirs);
        MergeResult r2 = engine.mergeObject(JsonPointer.compile(""), base, theirs, base);
        assertThat(r).isEqualTo(r2);

    }
    @Property
    void no_unrelated_path_is_modified(@ForAll("documents") ObjectNode base, @ForAll("documents") ObjectNode theirs){
        MergeResult r = engine.mergeObject(JsonPointer.compile(""), base,base,theirs);
        assertThat(r).isInstanceOf(MergeResult.MergeSuccess.class);
        JsonNode  merged = (JsonNode)((ValueSlot.Present) ((MergeResult.MergeSuccess) r).mergedOutcome()).node();
        List<DiffOperation> op_res = diffEngine.diff(base,merged);

        List<DiffOperation> op_ini= diffEngine.diff(base,theirs);

        assertThat(op_ini).extracting(DiffOperation::path).containsExactlyElementsOf(op_res.stream().map(DiffOperation::path).toList());
    }
}
