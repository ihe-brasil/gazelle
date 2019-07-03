UPDATE tf_actor SET last_modifier_id='Unknown' where last_modifier_id is null;

--This line need to be executed on GMM due to slony
UPDATE tf_actor SET last_modifier_id='Unknown' where last_modifier_id='0';

INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (nextval('cmn_application_preference_id_seq'), 'java.lang.String', '', 'application_no_reply_email', 'no-reply@ihe-europe.net');