ALTER TABLE product ADD COLUMN layer_name VARCHAR UNIQUE;

UPDATE product SET layer_name = name;

ALTER TABLE product ALTER COLUMN layer_name SET NOT NULL;
