package com.zaid.Delta_Forge.features.vcs_core.ai.validation;

import com.zaid.Delta_Forge.features.vcs_core.ai.suggestion.Suggestion;

public sealed interface ValidationResult
        permits ValidationResult.Accepted, ValidationResult.Rejected {

    record Accepted(Suggestion suggestion) implements ValidationResult {}
    record Rejected(String reason) implements ValidationResult {}

    static ValidationResult accepted(Suggestion s) { return new Accepted(s); }
    static ValidationResult rejected(String r) { return new Rejected(r); }

    default boolean isAccepted() { return this instanceof Accepted; }
}