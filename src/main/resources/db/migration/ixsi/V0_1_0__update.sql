ALTER TABLE ixsi.provider
  ADD COLUMN partner_id integer,
  ADD CONSTRAINT "FK_provider_partner_id" FOREIGN KEY (partner_id) REFERENCES ixsi.server_system (partner_id) ON UPDATE NO ACTION ON DELETE NO ACTION;