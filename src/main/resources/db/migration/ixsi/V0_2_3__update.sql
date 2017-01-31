ALTER TYPE ixsi.booking_create_type
RENAME TO event_origin;

ALTER TABLE ixsi.booking_create RENAME event_type TO event_origin;

ALTER TABLE ixsi.booking_change RENAME event_type TO change_type;
ALTER TABLE ixsi.booking_change ALTER COLUMN change_type SET NOT NULL;

BEGIN;
ALTER TABLE ixsi.booking_change ADD COLUMN event_origin ixsi.event_origin;
UPDATE ixsi.booking_change SET event_origin = 'INTERNAL';
ALTER TABLE ixsi.booking_change ALTER COLUMN event_origin SET NOT NULL;
COMMIT;
