package com.zaid.Delta_Forge.features.vcs_core.ai.AiSuggestion;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import tools.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.UUID;

@Table("ai_suggestions")
public record AiSuggestion(
        @Id UUID id,
        @Column("merge_request_id") UUID mergeRequestId,
        @Column("conflict_id") String conflictId,
        @Column("action") String action,                    // USE_OURS, USE_THEIRS, CUSTOM_JSON, etc.
        @Column("proposed_value") JsonNode proposedValue,
        @Column("explanation") String explanation,
        @Column("status") String status,     // PENDING, ACCEPTED, EDITED, REJECTED
        @Column("provider") String provider,
        @Column("created_at") OffsetDateTime createdAt
) {}