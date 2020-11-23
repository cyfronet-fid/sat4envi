ALTER TABLE app_user
    DROP COLUMN created_by,
    DROP COLUMN last_modified_by,
    ADD COLUMN created_by BIGINT,
    ADD COLUMN last_modified_by BIGINT;

ALTER TABLE institution
    DROP COLUMN created_by,
    DROP COLUMN last_modified_by,
    ADD COLUMN created_by BIGINT,
    ADD COLUMN last_modified_by BIGINT;

ALTER TABLE user_role
    DROP COLUMN created_by,
    ADD COLUMN created_by BIGINT;

ALTER TABLE schema
    DROP COLUMN created_by,
    ADD COLUMN created_by BIGINT;

ALTER TABLE product
    DROP COLUMN created_by,
    DROP COLUMN last_modified_by,
    ADD COLUMN created_by BIGINT,
    ADD COLUMN last_modified_by BIGINT;

ALTER TABLE scene
    DROP COLUMN created_by,
    DROP COLUMN last_modified_by,
    ADD COLUMN created_by BIGINT,
    ADD COLUMN last_modified_by BIGINT;

ALTER TABLE wms_overlay
    DROP COLUMN created_by,
    DROP COLUMN last_modified_by,
    ADD COLUMN created_by BIGINT,
    ADD COLUMN last_modified_by BIGINT;
