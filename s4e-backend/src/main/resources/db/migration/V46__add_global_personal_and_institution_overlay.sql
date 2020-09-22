ALTER TABLE wms_overlay
RENAME COLUMN name TO label;
ALTER TABLE wms_overlay
ADD COLUMN layer_name VARCHAR NOT NULL;
ALTER TABLE wms_overlay
ADD COLUMN owner_type VARCHAR NOT NULL;
ALTER TABLE wms_overlay
ADD COLUMN institution_id BIGINT REFERENCES institution ON DELETE CASCADE;
ALTER TABLE wms_overlay
ADD COLUMN app_user_id BIGINT REFERENCES app_user ON DELETE CASCADE;
ALTER TABLE wms_overlay
ADD COLUMN created_at TIMESTAMP;
ALTER TABLE wms_overlay
ADD COLUMN created_by VARCHAR;
ALTER TABLE wms_overlay
ADD COLUMN last_modified_at TIMESTAMP;
ALTER TABLE wms_overlay
ADD COLUMN last_modified_by VARCHAR;

ALTER TABLE prg_overlay
ADD COLUMN wms_overlay_id BIGINT REFERENCES wms_overlay ON DELETE CASCADE;

with new_wms_overlay as (
    INSERT INTO wms_overlay (label, url, owner_type)
        SELECT
            name as label,
            '' as url,
            'global' as owner_type
        FROM prg_overlay
        WHERE NOT EXISTS(
            SELECT 1
            FROM wms_overlay
            WHERE
                wms_overlay.id = prg_overlay.wms_overlay_id
            LIMIT 1
        )
    returning id, label
)
UPDATE prg_overlay
SET wms_overlay_id = new_wms_overlay.id
FROM (SELECT id, label FROM new_wms_overlay) as new_wms_overlay
WHERE new_wms_overlay.label = prg_overlay.name;

ALTER TABLE prg_overlay
ALTER COLUMN wms_overlay_id SET NOT NULL;
ALTER TABLE prg_overlay
    DROP COLUMN name;

ALTER TABLE app_user
ADD COLUMN preferences JSONB NOT NULL default '{
  "nonVisibleOverlays": []
}'::jsonb;