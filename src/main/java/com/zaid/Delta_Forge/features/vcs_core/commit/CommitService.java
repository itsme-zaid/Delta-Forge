package com.zaid.Delta_Forge.features.vcs_core.commit;

import com.zaid.Delta_Forge.features.json.JsonDocumentService;
import com.zaid.Delta_Forge.features.merge.MergeEngine;
import com.zaid.Delta_Forge.features.vcs_core.blob.SnapshotHash;
import com.zaid.Delta_Forge.features.vcs_core.blob.SnapshotHasher;
import com.zaid.Delta_Forge.features.vcs_core.branch.Branch;
import com.zaid.Delta_Forge.features.vcs_core.branch.BranchName;
import com.zaid.Delta_Forge.features.vcs_core.branch.BranchRepository;
import com.zaid.Delta_Forge.features.vcs_core.branch.BranchService;
import com.zaid.Delta_Forge.features.vcs_core.exceptions.UnrelatedHistoriesException;
import com.zaid.Delta_Forge.features.vcs_core.exceptions.UnsupportedHistoryException;
import com.zaid.Delta_Forge.features.vcs_core.repository.Repository;
import com.zaid.Delta_Forge.features.vcs_core.repository.RepositoryId;
import com.zaid.Delta_Forge.features.vcs_core.repository.RepositoryRepo;
import com.zaid.Delta_Forge.features.vcs_core.user.UserId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CommitService {
    private final JsonDocumentService jsonService;
    private final MergeEngine mergeEngine;
    @Autowired
    private CommitRepository commitRepository;
    @Autowired
    private BranchService branchService;
    private final JdbcAggregateTemplate template;
    public CommitService(JsonDocumentService jsonService, JdbcAggregateTemplate template){
        this.jsonService = jsonService;
        this.mergeEngine = new MergeEngine(jsonService);
        this.template = template;
    }
    public Commit createCommit(RepositoryId repoId, SnapshotHash snapshotHash, List<CommitId> parents, UserId authorId, String message){
        // validate parents exists in this repo;

        return new Commit(CommitId.random(),repoId,snapshotHash,authorId,parents,message, Instant.now());
    }
    public Commit createFirstCommit(RepositoryId repoId, BranchName branchName,
                                    JsonNode document, UserId authorId, String message) {
        // Called when the user commits the first document to an empty repo.
        // Creates the snapshot, the root commit, and the branch.
        SnapshotHash hash = SnapshotHasher.hash(jsonService.serialize(document));
        Commit rootCommit = createCommit(repoId,hash,List.of(),authorId,message);
        Branch branch = new Branch(UUID.randomUUID(),repoId,branchName,rootCommit.id(),Instant.now(),Instant.now());
        branchService.save(branch);
        return template.insert(rootCommit);
    }
    public Commit commit(RepositoryId repoId,
                         BranchName branchName,
                         CommitId expectedHead,
                         JsonNode document,
                         UserId authorId,
                         String message) {
        Branch branch = branchService.findByRepoIdAndName(repoId, branchName);
        synchronized (branch) {
            if (!branch.headCommitId().equals(expectedHead)) {
                throw new RuntimeException(
                        "branch " + branchName + " moved from " + expectedHead
                                + " to " + branch.headCommitId());

            }
        }
        SnapshotHash hash = SnapshotHasher.hash(jsonService.serialize(document));
        Commit commit = createCommit(repoId,hash,List.of(expectedHead),authorId,message);
        branchService.casHead(repoId,branchName,expectedHead,commit.id());
        return template.insert(commit);
    }
    public List<Commit> listCommits(RepositoryId repoId, BranchName branchName, int limit, int offset){
        // returning all the commits without any distinction
        return commitRepository.findByRepositoryIdOrderByCreatedAtDesc(repoId);
    }
    public Commit findRoot(RepositoryId repoId, BranchName branchName){
        Branch b = branchService.findByRepoIdAndName(repoId,branchName);
        // currently only returning the rootCommit of the entire repo without any distinction with branch
        return commitRepository.findRootCommit(repoId);
    }
    public boolean commitExists(CommitId commitId, RepositoryId repoId){
        return commitRepository.existsByIdAndRepositoryId(commitId,repoId);
    }
    public Commit getCommit(CommitId id,RepositoryId repoId){
        return commitRepository.findByIdAndRepositoryId(id,repoId).orElseThrow(()-> new RuntimeException("Invalid Commit Id"));
    }
    public CommitId findBestCommonAncestor(CommitId ours, CommitId theirs){
        List<UUID> bestIds = commitRepository.findBestCommonAncestorIds(ours.commit_id(),theirs.commit_id());
        if(bestIds.isEmpty()) throw new UnrelatedHistoriesException(ours,theirs);
        if(bestIds.size()>1) throw new UnsupportedHistoryException("criss cross merge: multiple best common ancestor" + bestIds) ;
        return new CommitId(bestIds.getFirst());
    }
}
