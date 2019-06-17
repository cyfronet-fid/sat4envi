CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR UNIQUE NOT NULL,
    password VARCHAR NOT NULL,
    roles VARCHAR
);

CREATE TABLE sld_style (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR UNIQUE NOT NULL,
    created BOOLEAN DEFAULT FALSE
);

CREATE TABLE prg_overlay (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR UNIQUE NOT NULL,
    feature_type VARCHAR,
    created BOOLEAN DEFAULT FALSE,
    sld_style_id BIGSERIAL REFERENCES sld_style
);

CREATE TABLE wms_overlay (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR UNIQUE NOT NULL,
    url VARCHAR NOT NULL
);

CREATE TABLE product_type (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR UNIQUE NOT NULL
);

CREATE TABLE product (
    id BIGSERIAL PRIMARY KEY,
    product_type_id BIGSERIAL REFERENCES product_type,
    "timestamp" TIMESTAMP NOT NULL,
    s3path VARCHAR NOT NULL,
    layer_name VARCHAR UNIQUE,
    created BOOLEAN DEFAULT FALSE,
    UNIQUE (product_type_id, "timestamp")
);

CREATE TABLE place (
    id SERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    type VARCHAR NOT NULL,
    latitude FLOAT NOT NULL,
    longitude FLOAT NOT NULL,
    voivodeship VARCHAR NOT NULL
);
