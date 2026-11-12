package com.zaid.Delta_Forge.features.vcs_core.branch;

import com.zaid.Delta_Forge.features.auth.CustomUserDetails;
import com.zaid.Delta_Forge.features.vcs_core.commit.CommitId;
import com.zaid.Delta_Forge.features.vcs_core.dto.auth.AuthorizationService;
import com.zaid.Delta_Forge.features.vcs_core.dto.branch.BranchResponse;
import com.zaid.Delta_Forge.features.vcs_core.dto.branch.CreateBranchRequest;
import com.zaid.Delta_Forge.features.vcs_core.repository.RepositoryId;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/repositories/{repoId}/branches")
public class BranchController {
    private final BranchService branchService;
    @Autowired
    private AuthorizationService authorizationService;
    public BranchController(BranchService branchService) { this.branchService = branchService; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BranchResponse create(@PathVariable UUID repoId,
                                 @Valid @RequestBody CreateBranchRequest req, @AuthenticationPrincipal CustomUserDetails userDetails) {
        authorizationService.requireOwner(repoId,userDetails.getUserId());
        Branch b = branchService.createBranch(new RepositoryId(repoId),
                new BranchName(req.name()), new CommitId(req.fromCommitId()));
        return toResponse(b);
    }

    @GetMapping
    public List<BranchResponse> list(@PathVariable UUID repoId) {
        return branchService.listByRepo(new RepositoryId(repoId)).stream()
                .map(this::toResponse).toList();
    }

    @GetMapping("/{name}")
    public BranchResponse get(@PathVariable UUID repoId, @PathVariable String name, @AuthenticationPrincipal CustomUserDetails userDetails) {
        authorizationService.requireReadable(repoId,userDetails.getUserId());
        return toResponse(branchService.findByRepoIdAndName(new RepositoryId(repoId), new BranchName(name)));
    }

    private BranchResponse toResponse(Branch b) {
        return new BranchResponse(b.id(), b.name().branch_name(), b.headCommitId().commit_id(),
                b.createdAt(), b.updatedAt());
    }
}