ALTER TABLE institution
    ADD COLUMN pak BOOLEAN;

UPDATE institution SET pak = FALSE;

ALTER TABLE institution
    ALTER COLUMN pak SET NOT NULL;
