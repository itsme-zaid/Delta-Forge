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