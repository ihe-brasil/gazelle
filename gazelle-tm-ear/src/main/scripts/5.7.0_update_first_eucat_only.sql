-- EU-CAT
UPDATE tm_host set hostname = 'g200' where id = 1446 ;
UPDATE tm_host set hostname = 'g201' where id = 1448 ;
UPDATE tm_host set hostname = 'g202' where id = 1606 ;

UPDATE tm_host set hostname = 'nist100' where id = 1130 ;
UPDATE tm_host set hostname = 'nist101' where id = 1384 ;

UPDATE tm_host set hostname = 'ideosante3' where id = 1365 ;
UPDATE tm_host set hostname = 'elco7' where id = 145 ;
UPDATE tm_host set hostname = 'elco8' where id = 144 ;
UPDATE tm_host set hostname = 'elco9' where id = 348 ;
UPDATE tm_host set hostname = 'elco10' where id = 347 ;
UPDATE tm_host set hostname = 'bd.medisys.com.my1' where id = 1887;

-- ALL
-- Delete all entries that are broken
delete from tm_host where system_in_session_id is null ;

-- Update for all execpt IZAZA, GE and ITH
update tm_host SET institution_id  =
(SELECT
  tm_institution_system.institution_id
FROM
  public.tm_host h,
  public.tm_system_in_session,
  public.tm_institution_system
WHERE
  tm_host.system_in_session_id = tm_system_in_session.id AND
  tm_system_in_session.system_id = tm_institution_system.system_id AND
    tm_host.id = h.id and tm_institution_system.institution_id != 20 and  tm_institution_system.institution_id != 35 and  tm_institution_system.institution_id != 37);

-- Take care of GE ITH and ISAZA
update tm_host  set institution_id = 20 where institution_id is null and hostname like 'ge%';
update tm_host  set institution_id = 37 where institution_id is null and hostname like 'ith-icoserve%';
update tm_host  set institution_id = 20 where institution_id is null;

-- Set testing session id in the host.
UPDATE tm_host
SET testing_session_id = a.testing_session_id FROM
  (SELECT
     tm_host.system_in_session_id,
     tm_system_in_session.testing_session_id
   FROM tm_host, tm_system_in_session
   WHERE tm_host.system_in_session_id = tm_system_in_session.id) AS a
WHERE tm_host.system_in_session_id = a.system_in_session_id;

-- Drop old view (never used)
DROP VIEW hl7_destination_adt;
DROP VIEW hl7_destination_of;
DROP VIEW hl7_destination_ofim;
DROP VIEW hl7_destination_op;

ALTER TABLE tm_host DROP COLUMN system_in_session_id;

UPDATE tm_host SET used_by_more_one_system_in_session = FALSE;

-- update to PatientGeneration - 2.3.0
ALTER TABLE pat_patient_address ADD COLUMN dtype VARCHAR(31);
ALTER TABLE pam_phone_number ADD COLUMN dtype VARCHAR(31);
UPDATE pat_patient_address SET dtype = 'patient_address';
UPDATE pam_phone_number SET dtype = 'patient_phone_number';