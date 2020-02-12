ALTER TABLE scene
DROP CONSTRAINT product_layer_name_key,
ADD CONSTRAINT scene_layer_name_key UNIQUE(layer_name);

ALTER TABLE scene
DROP CONSTRAINT product_product_type_id_timestamp_key,
ADD CONSTRAINT scene_product_id_timestamp_key UNIQUE(product_id, "timestamp");

ALTER TABLE scene
DROP CONSTRAINT product_pkey,
ADD CONSTRAINT scene_pkey PRIMARY KEY (id);

ALTER TABLE scene DROP CONSTRAINT product_product_type_id_fkey;

ALTER TABLE product
DROP CONSTRAINT product_type_name_key,
ADD CONSTRAINT product_name_key UNIQUE(name);

ALTER TABLE product
DROP CONSTRAINT product_type_pkey,
ADD CONSTRAINT product_pkey PRIMARY KEY (id);

ALTER TABLE scene
ADD CONSTRAINT scene_product_id_fkey FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE;
