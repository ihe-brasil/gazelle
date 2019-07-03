CREATE FUNCTION get_aipo(actor_value text, profile_value text, option_value text) RETURNS text
    LANGUAGE plpgsql
    AS $$
DECLARE
  a_id INTEGER ;
  profile_id INTEGER ;
  option_id INTEGER ;
  ap_id INTEGER ;
  aipo_id INTEGER;

BEGIN

SELECT INTO a_id id FROM tf_actor WHERE keyword = actor_value;
SELECT INTO profile_id id FROM tf_integration_profile WHERE keyword = profile_value;
SELECT INTO option_id id FROM tf_integration_profile_option where keyword = option_value;
SELECT INTO ap_id  id from tf_actor_integration_profile where actor_id = a_id and   integration_profile_id = profile_id ;
SELECT INTO aipo_id id from tf_actor_integration_profile_option where actor_integration_profile_id = ap_id and integration_profile_option_id = option_id ;

return aipo_id ;
END;
$$;


ALTER FUNCTION public.get_aipo(actor_value text, profile_value text, option_value text) OWNER TO gazelle;

-- DOMAIN ID
DROP FUNCTION get_domain_id(domain_keyword text);
CREATE FUNCTION get_domain_id(domain_keyword text) RETURNS integer
  LANGUAGE plpgsql
  AS $$
DECLARE
  a_id INTEGER := -1;

BEGIN
  SELECT INTO a_id id FROM tf_domain WHERE keyword = domain_keyword;
  return a_id;
END;
$$;

ALTER FUNCTION public.get_domain_id(domain_keyword text) OWNER TO gazelle;


-- DOMAIN KW
DROP FUNCTION get_domain_keyword(domain_id integer);
CREATE FUNCTION get_domain_keyword(domain_id integer)RETURNS TEXT
  LANGUAGE plpgsql
  AS $$
DECLARE
  a_id TEXT := '';

BEGIN
  SELECT INTO a_id keyword FROM tf_domain WHERE id = domain_id;
  return a_id;
END;
$$;

ALTER FUNCTION public.get_domain_keyword(domain_id integer) OWNER TO gazelle;



-- ACTOR ID
DROP FUNCTION get_actor_id(actor_keyword text);
CREATE FUNCTION get_actor_id(actor_keyword text) RETURNS integer
  LANGUAGE plpgsql
  AS $$
DECLARE
  a_id INTEGER := -1;

BEGIN
    SELECT INTO a_id id FROM tf_actor WHERE keyword = actor_keyword;
    return a_id;
END;
$$;

ALTER FUNCTION public.get_actor_id(actor_keyword text) OWNER TO gazelle;

-- ACTOR ID BIS (return the id or -1 if the keyword does not exist)
DROP FUNCTION get_actor_id_bis(actor_keyword text);
CREATE FUNCTION get_actor_id_bis(actor_keyword text) RETURNS integer
  LANGUAGE plpgsql
  AS $$
  DECLARE
  ip_id INTEGER := -1;
BEGIN
  SELECT INTO ip_id CASE WHEN (SELECT COUNT(*) FROM tf_actor where keyword = actor_keyword) = 0 THEN -1 else (SELECT id FROM tf_actor where keyword = actor_keyword) END;
  return ip_id;
END;
$$;

ALTER FUNCTION public.get_actor_id_bis(actor_keyword text) OWNER TO gazelle;

-- ACTOR KW
DROP FUNCTION get_actor_keyword(actor_id integer);
CREATE FUNCTION get_actor_keyword(actor_id integer) RETURNS TEXT
  LANGUAGE plpgsql
  AS $$
DECLARE
  a_id TEXT := '';

BEGIN
  SELECT INTO a_id keyword FROM tf_actor WHERE id = actor_id;
  return a_id;
END;
$$;

ALTER FUNCTION public.get_actor_keyword(actor_id integer) OWNER TO gazelle;




-- PROFILE
DROP FUNCTION get_profile_id(ip_keyword text);
CREATE FUNCTION get_profile_id(ip_keyword text) RETURNS integer
  LANGUAGE plpgsql
  AS $$
DECLARE
  ip_id INTEGER := -1;

BEGIN
  SELECT INTO ip_id id FROM tf_integration_profile WHERE keyword = ip_keyword;
  return ip_id;
END;
$$;

ALTER FUNCTION public.get_profile_id(ip_keyword text) OWNER TO gazelle;

-- PROFILE BIS (return the id or -1 if the keyword does not exist)
DROP FUNCTION get_profile_id_bis(ip_keyword text);
CREATE FUNCTION get_profile_id_bis(ip_keyword text) RETURNS integer
  LANGUAGE plpgsql
  AS $$
  DECLARE
  ip_id INTEGER := -1;
