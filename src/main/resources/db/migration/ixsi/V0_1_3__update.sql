CREATE TABLE ixsi.booking_create
(
  event_timestamp timestamp without time zone,
  provider_id character varying,
  booking_target_id character varying,
  user_id character varying,
  booking_id character varying,
  time_period ixsi.time_period,
  from_place_id character varying,
  to_place_id character varying,
  CONSTRAINT "FK_booking_create_btid_pid" FOREIGN KEY (booking_target_id, provider_id)
    REFERENCES ixsi.booking_target (booking_target_id, provider_id) ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT "FK_booking_create_from_pid" FOREIGN KEY (from_place_id)
    REFERENCES ixsi.place (place_id) ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT "FK_booking_create_to_pid" FOREIGN KEY (to_place_id)
    REFERENCES ixsi.place (place_id) ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TYPE ixsi.booking_change_type
  AS ENUM ('UPDATE', 'CANCEL');

CREATE TABLE ixsi.booking_change
(
  event_timestamp timestamp without time zone,
  event_type ixsi.booking_change_type,
  booking_id character varying,
  new_time_period ixsi.time_period
);