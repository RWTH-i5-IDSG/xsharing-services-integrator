CREATE TABLE ixsi.server_feature (
  partner_id INTEGER,
  feature_group CHARACTER VARYING(25) NOT NULL,
  feature_name CHARACTER VARYING(50) NOT NULL,
  UNIQUE (partner_id, feature_group, feature_name),
  FOREIGN KEY (partner_id) REFERENCES ixsi.server_system (partner_id)
  MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION
);
COMMENT ON TABLE ixsi.server_feature IS 'Contains ALL activated IXSI features (i.e. service-operation pairs) for a partner';