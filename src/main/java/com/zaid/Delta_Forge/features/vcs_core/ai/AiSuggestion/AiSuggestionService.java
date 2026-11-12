package com.zaid.Delta_Forge.features.vcs_core.ai.AiSuggestion;

import com.zaid.Delta_Forge.features.merge.ConflictType;
import com.zaid.Delta_Forge.features.merge.MergeConflict;
import com.zaid.Delta_Forge.features.json.ValueSlot;
import com.zaid.Delta_Forge.features.resolution.Resolution;
import com.zaid.Delta_Forge.features.vcs_core.ai.ConflictContext;
import com.zaid.Delta_Forge.features.vcs_core.ai.resolution.ResolutionRepository;
import com.zaid.Delta_Forge.features.vcs_core.ai.resolution.ResolutionRow;
import com.zaid.Delta_Forge.features.vcs_core.ai.suggestion.Suggestion;
import com.zaid.Delta_Forge.features.vcs_core.ai.suggestion_provider.AiSuggestionProvider;
import com.zaid.Delta_Forge.features.vcs_core.ai.validation.ConflictIds;
import com.zaid.Delta_Forge.features.vcs_core.ai.validation.SuggestionValidator;
import com.zaid.Delta_Forge.features.vcs_core.ai.validation.ValidationResult;
import com.zaid.Delta_Forge.features.vcs_core.audit.AuditService;
import com.zaid.Delta_Forge.features.vcs_core.dto.ConflictWithId;
import com.zaid.Delta_Forge.features.vcs_core.merge.*;
import com.zaid.Delta_Forge.features.vcs_core.merge.merge_request.MergeRequest;
import com.zaid.Delta_Forge.features.vcs_core.merge.merge_request.MergeRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class AiSuggestionService {

    private final AiSuggestionRepository suggestionRepository;
    private final ResolutionRepository resolutionRepository;
    private final MergeRequestService mergeRequestService;
    private final AiSuggestionProvider provider;
    private final SuggestionValidator validator;
    private final ObjectMapper objectMapper;
    private final JdbcAggregateTemplate template;
    @Autowired
    private AuditService auditService;
    public AiSuggestionService(AiSuggestionRepository suggestionRepository,
                               ResolutionRepository resolutionRepository,
                               MergeRequestService mergeRequestService,
                               AiSuggestionProvider provider,
                               SuggestionValidator validator,
                               ObjectMapper objectMapper,
                               JdbcAggregateTemplate template) {
        this.suggestionRepository = suggestionRepository;
        this.resolutionRepository = resolutionRepository;
        this.mergeRequestService = mergeRequestService;
        this.provider = provider;
        this.validator = validator;
        this.objectMapper = objectMapper;
        this.template = template;
    }

    // ============ suggest ============

    @Transactional
    public AiSuggestion suggest(UUID mergeRequestId, String conflictId) {
        MergeRequest mr = mergeRequestService.get(mergeRequestId);
        MergeConflict conflict = findConflict(mr, conflictId);
        auditService.logSuggestionRequested(mergeRequestId, conflictId, providerName());
        ConflictContext ctx = buildContext(mr, conflict);
        Suggestion raw = provider.suggest(ctx);

        ValidationResult result = validator.validate(conflict, raw);
        if (result instanceof ValidationResult.Rejected r) {
            AiSuggestion rejected = new AiSuggestion(
                    UUID.randomUUID(), mergeRequestId, conflictId,
                    "ABSTAIN", null,
                    "Validation failed: " + r.reason(),
                    "REJECTED", providerName(),
                    OffsetDateTime.now());
            template.insert(rejected);
            auditService.logValidationFailed(mergeRequestId, conflictId, ((ValidationResult.Rejected) result).reason());
            return rejected;
        }

        AiSuggestion suggestion = new AiSuggestion(
                UUID.randomUUID(), mergeRequestId, conflictId,
                raw.action().name(), raw.proposedValue(), raw.explanation(),
                "PENDING", providerName(), OffsetDateTime.now());
        template.insert(suggestion);
        return suggestion;
    }

    // ============ accept ============

    @Transactional
    public Resolution accept(UUID suggestionId, String resolvedBy) {
        AiSuggestion s = requirePending(suggestionId);
        AiSuggestion accepted = new AiSuggestion(
                s.id(), s.mergeRequestId(), s.conflictId(), s.action(),
                s.proposedValue(), s.explanation(), "ACCEPTED", s.provider(),
                s.createdAt());
        template.update(accepted);

        auditService.logUserAction("USER_ACCEPTED", resolvedBy, s.mergeRequestId(), s.conflictId(), objectMapper.valueToTree(Map.of("suggestionId", s.id(), "action", s.action())));
        return persistResolution(s, s.proposedValue(), Resolution.ResolutionSource.AI_ACCEPTED, resolvedBy);
    }

    // ============ edit ============

    @Transactional
    public Resolution edit(UUID suggestionId, JsonNode newValue, String resolvedBy) {
        AiSuggestion s = requirePending(suggestionId);

        AiSuggestion edited = new AiSuggestion(
                s.id(), s.mergeRequestId(), s.conflictId(), "CUSTOM_JSON",
                newValue, s.explanation(), "EDITED", s.provider(),
                s.createdAt());
        template.update(edited);
        auditService.logUserAction("USER_EDITED", resolvedBy, s.mergeRequestId(), s.conflictId(), objectMapper.valueToTree(Map.of("suggestionId", s.id(), "originalAction", s.action(), "editedValue", newValue)));
        return persistResolution(s, newValue, Resolution.ResolutionSource.AI_EDITED, resolvedBy);
    }

    // ============ reject ============

    @Transactional
    public void reject(UUID suggestionId, String resolvedBy) {
        AiSuggestion s = requirePending(suggestionId);

        AiSuggestion rejected = new AiSuggestion(
                s.id(), s.mergeRequestId(), s.conflictId(), s.action(),
                s.proposedValue(), s.explanation(), "REJECTED", s.provider(),
                s.createdAt());
        template.update(rejected);
        auditService.logUserAction("USER_REJECTED", resolvedBy, s.mergeRequestId(), s.conflictId(), objectMapper.valueToTree(Map.of("suggestionId", s.id(), "action", s.action())));
    }

    // ============ helpers ============

    private AiSuggestion requirePending(UUID suggestionId) {
        AiSuggestion s = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new NoSuchElementException("suggestion not found: " + suggestionId));
        if (!"PENDING".equals(s.status())) {
            throw new IllegalStateException("suggestion is not pending: " + s.status());
        }
        return s;
    }

    private Resolution persistResolution(AiSuggestion s, JsonNode customValue,
                                         Resolution.ResolutionSource source, String resolvedBy) {
        // 1. Look up the merge request and the conflict
        MergeRequest mr = mergeRequestService.get(s.mergeRequestId());
        MergeConflict conflict = findConflict(mr, s.conflictId());

        // 2. Resolve the value from the action
        ValueSlot resolved = switch (s.action()) {
            case "USE_OURS"   -> conflict.ours();
            case "USE_THEIRS" -> conflict.theirs();
            case "USE_BASE"   -> conflict.base();
            case "DELETE"     -> new ValueSlot.Missing();
            case "CUSTOM_JSON", "EDITED" -> new ValueSlot.Present(customValue);
            default -> throw new IllegalStateException("unknown action: " + s.action());
        };

        // 3. Build the domain Resolution
        Resolution domain = new Resolution(
                s.conflictId(), resolved, source, resolvedBy, OffsetDateTime.now(), s.explanation());

        // 4. Build the persistence row
        ResolutionRow row = new ResolutionRow(
                UUID.randomUUID(),
                s.mergeRequestId(),
                s.conflictId(),
                resolved instanceof ValueSlot.Present,
                resolved instanceof ValueSlot.Present p ? p.node() : null,
                source.name(),
                resolvedBy,
                OffsetDateTime.now(),
                s.explanation());
        template.insert(row);
        return domain;
    }

    private MergeConflict findConflict(MergeRequest mr, String conflictId) {
        try {
            List<ConflictWithId> conflicts = objectMapper
                    .readerForListOf(ConflictWithId.class)
                    .readValue(mr.conflicts());

            ConflictWithId c = conflicts.stream()
                    .filter(x -> x.conflictId().equals(conflictId))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException(
                            "conflict not found: " + conflictId));

            return new MergeConflict(
                    JsonPointer.compile(c.path()),
                    toSlot(c.oursPresent(), c.ours()),
                    toSlot(c.theirsPresent(), c.theirs()),
                    toSlot(c.basePresent(), c.base()),
                    ConflictType.valueOf(c.type())
            );
        } catch (Exception e) {
            throw new IllegalStateException("failed to read conflicts", e);
        }
    }

    private ValueSlot toSlot(boolean present, JsonNode node) {
        return present ? new ValueSlot.Present(node) : new ValueSlot.Missing();
    }

    private ConflictContext buildContext(MergeRequest mr, MergeConflict conflict) {
        return new ConflictContext(
                ConflictIds.of(conflict),
                conflict.path().toString(),
                conflict.conflictType().name(),
                conflict.base() instanceof ValueSlot.Present(var n) ? n : null,
                conflict.ours() instanceof ValueSlot.Present(var n) ? n : null,
                conflict.theirs() instanceof ValueSlot.Present(var n) ? n : null,
                mr.partialTree(),                // parent context — partial tree
                List.of(),                       // commit messages — not available here
                "",                               // merge policy
                null,                             // schema constraints
                List.of());                       // cross-field rules
    }

    private String providerName() {
        return provider.getClass().getSimpleName();
    }
}