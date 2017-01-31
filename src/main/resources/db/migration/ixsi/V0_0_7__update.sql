DROP TABLE ixsi.placegroup_probability;

CREATE TABLE ixsi.placegroup_place
(
  placegroup_id character varying(255) NOT NULL,
  place_id character varying(255) NOT NULL,
  probability smallint,
  CONSTRAINT "PK_placegroup_place" PRIMARY KEY (placegroup_id, place_id),
  CONSTRAINT "FK_placegroup_place_pgid" FOREIGN KEY (placegroup_id)
      REFERENCES ixsi.placegroup (placegroup_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT "FK_placegroup_place_pid" FOREIGN KEY (place_id)
      REFERENCES ixsi.place (place_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE,
  CONSTRAINT "CH_probability" CHECK (probability <= 0 AND probability <= 100)
);
