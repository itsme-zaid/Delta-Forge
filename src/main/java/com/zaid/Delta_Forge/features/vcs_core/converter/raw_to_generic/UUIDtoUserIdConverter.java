package com.zaid.Delta_Forge.features.vcs_core.converter.raw_to_generic;

import com.zaid.Delta_Forge.features.vcs_core.user.UserId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UUIDtoUserIdConverter implements Converter<UUID, UserId> {
    @Override public UserId convert(UUID source) { return new UserId(source); }
}