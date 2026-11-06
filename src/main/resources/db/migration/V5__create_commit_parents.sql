CREATE TABLE commit_parents (
                                commit_id  UUID NOT NULL REFERENCES commits(id),
                                parent_id  UUID NOT NULL REFERENCES commits(id),
                                position   SMALLINT NOT NULL CHECK (position IN (0, 1)),
                                PRIMARY KEY (commit_id, position)
);
-- For "history walk" (recursive CTE)
-- Already covered by PRIMARY KEY (commit_id, position) — the FK on parent_id also needs one
CREATE INDEX commit_parents_parent_idx ON commit_parents(parent_id);
