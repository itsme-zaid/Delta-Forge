package com.zaid.Delta_Forge.features.vcs_core.converter.generic_to_raw;

import com.zaid.Delta_Forge.features.vcs_core.repository.RepositoryId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.rmi.server.UID;
import java.util.UUID;

@Component
public class RepositoryIdToUUIDConverter implements Converter<RepositoryId, UUID> {
    @Override public UUID convert(RepositoryId id) { return id.value(); }
}