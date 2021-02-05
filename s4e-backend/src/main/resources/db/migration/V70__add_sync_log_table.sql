CREATE TABLE sync_record (
    id BIGSERIAL NOT NULL,
    initiated_by_method VARCHAR,
    scene_key VARCHAR,
    event_name VARCHAR,
    received_at TIMESTAMP,
    sensing_time TIMESTAMP,
    product_name VARCHAR,
    result_code VARCHAR,
    exception_message VARCHAR,
    parameters JSONB
);

CREATE INDEX IF NOT EXISTS idx_sync_record_received_at ON sync_record using btree (received_at);
CREATE INDEX IF NOT EXISTS idx_sync_record_sensing_time ON sync_record using btree (sensing_time);
