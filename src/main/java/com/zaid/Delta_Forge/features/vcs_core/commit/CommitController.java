package com.zaid.Delta_Forge.features.vcs_core.commit;

import com.zaid.Delta_Forge.features.auth.CustomUserDetails;
import com.zaid.Delta_Forge.features.public_feature.PublicController;
import com.zaid.Delta_Forge.features.vcs_core.branch.Branch;
import com.zaid.Delta_Forge.features.vcs_core.branch.BranchName;
import com.zaid.Delta_Forge.features.vcs_core.branch.BranchService;
import com.zaid.Delta_Forge.features.vcs_core.commit.CommitId;
import com.zaid.Delta_Forge.features.vcs_core.dto.branch.BranchResponse;
import com.zaid.Delta_Forge.features.vcs_core.dto.branch.CreateBranchRequest;
import com.zaid.Delta_Forge.features.vcs_core.dto.commit.CommitRequest;
import com.zaid.Delta_Forge.features.vcs_core.dto.commit.CommitResponse;
import com.zaid.Delta_Forge.features.vcs_core.dto.commit.FirstCommitRequest;
import com.zaid.Delta_Forge.features.vcs_core.repository.RepositoryId;
import com.zaid.Delta_Forge.features.vcs_core.user.UserId;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class CommitController {
    private final CommitService commitService;
    private final BranchService branchService;
    private static final Logger log = LoggerFactory.getLogger(CommitController.class);
    public CommitController(CommitService commitService, BranchService branchService) {
        this.commitService = commitService;
        this.branchService = branchService;
    }
    @PostMapping("/repositories/{repoId}/init-commit")
    @ResponseStatus(HttpStatus.CREATED)
    public CommitResponse firstCommit(@PathVariable UUID repoId,
                                 @Valid @RequestBody FirstCommitRequest req,
                                 @AuthenticationPrincipal CustomUserDetails auth) {
        UserId author = new UserId(auth.getUserId());

        // 1. Insert commit (creates snapshot internally, validates parents)
        Commit c = commitService.createFirstCommit(new RepositoryId(repoId),new BranchName(req.branchName()),req.document(),new UserId(auth.getUserId()),req.message());

        // 2. CAS the branch head — throws StaleHeadException on mismatch
        branchService.casHead(new RepositoryId(repoId),
                new BranchName(req.branchName()),
                new CommitId(c.id().commit_id()),
                c.id());

        return toResponse(c, req.branchName());
    }
    @PostMapping("/repositories/{repoId}/commit")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public CommitResponse commit(@PathVariable UUID repoId,
                                 @Valid @RequestBody CommitRequest req,
                                 @AuthenticationPrincipal CustomUserDetails auth) {
        UserId author = new UserId(auth.getUserId());
        log.atDebug().setMessage("expectedHead = " + req.expectedHead()).log();
        // 1. Insert commit (creates snapshot internally, validates parents)
        Commit c = commitService.commit(
                new RepositoryId(repoId),
                new BranchName(req.branch()),
                new CommitId(req.expectedHead()),
                req.document(),
                author,
                req.message());

        // 2. CAS the branch head — throws StaleHeadException on mismatch
//        branchService.casHead(new RepositoryId(repoId),
//                new BranchName(req.branch()),
//                new CommitId(req.expectedHead()),
//                c.id());

        return toResponse(c, req.branch());
    }

    @GetMapping("/repositories/{repoId}/get-commits")
    public List<Commit> history(@PathVariable UUID repoId,
                                        @RequestParam String branch,
                                        @RequestParam(defaultValue = "0")  int offset,
                                        @RequestParam(defaultValue = "50") int limit) {
//        return commitService.listCommits(new RepositoryId(repoId), new BranchName(branch), limit, offset)
//                .stream().map(s -> toResponse(s, branch)).toList();
        return commitService.listCommits(new RepositoryId(repoId),new BranchName(branch),10,0);
    }

    @GetMapping("/repositories/{repoId}/{headCommitId}")
    public CommitResponse get(@PathVariable UUID repoId, @PathVariable UUID commitId) {
        Commit c = commitService.getCommit( new CommitId(commitId),new RepositoryId(repoId));
        return toResponse(c, null);
    }

    private CommitResponse toResponse(Commit c, String branch) {
        return new CommitResponse(
                c.id().commit_id(),          // CommitId → UUID
                branch,
                c.snapshotHash().hash(),     // SnapshotHash → String
                c.authorId().user_id(),      // UserId → UUID
                c.message(),
                c.createdAt());
    }

//    private CommitResponse toResponse(CommitSummary c, String branch) {
//        return new CommitResponse(c.id().commit_id(), branch, c.snapshotHash().hash(),
//                c., c.message(), c.createdAt(),
//                c.parents().stream().map(CommitId::commit_id).toList());
//    }
}