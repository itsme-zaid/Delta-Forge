package com.zaid.Delta_Forge.features.conflict;

import com.zaid.Delta_Forge.features.json.ValueSlot;
import com.zaid.Delta_Forge.features.merge.ConflictType;
import com.zaid.Delta_Forge.features.merge.MergeConflict;
import com.zaid.Delta_Forge.features.merge.MergeResult;
import com.zaid.Delta_Forge.features.resolution.Resolution;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record ConflictReport(
        List<ConflictEntry> conflicts,
        ReportStatus status
) {
    public ConflictReport {
        conflicts = List.copyOf(conflicts);
    }

    public enum ReportStatus {
        UNRESOLVED,   // at least one conflict has no Resolution
        RESOLVED,     // all conflicts have Resolutions (not yet applied)
        APPLIED       // merge has been committed
    }

    public static ConflictReport from(List<MergeConflict> conflicts,
                                      List<Resolution> resolutions,
                                      ReportStatus status) {
        // Map resolutions by path; a conflict may or may not have one.
        // For Day 25, we just build entries with empty Optional for all.
        // Day 41 will wire in the actual resolution matching.
        List<ConflictEntry> entries = conflicts.stream()
                .map(c -> new ConflictEntry(
                        generateConflictId(c),
                        c,
                        Optional.empty()))
                .toList();
        return new ConflictReport(entries, status);
    }

    public boolean isFullyResolved() {
        return conflicts.stream().allMatch(ConflictEntry::isResolved);
    }

    private static String generateConflictId(MergeConflict c) {
        // Deterministic: same (path, kind) always yields the same id.
        // Day 20's MergeMetadata pins this; Day 24's DB stores it.
        return "c-" + Integer.toHexString(
                java.util.Objects.hash(c.path().toString(), c.conflictType()));
    }
}