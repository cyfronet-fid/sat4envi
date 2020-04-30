ALTER TABLE app_user
    ADD COLUMN created_at TIMESTAMP,
    ADD COLUMN created_by VARCHAR,
    ADD COLUMN last_modified_at TIMESTAMP,
    ADD COLUMN last_modified_by VARCHAR;

ALTER TABLE institution
    ADD COLUMN created_at TIMESTAMP,
    ADD COLUMN created_by VARCHAR,
    ADD COLUMN last_modified_at TIMESTAMP,
    ADD COLUMN last_modified_by VARCHAR;

ALTER TABLE user_role
    ADD COLUMN created_at TIMESTAMP,
    ADD COLUMN created_by VARCHAR;

ALTER TABLE schema
    ADD COLUMN created_at TIMESTAMP,
    ADD COLUMN created_by VARCHAR;

ALTER TABLE product
    ADD COLUMN created_at TIMESTAMP,
    ADD COLUMN created_by VARCHAR,
    ADD COLUMN last_modified_at TIMESTAMP,
    ADD COLUMN last_modified_by VARCHAR;

ALTER TABLE scene
    ADD COLUMN created_at TIMESTAMP,
    ADD COLUMN created_by VARCHAR,
    ADD COLUMN last_modified_at TIMESTAMP,
    ADD COLUMN last_modified_by VARCHAR;

