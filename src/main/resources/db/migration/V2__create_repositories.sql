CREATE TABLE repositories (
                              repository_id             UUID PRIMARY KEY,
                              user_id       UUID NOT NULL REFERENCES users(user_id),
                              name           TEXT NOT NULL,
                              default_branch TEXT NOT NULL,
                              visibility     TEXT NOT NULL DEFAULT 'public',
                              created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
                              UNIQUE (user_id, name)
);