package com.zaid.Delta_Forge.features.vcs_core.dto;

import com.zaid.Delta_Forge.features.json.ValueSlot;
import com.zaid.Delta_Forge.features.merge.MergeConflict;
import com.zaid.Delta_Forge.features.vcs_core.ai.validation.ConflictIds;
import tools.jackson.databind.JsonNode;

public record ConflictWithId(
        String conflictId,
        String path,
        String type,
        boolean basePresent,
        JsonNode base,
        boolean oursPresent,
        JsonNode ours,
        boolean theirsPresent,
        JsonNode theirs

) {
    public static ConflictWithId from(MergeConflict c) {
        return new ConflictWithId(
                ConflictIds.of(c),
                c.path().toString(),
                c.conflictType().name(),
                c.base() instanceof ValueSlot.Present,
                c.base() instanceof ValueSlot.Present p ? p.node() : null,
                c.ours() instanceof ValueSlot.Present,
                c.ours() instanceof ValueSlot.Present p ? p.node() : null,
                c.theirs() instanceof ValueSlot.Present,
                c.theirs() instanceof ValueSlot.Present p ? p.node() : null

        );
    }

}