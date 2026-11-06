package com.zaid.Delta_Forge.features.vcs_core.converter.raw_to_generic;

import com.zaid.Delta_Forge.features.vcs_core.commit.CommitId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UUIDtoCommitIdConverter implements Converter<UUID, CommitId> {
    @Override public CommitId convert(UUID source) { return new CommitId(source); }
}