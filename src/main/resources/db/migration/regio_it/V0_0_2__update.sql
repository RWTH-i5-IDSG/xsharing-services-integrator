ALTER TABLE regio_it.app_auth
  DROP COLUMN token_update_timestamp,
  ADD COLUMN token_update_timestamp timestamp with time zone;
