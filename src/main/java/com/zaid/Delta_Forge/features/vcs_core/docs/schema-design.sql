-- V1: users
CREATE TABLE users (
                       id            UUID PRIMARY KEY,
                       username      TEXT NOT NULL UNIQUE,
                       password_hash TEXT NOT NULL,
                       created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- V2: repositories
CREATE TABLE repositories (
                              id             UUID PRIMARY KEY,
                              owner_id       UUID NOT NULL REFERENCES users(id),
                              name           TEXT NOT NULL,
                              default_branch TEXT NOT NULL,
                              visibility     TEXT NOT NULL DEFAULT 'public',
                              created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
                              UNIQUE (owner_id, name)
);

-- V3: snapshots
CREATE TABLE snapshots (
                           repository_id         UUID NOT NULL REFERENCES repositories(id),
                           hash                  CHAR(64) NOT NULL,
                           document              JSONB NOT NULL,
                           canonical_bytes       BYTEA NOT NULL,
                           normalization_version TEXT NOT NULL,
                           created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
                           PRIMARY KEY (repository_id, hash)
);

-- V4: commits
CREATE TABLE commits (
                         id              UUID PRIMARY KEY,
                         repository_id   UUID NOT NULL REFERENCES repositories(id),
                         snapshot_hash   CHAR(64) NOT NULL,
                         author_id       UUID NOT NULL REFERENCES users(id),
                         message         TEXT NOT NULL,
                         created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                         FOREIGN KEY (repository_id, snapshot_hash)
                             REFERENCES snapshots (repository_id, hash)
);

-- V5: commit_parents
CREATE TABLE commit_parents (
                                commit_id  UUID NOT NULL REFERENCES commits(id),
                                parent_id  UUID NOT NULL REFERENCES commits(id),
                                position   SMALLINT NOT NULL CHECK (position IN (0, 1)),
                                PRIMARY KEY (commit_id, position)
);

-- V6: branches
CREATE TABLE branches (
                          id             UUID PRIMARY KEY,
                          repository_id  UUID NOT NULL REFERENCES repositories(id),
                          name           TEXT NOT NULL,
                          head_commit_id UUID NOT NULL REFERENCES commits(id),
                          created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
                          updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
                          UNIQUE (repository_id, name)
);

-- V7: merge_requests
CREATE TABLE merge_requests (
                                id                UUID PRIMARY KEY,
                                repository_id     UUID NOT NULL REFERENCES repositories(id),
                                target_branch_id  UUID NOT NULL REFERENCES branches(id),
                                source_branch_id  UUID NOT NULL REFERENCES branches(id),
                                base_commit_id    UUID REFERENCES commits(id),
                                ours_commit_id    UUID NOT NULL REFERENCES commits(id),
                                theirs_commit_id  UUID NOT NULL REFERENCES commits(id),
                                partial_tree      JSONB NOT NULL,
                                conflicts         JSONB NOT NULL,
                                status            TEXT NOT NULL DEFAULT 'OPEN',
                                revision          INT NOT NULL DEFAULT 0,
                                created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                applied_at        TIMESTAMPTZ
);

-- V8: resolutions
CREATE TABLE resolutions (
                             id                UUID PRIMARY KEY,
                             merge_request_id  UUID NOT NULL REFERENCES merge_requests(id),
                             conflict_id       TEXT NOT NULL,
                             resolved_value    JSONB,
                             resolved_present  BOOLEAN NOT NULL,
                             source            TEXT NOT NULL,
                             resolved_by       UUID NOT NULL REFERENCES users(id),
                             explanation       TEXT,
                             created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                             UNIQUE (merge_request_id, conflict_id)
);

-- For "list branches in a repo" (already covered by UNIQUE)
-- For "commits in a repo" (listCommits)
CREATE INDEX commits_repo_created_idx ON commits(repository_id, created_at DESC);

-- For "commits referencing a snapshot" (rare, but cheap)
CREATE INDEX commits_snapshot_idx ON commits(repository_id, snapshot_hash);

-- For "history walk" (recursive CTE)
-- Already covered by PRIMARY KEY (commit_id, position) — the FK on parent_id also needs one
CREATE INDEX commit_parents_parent_idx ON commit_parents(parent_id);

-- For "open merge requests in a repo" (Day 41's list endpoint)
CREATE INDEX merge_requests_repo_status_idx ON merge_requests(repository_id, status);