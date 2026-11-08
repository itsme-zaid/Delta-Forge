package com.zaid.Delta_Forge.features.vcs_core.dto.merge;



import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.UUID;

public record MergeResponse(
        String status,              // "MERGED" or "CONFLICTED"
        UUID mergeCommitId,         // non-null iff status == "MERGED"
        JsonNode mergedDocument,    // non-null iff status == "MERGED"
        JsonNode partialTree,       // non-null iff status == "CONFLICTED"
        List<ConflictDto> conflicts // non-null iff status == "CONFLICTED"
) {}