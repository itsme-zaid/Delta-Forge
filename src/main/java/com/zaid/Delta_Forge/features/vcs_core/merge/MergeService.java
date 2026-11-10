package com.zaid.Delta_Forge.features.vcs_core.merge;

import com.zaid.Delta_Forge.features.json.JsonDocumentService;
import com.zaid.Delta_Forge.features.json.ValueSlot;
import com.zaid.Delta_Forge.features.merge.MergeConflict;
import com.zaid.Delta_Forge.features.merge.MergeEngine;
import com.zaid.Delta_Forge.features.merge.MergeResult;
import com.zaid.Delta_Forge.features.vcs_core.blob.Snapshot;
import com.zaid.Delta_Forge.features.vcs_core.blob.SnapshotService;
import com.zaid.Delta_Forge.features.vcs_core.branch.Branch;
import com.zaid.Delta_Forge.features.vcs_core.branch.BranchName;
import com.zaid.Delta_Forge.features.vcs_core.branch.BranchService;
import com.zaid.Delta_Forge.features.vcs_core.commit.Commit;
import com.zaid.Delta_Forge.features.vcs_core.commit.CommitId;
import com.zaid.Delta_Forge.features.vcs_core.commit.CommitRepository;
import com.zaid.Delta_Forge.features.vcs_core.commit.CommitService;
import com.zaid.Delta_Forge.features.vcs_core.exceptions.StaleHeadException;
import com.zaid.Delta_Forge.features.vcs_core.merge.merge_request.MergeRequest;
import com.zaid.Delta_Forge.features.vcs_core.repository.RepositoryId;
import com.zaid.Delta_Forge.features.vcs_core.user.UserId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MergeService {
    @Autowired
    private BranchService branchService;
    @Autowired
    private SnapshotService snapshotService;
    @Autowired
    private CommitService commitService;
    @Autowired
    private CommitRepository commitRepository;
    private final JdbcAggregateTemplate mergeRequestTemplate;
    private ObjectMapper objectMapper;
    private final JsonDocumentService jsonDocumentService;
    private JdbcAggregateTemplate template;
    private final MergeEngine mergeEngine;

    public MergeService(JdbcAggregateTemplate template,JsonDocumentService jsonDocumentService,JdbcAggregateTemplate mergeRequestTemplate, ObjectMapper objectMapper) {
        this.jsonDocumentService = jsonDocumentService;
        this.mergeEngine = new MergeEngine(jsonDocumentService);
        this.template = template;
        this.objectMapper = objectMapper;
        this.mergeRequestTemplate = mergeRequestTemplate;
    }

    public MergeResult merge(RepositoryId repoId, BranchName ours, BranchName theirs) {
        //RepositoryState state = requireState(repoId);

        Branch oursBranch = branchService.findByRepoIdAndName(repoId, ours);

        Branch theirsBranch = branchService.findByRepoIdAndName(repoId, theirs);

        CommitId oursHead = oursBranch.headCommitId();
        CommitId theirsHead = theirsBranch.headCommitId();
        CommitId base = commitService.findBestCommonAncestor(oursHead, theirsHead);
        JsonNode baseDoc = snapshotService.findBySnapshotHash(repoId,commitService.getCommit(base, repoId).snapshotHash()).document();
        JsonNode oursDoc = snapshotService.findBySnapshotHash(repoId,commitService.getCommit(oursBranch.headCommitId(), repoId).snapshotHash()).document();
        JsonNode theirsDoc = snapshotService.findBySnapshotHash(repoId,commitService.getCommit(theirsBranch.headCommitId(), repoId).snapshotHash()).document();
        return mergeEngine.merge(JsonPointer.compile(""), oursDoc, theirsDoc, baseDoc);
    }

    @Transactional public MergeReport mergeAndReport(RepositoryId repoId, BranchName sourceBranch, BranchName targetBranch,
                                      CommitId expectedTargetHead, UserId authorId, String message) {
        MergeResult result = merge(repoId, sourceBranch, targetBranch);   // pure compute
        if (result instanceof MergeResult.MergeConflicted c) {
            // Persist a merge request; nothing is committed
            Branch source = branchService.findByRepoIdAndName(repoId, sourceBranch);
            Branch target = branchService.findByRepoIdAndName(repoId, targetBranch);
            CommitId baseId = commitService.findBestCommonAncestor(
                    target.headCommitId(), source.headCommitId());

            MergeRequest mr = new MergeRequest(
                    UUID.randomUUID(),
                    repoId.repository_id(),
                    target.id(),                          // target branch
                    source.id(),                          // source branch
                    baseId.commit_id(),                               // base commit
                    target.headCommitId().commit_id(),                // ours commit
                    source.headCommitId().commit_id(),                // theirs commit
                    c.partialMergedOutcome(),
                    serializeConflicts(c.conflicts()),
                    MergeRequestStatus.OPEN.name(),
                    0,
                    OffsetDateTime.now(),
                    null);                                // appliedAt = null while OPEN

            mergeRequestTemplate.insert(mr);

            return new MergeReport.Conflicted(mr.id(), c.partialMergedOutcome(), c.conflicts());
        }
        MergeResult.MergeSuccess s = (MergeResult.MergeSuccess) result;
        JsonNode merged = ((ValueSlot.Present) s.mergedOutcome()).node();

        Commit mergeCommit = applyMerge(
                repoId, sourceBranch, targetBranch,
                expectedTargetHead, merged, authorId, message);

        return new MergeReport.Merged(mergeCommit.id(), merged);
    }
    private JsonNode serializeConflicts(List<MergeConflict> conflicts) {
        return objectMapper.valueToTree(conflicts);
    }
    @Transactional
    public Commit applyMerge(RepositoryId repoId, BranchName sourceBranchName, BranchName targetBranchName,
                             CommitId expectedTargetHead, JsonNode resolvedDocument,
                             UserId author, String message) {

        // 1. Load branch to get the source head (read-only, no lock needed)
        Branch src = branchService.findByRepoIdAndName(repoId, sourceBranchName);
        CommitId sourceHead = src.headCommitId();
        Branch target = branchService.findByRepoIdAndName(repoId, targetBranchName);
        if (!target.headCommitId().equals(expectedTargetHead)) {
            throw new StaleHeadException(
                    "target " + targetBranchName + " moved from " + expectedTargetHead
                            + " to " + target.headCommitId());
        }
        // 2. Store the snapshot (dedup by hash)
        Snapshot snapshot = snapshotService.saveSnapshot(repoId,resolvedDocument);

        // 3. Insert the commit (with parents)
        Commit commit = commitService.createCommit(repoId, snapshot.hash(),author, message);
        template.insert(commit);
        commitRepository.insertParent(commit.id().commit_id(),expectedTargetHead.commit_id(),0);
        commitRepository.insertParent(commit.id().commit_id(),sourceHead.commit_id(),1);

        // 4. CAS on the branch head — this is the atomic check-and-set
        int rowsUpdated = branchService.casHead(repoId, targetBranchName, expectedTargetHead, commit.id());

        if (rowsUpdated == 0) {
            throw new StaleHeadException(
                    "target " + target + " moved from " + expectedTargetHead);
        }

        return commit;
    }

}
