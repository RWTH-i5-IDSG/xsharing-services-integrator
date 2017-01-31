CREATE TYPE ixsi.booking_create_type AS ENUM ('INTERNAL', 'EXTERNAL');

BEGIN;
ALTER TABLE ixsi.booking_create ADD event_type ixsi.booking_create_type;
UPDATE ixsi.booking_create SET event_type = 'INTERNAL'; -- until now, all created and stored bookings were internal
ALTER TABLE ixsi.booking_create ALTER COLUMN event_type SET NOT NULL;
COMMIT;