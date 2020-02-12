ALTER TABLE email_verification
DROP CONSTRAINT email_verification_app_user_id_fkey,
ADD CONSTRAINT email_verification_app_user_id_fkey
FOREIGN KEY (app_user_id)
REFERENCES app_user(id)
ON DELETE CASCADE;