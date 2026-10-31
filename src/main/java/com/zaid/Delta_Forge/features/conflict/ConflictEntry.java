package com.zaid.Delta_Forge.features.conflict;

import com.zaid.Delta_Forge.features.merge.MergeConflict;
import com.zaid.Delta_Forge.features.resolution.Resolution;
import tools.jackson.core.JsonPointer;

import java.util.Optional;

public record ConflictEntry(
        String conflictId,
        MergeConflict conflict,
        Optional<Resolution> resolution
) {
    public ConflictEntry {
        if (conflictId == null || conflictId.isBlank()) {
            throw new IllegalArgumentException("conflictId must not be blank");
        }
        // Optional is never null; the caller must pass Optional.empty() for unresolved.
    }

    public boolean isResolved() {
        return resolution.isPresent();
    }

    public JsonPointer path() {
        return conflict.path();
    }
    public ConflictDisplayStatus displayStatus() {
        return resolution.map(value -> switch (value.source()) {
            case MANUAL -> ConflictDisplayStatus.MANUALLY_RESOLVED;
            case AI_ACCEPTED, AI_EDITED -> ConflictDisplayStatus.USER_ACCEPTED;
        }).orElse(ConflictDisplayStatus.UNRESOLVED);
    }
}
