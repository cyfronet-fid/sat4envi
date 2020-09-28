ALTER TABLE institution DROP CONSTRAINT institution_parent_id_fkey;
ALTER TABLE institution
    ADD CONSTRAINT institution_parent_id_fkey FOREIGN KEY (parent_id) REFERENCES institution (id) ON DELETE CASCADE ;
