package com.zaid.Delta_Forge.features.vcs_core.commit;

import java.util.Objects;
import java.util.UUID;

public record CommitId(UUID commit_id) {
    public CommitId {
        Objects.requireNonNull(commit_id, "commit_id");
    }

    public static CommitId random() {
        return new CommitId(UUID.randomUUID());
    }
}

