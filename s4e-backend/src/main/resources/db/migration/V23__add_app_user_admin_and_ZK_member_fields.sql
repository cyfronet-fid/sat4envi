ALTER TABLE app_user ADD COLUMN member_zk BOOLEAN DEFAULT FALSE;
ALTER TABLE app_user ADD COLUMN admin BOOLEAN DEFAULT FALSE;

UPDATE app_user SET member_zk = FALSE;
UPDATE app_user SET admin = FALSE;