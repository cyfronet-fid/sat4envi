-- Fire this trigger more selectively, instead of on every row UPDATE.
DROP TRIGGER IF EXISTS scene_generate_s3path ON scene;

CREATE TRIGGER scene_generate_s3path BEFORE
    INSERT OR UPDATE OF product_id, scene_content, metadata_content ON scene
    FOR EACH ROW
EXECUTE PROCEDURE scene_generate_s3path();


-- Add download_only field to product and scene.
ALTER TABLE product
    ADD COLUMN download_only BOOLEAN DEFAULT FALSE NOT NULL;

ALTER TABLE scene
    ADD COLUMN download_only BOOLEAN DEFAULT FALSE NOT NULL,
    ALTER COLUMN s3path DROP NOT NULL,
    ADD CONSTRAINT scene_s3path_conditional_not_null CHECK (download_only = TRUE OR s3path IS NOT NULL);


-- Add a trigger which populates download_only from product.
CREATE OR REPLACE FUNCTION scene_download_only_trigger() RETURNS TRIGGER AS
$$
DECLARE
    new_scene scene;
BEGIN
    new_scene = cast(NEW AS scene);
    NEW.download_only = (SELECT product.download_only FROM product WHERE product.id = new_scene.product_id);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER scene_download_only_trigger BEFORE
    INSERT ON scene
    FOR EACH ROW
EXECUTE PROCEDURE scene_download_only_trigger();

-- Add a trigger which propagates download_only changes to all scenes of a product.
CREATE OR REPLACE FUNCTION product_download_only_trigger() RETURNS TRIGGER AS
$$
DECLARE
    new_product product;
BEGIN
    new_product = cast(NEW AS product);
    UPDATE scene SET download_only = new_product.download_only WHERE product_id = new_product.id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER product_download_only_trigger AFTER
    UPDATE OF download_only ON product
    FOR EACH ROW
EXECUTE PROCEDURE product_download_only_trigger();
