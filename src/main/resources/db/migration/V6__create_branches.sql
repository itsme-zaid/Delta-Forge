-- V6: branches
CREATE TABLE branches (
                          id             UUID PRIMARY KEY,
                          repository_id  UUID NOT NULL REFERENCES repositories(repository_id),
                          branch_name           TEXT NOT NULL,
                          head_commit_id UUID NOT NULL REFERENCES commits(commit_id),
                          created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
                          updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
                          UNIQUE (repository_id, branch_name)
);