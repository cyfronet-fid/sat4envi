CREATE TABLE report_template (
    id UUID PRIMARY KEY,
    owner_id BIGINT NOT NULL REFERENCES app_user ON DELETE CASCADE,
    caption VARCHAR,
    notes VARCHAR,
    overlay_ids BIGINT[],
    product_id BIGINT,
    created_at TIMESTAMP,
    created_by VARCHAR
);
