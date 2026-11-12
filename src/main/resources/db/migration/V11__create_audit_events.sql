CREATE TABLE audit_events (
                              id              UUID PRIMARY KEY,
                              merge_request_id UUID NOT NULL REFERENCES merge_requests(id),
                              conflict_id     TEXT NOT NULL,
                              event_type      TEXT NOT NULL,      -- SUGGESTION_REQUESTED, SUGGESTION_VALIDATION_FAILED, USER_ACCEPTED, USER_EDITED, USER_REJECTED
                              actor           TEXT NOT NULL,      -- username (or "system" for AI events)
                              details         JSONB,              -- action, proposed value, reason, etc.
                              created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);