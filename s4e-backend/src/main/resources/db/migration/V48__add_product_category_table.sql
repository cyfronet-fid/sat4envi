CREATE TABLE product_category (
      id BIGSERIAL PRIMARY KEY,
      label VARCHAR UNIQUE NOT NULL,
      name VARCHAR UNIQUE NOT NULL,
      icon_name VARCHAR NOT NULL
);

INSERT INTO product_category (label, name, icon_name) VALUES ('Inne', 'other', 'ico_earth') ON CONFLICT DO NOTHING;
ALTER TABLE product ADD COLUMN product_category_id BIGINT NOT NULL REFERENCES product_category ON DELETE CASCADE DEFAULT 1;