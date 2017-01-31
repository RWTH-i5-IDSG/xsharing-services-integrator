CREATE OR REPLACE FUNCTION ixsi.array_distinct_append(to_array anyarray, the_element anyelement)
  RETURNS anyarray AS
$BODY$
  SELECT ARRAY( 
    SELECT DISTINCT unnest(array_append(to_array, the_element))
  )
$BODY$
  LANGUAGE sql IMMUTABLE
  COST 100;