package com.zaid.Delta_Forge.features.vcs_core.dto.auth;

import com.zaid.Delta_Forge.features.vcs_core.exceptions.ForbiddenException;
import com.zaid.Delta_Forge.features.vcs_core.repository.Repository;
import com.zaid.Delta_Forge.features.vcs_core.repository.RepositoryService;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class AuthorizationService {

    private final RepositoryService repositoryService;

    public AuthorizationService(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    /** Throws 403 if the user is not the owner. */
    public void requireOwner(UUID repoId, UUID userId) {
        Repository repo = repositoryService.getById(repoId);
        if(repo == null) throw new RuntimeException("Repository does not exist");
        if (!repo.ownerId().user_id().equals(userId)) {
            throw new ForbiddenException("not the owner of repository " + repoId);
        }
    }

    /** Throws 404 if the user can't see the repo (private and not owner). */
    public void requireReadable(UUID repoId, UUID userId) {
        Repository repo = repositoryService.getById(repoId);
        if(repo == null) throw new RuntimeException("Repository does not exist");
        boolean isOwner = repo.ownerId().user_id().equals(userId);
        boolean isPublic = "public".equals(repo.visibility());
        if (!isOwner && !isPublic) {
            // 404, not 403, to avoid leaking that a private repo exists
            throw new NoSuchElementException("repository not found: " + repoId);
        }
    }
}