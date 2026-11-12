package com.zaid.Delta_Forge.features.vcs_core.commit;

import com.zaid.Delta_Forge.features.json.JsonDocumentService;
import com.zaid.Delta_Forge.features.merge.MergeEngine;
import com.zaid.Delta_Forge.features.vcs_core.blob.Snapshot;
import com.zaid.Delta_Forge.features.vcs_core.blob.SnapshotHash;
import com.zaid.Delta_Forge.features.vcs_core.blob.SnapshotHasher;
import com.zaid.Delta_Forge.features.vcs_core.blob.SnapshotService;
import com.zaid.Delta_Forge.features.vcs_core.branch.Branch;
import com.zaid.Delta_Forge.features.vcs_core.branch.BranchName;
import com.zaid.Delta_Forge.features.vcs_core.branch.BranchService;
import com.zaid.Delta_Forge.features.vcs_core.exceptions.UnrelatedHistoriesException;
import com.zaid.Delta_Forge.features.vcs_core.exceptions.UnsupportedHistoryException;
import com.zaid.Delta_Forge.features.vcs_core.repository.RepositoryId;
import com.zaid.Delta_Forge.features.vcs_core.user.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.time.OffsetDateTime;
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
    @Autowired
    private SnapshotService snapshotService;
    private final JdbcAggregateTemplate template;

    private static final Logger log = LoggerFactory.getLogger(CommitService.class);
    public CommitService(JsonDocumentService jsonService, JdbcAggregateTemplate template){
        this.jsonService = jsonService;
        this.mergeEngine = new MergeEngine(jsonService);
        this.template = template;
    }
    public Commit createCommit(RepositoryId repoId, SnapshotHash snapshotHash, UserId authorId, String message){
        // validate parents exists in this repo;

        return new Commit(CommitId.random(),repoId,snapshotHash,authorId,message, OffsetDateTime.now());
    }
    public Commit createFirstCommit(RepositoryId repoId, BranchName branchName,
                                    JsonNode document, UserId authorId, String message) {
        // Called when the user commits the first document to an empty repo.
        // Creates the snapshot, the root commit, and the branch.
        Snapshot sn = snapshotService.saveSnapshot(repoId,document);
        Commit rootCommit = createCommit(repoId,sn.hash(),authorId,message);
        Branch branch = new Branch(UUID.randomUUID(),repoId,branchName,rootCommit.id(),OffsetDateTime.now(),OffsetDateTime.now());
        Commit commit = template.insert(rootCommit);
        branchService.save(branch);
        return commit;
    }
    @Transactional
    public Commit commit(RepositoryId repoId,
                         BranchName branchName,
                         CommitId expectedHead,
                         JsonNode document,
                         UserId authorId,
                         String message) {
        Branch branch = branchService.findByRepoIdAndName(repoId, branchName);
//        synchronized (branch) {
            if (!branch.headCommitId().equals(expectedHead)) {
                throw new RuntimeException(
                        "branch " + branchName + " moved from " + expectedHead
                                + " to " + branch.headCommitId());

            }
//        }
        Snapshot sn = snapshotService.saveSnapshot(repoId,document);
        Commit commit = createCommit(repoId,sn.hash(),authorId,message);
        log.atDebug().setMessage("expectedHead = " + expectedHead.commit_id() ).log();;
        Commit c = template.insert(commit);

        List<UUID> parents = List.of(expectedHead.commit_id());
        for (int i = 0; i < parents.size(); i++) {
            commitRepository.insertParent(commit.id().commit_id(), parents.get(i), i);
        }
        branchService.casHead(repoId,branchName,expectedHead,commit.id());
        return c;
    }
    public List<Commit> listCommits(RepositoryId repoId, BranchName branchName, int limit, int offset){
        // returning all the commits without any distinction
        Branch branch = branchService.findByRepoIdAndName(repoId, branchName);
        UUID headId = branch.headCommitId().commit_id();
        return commitRepository.findReachableFrom(headId, limit, offset);
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
