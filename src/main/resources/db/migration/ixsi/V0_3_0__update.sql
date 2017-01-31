DROP TABLE ixsi.push_event_consumption;

CREATE TABLE ixsi.push_event_consumption (
  consumption_id INTEGER NOT NULL,
  event_timestamp TIMESTAMP WITHOUT TIME ZONE,
  success BOOLEAN NOT NULL,
  FOREIGN KEY (consumption_id) REFERENCES ixsi.consumption (consumption_id)
  MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
);
