CREATE TABLE invitation (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR NOT NULL,
    created_at TIMESTAMP,
    created_by VARCHAR,
    last_modified_at TIMESTAMP,
    last_modified_by VARCHAR,
    token VARCHAR UNIQUE NOT NULL,
    status VARCHAR NOT NULL,
    institution_id BIGINT NOT NULL REFERENCES institution ON DELETE CASCADE,
    UNIQUE (email, institution_id)
);