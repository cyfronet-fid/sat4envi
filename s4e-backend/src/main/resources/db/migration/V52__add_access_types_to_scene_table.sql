CREATE TABLE license_grant(
    id BIGSERIAL PRIMARY KEY,
    institution_id BIGINT REFERENCES institution ON DELETE CASCADE,
    product_id BIGINT REFERENCES product ON DELETE CASCADE,
    created_at TIMESTAMP,
    created_by VARCHAR,
    last_modified_at TIMESTAMP,
    last_modified_by VARCHAR,
    UNIQUE(institution_id, product_id)
);

ALTER TABLE product ADD COLUMN access_type VARCHAR;

UPDATE product SET access_type = 'OPEN';

ALTER TABLE product ALTER COLUMN access_type SET NOT NULL;
