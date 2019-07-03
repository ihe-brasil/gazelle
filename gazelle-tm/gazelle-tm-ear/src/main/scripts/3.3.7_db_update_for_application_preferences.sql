-- This values were missing from the application_preferences dump


INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (nextval('cmn_application_preference_id_seq'), 'java.lang.Integer', '', 'ping_time', '30');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (nextval('cmn_application_preference_id_seq'), 'java.lang.Boolean', 'Is PR', 'is_product_registry', 'false');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (nextval('cmn_application_preference_id_seq'), 'java.lang.Boolean', 'Is TM', 'is_test_management', 'true');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (nextval('cmn_application_preference_id_seq'), 'java.lang.Boolean', '', 'deploy_scheduled', 'false');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (nextval('cmn_application_preference_id_seq'), 'java.lang.String', '', 'default_color', '#4a75b5');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (nextval('cmn_application_preference_id_seq'), 'java.lang.Boolean', 'Is GMM', 'is_master_model', 'false');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (nextval('cmn_application_preference_id_seq'), 'java.lang.Boolean', '', 'help_show', 'false');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (nextval('cmn_application_preference_id_seq'), 'java.lang.String', '', 'application_gazelle_documentation_url', 'http://gazelle.ihe.net/content/gazelle');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (nextval('cmn_application_preference_id_seq'), 'java.lang.String', '', 'application_admin_title', 'IHE Test Management');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (nextval('cmn_application_preference_id_seq'), 'java.lang.String', '', 'use_messages_session_timeout', '60');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (nextval('cmn_application_preference_id_seq'), 'java.lang.Boolean', '', 'auto_update_results', 'true');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (nextval('cmn_application_preference_id_seq'), 'java.lang.Boolean', '', 'use_messages', 'true');

INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (nextval('cmn_application_preference_id_seq'), 'java.lang.String', '', 'x-frame-options', 'SAMEORIGIN');

INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (nextval('cmn_application_preference_id_seq'), 'java.lang.String', '', 'cache-control', 'private, no-cache, no-store, must-revalidate, max-age=0');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (nextval('cmn_application_preference_id_seq'), 'java.lang.String', '', 'content-security-policy', '');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (nextval('cmn_application_preference_id_seq'), 'java.lang.String', '', 'content-security-policy-report-only', 'default-src ''self'' *.ihe.net www.epsos.eu; script-src ''self'' ''unsafe-eval'' ''unsafe-inline''; style-src ''self'' ''unsafe-inline'';');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (nextval('cmn_application_preference_id_seq'), 'java.lang.String', '', 'strict-transport-security', 'max-age=31536000 ; includeSubDomains');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (nextval('cmn_application_preference_id_seq'), 'java.lang.Boolean', '', 'security-policies', 'true');
