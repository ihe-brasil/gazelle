-- Function: f_readfile(text)
-- This function uses postgres system administration function pg_read_file to read the file contents. 
-- It requires the file to be present in the data directory of postgres. For example, in windows the 
-- data directory is C:\Program Files\PostgreSQL\9.1\data.

CREATE OR REPLACE FUNCTION f_readfile(myfile text)
  RETURNS text AS
$BODY$
BEGIN

RETURN pg_read_file(myfile, 0, 1000000); -- 1 MB max.Gazelle profile XMLs for PIX/PDQ are less than 600 KB. 

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION f_readfile(text)
  OWNER TO postgres;
