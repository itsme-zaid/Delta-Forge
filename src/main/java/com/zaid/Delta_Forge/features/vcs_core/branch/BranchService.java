package com.zaid.Delta_Forge.features.vcs_core.branch;

import com.zaid.Delta_Forge.features.vcs_core.commit.CommitId;
import com.zaid.Delta_Forge.features.vcs_core.commit.CommitRepository;
import com.zaid.Delta_Forge.features.vcs_core.commit.CommitService;
import com.zaid.Delta_Forge.features.vcs_core.repository.RepositoryId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class BranchService {

    @Autowired
    private BranchRepository repo;
    @Autowired
    private CommitRepository commitRepository;
    private final JdbcAggregateTemplate template;
    public BranchService(JdbcAggregateTemplate template) {
        this.template = template;
    }
    public Branch createBranch(RepositoryId repoId,
                               BranchName newBranchName,
                               CommitId fromCommitId) {

        if(repo.findByRepositoryIdAndName(repoId.repository_id(),newBranchName.branch_name()).isPresent()){
            throw new IllegalStateException("branch already exists: " + newBranchName);
        }
        // Validate parent commit exists (this also validates fromCommitId is
        // in this repository — the FK invariant from Day 29).
        //state.requireCommit(fromCommitId);
        if(!commitRepository.existsByIdAndRepositoryId(fromCommitId,repoId)){
            throw new IllegalStateException("Commit does not exists" + fromCommitId);
        }
        Branch branch = new Branch(
                UUID.randomUUID(), repoId, newBranchName,
                fromCommitId, OffsetDateTime.now(), OffsetDateTime.now());
        return template.insert(branch);
//        return branch;

//        return repo.save(branch);
    }
    public Branch findByRepoIdAndName(RepositoryId repoId,BranchName branchName){
        return repo.findByRepositoryIdAndName(repoId.repository_id(),branchName.branch_name()).orElseThrow(()->new RuntimeException("Branch not found " + branchName));
    }

    public Branch save(Branch branch) {
        return template.insert(branch);
    }
    public int casHead(RepositoryId repoId,BranchName branchName,CommitId expectedHead, CommitId newHead){
        return repo.casHead(repoId.repository_id(), branchName.branch_name(),expectedHead.commit_id(),newHead.commit_id());
    }
}
