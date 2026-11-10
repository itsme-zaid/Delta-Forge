package com.zaid.Delta_Forge.features.vcs_core.merge.merge_request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/merge-requests")
public class MergeRequestController {
    @Autowired
    private MergeRequestService service;

    @GetMapping("/{id}")
    public MergeRequest get(@PathVariable UUID id) {
        return service.get(id);
    }

    @GetMapping
    public List<MergeRequest> list(@RequestParam UUID repoId) {
        return service.listByRepo(repoId);
    }

//    private MergeRequestResponse toResponse(MergeRequest mr) { ... }
}