ALTER TABLE email_verification
ALTER COLUMN app_user_id SET DEFAULT null;
DROP SEQUENCE email_verification_app_user_id_seq;

ALTER TABLE inst_group
ALTER COLUMN institution_id SET DEFAULT null;
DROP SEQUENCE inst_group_institution_id_seq;

ALTER TABLE inst_group_app_users
ALTER COLUMN app_user_id SET DEFAULT null,
ALTER COLUMN inst_group_id SET DEFAULT null;
DROP SEQUENCE inst_group_app_users_app_user_id_seq;
DROP SEQUENCE inst_group_app_users_inst_group_id_seq;

ALTER TABLE password_reset
ALTER COLUMN app_user_id SET DEFAULT null;
DROP SEQUENCE password_reset_app_user_id_seq;

ALTER TABLE prg_overlay
ALTER COLUMN sld_style_id SET DEFAULT null;
DROP SEQUENCE prg_overlay_sld_style_id_seq;

ALTER TABLE product
ALTER COLUMN product_type_id SET DEFAULT null;
DROP SEQUENCE product_product_type_id_seq;

ALTER TABLE refresh_token
ALTER COLUMN app_user_id SET DEFAULT null;
DROP SEQUENCE refresh_token_app_user_id_seq;
