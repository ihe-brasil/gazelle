-- Populates the proxy_type lookup table
insert into proxy_type (id, keyword, description, label_key_for_display) values 
    (1, 'GAZELLE', 'Gazelle Proxy', 'proxyType.gazelle'),
    (2, 'NIST', 'NIST Proxy', 'proxyType.nist');
    
insert into validation_service (id,key,base_url,url_path,description,label_key_for_display,xslt_url) values
	(1,'GAZELLEHL7V2VALIDATOR','http://131.254.209.20:8080','/GazelleHL7v2Validator-GazelleHL7v2Validator-ejb/GazelleHL7v2ValidationWS','GAZELLE HL7 V2 VALIDATOR','gazelle.tm.tee.validation.servicetype.gazelle','http://gazelle.ihe.net/xsl/hl7Validation/resultStylesheet.xsl'),
	(2,'NISTHL7V2VALIDATOR','http://hit-testing.nist.gov:8080','/hl7v2ws/services/soap/MessageValidationV2/','NIST HL7 V2 VALIDATOR','gazelle.tm.tee.validation.servicetype.nist','http://tano.li/xmlv2reportTohtml.xsl');
	
-- Ugly/Temporary way of inserting tm_test_step_message_type values for test steps until UI allows test editor to select/persist to the database for tm_test_steps
insert into tm_test_step_message_profile (id, auto_validate, direction, tf_hl7_message_profile_id, tm_test_steps_id) values
  (1, 1, 0, 77, 1498423),
  (2, 1, 1, 147, 1498423),    
  (3, 1, 0, 77, 1498433),
  (4, 1, 1, 147, 1498433),
  (5, 1, 0, 77, 1498434),
  (6, 1, 1, 147, 1498434);
  	
-- Lookup entries for possible validation_service_type entries for each tf_hl7_message_profile
insert into message_validation_service (id, tm_test_step_message_profile_id, validation_service_id) values
  (1, 1, 1),
  (2, 2, 1),    
  (3, 3, 1),
  (4, 4, 1),
  (5, 5, 1),
  (6, 6, 1);
  

insert into  cmn_application_preference(
            id, class_name, description, preference_name, preference_value) values
  (10559, 'java.lang.Boolean', 'Allow user to delete test steps', 'delete_on_gmm', 'true');

