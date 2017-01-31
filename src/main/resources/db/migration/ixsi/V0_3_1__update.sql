-- 0) Prepare constraints
ALTER TABLE ixsi.push_event_consumption
  DROP CONSTRAINT push_event_consumption_consumption_id_fkey,
  ADD FOREIGN KEY (consumption_id) REFERENCES ixsi.consumption (consumption_id)
  ON UPDATE NO ACTION ON DELETE CASCADE;

-- 1) Add the split columns
ALTER TABLE ixsi.consumption
    ADD COLUMN time_period_from timestamp without time zone,
    ADD COLUMN time_period_to timestamp without time zone;

-- 2) split time_period (tsrange) into the two new columns
UPDATE ixsi.consumption con
  SET time_period_from = lower(con.time_period),
      time_period_to = upper(con.time_period);

-- 3) delete all consumptions with 'empty' tsrange.
-- with this we lose data, but it is not a deal breaker since we can always subscribe to get the consumptions again
WITH booking_id_select AS (
    SELECT DISTINCT cc.booking_id
    FROM ixsi.consumption cc
    WHERE cc.time_period = 'empty'
)
DELETE FROM ixsi.consumption c
WHERE c.booking_id IN (SELECT booking_id FROM booking_id_select);

-- 4) delete time_period (redundant now)
ALTER TABLE ixsi.consumption DROP COLUMN time_period;
