package com.zaid.Delta_Forge.features.vcs_core.merge;

import com.zaid.Delta_Forge.features.json.JsonDocumentService;
import com.zaid.Delta_Forge.features.json.ValueSlot;
import com.zaid.Delta_Forge.features.merge.MergeEngine;
import com.zaid.Delta_Forge.features.merge.MergeResult;
import com.zaid.Delta_Forge.features.vcs_core.blob.Snapshot;
import com.zaid.Delta_Forge.features.vcs_core.blob.SnapshotService;
import com.zaid.Delta_Forge.features.vcs_core.branch.Branch;
import com.zaid.Delta_Forge.features.vcs_core.branch.BranchName;
import com.zaid.Delta_Forge.features.vcs_core.branch.BranchService;
import com.zaid.Delta_Forge.features.vcs_core.commit.Commit;
import com.zaid.Delta_Forge.features.vcs_core.commit.CommitId;
import com.zaid.Delta_Forge.features.vcs_core.commit.CommitService;
import com.zaid.Delta_Forge.features.vcs_core.exceptions.StaleHeadException;
import com.zaid.Delta_Forge.features.vcs_core.repository.RepositoryId;
import com.zaid.Delta_Forge.features.vcs_core.user.UserId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;

import java.util.List;

@Service
public class MergeService {
    @Autowired
    private BranchService branchService;
    @Autowired
    private SnapshotService snapshotService;
    @Autowired
    private CommitService commitService;
    private final JsonDocumentService jsonDocumentService;
    private final MergeEngine mergeEngine;

    public MergeService(JsonDocumentService jsonDocumentService) {
        this.jsonDocumentService = jsonDocumentService;
        this.mergeEngine = new MergeEngine(jsonDocumentService);
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

    public MergeReport mergeAndReport(RepositoryId repoId, BranchName source, BranchName target,
                                      CommitId expectedTargetHead, UserId author, String message) {
        MergeResult r = merge(repoId, source, target);   // pure compute
        return switch (r) {
            case MergeResult.MergeSuccess s -> {
                // Only apply on success
                JsonNode merged = ((ValueSlot.Present) s.mergedOutcome()).node();
                Commit c = applyMerge(repoId, source, target, expectedTargetHead, merged, author, message);
                yield new MergeReport.Merged(c.id(), merged);
            }
            case MergeResult.MergeConflicted c -> {
                // Do NOT apply — return the report to the caller
                yield new MergeReport.Conflicted(c.partialMergedOutcome(), c.conflicts());
            }
        };
    }
    @Transactional
    public Commit applyMerge(RepositoryId repoId, BranchName source, BranchName target,
                             CommitId expectedTargetHead, JsonNode resolvedDocument,
                             UserId author, String message) {

        // 1. Load branch to get the source head (read-only, no lock needed)
        Branch src = branchService.findByRepoIdAndName(repoId, source);
        CommitId sourceHead = src.headCommitId();

        // 2. Store the snapshot (dedup by hash)
        Snapshot snapshot = snapshotService.saveSnapshot(resolvedDocument);

        // 3. Insert the commit (with parents)
        Commit commit = commitService.createCommit(repoId, snapshot.hash(),
                List.of(expectedTargetHead, sourceHead), author, message);

        // 4. CAS on the branch head — this is the atomic check-and-set
        int rowsUpdated = branchService.casHead(repoId, target, expectedTargetHead, commit.id());

        if (rowsUpdated == 0) {
            throw new StaleHeadException(
                    "target " + target + " moved from " + expectedTargetHead);
        }

        return commit;
    }

}
