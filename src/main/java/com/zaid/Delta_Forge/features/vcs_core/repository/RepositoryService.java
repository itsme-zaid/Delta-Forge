package com.zaid.Delta_Forge.features.vcs_core.repository;

import com.zaid.Delta_Forge.features.json.JsonDocumentService;
import com.zaid.Delta_Forge.features.merge.MergeEngine;
import com.zaid.Delta_Forge.features.vcs_core.blob.SnapshotHash;
import com.zaid.Delta_Forge.features.vcs_core.blob.SnapshotHasher;
import com.zaid.Delta_Forge.features.vcs_core.branch.Branch;
import com.zaid.Delta_Forge.features.vcs_core.branch.BranchName;
import com.zaid.Delta_Forge.features.vcs_core.commit.Commit;
import com.zaid.Delta_Forge.features.vcs_core.commit.CommitId;
import com.zaid.Delta_Forge.features.vcs_core.commit.CommitRepository;
import com.zaid.Delta_Forge.features.vcs_core.exceptions.RepositoryNotFoundException;
import com.zaid.Delta_Forge.features.vcs_core.user.UserId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service public class RepositoryService {
    private final JsonDocumentService jsonService;
    @Autowired
    private RepositoryRepo repositoryRepo;
    @Autowired
    private CommitRepository commitRepository;
    public RepositoryService(JsonDocumentService jsonService) {
        this.jsonService = jsonService;
    }
    public Repository init(String repoName, String visibility, UserId ownerId){
        Repository repo = new Repository(new RepositoryId(UUID.randomUUID()),repoName,ownerId,"main","public",Instant.now());
        repositoryRepo.save(repo);
        return repo;
    }



}
