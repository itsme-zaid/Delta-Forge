package com.zaid.Delta_Forge.features.config;

import org.postgresql.util.PGobject;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.core.convert.converter.Converter;

import java.time.OffsetDateTime;
import java.util.List;

@Configuration
public class JsonbConfig extends AbstractJdbcConfiguration {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Override
    protected List<?> userConverters() {
        return List.of(
                new JsonNodeWritingConverter(mapper),
                new JsonNodeReadingConverter(mapper),
                new StringToOffsetDateTimeConverter()
        );
    }
    @ReadingConverter
    static class StringToOffsetDateTimeConverter implements Converter<String, OffsetDateTime> {
        @Override
        public OffsetDateTime convert(String source) {
            // Postgres TIMESTAMPTZ string format: "2026-11-09 13:13:50.979161+05:30"
            // Replace space with 'T' to make it ISO-8601 compatible
            return OffsetDateTime.parse(source.replace(' ', 'T'));
        }
    }
    static class PgObjectToOffsetDateTimeConverter implements Converter<PGobject, OffsetDateTime> {
        @Override
        public OffsetDateTime convert(PGobject source) {
            return OffsetDateTime.parse(source.getValue().replace(' ', 'T'));
        }
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
