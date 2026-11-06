package com.zaid.Delta_Forge.features.vcs_core.converter.raw_to_generic;

import com.zaid.Delta_Forge.features.vcs_core.repository.RepositoryId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UUIDtoRepositoryIdConverter implements Converter<UUID, RepositoryId> {
    @Override public RepositoryId convert(UUID source) { return new RepositoryId(source); }
}