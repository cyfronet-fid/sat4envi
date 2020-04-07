ALTER TABLE product
    DROP COLUMN granule_artifact,
    ADD COLUMN granule_artifact_rule JSONB;
