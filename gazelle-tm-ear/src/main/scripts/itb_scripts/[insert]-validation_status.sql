INSERT INTO validation_status (id,key,description,label_key_for_display) values
(1,'ERROR','Validation service could not be called.','gazelle.tm.tee.validation.status.error');

INSERT INTO validation_status (id,key,description,label_key_for_display) values
(2,'FAIL','Validation service returned errors.','gazelle.tm.tee.validation.status.fail');

INSERT INTO validation_status (id,key,description,label_key_for_display) values
(3,'WARNING','Validation service returned warnings.','gazelle.tm.tee.validation.status.warning');

INSERT INTO validation_status (id,key,description,label_key_for_display) values
(4,'PASS','Validation passed successfully.','gazelle.tm.tee.validation.status.pass');