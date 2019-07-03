SHOW timezone;

SET timezone='Europe/Paris';

CREATE FUNCTION exec(text) RETURNS text AS $$ BEGIN EXECUTE $1; RETURN $1; END $$ LANGUAGE plpgsql;

select exec('ALTER TABLE ' || table_name || ' ALTER ' || column_name || ' TYPE TIMESTAMP WITH TIME ZONE;') As command from information_schema.columns where table_schema ='public' and data_type ='timestamp without time zone';

drop function exec(text);

--set timezone back to original value;
--SET timezone='Europe/Paris';
