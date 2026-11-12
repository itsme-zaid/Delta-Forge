package com.zaid.Delta_Forge.features.vcs_core.merge.merge_request;

import com.zaid.Delta_Forge.features.merge.MergeConflict;
import com.zaid.Delta_Forge.features.vcs_core.dto.ConflictWithId;
import com.zaid.Delta_Forge.features.vcs_core.dto.merge.MergeRequestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/merge-requests")
public class MergeRequestController {
    @Autowired
    private MergeRequestService service;
    private final ObjectMapper objectMapper;

    public MergeRequestController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    @GetMapping("/{id}")
    public MergeRequestResponse get(@PathVariable UUID id) {
        return toResponse(service.get(id));
    }

    @GetMapping
    public List<MergeRequestResponse> list(@RequestParam UUID repoId) {
        return service.listByRepo(repoId).stream()
                .map(this::toResponse)
                .toList();
    }

//    private MergeRequestResponse toResponse(MergeRequest mr) { ... }
private MergeRequestResponse toResponse(MergeRequest mr) {
//    List<MergeConflict> conflicts = deserializeConflicts(mr.conflicts());
//    List<ConflictWithId> withIds = conflicts.stream()
//            .map(ConflictWithId::from)
//            .toList();

    return new MergeRequestResponse(
            mr.id(),
            mr.repositoryId(),
            mr.targetBranchId(),
            mr.sourceBranchId(),
            mr.baseCommitId(),
            mr.oursCommitId(),
            mr.theirsCommitId(),
            mr.partialTree(),
            deserializeConflicts(mr.conflicts()),
            mr.status(),
            mr.revision(),
            mr.createdAt(),
            mr.appliedAt()
    );
}

    private List<ConflictWithId> deserializeConflicts(JsonNode conflictsJson) {
        try {
            return objectMapper.readerForListOf(ConflictWithId.class)
                    .readValue(conflictsJson);
        } catch (Exception e) {
            throw new IllegalStateException("failed to deserialize conflicts", e);
        }
    }
}