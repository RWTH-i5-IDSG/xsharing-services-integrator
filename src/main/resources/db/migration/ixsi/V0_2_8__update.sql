CREATE TABLE ixsi.base_push_event (
  partner_id INTEGER NOT NULL,
  booking_id CHARACTER VARYING(255) NOT NULL,
  event_timestamp TIMESTAMP WITHOUT TIME ZONE,
  success BOOLEAN NOT NULL,
  FOREIGN KEY (partner_id) REFERENCES ixsi.server_system (partner_id)
  MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE ixsi.push_event_consumption ( ) INHERITS (ixsi.base_push_event);

CREATE TABLE ixsi.push_event_external_booking ( ) INHERITS (ixsi.base_push_event);
