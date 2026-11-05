package com.zaid.Delta_Forge.features.vcs_core.commit;

import com.zaid.Delta_Forge.features.vcs_core.blob.SnapshotHash;

import java.time.Instant;
import java.util.List;

public record CommitSummary(
        CommitId id,
        String author,
        String message,
        Instant createdAt,
        SnapshotHash snapshotHash,
        List<CommitId> parentIds,
        List<String> branches    // which branches point at or contain this commit
) {
    public boolean isMergeCommit() {
        return parentIds.size() == 2;
    }

    public boolean isRootCommit() {
        return parentIds.isEmpty();
    }
}