package com.zaid.Delta_Forge.features.vcs_core.converter.generic_to_raw;

import com.zaid.Delta_Forge.features.vcs_core.user.UserId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserIdToUUIDCoverter implements Converter<UserId, UUID> {
    @Override public UUID convert(UserId id) { return id.user_id(); }
}