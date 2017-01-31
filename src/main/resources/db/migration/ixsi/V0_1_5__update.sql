ALTER TABLE ixsi.booking_create
  ADD CONSTRAINT "UQ_booking_create_pid_bid" UNIQUE (provider_id, booking_id);

ALTER TABLE ixsi.booking_target_status_inavailability
  ADD COLUMN booking_id character varying;

ALTER TABLE ixsi.booking_target_status_inavailability
  ADD CONSTRAINT "FK_booking_target_status_inavailability_pid_bid" FOREIGN KEY (provider_id, booking_id)
  REFERENCES ixsi.booking_create (provider_id, booking_id) ON UPDATE NO ACTION ON DELETE CASCADE;
