-- Initially, these data types were more restricted (smallint) which caused problems every now and then.
-- It is probably a good idea to be less restrictive and upgrade them to integer.

ALTER TABLE ixsi.attribute
ALTER COLUMN importance TYPE INTEGER;

ALTER TABLE ixsi.base_stop_link
ALTER COLUMN duration_in_seconds TYPE INTEGER,
ALTER COLUMN duration_variance_in_seconds TYPE INTEGER,
ALTER COLUMN distance_in_meters TYPE INTEGER;

ALTER TABLE ixsi.booking_target
ALTER COLUMN booking_grid_in_minutes TYPE INTEGER,
ALTER COLUMN co2_factor TYPE INTEGER;

ALTER TABLE ixsi.booking_target_status
ALTER COLUMN current_charge TYPE INTEGER;

ALTER TABLE ixsi.booking_target_status
ALTER COLUMN current_charge TYPE INTEGER;

ALTER TABLE ixsi.floating_area
ALTER COLUMN duration_in_seconds TYPE INTEGER,
ALTER COLUMN duration_variance_in_seconds TYPE INTEGER;

ALTER TABLE ixsi.floating_area_sub_area
ALTER COLUMN duration_in_seconds TYPE INTEGER,
ALTER COLUMN duration_variance_in_seconds TYPE INTEGER;

ALTER TABLE ixsi.place
ALTER COLUMN capacity TYPE INTEGER,
ALTER COLUMN on_premises_time_in_seconds TYPE INTEGER,
ALTER COLUMN on_premises_time_in_seconds SET DEFAULT 0,
ALTER COLUMN available_capacity TYPE INTEGER;

ALTER TABLE ixsi.placegroup
ALTER COLUMN probability TYPE INTEGER;

ALTER TABLE ixsi.placegroup_place
ALTER COLUMN probability TYPE INTEGER;

ALTER TABLE ixsi.server_system
ALTER COLUMN number_of_connections TYPE INTEGER,
ALTER COLUMN number_of_connections SET NOT NULL;