BEGIN
  SELECT INTO ip_id CASE WHEN (SELECT COUNT(*) FROM tf_integration_profile where keyword = ip_keyword) = 0 THEN -1 else (SELECT id FROM tf_integration_profile where keyword = ip_keyword) END;
  return ip_id;
END;
$$;

ALTER FUNCTION public.get_profile_id_bis(ip_keyword text) OWNER TO gazelle;

-- PROFILE KW
DROP FUNCTION get_profile_keyword(ip_id integer);
CREATE FUNCTION get_profile_keyword(ip_id integer) RETURNS text
  LANGUAGE plpgsql
  AS $$
DECLARE
  ip_keyword text := '';

BEGIN
  SELECT INTO ip_keyword keyword FROM tf_integration_profile WHERE id = ip_id;
  return ip_keyword;
END;
$$;

ALTER FUNCTION public.get_profile_keyword(ip_id integer) OWNER TO gazelle;



-- OPTION
DROP FUNCTION get_integration_profile_option_id(integration_profile_option_keyword text);
CREATE FUNCTION get_integration_profile_option_id(integration_profile_option_keyword text) RETURNS integer
  LANGUAGE plpgsql
  AS $$
DECLARE
  ip_id INTEGER := -1;

BEGIN
  SELECT INTO ip_id id FROM tf_integration_profile_option WHERE keyword = integration_profile_option_keyword;
  return ip_id;
END;
$$;

ALTER FUNCTION public.get_integration_profile_option_id(integration_profile_option_keyword text) OWNER TO gazelle;

-- OPTION BIS (return the id or -1 if the keyword does not exist)
DROP FUNCTION get_integration_profile_option_bis(integration_profile_option_keyword text);
CREATE FUNCTION get_integration_profile_option_bis(integration_profile_option_keyword text) RETURNS integer
  LANGUAGE plpgsql
  AS $$
  DECLARE
  ip_id INTEGER := -1;
BEGIN
  SELECT INTO ip_id CASE WHEN (SELECT COUNT(*) FROM tf_integration_profile_option where keyword = integration_profile_option_keyword) = 0 THEN -1 else (SELECT id FROM tf_integration_profile_option where keyword = integration_profile_option_keyword) END;
  return ip_id;
END;
$$;

ALTER FUNCTION public.get_integration_profile_option_bis(integration_profile_option_keyword text) OWNER TO gazelle;


-- TRANSACTION
DROP FUNCTION get_transaction_id(transaction_keyword text);
CREATE FUNCTION get_transaction_id(transaction_keyword text) RETURNS integer
  LANGUAGE plpgsql
  AS $$
DECLARE
  ip_id INTEGER := -1;

BEGIN
  SELECT INTO ip_id id FROM tf_transaction WHERE keyword = transaction_keyword;
  return ip_id;
END;
$$;

ALTER FUNCTION public.get_transaction_id(transaction_keyword text) OWNER TO gazelle;

-- TRANSACTION BIS (return the id or -1 if the keyword does not exist)
DROP FUNCTION get_transaction_id_bis(transaction_keyword text);
CREATE FUNCTION get_transaction_id_bis(transaction_keyword text) RETURNS integer
  LANGUAGE plpgsql
  AS $$
  DECLARE
  ip_id INTEGER := -1;
BEGIN
  SELECT INTO ip_id CASE WHEN (SELECT COUNT(*) FROM tf_transaction where keyword = transaction_keyword) = 0 THEN -1 else (SELECT id FROM tf_transaction where keyword = transaction_keyword) END;
  return ip_id;
END;
$$;

ALTER FUNCTION public.get_transaction_id_bis(transaction_keyword text) OWNER TO gazelle;

-- TRANSACTION KW
DROP FUNCTION get_transaction_keyword(transaction_id INTEGER);
CREATE FUNCTION get_transaction_keyword(transaction_id INTEGER) RETURNS TEXT
  LANGUAGE plpgsql
  AS $$
DECLARE
  ip_id TEXT := '';

BEGIN
  SELECT INTO ip_id keyword FROM tf_transaction WHERE id = transaction_id;
  return ip_id;
END;
$$;

ALTER FUNCTION public.get_transaction_keyword(transaction_id INTEGER) OWNER TO gazelle;


-- DOCUMENT SECTION
DROP FUNCTION get_document_section_id(document_section_keyword text);
CREATE FUNCTION get_document_section_id(document_section_keyword text) RETURNS integer
  LANGUAGE plpgsql
  AS $$
