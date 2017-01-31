ALTER TABLE ixsi.attribute
  ADD COLUMN partner_id integer;

ALTER TABLE ixsi.attribute
  ADD CONSTRAINT "FK_attribute_partner_id" FOREIGN KEY (partner_id)
  REFERENCES ixsi.server_system (partner_id) ON UPDATE NO ACTION ON DELETE CASCADE;
