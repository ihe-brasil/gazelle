-- new application preferences
INSERT INTO cmn_application_preference(id, class_name, description, preference_name, preference_value) VALUES (10015, 'java.lang.String', 'URL to retrieve value sets', 'svs_repository_url', 'http://gazelle.ihe.net');
INSERT INTO cmn_application_preference(id, class_name, description, preference_name, preference_value) VALUES (10016, 'java.lang.String', 'OID of the authority used to send HL7v3 messages', 'hl7v3_authority_oid', 'to be determined');
INSERT INTO cmn_application_preference(id, class_name, description, preference_name, preference_value) VALUES (10017, 'java.lang.String', 'ID of the sender device for HL7v3', 'hl7v3_sender_device_id', 'to be determined');

-- remove tm_patient_type_code table and foreign key
ALTER TABLE tm_patient_identifier DROP COLUMN type_code_id;
DROP TABLE tm_patient_type_code;

-- update patient identifiers
update tm_patient_identifier set type_code = 'PI';

-- uniformize HL7 version in logs
update tm_share_patient_logger set hl7_version = '2.5' where hl7_version like '%25';
update tm_share_patient_logger set hl7_version = '2.3.1' where hl7_version like '%2.3.1';
update tm_share_patient_logger set hl7_version = '2.5' where hl7_version like '%2.5';
update tm_share_patient_logger set hl7_version = '3' where hl7_version like '%3';


-- update patient data
update tm_patient set state = address_state;
update tm_patient set zip_code = address_zip_code;
update tm_patient set city = address_city;
update tm_patient set street = address_street;
update tm_patient set country_code = address_country_code;
update tm_patient set creation_date = last_changed;
update tm_patient set creator_id = last_modifier_id;

-- remove old columns
alter table tm_patient drop column address_state;
alter table tm_patient drop column address_zip_code;
alter table tm_patient drop column address_city;
alter table tm_patient drop column address_street;
alter table tm_patient drop column address_country_code;
alter table tm_patient drop column last_changed;
alter table tm_patient drop column last_modifier_id;