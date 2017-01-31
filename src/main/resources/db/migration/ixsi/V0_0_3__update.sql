ALTER TABLE ixsi.booking_target_availability
  DROP COLUMN inavailability;

ALTER TABLE ixsi.booking_target_availability
  RENAME TO booking_target_status;

CREATE TABLE ixsi.booking_target_inavailability
(
  booking_target_id character varying(255) NOT NULL,
  provider_id character varying(255) NOT NULL,
  inavailability ixsi.time_period NOT NULL,
  CONSTRAINT "PK_booking_target_inavailability" PRIMARY KEY (booking_target_id, provider_id, inavailability),
  CONSTRAINT "FK_booking_target_inavailability_btid_pid" FOREIGN KEY (booking_target_id, provider_id)
      REFERENCES ixsi.booking_target (booking_target_id, provider_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE
);
