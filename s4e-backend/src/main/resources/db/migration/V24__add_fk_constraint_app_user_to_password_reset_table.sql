ALTER TABLE password_reset
DROP CONSTRAINT password_reset_app_user_id_fkey,
ADD CONSTRAINT password_reset_app_user_id_fkey
FOREIGN KEY (app_user_id)
REFERENCES app_user(id)
ON DELETE CASCADE;