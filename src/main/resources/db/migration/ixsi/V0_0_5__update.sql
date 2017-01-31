ALTER TABLE ixsi.consumption
  ADD COLUMN provider_id character varying(255) NOT NULL;

ALTER TABLE ixsi.consumption
  ADD CONSTRAINT "FK_consumption_provider_id" FOREIGN KEY (provider_id) REFERENCES ixsi.provider (provider_id) ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE ixsi.consumption
  DROP CONSTRAINT "PK_consumption";

DROP TABLE ixsi.parameters;

CREATE TABLE ixsi.server_system
(
  partner_id serial NOT NULL,
  partner_name character varying(50) NOT NULL,
  enabled boolean NOT NULL DEFAULT FALSE,
  base_path character varying(50) NOT NULL,
  number_of_connections smallint NOT NULL,
  btir_delivery_timestamp timestamp with time zone, -- Timestamp of BookingTargetsInfoResponse
  CONSTRAINT "PK_partner" PRIMARY KEY (partner_id)
);

COMMENT ON COLUMN ixsi.server_system.btir_delivery_timestamp IS 'Timestamp of BookingTargetsInfoResponse';

INSERT INTO ixsi.server_system (partner_name, base_path, number_of_connections) VALUES ('TestPartner', 'ws://localhost:80/test', 1);
