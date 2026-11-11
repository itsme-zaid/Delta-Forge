package com.zaid.Delta_Forge.features.vcs_core.ai.suggestion_provider;

import com.zaid.Delta_Forge.features.vcs_core.ai.ConflictContext;
import com.zaid.Delta_Forge.features.vcs_core.ai.Suggestion.Suggestion;

public interface AiSuggestionProvider {
    Suggestion suggest(ConflictContext context);
}
