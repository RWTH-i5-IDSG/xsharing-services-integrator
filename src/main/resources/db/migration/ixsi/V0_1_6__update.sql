ALTER TABLE ixsi.booking_change
ALTER COLUMN new_time_period TYPE tsrange USING (tsrange((new_time_period).begin,(new_time_period).end));

ALTER TABLE ixsi.booking_create
ALTER COLUMN time_period TYPE tsrange USING (tsrange((time_period).begin,(time_period).end));

ALTER TABLE ixsi.consumption
ALTER COLUMN time_period TYPE tsrange USING (tsrange((time_period).begin,(time_period).end));

ALTER TABLE ixsi.booking_target_status_inavailability
ALTER COLUMN inavailability TYPE tsrange USING (tsrange((inavailability).begin,(inavailability).end));