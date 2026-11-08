package com.zaid.Delta_Forge.features.vcs_core.repository;

import com.zaid.Delta_Forge.features.auth.CustomUserDetails;
import com.zaid.Delta_Forge.features.vcs_core.dto.repo.CreateRepositoryRequest;
import com.zaid.Delta_Forge.features.vcs_core.dto.repo.RepositoryResponse;
import com.zaid.Delta_Forge.features.vcs_core.user.UserId;
import jakarta.validation.Valid;
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

    public RepositoryController(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RepositoryResponse create(@Valid @RequestBody CreateRepositoryRequest req,
                                     @AuthenticationPrincipal CustomUserDetails auth) {
        UserId owner = new UserId(auth.getUserId());
        Repository repo = repositoryService.init(req.name(), req.defaultBranch(), owner);
        return toResponse(repo);
    }

//    @GetMapping
//    public List<RepositoryResponse> list(Authentication auth) {
//        UserId owner = ((CustomUserDetails) auth.getPrincipal()).getUserId();
//        return repositoryService.list(owner).stream().map(this::toResponse).toList();
//    }

    @GetMapping("/{id}")
    public RepositoryResponse get(@PathVariable UUID id) {
        return toResponse(repositoryService.get(new RepositoryId(id)));
    }

    private RepositoryResponse toResponse(Repository r) {
        return new RepositoryResponse(r.id().repository_id(), r.name(), r.defaultBranch(),
                r.visibility(), r.createdAt());
    }
}