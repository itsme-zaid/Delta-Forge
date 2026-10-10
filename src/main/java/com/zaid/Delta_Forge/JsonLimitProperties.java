package com.zaid.Delta_Forge;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.json.limits")
public record JsonLimitProperties (
        long maxDocumentBytes,
        int maxNestingDepth,
        int maxNumberLength,
        int maxStringLength
){}
