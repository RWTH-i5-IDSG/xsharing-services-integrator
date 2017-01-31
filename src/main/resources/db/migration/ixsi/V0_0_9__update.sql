--
-- distinguish consumptions not by provider_id, but by partner_id
--
ALTER TABLE ixsi.consumption
  DROP COLUMN provider_id;

ALTER TABLE ixsi.consumption
  ADD COLUMN partner_id integer NOT NULL;

ALTER TABLE ixsi.consumption
  ADD CONSTRAINT "FK_consumption_partner_id" FOREIGN KEY (partner_id) REFERENCES ixsi.server_system (partner_id)
  ON UPDATE NO ACTION ON DELETE NO ACTION;
