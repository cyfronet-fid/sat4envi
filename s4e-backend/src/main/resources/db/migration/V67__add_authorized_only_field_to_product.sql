ALTER TABLE product
    ADD COLUMN authorized_only BOOLEAN;

UPDATE product SET authorized_only = FALSE;

ALTER TABLE product
    ALTER COLUMN authorized_only SET NOT NULL;
