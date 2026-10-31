package com.zaid.Delta_Forge.features.conflict;

import com.zaid.Delta_Forge.features.json.ValueSlot;
import com.zaid.Delta_Forge.features.merge.ConflictType;
import com.zaid.Delta_Forge.features.merge.MergeConflict;
import com.zaid.Delta_Forge.features.merge.MergeResult;
import com.zaid.Delta_Forge.features.resolution.Resolution;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
                                      ReportStatus baseStatus) {
        // Map resolutions by conflictId for O(1) matching
        Map<String, Resolution> byId = resolutions.stream()
                .collect(Collectors.toMap(Resolution::conflictId, r -> r));

        List<ConflictEntry> entries = conflicts.stream()
                .map(c -> {
                    String id = generateConflictId(c);
                    return new ConflictEntry(id, c, Optional.ofNullable(byId.get(id)));
                })
                .toList();

        ReportStatus status = baseStatus == ReportStatus.APPLIED
                ? ReportStatus.APPLIED
                : (entries.stream().allMatch(ConflictEntry::isResolved)
                ? ReportStatus.RESOLVED
                : ReportStatus.UNRESOLVED);

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