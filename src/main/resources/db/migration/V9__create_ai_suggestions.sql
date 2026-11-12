CREATE TABLE ai_suggestions (
                                id                UUID PRIMARY KEY,
                                merge_request_id  UUID NOT NULL REFERENCES merge_requests(id),
                                conflict_id       TEXT NOT NULL,
                                action            TEXT NOT NULL,
                                proposed_value    JSONB,
                                explanation       TEXT,
                                status            TEXT NOT NULL DEFAULT 'PENDING',
                                provider          TEXT,
                                created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ai_suggestions_mr_idx ON ai_suggestions(merge_request_id, conflict_id);