DECLARE
  ip_id INTEGER := -1;

BEGIN
  SELECT INTO ip_id id FROM tf_document_sections WHERE section = document_section_keyword;
  return ip_id;
END;
$$;

ALTER FUNCTION public.get_document_section_id(document_section_keyword text) OWNER TO gazelle;


-- ACTOR INTEGRATION PROFILE
DROP FUNCTION get_actor_integration_profile_id(actor_keyword text, integration_profile_keyword text);
CREATE FUNCTION get_actor_integration_profile_id(actor_keyord text, integration_profile_keyword text) RETURNS integer
  LANGUAGE plpgsql
  AS $$
DECLARE
  ip_id INTEGER := -1;

BEGIN
  SELECT INTO ip_id id FROM tf_actor_integration_profile WHERE actor_id = get_actor_id($1) and integration_profile_id = get_profile_id($2);
  return ip_id;
END;
$$;

ALTER FUNCTION public.get_actor_integration_profile_id(actor_keyword text, integration_profile_keyword text) OWNER TO gazelle;

-- ACTOR INTEGRATION PROFILE OPTION
DROP FUNCTION get_actor_integration_profile_option_id(actor_keyword text, integration_profile_keyword text, option text);
CREATE FUNCTION get_actor_integration_profile_option_id(actor_keyord text, integration_profile_keyword text, option text) RETURNS integer
  LANGUAGE plpgsql
  AS $$
DECLARE
  aipo_id INTEGER := -1;

BEGIN
  SELECT INTO aipo_id id FROM tf_actor_integration_profile_option WHERE integration_profile_option_id = get_integration_profile_option_id($3) and actor_integration_profile_id = get_actor_integration_profile_id($1, $2);
  return aipo_id;
END;
$$;

ALTER FUNCTION public.get_actor_integration_profile_option_id(actor_keyword text, integration_profile_keyword text) OWNER TO gazelle;


--
-- Name: get_number_of_participant_for_institution(integer, integer); Type: FUNCTION; Schema: public; Owner: gazelle
--

CREATE FUNCTION get_number_of_participant_for_institution(integer, integer) RETURNS integer
    LANGUAGE plpgsql
    AS $_$declare
n integer;
s integer;
begin

SELECT into n count(id)  from tm_connectathon_participant where testing_session_id = $2 and institution_ok_id = $1 ; 
return n ;
end;
$_$;


ALTER FUNCTION public.get_number_of_participant_for_institution(integer, integer) OWNER TO gazelle;

--
-- Name: get_test_steps_option_label(integer); Type: FUNCTION; Schema: public; Owner: gazelle
--

CREATE FUNCTION get_test_steps_option_label(input_id integer) RETURNS text
    LANGUAGE plpgsql
    AS $$
DECLARE 
option_label text;
BEGIN
SELECT INTO option_label label_to_display FROM tm_test_steps_option WHERE id = input_id;
return option_label;
END
$$;


ALTER FUNCTION public.get_test_steps_option_label(input_id integer) OWNER TO gazelle;

--
-- Name: insert_patient(text, text, text, text, text, timestamp without time zone, timestamp without time zone, boolean, text, text, integer, text, text, text, text, text, text, text, text, text, text, text, text); Type: FUNCTION; Schema: public; Owner: gazelle
--

CREATE FUNCTION insert_patient(address_city_value text, address_country_value text, address_state_value text, address_street_value text, address_zip_code_value text, date_of_birth_value timestamp without time zone, date_of_death_value timestamp without time zone, editable_value boolean, first_name_value text, gender_value text, derived_from_value integer, last_name_value text, race_value text, religion_value text, address_country_code_value text, address_state_code_value text, gender_code_value text, race_code_value text, religion_code_value text, creator_id_value text, mother_maiden_name_value text, test_keyword_value text, pid_value text) RETURNS text
    LANGUAGE plpgsql
    AS $$
DECLARE
  p_id INTEGER ;
  pid_blue_id INTEGER ;
  pid_red_id INTEGER ;
  pid_yellow_id INTEGER ;

BEGIN

SELECT INTO p_id  nextval('tm_patient_id_seq') ;
SELECT INTO pid_blue_id nextval('tm_patient_identifier_id_seq') ;
SELECT INTO pid_red_id nextval('tm_patient_identifier_id_seq') ;
SELECT INTO pid_yellow_id nextval('tm_patient_identifier_id_seq') ;

