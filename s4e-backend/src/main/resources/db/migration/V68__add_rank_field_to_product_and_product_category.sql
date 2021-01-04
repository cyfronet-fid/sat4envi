ALTER TABLE product
    ADD COLUMN rank BIGINT UNIQUE;

ALTER TABLE product_category
    ADD COLUMN rank BIGINT UNIQUE;

UPDATE product SET rank = 1000 * id;
UPDATE product_category SET rank = 1000 * id;

ALTER TABLE product
    ALTER COLUMN rank SET NOT NULL;

ALTER TABLE product_category
    ALTER COLUMN rank SET NOT NULL;
