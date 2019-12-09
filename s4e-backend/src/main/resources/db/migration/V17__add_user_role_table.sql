CREATE TABLE user_role (
    id BIGSERIAL PRIMARY KEY,
    role VARCHAR NOT NULL,
    app_user_id BIGINT NOT NULL REFERENCES app_user ON DELETE CASCADE,
    inst_group_id BIGINT NOT NULL REFERENCES inst_group ON DELETE CASCADE,
    UNIQUE (role, app_user_id, inst_group_id)
);