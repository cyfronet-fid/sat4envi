ALTER TABLE scene
    DROP COLUMN layer_name,
    DROP COLUMN created,
    ADD COLUMN granule_path VARCHAR NOT NULL,
    ADD COLUMN footprint GEOMETRY(POLYGON, 3857) NOT NULL;
