DELETE FROM cmn_application_preference where preference_name='cas_url';

insert into  tm_oid_root_definition_label (id,label) select nextval('tm_oid_root_definition_label_id_seq'),comment from tm_oid_root_definition;
update tm_oid_root_definition set label_id = (select tm_oid_root_definition_label.id from tm_oid_root_definition_label where label = tm_oid_root_definition.comment);
ALTER TABLE tm_oid_root_definition DROP COLUMN comment;

INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (nextval('cmn_application_preference_id_seq'), 'java.lang.Boolean','Display test menu as not loggedIn', 'is_tests_display_as_not_logged_in', 'false');

DELETE FROM cmn_application_preference where preference_name='dds_ws_endpoint';
DELETE FROM cmn_application_preference where preference_name='dds_mode';
DELETE FROM cmn_application_preference where preference_name='svs_repository_url';
DELETE FROM cmn_application_preference where preference_name='hl7v3_authority_oid';
DELETE FROM cmn_application_preference where preference_name='hl7v3_sender_device_id';
DELETE FROM cmn_application_preference where preference_name='enable_order_manager';
DELETE FROM cmn_application_preference where preference_name='create_worklist_url';

DROP TABLE tm_patient_assigning_authority_sis, tm_patient_patient_identifier ,
 tm_patient_identifier , tm_patient_assigning_autority, pat_patient_address , pat_patient, tm_patient_type_code,tm_share_patient_logger ;
