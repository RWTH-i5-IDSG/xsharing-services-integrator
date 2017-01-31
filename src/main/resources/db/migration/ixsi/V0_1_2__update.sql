ALTER TABLE ixsi.placegroup
  ADD COLUMN partner_id integer;

ALTER TABLE ixsi.placegroup
  ADD CONSTRAINT "FK_placegroup_partner_id" FOREIGN KEY (partner_id)
  REFERENCES ixsi.server_system (partner_id) ON UPDATE NO ACTION ON DELETE CASCADE;
