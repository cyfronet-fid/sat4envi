CREATE TABLE inst_group (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    slug VARCHAR NOT NULL,
    institution_id BIGSERIAL NOT NULL REFERENCES institution ON DELETE CASCADE,
    UNIQUE (name,institution_id),
    UNIQUE (slug,institution_id)
);
