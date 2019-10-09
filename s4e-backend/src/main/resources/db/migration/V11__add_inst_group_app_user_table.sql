CREATE TABLE inst_group_app_users (
    inst_group_id BIGSERIAL REFERENCES inst_group ON DELETE CASCADE,
    app_user_id BIGSERIAL REFERENCES app_user ON DELETE CASCADE,
    UNIQUE(inst_group_id,app_user_id)
);