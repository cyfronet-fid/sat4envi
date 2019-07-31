CREATE TABLE refresh_token (
    id BIGSERIAL PRIMARY KEY,
    app_user_id BIGSERIAL REFERENCES app_user NOT NULL,
    jti VARCHAR UNIQUE NOT NULL,
    expiry_timestamp TIMESTAMP NOT NULL
);
