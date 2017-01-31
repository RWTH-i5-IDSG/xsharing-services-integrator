CREATE TYPE ixsi.status AS ENUM ('ACTIVE','INACTIVE');

BEGIN;
ALTER TABLE ixsi.booking_target ADD status ixsi.status;
UPDATE ixsi.booking_target SET status = 'ACTIVE';
ALTER TABLE ixsi.booking_target ALTER COLUMN status SET NOT NULL;
COMMIT;

BEGIN;
ALTER TABLE ixsi.floating_area ADD status ixsi.status;
UPDATE ixsi.floating_area SET status = 'ACTIVE';
ALTER TABLE ixsi.floating_area ALTER COLUMN status SET NOT NULL;
COMMIT;

BEGIN;
ALTER TABLE ixsi.place ADD status ixsi.status;
UPDATE ixsi.place SET status = 'ACTIVE';
ALTER TABLE ixsi.place ALTER COLUMN status SET NOT NULL;
COMMIT;

BEGIN;
ALTER TABLE ixsi.placegroup ADD status ixsi.status;
UPDATE ixsi.placegroup SET status = 'ACTIVE';
ALTER TABLE ixsi.placegroup ALTER COLUMN status SET NOT NULL;
COMMIT;

BEGIN;
ALTER TABLE ixsi.provider ADD status ixsi.status;
UPDATE ixsi.provider SET status = 'ACTIVE';
ALTER TABLE ixsi.provider ALTER COLUMN status SET NOT NULL;
COMMIT;

BEGIN;
ALTER TABLE ixsi.attribute ADD status ixsi.status;
UPDATE ixsi.attribute SET status = 'ACTIVE';
ALTER TABLE ixsi.attribute ALTER COLUMN status SET NOT NULL;
COMMIT;