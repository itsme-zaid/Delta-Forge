package com.zaid.Delta_Forge.features.vcs_core.ai.resolution;

import com.zaid.Delta_Forge.features.json.ValueSlot;
import com.zaid.Delta_Forge.features.resolution.Resolution;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Table("resolutions")
public record ResolutionRow(
        @Id UUID id,
        @Column("merge_request_id") UUID mergeRequestId,
        @Column("conflict_id") String conflictId,
        @Column("resolved_present") boolean resolvedPresent,
        @Column("resolved_value") JsonNode resolvedValue,
        @Column("source") String source,
        @Column("resolved_by") String resolvedBy,
        @Column("created_at") OffsetDateTime resolvedAt,
        @Column("explanation") String explanation

) {
    public ValueSlot toValueSlot() {
        return resolvedPresent
                ? new ValueSlot.Present(resolvedValue)
                : new ValueSlot.Missing();
    }

    public static ResolutionRow from(Resolution r, UUID id, UUID mergeRequestId) {
        boolean present = r.resolvedValue() instanceof ValueSlot.Present;
        JsonNode value = present
                ? ((ValueSlot.Present) r.resolvedValue()).node()
                : null;
        return new ResolutionRow(
                id, mergeRequestId, r.conflictId(), present, value,
                r.source().name(), r.resolvedBy(), r.resolvedAt(), r.explanation());
    }
}