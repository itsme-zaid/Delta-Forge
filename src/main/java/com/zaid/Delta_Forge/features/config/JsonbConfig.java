package com.zaid.Delta_Forge.features.config;

import org.postgresql.util.PGobject;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.core.convert.converter.Converter;
import java.util.List;

@Configuration
public class JsonbConfig extends AbstractJdbcConfiguration {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Override
    protected List<?> userConverters() {
        return List.of(
                new JsonNodeWritingConverter(mapper),
                new JsonNodeReadingConverter(mapper)
        );
    }

    @WritingConverter
    static class JsonNodeWritingConverter implements Converter<JsonNode, PGobject> {
        private final JsonMapper mapper;
        JsonNodeWritingConverter(JsonMapper mapper) { this.mapper = mapper; }

        @Override
        public PGobject convert(JsonNode source) {
            PGobject pg = new PGobject();
            pg.setType("jsonb");
            try {
                pg.setValue(mapper.writeValueAsString(source));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return pg;
        }
    }

    @ReadingConverter
    static class JsonNodeReadingConverter implements Converter<PGobject, JsonNode> {
        private final JsonMapper mapper;
        JsonNodeReadingConverter(JsonMapper mapper) { this.mapper = mapper; }

        @Override
        public JsonNode convert(PGobject source) {
            try {
                return mapper.readTree(source.getValue());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
