CREATE TABLE schema (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR UNIQUE NOT NULL,
    type VARCHAR NOT NULL,
    content VARCHAR NOT NULL,
    previous_id BIGINT REFERENCES schema
);

ALTER TABLE product
    ADD COLUMN scene_schema_id BIGINT REFERENCES schema,
    ADD COLUMN metadata_schema_id BIGINT REFERENCES schema,
    ADD COLUMN granule_artifact VARCHAR,
    ADD COLUMN searchable_metadata JSONB;

ALTER TABLE scene
    ADD COLUMN scene_content JSONB,
    ADD COLUMN metadata_content JSONB;
