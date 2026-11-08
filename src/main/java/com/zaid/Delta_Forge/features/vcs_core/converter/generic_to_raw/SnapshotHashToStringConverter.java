package com.zaid.Delta_Forge.features.vcs_core.converter.generic_to_raw;

import com.zaid.Delta_Forge.features.vcs_core.blob.Snapshot;
import com.zaid.Delta_Forge.features.vcs_core.blob.SnapshotHash;
import com.zaid.Delta_Forge.features.vcs_core.blob.SnapshotHasher;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SnapshotHashToStringConverter implements Converter<SnapshotHash,String> {
    @Override public String convert(SnapshotHash s) { return s.hash(); }
}