package com.zaid.Delta_Forge.features.vcs_core.ai.config;

import com.zaid.Delta_Forge.features.vcs_core.ai.suggestion_provider.AiSuggestionProvider;
import com.zaid.Delta_Forge.features.vcs_core.ai.suggestion_provider.GeminiSuggestionProvider;
import com.zaid.Delta_Forge.features.vcs_core.ai.suggestion_provider.ResilientAiSuggestionProvider;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class AiConfig {

    @Bean
    @ConditionalOnProperty(name = "ai.provider", havingValue = "gemini")
    public AiSuggestionProvider geminiProvider(ChatClient.Builder builder,
                                               ObjectMapper objectMapper) {
        return new ResilientAiSuggestionProvider(
                new GeminiSuggestionProvider(builder, objectMapper));
    }

//    @Bean
//    @ConditionalOnProperty(name = "ai.provider", havingValue = "fake", matchIfMissing = true)
//    public AiSuggestionProvider fakeProvider() {
//        return new FakeAiSuggestionProvider();
//    }
}