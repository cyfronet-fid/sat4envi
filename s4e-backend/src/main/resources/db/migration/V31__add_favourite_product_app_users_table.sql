CREATE TABLE favourite_product_app_users (
    product_id BIGSERIAL REFERENCES product ON DELETE CASCADE,
    app_user_id BIGSERIAL REFERENCES app_user ON DELETE CASCADE,
    UNIQUE(product_id,app_user_id)
);