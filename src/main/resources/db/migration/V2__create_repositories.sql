CREATE TABLE repositories (
                              id             UUID PRIMARY KEY,
                              owner_id       UUID NOT NULL REFERENCES users(id),
                              name           TEXT NOT NULL,
                              default_branch TEXT NOT NULL,
                              visibility     TEXT NOT NULL DEFAULT 'public',
                              created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
                              UNIQUE (owner_id, name)
);