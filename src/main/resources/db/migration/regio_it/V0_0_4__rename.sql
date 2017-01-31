ALTER TABLE regio_it.app_auth ADD COLUMN roles character varying[];
ALTER TABLE regio_it.app_auth RENAME TO auth;