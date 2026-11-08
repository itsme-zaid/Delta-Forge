CREATE TABLE snapshots (
                           repository_id         UUID NOT NULL REFERENCES repositories(repository_id),
                           hash                  CHAR(64) NOT NULL,
                           document              JSONB NOT NULL,
                           canonical_bytes       BYTEA NOT NULL,
                           normalization_version TEXT NOT NULL,
                           created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
                           PRIMARY KEY (repository_id, hash)
);