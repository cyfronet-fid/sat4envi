ALTER TABLE product ADD COLUMN display_name VARCHAR UNIQUE;

UPDATE product SET display_name = name;

ALTER TABLE product ALTER COLUMN display_name SET NOT NULL;
