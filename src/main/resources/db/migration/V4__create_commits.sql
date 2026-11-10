CREATE TABLE commits (
                         commit_id              UUID PRIMARY KEY,
                         repository_id   UUID NOT NULL REFERENCES repositories(repository_id),
                         snapshot_hash   CHAR(64) NOT NULL,
                         user_id       UUID NOT NULL REFERENCES users(user_id),
                         message         TEXT NOT NULL,
                         created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                         FOREIGN KEY (repository_id, snapshot_hash)
                             REFERENCES snapshots (repository_id, hash)
);
-- For "list branches in a repo" (already covered by UNIQUE)
-- For "commits in a repo" (listCommits)
CREATE INDEX commits_repo_created_idx ON commits(repository_id, created_at DESC);

-- For "commits referencing a snapshot" (rare, but cheap)
CREATE INDEX commits_snapshot_idx ON commits(repository_id, snapshot_hash);
