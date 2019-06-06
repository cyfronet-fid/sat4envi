ALTER TABLE app_user ADD COLUMN
    enabled BOOLEAN DEFAULT FALSE;

UPDATE app_user
SET enabled = TRUE;