INSERT INTO tm_patient (id, last_changed, last_modifier_id, address_city, address_country, address_state, address_street, address_zip_code, date_of_birth, date_of_death, editable, first_name, gender, derived_from, last_name, race, religion, address_country_code, address_state_code, gender_code, race_code, religion_code, creator_id, mother_maiden_name, test_keyword) VALUES (p_id, 'now', 'epoiseau',  address_city_value, address_country_value, address_state_value, address_street_value, address_zip_code_value, date_of_birth_value, date_of_death_value, editable_value, first_name_value, gender_value, derived_from_value, last_name_value, race_value, religion_value, address_country_code_value, address_state_code_value, gender_code_value, race_code_value, religion_code_value, creator_id_value, mother_maiden_name_value, test_keyword_value);
INSERT INTO tm_patient_identifier (id ,identifier, ass_auth_id, type_code_id ) values (pid_blue_id, pid_value, 5, 1 );
INSERT INTO tm_patient_identifier (id ,identifier, ass_auth_id, type_code_id ) values (pid_yellow_id, pid_value, 9, 1 );
INSERT INTO tm_patient_identifier (id ,identifier, ass_auth_id, type_code_id ) values (pid_red_id, pid_value, 10, 1 );
INSERT INTO tm_patient_patient_identifier VALUES (p_id, pid_blue_id);
INSERT INTO tm_patient_patient_identifier VALUES (p_id, pid_yellow_id);
INSERT INTO tm_patient_patient_identifier VALUES (p_id, pid_red_id);

return 1 ;
END;
$$;


ALTER FUNCTION public.insert_patient(address_city_value text, address_country_value text, address_state_value text, address_street_value text, address_zip_code_value text, date_of_birth_value timestamp without time zone, date_of_death_value timestamp without time zone, editable_value boolean, first_name_value text, gender_value text, derived_from_value integer, last_name_value text, race_value text, religion_value text, address_country_code_value text, address_state_code_value text, gender_code_value text, race_code_value text, religion_code_value text, creator_id_value text, mother_maiden_name_value text, test_keyword_value text, pid_value text) OWNER TO gazelle;



--
-- Name: is_proxy_used(integer, integer); Type: FUNCTION; Schema: public; Owner: gazelle
--

CREATE FUNCTION is_proxy_used(integer, integer) RETURNS integer
    LANGUAGE plpgsql IMMUTABLE STRICT
    AS $_$declare
    n integer;
    begin
    SELECT into n
  count(tm_test_steps_data.id)
FROM
  public.tm_test_instance,
  public.tm_test,
  public.tm_test_steps_data,
  public.tm_test_steps_instance,
  public.tm_test_instance_test_steps_instance,
  public.tm_test_steps_test_steps_data
WHERE
  tm_test.id = tm_test_instance.test_id AND
  tm_test_instance_test_steps_instance.test_instance_id = tm_test_instance.id AND
  tm_test_instance_test_steps_instance.test_steps_instance_id = tm_test_steps_instance.id AND
  tm_test_steps_test_steps_data.test_steps_data_id = tm_test_steps_data.id AND
  tm_test_steps_test_steps_data.test_steps_id = tm_test_steps_instance.id AND
  tm_test_instance.testing_session_id = $2 AND
  tm_test_steps_data.data_type_id = 2 AND tm_test_instance.id = $1 ;
  return n;
  end;
$_$;


ALTER FUNCTION public.is_proxy_used(integer, integer) OWNER TO gazelle;


--
-- Name: remove_duplicates(text); Type: FUNCTION; Schema: public; Owner: gazelle
--

CREATE FUNCTION remove_duplicates(text) RETURNS void
    LANGUAGE plpgsql
    AS $_$
DECLARE
  tablename ALIAS FOR $1;
BEGIN
  EXECUTE 'CREATE TEMPORARY TABLE _DISTINCT_' || tablename || ' AS (SELECT DISTINCT * FROM ' || tablename || ');';
  EXECUTE 'DELETE FROM ' || tablename || ';';
  EXECUTE 'INSERT INTO ' || tablename || ' (SELECT * FROM _DISTINCT_' || tablename || ');';
  EXECUTE 'DROP TABLE _DISTINCT_' || tablename || ';';
  RETURN;
END;
$_$;


ALTER FUNCTION public.remove_duplicates(text) OWNER TO gazelle;


--
-- Name: set_oids(integer); Type: FUNCTION; Schema: public; Owner: gazelle
--

CREATE FUNCTION set_oids(id_to_process integer) RETURNS text
    LANGUAGE plpgsql
    AS $$
DECLARE
  oid_root_id INTEGER ;
  oid_value_new TEXT ;
BEGIN

