CREATE TABLE institution (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    slug VARCHAR NOT NULL,
    UNIQUE (name,slug)
);
