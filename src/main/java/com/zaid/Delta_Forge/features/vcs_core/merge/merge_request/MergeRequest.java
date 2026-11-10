package com.zaid.Delta_Forge.features.vcs_core.merge.merge_request;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import tools.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.UUID;

@Table("merge_requests")
public record MergeRequest(
        @Id UUID id,
        @Column("repository_id") UUID repositoryId,
        @Column("target_branch_id") UUID targetBranchId,
        @Column("source_branch_id") UUID sourceBranchId,
        @Column("base_commit_id") UUID baseCommitId,
        @Column("ours_commit_id") UUID oursCommitId,
        @Column("theirs_commit_id") UUID theirsCommitId,
        @Column("partial_tree") JsonNode partialTree,
        @Column("conflicts") JsonNode conflicts,
        @Column("status") String status,
        @Column("revision") int revision,
        @Column("created_at") OffsetDateTime createdAt,
        @Column("applied_at") OffsetDateTime appliedAt
) {}