ALTER TABLE app_user
    DROP COLUMN member_zk;

ALTER TABLE institution
    ADD COLUMN zk BOOLEAN;

UPDATE institution SET zk = TRUE;

ALTER TABLE institution
    ALTER COLUMN zk SET NOT NULL;
