package com.zaid.Delta_Forge.features.vcs_core.ai.suggestion_provider;

import com.zaid.Delta_Forge.features.vcs_core.ai.ConflictContext;
import com.zaid.Delta_Forge.features.vcs_core.ai.suggestion.Action;
import com.zaid.Delta_Forge.features.vcs_core.ai.suggestion.Suggestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

public class ResilientAiSuggestionProvider implements AiSuggestionProvider {
    private static final Logger log = LoggerFactory.getLogger(ResilientAiSuggestionProvider.class);
    private final GeminiSuggestionProvider delegate;

    public ResilientAiSuggestionProvider(GeminiSuggestionProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public Suggestion suggest(ConflictContext context) {
        String key = System.getenv("GEMINI_API_KEY");
        log.info("Key prefix: {}, length: {}", key != null ? key.substring(0, 4) : "null", key != null ? key.length() : 0);
        try {
            return delegate.suggest(context);
        } catch (Exception e) {
            // After Spring AI's internal retries are exhausted, don't crash the merge.
            log.error("Gemini call failed for conflict {}", context.conflictId(), e);
            // Abstaining is the safe failure mode: the user resolves manually.
            return new Suggestion(context.conflictId(), Action.ABSTAIN, null,
                    "AI unavailable after retries: " + e.getMessage(), null);
        }
    }
}
