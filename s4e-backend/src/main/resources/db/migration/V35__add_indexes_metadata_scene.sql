CREATE OR REPLACE FUNCTION f_cast_isots(text)
  RETURNS timestamptz AS
$$SELECT to_timestamp($1, 'YYYY-MM-DD"T"HH24:MI:SS"Z"')$$
  LANGUAGE sql IMMUTABLE;

CREATE INDEX IF NOT EXISTS idx_sensing_time ON scene using btree (f_cast_isots(metadata_content->>'sensing_time'));
CREATE INDEX IF NOT EXISTS idx_ingestion_time ON scene using btree (f_cast_isots(metadata_content->>'ingestion_time'));

CREATE INDEX IF NOT EXISTS idx_cloud_cover ON scene using btree (((metadata_content ->> 'cloud_cover')::float));

CREATE INDEX IF NOT EXISTS idx_spacecraft ON scene using hash ((metadata_content->>'spacecraft') varchar_pattern_ops);
CREATE INDEX IF NOT EXISTS idx_processing_level ON scene using hash ((metadata_content->>'processing_level') varchar_pattern_ops);
CREATE INDEX IF NOT EXISTS idx_product_type ON scene using hash ((metadata_content->>'product_type') varchar_pattern_ops);
CREATE INDEX IF NOT EXISTS idx_polarisation ON scene using hash ((metadata_content->>'polarisation') varchar_pattern_ops);
CREATE INDEX IF NOT EXISTS idx_sensor_mode ON scene using hash ((metadata_content->>'sensor_mode') varchar_pattern_ops);
CREATE INDEX IF NOT EXISTS idx_collection ON scene using hash ((metadata_content->>'collection') varchar_pattern_ops);
CREATE INDEX IF NOT EXISTS idx_timeliness ON scene using hash ((metadata_content->>'timeliness') varchar_pattern_ops);
CREATE INDEX IF NOT EXISTS idx_instrument ON scene using hash ((metadata_content->>'instrument') varchar_pattern_ops);
CREATE INDEX IF NOT EXISTS idx_product_level ON scene using hash ((metadata_content->>'product_level') varchar_pattern_ops);
CREATE INDEX IF NOT EXISTS idx_relative_orbit_number ON scene using hash ((metadata_content->>'relative_orbit_number') varchar_pattern_ops);
CREATE INDEX IF NOT EXISTS idx_absolute_orbit_number ON scene using hash ((metadata_content->>'absolute_orbit_number') varchar_pattern_ops);
