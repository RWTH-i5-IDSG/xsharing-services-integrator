--
-- rename booking_target_address to booking_target_status_address
--

DROP TABLE ixsi.booking_target_address;

CREATE TABLE ixsi.booking_target_status_address
(
  booking_target_id character varying(255) NOT NULL,
  provider_id character varying(255) NOT NULL,
  CONSTRAINT "PK_booking_target_status_address" PRIMARY KEY (booking_target_id, provider_id),
  CONSTRAINT "FK_booking_target_status_address_btid_pid" FOREIGN KEY (booking_target_id, provider_id)
  REFERENCES ixsi.booking_target (booking_target_id, provider_id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE CASCADE
)
  INHERITS (ixsi.base_address);

--
-- rename booking_target_inavailability to booking_target_status_inavailability
--

DROP TABLE ixsi.booking_target_inavailability;

CREATE TABLE ixsi.booking_target_status_inavailability
(
  booking_target_id character varying(255) NOT NULL,
  provider_id character varying(255) NOT NULL,
  inavailability ixsi.time_period NOT NULL,
  CONSTRAINT "PK_booking_target_status_inavailability" PRIMARY KEY (booking_target_id, provider_id, inavailability),
  CONSTRAINT "FK_booking_target_status_inavailability_btid_pid" FOREIGN KEY (booking_target_id, provider_id)
  REFERENCES ixsi.booking_target (booking_target_id, provider_id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE CASCADE
);

--
-- incorporate place, placegroup and floating_area tables, which are part of static data, into the base booking_target
--

ALTER TABLE ixsi.booking_target
  ADD COLUMN exclusive_to_floating_area_id character varying(255);
ALTER TABLE ixsi.booking_target
  ADD COLUMN exclusive_to_place_id character varying(255);
ALTER TABLE ixsi.booking_target
  ADD COLUMN exclusive_to_placegroup_id character varying(255);
ALTER TABLE ixsi.booking_target
  ADD CONSTRAINT "FK_booking_target_faid" FOREIGN KEY (exclusive_to_floating_area_id) REFERENCES ixsi.floating_area (floating_area_id)
  ON UPDATE NO ACTION ON DELETE CASCADE;
ALTER TABLE ixsi.booking_target
  ADD CONSTRAINT "FK_booking_target_plid" FOREIGN KEY (exclusive_to_place_id) REFERENCES ixsi.place (place_id)
  ON UPDATE NO ACTION ON DELETE CASCADE;
ALTER TABLE ixsi.booking_target
  ADD CONSTRAINT "FK_booking_target_pgid" FOREIGN KEY (exclusive_to_placegroup_id) REFERENCES ixsi.placegroup (placegroup_id)
  ON UPDATE NO ACTION ON DELETE CASCADE;

DROP TABLE ixsi.booking_target_floating_area;
DROP TABLE ixsi.booking_target_place;
DROP TABLE ixsi.booking_target_placegroup;

--
-- new table to store place changes in availability subscription
--

CREATE TABLE ixsi.booking_target_status_place
(
  booking_target_id character varying(255) NOT NULL,
  provider_id character varying(255) NOT NULL,
  place_id character varying(255) NOT NULL,
  CONSTRAINT "PK_booking_target_status_place" PRIMARY KEY (booking_target_id, provider_id),
  CONSTRAINT "FK_booking_target_status_place_btid_pid" FOREIGN KEY (booking_target_id, provider_id)
  REFERENCES ixsi.booking_target (booking_target_id, provider_id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT "FK_booking_target_status_place_plid" FOREIGN KEY (place_id)
  REFERENCES ixsi.place (place_id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE CASCADE
);