INSERT INTO tm_test_type (id,description,keyword,label_to_display) values
(nextval('tm_test_type_id_seq'),'interoperability-testbed','interoperability-testbed','interoperability-testbed');



INSERT INTO tm_step_inst_msg_process_status (id,key,description,label_key_for_display) values
(nextval('tm_step_inst_msg_process_status_id_seq'),'PROCESSING','Message is being processed.','net.ihe.gazelle.tm.message.processing');

INSERT INTO tm_step_inst_msg_process_status (id,key,description,label_key_for_display) values
(nextval('tm_step_inst_msg_process_status_id_seq'),'PROCESSED','Message has been processed.','net.ihe.gazelle.tm.message.processed');

INSERT INTO tm_step_inst_msg_process_status (id,key,description,label_key_for_display) values
(nextval('tm_step_inst_msg_process_status_id_seq'),'ERROR','Message processing encountered an unexpected error.','net.ihe.gazelle.tm.message.error');



INSERT INTO tm_step_instance_exec_status (id,key,description,label_key_for_display) values
(nextval('tm_step_instance_exec_status_id_seq'),'INACTIVE','Not active yet.','net.ihe.gazelle.tm.Inactive');

INSERT INTO tm_step_instance_exec_status (id,key,description,label_key_for_display) values
(nextval('tm_step_instance_exec_status_id_seq'),'WAITING','Active but no messages have been intercepted yet from sender and receiver','net.ihe.gazelle.tm.Waiting');

INSERT INTO tm_step_instance_exec_status (id,key,description,label_key_for_display) values
(nextval('tm_step_instance_exec_status_id_seq'),'INTERRUPTED','The test step instance was stopped by the system after it had become active at one point.','net.ihe.gazelle.tm.Interrupted');

INSERT INTO tm_step_instance_exec_status (id,key,description,label_key_for_display) values
(nextval('tm_step_instance_exec_status_id_seq'),'SKIPPED','The test step has been skipped by the participant.','net.ihe.gazelle.tm.Skipped');

INSERT INTO tm_step_instance_exec_status (id,key,description,label_key_for_display) values
(nextval('tm_step_instance_exec_status_id_seq'),'PAUSED','The test step has been paused by the participant after it had become active.','net.ihe.gazelle.tm.Paused');

INSERT INTO tm_step_instance_exec_status (id,key,description,label_key_for_display) values
(nextval('tm_step_instance_exec_status_id_seq'),'ABORTED','The test step has been aborted by the participant after it had become active.','net.ihe.gazelle.tm.Aborted');

INSERT INTO tm_step_instance_exec_status (id,key,description,label_key_for_display) values
(nextval('tm_step_instance_exec_status_id_seq'),'INITIATED','A request message has been intercepted.','net.ihe.gazelle.tm.Initiated');

INSERT INTO tm_step_instance_exec_status (id,key,description,label_key_for_display) values
(nextval('tm_step_instance_exec_status_id_seq'),'RESPONDED','A response message has been intercepted.','net.ihe.gazelle.tm.Responded');

INSERT INTO tm_step_instance_exec_status (id,key,description,label_key_for_display) values
(nextval('tm_step_instance_exec_status_id_seq'),'COMPLETED','The test step instance is complete.','net.ihe.gazelle.tm.Completed');

INSERT INTO tm_test_instance_exec_status (id,key,description,label_key_for_display) values
(nextval('tm_test_instance_exec_status_id_seq'),'INACTIVE','Not active yet.','net.ihe.gazelle.tm.Inactive');

INSERT INTO tm_test_instance_exec_status (id,key,description,label_key_for_display) values
(nextval('tm_test_instance_exec_status_id_seq'),'ACTIVE','One or more test step instances are in active status (waiting, initiated, responded).','net.ihe.gazelle.tm.Active');

INSERT INTO tm_test_instance_exec_status (id,key,description,label_key_for_display) values
(nextval('tm_test_instance_exec_status_id_seq'),'INTERRUPTED','The test instance was stopped by the system after it had become active at one point.','net.ihe.gazelle.tm.Interrupted');

INSERT INTO tm_test_instance_exec_status (id,key,description,label_key_for_display) values
(nextval('tm_test_instance_exec_status_id_seq'),'SKIPPED','The test instance has been skipped by the participant.','net.ihe.gazelle.tm.Skipped');

INSERT INTO tm_test_instance_exec_status (id,key,description,label_key_for_display) values
(nextval('tm_test_instance_exec_status_id_seq'),'PAUSED','The test instance has been paused by the participant after it had become active.','net.ihe.gazelle.tm.Paused');

INSERT INTO tm_test_instance_exec_status (id,key,description,label_key_for_display) values
(nextval('tm_test_instance_exec_status_id_seq'),'ABORTED','The test instance has been aborted by the participant after it had become active.','net.ihe.gazelle.tm.Aborted');

INSERT INTO tm_test_instance_exec_status (id,key,description,label_key_for_display) values
(nextval('tm_test_instance_exec_status_id_seq'),'COMPLETED','The test step instance is complete.','net.ihe.gazelle.tm.Completed');


INSERT INTO validation_status (id,key,description,label_key_for_display) values
(nextval('validation_status_id_seq'),'ERROR','Validation service could not be called.','gazelle.tm.tee.validation.status.error');

INSERT INTO validation_status (id,key,description,label_key_for_display) values
(nextval('validation_status_id_seq'),'FAIL','Validation service returned errors.','gazelle.tm.tee.validation.status.failed');

INSERT INTO validation_status (id,key,description,label_key_for_display) values
(nextval('validation_status_id_seq'),'WARNING','Validation service returned warnings.','gazelle.tm.tee.validation.status.warning');

INSERT INTO validation_status (id,key,description,label_key_for_display) values
(nextval('validation_status_id_seq'),'PASS','Validation passed successfully.','gazelle.tm.tee.validation.status.passed');


insert into proxy_type (id, keyword, description, label_key_for_display) values 
(nextval('proxy_type_sequence'), 'GAZELLE', 'Gazelle Proxy', 'proxyType.gazelle'),
(nextval('proxy_type_sequence'), 'NIST', 'NIST Proxy', 'proxyType.nist');

insert into validation_service (id,key,base_url,url_path,description,label_key_for_display,xslt_url) values
(nextval('validation_service_id_seq'),'GAZELLEHL7V2VALIDATOR','http://131.254.209.20:8080','/GazelleHL7v2Validator-GazelleHL7v2Validator-ejb/GazelleHL7v2ValidationWS','GAZELLE HL7 V2 VALIDATOR','gazelle.tm.tee.validation.servicetype.gazelle','http://gazelle.ihe.net/xsl/hl7Validation/resultStylesheet.xsl'),
(nextval('validation_service_id_seq'),'NISTHL7V2VALIDATOR','http://hl7v2.ws.nist.gov:8090','/hl7v2ws/services/soap/MessageValidationV2/','NIST HL7 V2 VALIDATOR','gazelle.tm.tee.validation.servicetype.nist','http://tano.li/xmlv2reportTohtml.xsl');