SELECT INTO oid_root_id tord.id from tm_oid_system_assignment tosa, tm_oid_requirement tor, tm_oid_root_definition tord where tor.oid_root_definition_id = tord.id and tosa.id= id_to_process and tosa.oid_requirement_id = tor.id;
select into oid_value_new root_oid || '.' || last_value from tm_oid_root_definition where id = oid_root_id ;
UPDATE tm_oid_root_definition set last_value = last_value + 1 where id = oid_root_id ;
UPDATE tm_oid_system_assignment set oid_value = oid_value_new  where id = id_to_process ;
return oid_root_id as new_oid_value;
END;
$$;


ALTER FUNCTION public.set_oids(id_to_process integer) OWNER TO gazelle;


--
-- Name: update_actor_integration_profile_option_id(integer, integer); Type: FUNCTION; Schema: public; Owner: gazelle
--

CREATE FUNCTION update_actor_integration_profile_option_id(old_id integer, new_id integer) RETURNS text
    LANGUAGE plpgsql
    AS $$

BEGIN
UPDATE tm_system_actor_profiles set actor_integration_profile_option_id =new_id where actor_integration_profile_option_id =old_id ;
return 1 ;
END;
$$;


ALTER FUNCTION public.update_actor_integration_profile_option_id(old_id integer, new_id integer) OWNER TO gazelle;

--
-- Name: update_tm_test_roles_id(integer, integer); Type: FUNCTION; Schema: public; Owner: gazelle
--

CREATE FUNCTION update_tm_test_roles_id(old_id integer, new_id integer) RETURNS text
    LANGUAGE plpgsql
    AS $$
BEGIN
INSERT INTO tm_test_roles select new_id as id,  last_changed, last_modifier_id,   card_max, card_min , url , url_doc , role_in_test_id , test_id , test_option_id , number_of_tests_to_realize   from tm_test_roles where id = old_id ;
UPDATE tm_test_steps set test_roles_initiator_id =new_id where test_roles_initiator_id =old_id ;
UPDATE tm_test_steps_aud set test_roles_initiator_id =new_id where test_roles_initiator_id =old_id ;
UPDATE tm_test_steps set test_roles_responder_id =new_id where test_roles_responder_id =old_id ;
UPDATE tm_test_steps_aud set test_roles_responder_id =new_id where test_roles_responder_id =old_id ;
UPDATE tm_meta_test_test_roles set test_roles_id =new_id where test_roles_id =old_id ;
UPDATE tm_meta_test_test_roles_aud set test_roles_id =new_id where test_roles_id =old_id ;
DELETE FROM tm_test_roles where id = old_id ;
DELETE FROM tm_test_roles_aud where id = old_id ;
return 1 ;
END;
$$;


ALTER FUNCTION public.update_tm_test_roles_id(old_id integer, new_id integer) OWNER TO gazelle;

--
-- Name: update_transaction_id(integer, integer); Type: FUNCTION; Schema: public; Owner: gazelle
--

CREATE FUNCTION update_transaction_id(old_id integer, new_id integer) RETURNS text
    LANGUAGE plpgsql
    AS $$
BEGIN
insert into tf_transaction select new_id as id, last_changed, last_modifier_id, description, keyword || '_' || old_id as keyword, name || ' (fix me)' as name , transaction_status_type_id  from tf_transaction where id = old_id ;
UPDATE tf_profile_link set transaction_id=new_id where transaction_id=old_id ;
UPDATE tf_profile_link_aud set transaction_id =new_id where transaction_id =old_id ;
UPDATE tf_transaction_link set transaction_id =new_id where transaction_id =old_id ;
UPDATE tf_transaction_link_aud set transaction_id =new_id where transaction_id =old_id ;
UPDATE tf_hl7_message_profile set transaction_id =new_id where transaction_id =old_id ;
UPDATE tm_test_steps set transaction_id =new_id where transaction_id =old_id ;
UPDATE tm_test_steps_aud set transaction_id =new_id where transaction_id =old_id ;
DELETE FROM tf_transaction where id = old_id ;
UPDATE tf_transaction SET name = replace (name, ' (fix me)', '');
UPDATE tf_transaction SET keyword = replace (keyword, '_' || old_id, '');
return 1 ;
END;
$$;


ALTER FUNCTION public.update_transaction_id(old_id integer, new_id integer) OWNER TO gazelle;

--
-- Name: textcat_all(text); Type: AGGREGATE; Schema: public; Owner: gazelle
--

CREATE AGGREGATE textcat_all(text) (
    SFUNC = textcat,
    STYPE = text,
    INITCOND = ''
);


ALTER AGGREGATE public.textcat_all(text) OWNER TO gazelle;
