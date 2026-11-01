package com.zaid.Delta_Forge.features.vcs_core.commit;

import java.util.Objects;
import java.util.UUID;

public record CommitId(UUID value) {
    public CommitId {
        Objects.requireNonNull(value, "value");
    }
    public static CommitId random() { return new CommitId(UUID.randomUUID()); }
}

