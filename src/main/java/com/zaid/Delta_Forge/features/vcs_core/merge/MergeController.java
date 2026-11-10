package com.zaid.Delta_Forge.features.vcs_core.merge;
import com.zaid.Delta_Forge.features.auth.CustomUserDetails;
import com.zaid.Delta_Forge.features.json.ValueSlot;
import com.zaid.Delta_Forge.features.merge.MergeConflict;
import com.zaid.Delta_Forge.features.public_feature.PublicController;
import com.zaid.Delta_Forge.features.vcs_core.branch.Branch;
import com.zaid.Delta_Forge.features.vcs_core.branch.BranchName;
import com.zaid.Delta_Forge.features.vcs_core.branch.BranchService;
import com.zaid.Delta_Forge.features.vcs_core.commit.CommitId;
import com.zaid.Delta_Forge.features.vcs_core.dto.auth.AuthorizationService;
import com.zaid.Delta_Forge.features.vcs_core.dto.branch.BranchResponse;
import com.zaid.Delta_Forge.features.vcs_core.dto.branch.CreateBranchRequest;
import com.zaid.Delta_Forge.features.vcs_core.dto.commit.CommitRequest;
import com.zaid.Delta_Forge.features.vcs_core.dto.commit.CommitResponse;
import com.zaid.Delta_Forge.features.vcs_core.dto.commit.FirstCommitRequest;
import com.zaid.Delta_Forge.features.vcs_core.dto.merge.ConflictDto;
import com.zaid.Delta_Forge.features.vcs_core.dto.merge.MergeRequestDto;
import com.zaid.Delta_Forge.features.vcs_core.dto.merge.MergeResponse;
import com.zaid.Delta_Forge.features.vcs_core.repository.RepositoryId;
import com.zaid.Delta_Forge.features.vcs_core.user.UserId;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.UUID;

@RestController
@RequestMapping("/repositories/{repoId}/merge")
public class MergeController {
    private final MergeService mergeService;
    @Autowired
    private AuthorizationService authorizationService;

    public MergeController(MergeService mergeService) { this.mergeService = mergeService; }

    @PostMapping
    public ResponseEntity<MergeResponse> merge(@PathVariable UUID repoId,
                                               @Valid @RequestBody MergeRequestDto req,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        authorizationService.requireOwner(repoId,userDetails.getUserId());
        UserId author = new UserId(userDetails.getUserId());
        String source = req.source();
        String target = req.target();
        MergeReport report = mergeService.mergeAndReport(
                new RepositoryId(repoId),
                new BranchName(source),
                new BranchName(target),
                new CommitId(req.expectedTargetHead()),
                author,
                "merge" + source + " + " + target);

        return switch (report) {
            case MergeReport.Merged m -> ResponseEntity.ok(new MergeResponse(
                    "MERGED", m.mergeCommitId().commit_id(), m.mergedDocument(), null, null,null));
            case MergeReport.Conflicted c -> ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MergeResponse(
                            "CONFLICTED", null, null, c.partialTree(),
                            c.conflicts().stream().map(this::toConflictDto).toList(),c.mergeRequestId()));
        };
    }

    private ConflictDto toConflictDto(MergeConflict c) {
        JsonNode base    = c.base()   instanceof ValueSlot.Present(var n) ? n : null;
        JsonNode ours    = c.ours()   instanceof ValueSlot.Present(var n) ? n : null;
        JsonNode theirs  = c.theirs() instanceof ValueSlot.Present(var n) ? n : null;
        return new ConflictDto(
                c.path().toString(),
                c.conflictType().name(),
                c.base()   instanceof ValueSlot.Present,   // basePresent
                base,
                c.ours()   instanceof ValueSlot.Present,   // oursPresent
                ours,
                c.theirs() instanceof ValueSlot.Present,   // theirsPresent
                theirs);
    }
}