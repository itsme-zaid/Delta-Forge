package com.zaid.Delta_Forge.features.vcs_core.audit;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import tools.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.UUID;

@Table("audit_events")
public record AuditEvent(
        @Id UUID id,
        @Column("merge_request_id") UUID mergeRequestId,
        @Column("conflict_id") String conflictId,
        @Column("event_type") String eventType,
        @Column("actor") String actor,
        @Column("details") JsonNode details,
        @Column("created_at") OffsetDateTime createdAt
) {}