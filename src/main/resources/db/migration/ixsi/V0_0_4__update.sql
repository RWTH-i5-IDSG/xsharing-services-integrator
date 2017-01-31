ALTER TABLE ixsi.booking_target_inavailability
  DROP COLUMN inavailability;

ALTER TABLE ixsi.consumption
  DROP COLUMN time_period;

ALTER TYPE ixsi.time_period
  ALTER ATTRIBUTE begin SET DATA TYPE timestamp without time zone;
ALTER TYPE ixsi.time_period
  ALTER ATTRIBUTE "end" SET DATA TYPE timestamp without time zone;

ALTER TABLE ixsi.booking_target_inavailability
  ADD COLUMN inavailability ixsi.time_period;

ALTER TABLE ixsi.booking_target_inavailability
  ADD CONSTRAINT "PK_booking_target_inavailability" PRIMARY KEY (booking_target_id, provider_id, inavailability);

ALTER TABLE ixsi.consumption
  ADD COLUMN time_period ixsi.time_period;

