package com.zaid.Delta_Forge.features.vcs_core.ai.suggestion_provider;

import com.zaid.Delta_Forge.features.vcs_core.ai.ConflictContext;
import com.zaid.Delta_Forge.features.vcs_core.ai.Suggestion.Action;
import com.zaid.Delta_Forge.features.vcs_core.ai.Suggestion.Suggestion;
import org.springframework.stereotype.Component;

@Component
public class ResilientAiSuggestionProvider implements AiSuggestionProvider {

    private final GeminiSuggestionProvider delegate;

    public ResilientAiSuggestionProvider(GeminiSuggestionProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public Suggestion suggest(ConflictContext context) {
        try {
            return delegate.suggest(context);
        } catch (Exception e) {
            // After Spring AI's internal retries are exhausted, don't crash the merge.
            // Abstaining is the safe failure mode: the user resolves manually.
            return new Suggestion(context.conflictId(), Action.ABSTAIN, null,
                    "AI unavailable after retries: " + e.getMessage(), null);
        }
    }
}
