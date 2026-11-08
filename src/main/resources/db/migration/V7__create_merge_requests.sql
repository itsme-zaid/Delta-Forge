CREATE TABLE merge_requests (
                                id                UUID PRIMARY KEY,
                                repository_id     UUID NOT NULL REFERENCES repositories(repository_id),
                                target_branch_id  UUID NOT NULL REFERENCES branches(id),
                                source_branch_id  UUID NOT NULL REFERENCES branches(id),
                                base_commit_id    UUID REFERENCES commits(commit_id),
                                ours_commit_id    UUID NOT NULL REFERENCES commits(commit_id),
                                theirs_commit_id  UUID NOT NULL REFERENCES commits(commit_id),
                                partial_tree      JSONB NOT NULL,
                                conflicts         JSONB NOT NULL,
                                status            TEXT NOT NULL DEFAULT 'OPEN',
                                revision          INT NOT NULL DEFAULT 0,
                                created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                applied_at        TIMESTAMPTZ
);
-- For "open merge requests in a repo" (Day 41's list endpoint)
CREATE INDEX merge_requests_repo_status_idx ON merge_requests(repository_id, status);