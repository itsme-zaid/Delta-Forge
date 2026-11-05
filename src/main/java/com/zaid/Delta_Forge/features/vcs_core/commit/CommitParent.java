package com.zaid.Delta_Forge.features.vcs_core.commit;

public record CommitParent(
        CommitId commitId,
        CommitId parentId,
        int position       // 0 (first parent) or 1 (second parent)
) {
}
