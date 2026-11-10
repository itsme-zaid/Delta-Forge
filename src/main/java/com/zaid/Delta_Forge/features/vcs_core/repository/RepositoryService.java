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
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service public class RepositoryService {
    @Autowired
    private RepositoryRepo repositoryRepo;
    @Autowired
    private CommitRepository commitRepository;
    private JdbcAggregateTemplate template;

    public RepositoryService(JdbcAggregateTemplate template){
        this.template = template;
    }
    public Repository getById(UUID repoId){
        Optional<Repository> repo = repositoryRepo.findById(new RepositoryId(repoId));
        return repo.orElse(null);
    }
    public Repository init(String repoName, String defaultBranch, String visibility, UserId ownerId){
        Repository repo = new Repository(new RepositoryId(UUID.randomUUID()),repoName,ownerId,defaultBranch,visibility, OffsetDateTime.now());
        template.insert(repo);
        return repo;
    }
    public Repository get(RepositoryId repositoryId){
        return  repositoryRepo.findById(repositoryId).orElseThrow(()-> new RuntimeException("Repository not found"));
    }


}
