package com.zaid.Delta_Forge.features.vcs_core.audit;

import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class AuditService {
    private final AuditEventRepository repository;
    private final JdbcAggregateTemplate template;
    private final ObjectMapper objectMapper;

    public AuditService(AuditEventRepository repository, JdbcAggregateTemplate template, ObjectMapper objectMapper) {
        this.repository = repository;
        this.template = template;
        this.objectMapper = objectMapper;
    }

    public void logSuggestionRequested(UUID mergeRequestId, String conflictId, String provider) {
        record("SUGGESTION_REQUESTED", "system", mergeRequestId, conflictId,
                objectMapper.valueToTree(Map.of("provider", provider)));
    }

    public void logValidationFailed(UUID mergeRequestId, String conflictId, String reason) {
        record("SUGGESTION_VALIDATION_FAILED", "system", mergeRequestId, conflictId,
                objectMapper.valueToTree(Map.of("reason", reason)));
    }

    public void logUserAction(String action, String username, UUID mergeRequestId,
                              String conflictId, JsonNode details) {
        record(action, username, mergeRequestId, conflictId, details);
    }

    private void record(String type, String actor, UUID mrId, String conflictId, JsonNode details) {
        AuditEvent event = new AuditEvent(
                UUID.randomUUID(), mrId, conflictId, type, actor, details, OffsetDateTime.now());
        template.insert(event);
    }
}