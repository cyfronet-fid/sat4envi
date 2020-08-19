-- Drop indexes which depend on f_cast_isots, which will be dropped and recreated.
DROP INDEX idx_sensing_time;
DROP INDEX idx_ingestion_time;

-- Drop and recreate f_cast_isots, this time handling timezone better.
DROP FUNCTION f_cast_isots(text);
CREATE OR REPLACE FUNCTION f_cast_isots(text varchar) RETURNS TIMESTAMP AS
$$
SELECT text::TIMESTAMPTZ AT TIME ZONE 'UTC'
$$ LANGUAGE sql IMMUTABLE;

-- Recreate the indexes.
CREATE INDEX IF NOT EXISTS idx_sensing_time ON scene using btree (f_cast_isots(metadata_content->>'sensing_time'));
CREATE INDEX IF NOT EXISTS idx_ingestion_time ON scene using btree (f_cast_isots(metadata_content->>'ingestion_time'));
