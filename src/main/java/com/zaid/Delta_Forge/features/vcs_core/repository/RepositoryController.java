package com.zaid.Delta_Forge.features.vcs_core.repository;

import com.zaid.Delta_Forge.features.auth.CustomUserDetails;
import com.zaid.Delta_Forge.features.vcs_core.dto.auth.AuthorizationService;
import com.zaid.Delta_Forge.features.vcs_core.dto.repo.CreateRepositoryRequest;
import com.zaid.Delta_Forge.features.vcs_core.dto.repo.RepositoryResponse;
import com.zaid.Delta_Forge.features.vcs_core.user.UserId;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/repositories")
public class RepositoryController {
    private final RepositoryService repositoryService;
    @Autowired
    private AuthorizationService authorizationService;
    public RepositoryController(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RepositoryResponse create(@Valid @RequestBody CreateRepositoryRequest req,
                                     @AuthenticationPrincipal CustomUserDetails auth) {

        Repository repo = repositoryService.init(req.name(), req.defaultBranch(), req.visibility().toString(),new UserId(auth.getUserId()));
        return toResponse(repo);
    }

    @GetMapping
    public List<RepositoryResponse> list(@AuthenticationPrincipal CustomUserDetails userDetails) {

        return repositoryService.list(new UserId(userDetails.getUserId())).stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}/get-repo")
    public RepositoryResponse get(@PathVariable UUID id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        authorizationService.requireReadable(id,userDetails.getUserId());
        return toResponse(repositoryService.get(new RepositoryId(id)));
    }

    private RepositoryResponse toResponse(Repository r) {
        return new RepositoryResponse(r.id().repository_id(), r.name(), r.defaultBranch(),
                r.visibility(), r.createdAt());
    }
}