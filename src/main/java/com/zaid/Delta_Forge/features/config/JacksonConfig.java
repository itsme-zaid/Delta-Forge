package com.zaid.Delta_Forge.features.config;
import java.util.List;

import com.zaid.Delta_Forge.JsonLimitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;

import tools.jackson.core.StreamReadConstraints;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class JacksonConfig {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    JsonMapper.Builder jsonMapperBuilder(
            List<JsonMapperBuilderCustomizer> customizers,
            JsonLimitProperties limits
    ) {
        var constraints = StreamReadConstraints.builder()
                .maxDocumentLength(limits.maxDocumentBytes())
                .maxNestingDepth(limits.maxNestingDepth())
                .maxNumberLength(limits.maxNumberLength())
                .maxStringLength(limits.maxStringLength())
                .build();

        JsonFactory factory = JsonFactory.builder()
                .streamReadConstraints(constraints)
                .build();

        JsonMapper.Builder builder = JsonMapper.builder(factory);

        customizers.forEach(customizer ->
                customizer.customize(builder)
        );

        return builder;
    }
}
