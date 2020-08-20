-- Remove all the product related views, so that they don't block extensive changes in between.
DO
$$
    DECLARE
        product_cur CURSOR FOR
            SELECT * FROM product;
        product_row product%rowtype;
    BEGIN
        FOR product_row in product_cur LOOP
                EXECUTE 'DROP VIEW IF EXISTS scene_' || product_row.layer_name;
            END LOOP;
    END;
$$;

-- Drop columns, which will be calculated dynamically by a view and switch timestamp to a generated column.
ALTER TABLE scene
    DROP granule_path,
    DROP s3path,
    DROP timestamp,
    ADD timestamp TIMESTAMP GENERATED ALWAYS AS (f_cast_isots(scene.metadata_content ->> 'sensing_time')) STORED,
    ALTER COLUMN scene_content SET NOT NULL,
    ALTER COLUMN metadata_content SET NOT NULL;

ALTER TABLE product
    ALTER COLUMN granule_artifact_rule SET NOT NULL;

CREATE OR REPLACE FUNCTION get_scene_granule_path_prefix() RETURNS VARCHAR AS
$$
SELECT property.value
FROM property
WHERE property.name = 'scene_granule_path_prefix';
$$ LANGUAGE sql;

CREATE OR REPLACE FUNCTION get_scene_s3path(
    scene scene
) RETURNS VARCHAR AS
$$
DECLARE
    format VARCHAR;
    granule_artifact_rule JSONB;
    artifact_name VARCHAR;
    path VARCHAR;
BEGIN
    format = scene.metadata_content ->> 'format';
    IF (format IS NULL) THEN
        RETURN NULL;
    END IF;

    SELECT product.granule_artifact_rule
    INTO granule_artifact_rule
    FROM product
    WHERE product.id = scene.product_id;

    artifact_name = granule_artifact_rule ->> format;
    IF (artifact_name IS NULL) THEN
        artifact_name = granule_artifact_rule ->> 'default';
        IF (artifact_name IS NULL) THEN
            RETURN NULL;
        END IF;
    END IF;
    path = scene.scene_content -> 'artifacts' ->> artifact_name;

    RETURN substring(path FROM 2);
END;
$$ LANGUAGE plpgsql;

-- Create an intermediate scene view, containing calculated columns.
CREATE VIEW scene_extended AS
SELECT
    scene.id,
    scene.product_id,
    scene.footprint,
    scene.timestamp,
    get_scene_s3path(scene) AS s3path,
    get_scene_granule_path_prefix() || get_scene_s3path(scene) AS granule_path
FROM scene;

-- Update the trigger, which creates product views to refer to the intermediate view.
CREATE OR REPLACE FUNCTION product_trigger() RETURNS TRIGGER AS
$$
DECLARE
    new_product product;
    old_product product;
BEGIN
    IF (TG_OP = 'UPDATE' OR TG_OP = 'DELETE') THEN
        old_product = cast(OLD AS product);
        EXECUTE 'DROP VIEW IF EXISTS scene_' || old_product.layer_name;
    END IF;
    IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN
        new_product = cast(NEW AS product);
        EXECUTE 'CREATE VIEW scene_' || new_product.layer_name || ' AS ' ||
                'SELECT ' ||
                'scene_ext.id, ' ||
                'scene_ext.footprint, ' ||
                'scene_ext.timestamp, ' ||
                'scene_ext.granule_path ' ||
                'FROM scene_extended scene_ext ' ||
                'WHERE scene_ext.product_id = ' || new_product.id || ';';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create a trigger, to verify s3path is not null
CREATE OR REPLACE FUNCTION scene_verify_s3path_not_null() RETURNS TRIGGER AS
$$
DECLARE
    s3path VARCHAR;
BEGIN
    s3path = get_scene_s3path(NEW);
    IF (s3path IS NULL) THEN
        RAISE EXCEPTION 'Generated s3path is null for scene key [%]', NEW.scene_key;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create a trigger, to verify s3paths are not null when updating product.
CREATE OR REPLACE FUNCTION product_verify_scenes_s3path_not_null() RETURNS TRIGGER AS
$$
DECLARE
    any_s3path_is_null BOOLEAN;
BEGIN
    SELECT exists(
        SELECT 1
        FROM scene_extended
        WHERE scene_extended.product_id = NEW.id AND scene_extended.s3path IS NULL
    ) INTO any_s3path_is_null;
    IF (any_s3path_is_null) THEN
        RAISE EXCEPTION 'Generated s3path is null for some scenes of product [%]', NEW.id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER scene_verify_s3path_not_null AFTER
    INSERT OR UPDATE ON scene
    FOR EACH ROW
EXECUTE PROCEDURE scene_verify_s3path_not_null();

CREATE TRIGGER product_verify_scenes_s3path_not_null AFTER
    UPDATE ON scene
    FOR EACH ROW
EXECUTE PROCEDURE product_verify_scenes_s3path_not_null();

-- Recreate all the product related views, now referring to the intermediate view.
DO
$$
    DECLARE
        product_cur CURSOR FOR
            SELECT * FROM product;
        product_row product%rowtype;
    BEGIN
        FOR product_row in product_cur LOOP
                EXECUTE 'CREATE VIEW scene_' || product_row.layer_name || ' AS ' ||
                        'SELECT ' ||
                        'scene_ext.id, ' ||
                        'scene_ext.footprint, ' ||
                        'scene_ext.timestamp, ' ||
                        'scene_ext.granule_path ' ||
                        'FROM scene_extended scene_ext ' ||
                        'WHERE scene_ext.product_id = ' || product_row.id || ';';
            END LOOP;
    END;
$$;
