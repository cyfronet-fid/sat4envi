ALTER TABLE app_user
    ADD COLUMN eumetsat_license BOOLEAN;

ALTER TABLE institution ADD COLUMN eumetsat_license BOOLEAN;
