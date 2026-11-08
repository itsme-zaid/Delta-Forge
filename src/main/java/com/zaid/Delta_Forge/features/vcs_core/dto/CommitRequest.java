package com.zaid.Delta_Forge.features.vcs_core.dto;

import com.zaid.Delta_Forge.features.vcs_core.branch.BranchName;
import com.zaid.Delta_Forge.features.vcs_core.commit.CommitId;
import com.zaid.Delta_Forge.features.vcs_core.repository.RepositoryId;

public class CommitRequest {
    private RepositoryId repoId;
    private BranchName branchName;
    private CommitId commitId;

}
