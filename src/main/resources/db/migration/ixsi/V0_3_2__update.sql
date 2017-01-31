ALTER TABLE ixsi.user
  ALTER COLUMN pin TYPE character varying(255)
  USING pin::text;
