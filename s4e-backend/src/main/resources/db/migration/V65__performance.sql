-- Mark generation functions as parallel safe.
ALTER FUNCTION get_scene_granule_path_prefix() PARALLEL SAFE;
ALTER FUNCTION get_scene_s3path(scene scene) PARALLEL SAFE;

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

DROP VIEW IF EXISTS scene_extended;

-- Remove all the unnecessary triggers.
DROP TRIGGER IF EXISTS scene_verify_s3path_not_null ON scene;
DROP FUNCTION IF EXISTS scene_verify_s3path_not_null();

DROP TRIGGER IF EXISTS product_verify_scenes_s3path_not_null ON scene;
DROP FUNCTION IF EXISTS product_verify_scenes_s3path_not_null();

-- Create s3path column in scene, populate it and enforce a not null constraint.
ALTER TABLE scene
    ADD s3path VARCHAR;

UPDATE scene
SET s3path = get_scene_s3path(scene);

ALTER TABLE scene
    ALTER s3path SET NOT NULL;

-- Add triggers to update the s3path values.
CREATE OR REPLACE FUNCTION scene_generate_s3path() RETURNS TRIGGER AS
$$
BEGIN
    NEW.s3path = get_scene_s3path(CAST(NEW AS scene));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION product_generate_scene_s3path() RETURNS TRIGGER AS
$$
DECLARE
    new_product product;
BEGIN
    new_product = CAST(NEW AS product);
    UPDATE scene
    SET s3path = get_scene_s3path(scene)
    WHERE product_id = new_product.id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER scene_generate_s3path BEFORE
    INSERT OR UPDATE ON scene
    FOR EACH ROW
EXECUTE PROCEDURE scene_generate_s3path();

CREATE TRIGGER product_generate_scene_s3path AFTER
    UPDATE OF granule_artifact_rule ON product
    FOR EACH ROW
EXECUTE PROCEDURE product_generate_scene_s3path();

-- Recreate all the product related views, now referring to the table scene.
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
                        'id, ' ||
                        'footprint, ' ||
                        'timestamp, ' ||
                        'get_scene_granule_path_prefix() || s3path AS granule_path ' ||
                        'FROM scene ' ||
                        'WHERE product_id = ' || product_row.id || ';';
            END LOOP;
    END;
$$;

-- Update the trigger, which creates product views to refer to the scene table.
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
                'id, ' ||
                'footprint, ' ||
                'timestamp, ' ||
                'get_scene_granule_path_prefix() || s3path AS granule_path ' ||
                'FROM scene ' ||
                'WHERE product_id = ' || new_product.id || ';';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

