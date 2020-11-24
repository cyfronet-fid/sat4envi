CREATE TABLE app_user_authorities (
    app_user_id BIGINT NOT NULL,
    authority VARCHAR NOT NULL,
    UNIQUE(app_user_id, authority)
);

INSERT INTO app_user_authorities (app_user_id, authority)
    SELECT au.id as app_user_id, 'ROLE_ADMIN' as authority
        FROM app_user au
        WHERE admin;

INSERT INTO app_user_authorities (app_user_id, authority)
    SELECT au.id as app_user_id, 'LICENSE_EUMETSAT' as authority
        FROM app_user au
        WHERE eumetsat_license;

ALTER TABLE app_user
    DROP COLUMN admin,
    DROP COLUMN eumetsat_license;

