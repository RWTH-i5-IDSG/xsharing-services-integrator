ALTER TABLE ixsi.consumption DROP COLUMN name;
ALTER TABLE ixsi.consumption DROP COLUMN description;
ALTER TABLE ixsi.consumption ADD internal_booking_id SERIAL NOT NULL;
ALTER TABLE ixsi.consumption ADD PRIMARY KEY (internal_booking_id);
ALTER TABLE ixsi.consumption ADD final BOOLEAN DEFAULT FALSE NOT NULL;

CREATE TABLE ixsi.consumption_description
(
  internal_booking_id integer NOT NULL,
  language ixsi.language NOT NULL,
  value character varying(255),
  CONSTRAINT "PK_consumption_description" PRIMARY KEY (internal_booking_id, language),
  CONSTRAINT "FK_consumption_description_internal_bid" FOREIGN KEY (internal_booking_id)
  REFERENCES ixsi.consumption (internal_booking_id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE CASCADE
);