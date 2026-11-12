
ALTER TABLE resolutions DROP CONSTRAINT resolutions_resolved_by_fkey;
ALTER TABLE resolutions ALTER COLUMN resolved_by TYPE TEXT;