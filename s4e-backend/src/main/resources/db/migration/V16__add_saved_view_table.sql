CREATE TABLE saved_view (
    id UUID PRIMARY KEY,
    owner_id BIGINT NOT NULL REFERENCES app_user ON DELETE CASCADE,
    caption VARCHAR NOT NULL,
    created_at TIMESTAMP NOT NULL,
    thumbnail VARCHAR NOT NULL,
    configuration JSONB NOT NULL
);
