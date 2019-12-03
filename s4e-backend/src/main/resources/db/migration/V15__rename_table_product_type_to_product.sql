ALTER TABLE product_type RENAME TO product;
ALTER SEQUENCE product_type_id_seq RENAME TO product_id_seq;
ALTER TABLE scene RENAME product_type_id TO product_id;
