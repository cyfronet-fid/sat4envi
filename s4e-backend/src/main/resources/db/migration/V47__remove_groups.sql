-- user_role change group to institution
ALTER TABLE user_role ADD COLUMN institution_id BIGINT REFERENCES institution ON DELETE CASCADE;
UPDATE user_role SET institution_id = (SELECT institution_id FROM inst_group where id = inst_group_id);
ALTER TABLE user_role
    DROP CONSTRAINT user_role_inst_group_id_fkey,
    DROP CONSTRAINT user_role_role_app_user_id_inst_group_id_key,
    DROP COLUMN inst_group_id;
ALTER TABLE user_role
    ALTER COLUMN institution_id SET NOT NULL,
    ADD UNIQUE (role, app_user_id, institution_id);
-- drop group table
ALTER TABLE inst_group DROP CONSTRAINT inst_group_institution_id_fkey;
DROP TABLE inst_group;
