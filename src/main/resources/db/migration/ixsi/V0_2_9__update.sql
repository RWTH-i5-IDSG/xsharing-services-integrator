
-- The previous column name 'internal_booking_id' was a poor choice,
-- because it's actually an auto-gen PK in consumption table.
-- So, 'internal_booking_id' was never a database-wide unique key describing a booking id.

ALTER TABLE ixsi.consumption
RENAME internal_booking_id TO consumption_id;

ALTER TABLE ixsi.consumption_description
RENAME internal_booking_id TO consumption_id;
