CREATE TABLE product_category (
      id BIGSERIAL PRIMARY KEY,
      label VARCHAR UNIQUE NOT NULL,
      name VARCHAR UNIQUE NOT NULL
);

INSERT INTO product_category (label, name) VALUES ('Default', '');
ALTER TABLE product ADD COLUMN product_category_id BIGINT NOT NULL REFERENCES product_category ON DELETE CASCADE DEFAULT 1;