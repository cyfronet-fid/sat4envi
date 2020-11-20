ALTER TABLE license_grant
    ADD COLUMN owner BOOLEAN;

UPDATE license_grant SET owner = FALSE;

ALTER TABLE license_grant
    ALTER COLUMN owner SET NOT NULL;
