ALTER TABLE ixsi.server_system
ALTER COLUMN btir_delivery_timestamp TYPE timestamp without time zone;
COMMENT ON COLUMN ixsi.server_system.btir_delivery_timestamp IS 'Timestamp of BookingTargetsInfoResponse';