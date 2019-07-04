CREATE TABLE email_verification (
    id BIGSERIAL PRIMARY KEY,
    app_user_id BIGSERIAL UNIQUE REFERENCES app_user NOT NULL,
    token VARCHAR UNIQUE NOT NULL,
    expiry_timestamp TIMESTAMP NOT NULL
);
