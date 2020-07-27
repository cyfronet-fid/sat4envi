CREATE OR REPLACE FUNCTION product_trigger() RETURNS trigger AS
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
                's.id, ' ||
                's.footprint, ' ||
                's.timestamp, ' ||
                's.granule_path ' ||
                'FROM scene s ' ||
                'WHERE s.product_id = ' || new_product.id || ';';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER product_trigger AFTER
    INSERT OR
    UPDATE OF layer_name OR
    DELETE
    ON product
    FOR EACH ROW EXECUTE FUNCTION product_trigger();
