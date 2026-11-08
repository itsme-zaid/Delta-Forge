CREATE TABLE resolutions (
                             id                UUID PRIMARY KEY,
                             merge_request_id  UUID NOT NULL REFERENCES merge_requests(id),
                             conflict_id       TEXT NOT NULL,
                             resolved_value    JSONB,
                             resolved_present  BOOLEAN NOT NULL,
                             source            TEXT NOT NULL,
                             resolved_by       UUID NOT NULL REFERENCES users(user_id),
                             explanation       TEXT,
                             created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                             UNIQUE (merge_request_id, conflict_id)
);