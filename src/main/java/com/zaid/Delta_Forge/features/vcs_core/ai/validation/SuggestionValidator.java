package com.zaid.Delta_Forge.features.vcs_core.ai.validation;


import com.zaid.Delta_Forge.features.merge.MergeConflict;
import com.zaid.Delta_Forge.features.vcs_core.ai.suggestion.Action;
import com.zaid.Delta_Forge.features.vcs_core.ai.suggestion.Suggestion;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class SuggestionValidator {
    private final ObjectMapper objectMapper;

    public SuggestionValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Validates a suggestion against the conflict it was generated for.
     * Returns a validated suggestion, or an ABSTAIN if any gate fails.
     * Never throws; the caller decides whether to store the rejection.
     */
    public ValidationResult validate(MergeConflict conflict, Suggestion suggestion) {
        String expectedId = ConflictIds.of(conflict);
        // Gate 1: envelope shape (already enforced by structured output,
        // but re-checked for the fake provider and for defense in depth)
        if (suggestion == null
                || suggestion.conflictId() == null
                || suggestion.action() == null) {
            return ValidationResult.rejected("Malformed envelope");
        }

        // Gate 2: conflict identity
        if (!suggestion.conflictId().equals(expectedId)) {
            return ValidationResult.rejected(
                    "conflictId mismatch: expected " + expectedId
                            + " got " + suggestion.conflictId());
        }

        // Gate 3: value scope — only CUSTOM_JSON carries a value
        JsonNode proposedValue = null;
        if (suggestion.action() == Action.CUSTOM_JSON) {
            if (suggestion.proposedValue() == null) {
                return ValidationResult.rejected("CUSTOM_JSON without proposedValue");
            }
            proposedValue = suggestion.proposedValue();   // already parsed by provider
        } else if (suggestion.proposedValue() != null) {
            // A non-CUSTOM_JSON action must not carry a value (scope leak)
            return ValidationResult.rejected(
                    "action " + suggestion.action() + " must not carry proposedValue");
        }

        // Gate 4: whole-document validity.
        // Only run this if the resolution is fully determined by the suggestion.
        // For USE_OURS/USE_THEIRS/USE_BASE/DELETE, we can materialize the value
        // from the conflict itself and check the whole document.
//        JsonNode candidateValue = resolveValue(conflict, suggestion.action(), proposedValue);
//        if (candidateValue == MISSING) {
//            return ValidationResult.rejected("Unresolvable action: " + suggestion.action());
//        }
//
//        // Materialize at the conflict path only (Gate 3's scope enforcement)
//        JsonNode candidateDocument = materialize(conflict.path(), candidateValue);
//
//        if (!schemaValidator.isValid(candidateDocument)) {
//            return ValidationResult.rejected("Schema violation");
//        }
//        if (!invariantValidator.isValid(candidateDocument)) {
//            return ValidationResult.rejected("Invariant violation");
//        }

        // All gates passed
        return ValidationResult.accepted(suggestion);
    }

//    private JsonNode resolveValue(MergeConflict conflict, Action action, JsonNode custom) {
//        return switch (action) {
//            case USE_OURS -> presentOrMissing(conflict.ours());
//            case USE_THEIRS -> presentOrMissing(conflict.theirs());
//            case USE_BASE -> presentOrMissing(conflict.base());
//            case DELETE -> MISSING;
//            case CUSTOM_JSON -> custom;
//            case ABSTAIN -> MISSING;
//        };
//    }
}