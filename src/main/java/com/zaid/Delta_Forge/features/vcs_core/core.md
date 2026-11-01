### Current Schema : 
-- Repositories
CREATE TABLE repositories (
id             UUID PRIMARY KEY,
name           TEXT NOT NULL UNIQUE,
owner          TEXT NOT NULL,
default_branch TEXT NOT NULL,
created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Snapshots (content-addressed)
CREATE TABLE snapshots (
repository_id          UUID NOT NULL REFERENCES repositories(id),
hash                   CHAR(64) NOT NULL,           -- sha-256 hex
document               JSONB NOT NULL,              -- parsed tree, for fast read
canonical_bytes        BYTEA NOT NULL,              -- exact bytes that hashed
normalization_version  TEXT NOT NULL,               -- 'normalization-v1'
created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
PRIMARY KEY (repository_id, hash)
);

-- Commits
CREATE TABLE commits (
id              UUID PRIMARY KEY,
repository_id   UUID NOT NULL REFERENCES repositories(id),
snapshot_hash   CHAR(64) NOT NULL,
author          TEXT NOT NULL,
message         TEXT NOT NULL,
created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
FOREIGN KEY (repository_id, snapshot_hash)
REFERENCES snapshots(repository_id, hash)
);

-- Parent edges (0, 1, or 2 per commit)
CREATE TABLE commit_parents (
commit_id  UUID NOT NULL REFERENCES commits(id),
parent_id  UUID NOT NULL REFERENCES commits(id),
position   SMALLINT NOT NULL CHECK (position IN (0, 1)),
PRIMARY KEY (commit_id, position)
);

-- Branches
CREATE TABLE branches (
id             UUID PRIMARY KEY,
repository_id  UUID NOT NULL REFERENCES repositories(id),
name           TEXT NOT NULL,
head_commit_id UUID NOT NULL REFERENCES commits(id),
created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
UNIQUE (repository_id, name)
);

-- Merge requests (stub for Day 35; expands on Day 24)
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
status            TEXT NOT NULL,
revision          INT NOT NULL DEFAULT 0,
created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
applied_at        TIMESTAMPTZ
);

-- Resolutions (Day 24+; rows, not JSONB, because they get updated)
CREATE TABLE resolutions (
id              UUID PRIMARY KEY,
merge_request_id UUID NOT NULL REFERENCES merge_requests(id),
conflict_id     TEXT NOT NULL,                       -- "c-7" or similar
resolved_value  JSONB,                               -- null means "deleted"
resolved_present BOOLEAN NOT NULL,                   -- D4: distinguishes null from missing
source          TEXT NOT NULL,                       -- MANUAL | AI_ACCEPTED | AI_EDITED
resolved_by     TEXT NOT NULL,
explanation     TEXT,
created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);


