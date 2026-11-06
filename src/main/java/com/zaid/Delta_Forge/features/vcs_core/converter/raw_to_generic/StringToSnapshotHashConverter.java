package com.zaid.Delta_Forge.features.vcs_core.converter.raw_to_generic;

import com.zaid.Delta_Forge.features.vcs_core.blob.SnapshotHash;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToSnapshotHashConverter implements Converter<String, SnapshotHash> {
    @Override public SnapshotHash convert(String source) { return new SnapshotHash(source); }
}