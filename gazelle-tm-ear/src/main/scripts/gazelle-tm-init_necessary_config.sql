--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

--
-- Data for Name: cmn_application_preference; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (5, 'java.lang.String', 'Account (email address) where are sent all emails as BCC (for emails history/checking in)', 'application_email_account_for_history', 'net.gazelle@gmail.com');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (8, 'java.lang.String', 'Container for google analytics code (eg: "UA-2810172-2" for Gazelle) ', 'google_analytics_code', '');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (9, 'java.lang.String', 'Issue Tracker Website URL - Used by some links in the application to report a problem (eg. of value = http://sumo.irisa.fr:8080/jira/ )', 'application_issue_tracker_url', 'http://gazelle.ihe.net/jira/');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (12, 'java.lang.String', 'Application profile (possible values :  "dev", "prod") - Rendered pages/components depend on the profile value ', 'application_profile', 'prod');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (13, 'java.lang.String', 'Application build time (Ant)', 'application_build_time', 'November 02, 2009 - 10:02 AM');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (14, 'java.lang.String', 'This value does NOT change. It indicates if DB has been dropped and SQL imported during deployment ? ', 'application_db_drop_and_import', 'false');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (15, 'java.lang.String', 'This value does NOT change. It indicates if Drools has beeen loaded at startup ? (to improve speed deployment)', 'application_drools_loaded_at_startup', 'true');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (22, 'java.lang.String', 'RELATIVE PATH TO gazelle_home_path. This value normally does NOT change. It corresponds to Gazelle data path inside "gazelle_home_path". Here is where data (invoices, integration statements... will be stored).', 'data_path', 'data');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (23, 'java.lang.String', 'RELATIVE PATH TO gazelle_home_path. This value normally does NOT change. It corresponds to Gazelle reports path inside "gazelle_home_path". Here is where are stored required reports', 'reports_path', 'reports');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (24, 'java.lang.String', 'RELATIVE PATH TO gazelle_home_path. This value normally does NOT change. It corresponds to Gazelle bin path inside "gazelle_home_path". Here is where are stored required binaries', 'bin_path', 'bin');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (25, 'java.lang.String', 'RELATIVE PATH TO gazelle_home_path. This value normally does NOT change. It corresponds to Integration Statements path inside "data_path".', 'integration_statements_path', 'integrationStatements');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (30, 'java.lang.String', 'RELATIVE PATH TO gazelle_home_path. This value normally does NOT change. It corresponds to HL7 Conformance Statements path inside "data_path".', 'hl7_conformance_statements_path', 'hl7ConformanceStatements');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (31, 'java.lang.String', 'RELATIVE PATH TO gazelle_home_path. This value normally does NOT change. It corresponds to DICOM Conformance Statements path inside "data_path".', 'dicom_conformance_statements_path', 'dicomConformanceStatements');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (26, 'java.lang.String', 'RELATIVE PATH TO gazelle_home_path. This value normally does NOT change. It corresponds to Invoices path inside "data_path".', 'invoices_path', 'invoices');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (27, 'java.lang.String', 'RELATIVE PATH TO gazelle_home_path. This value normally does NOT change. It corresponds to Contracts path inside "data_path".', 'contracts_path', 'contracts');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (28, 'java.lang.String', 'RELATIVE PATH TO gazelle_home_path. This value normally does NOT change. It corresponds to Certificates path inside "data_path".', 'certificates_path', 'certificates');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (29, 'java.lang.String', 'RELATIVE PATH TO gazelle_home_path. This value normally does NOT change. It corresponds to log return path inside "data_path".', 'log_return_path', 'logReturns');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (32, 'java.lang.String', 'RELATIVE PATH TO gazelle_home_path. This value normally does NOT change. It corresponds to objects path inside "data_path".', 'objects_path', 'objects');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (56, 'java.lang.Boolean', 'Boolean value true or false', 'certificates_generated_by_host', 'true');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (57, 'java.lang.Integer', 'Length in bit of the key used for certificate', 'key_length_encryption', '1024');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (9999, 'java.lang.String', 'Checking Flag : if this value is inserted in the database, it means that database has been successfully populated during deployment', 'application_database_initialization_flag', 'database_successfully_initialized');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (2, 'java.lang.String', 'Application name', 'application_name', 'Gazelle');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10, 'java.lang.String', 'Gazelle Release Notes URL - Used by some links in the application to detail each Gazelle Release : improvements, bug fixed (eg. of value = http://sumo.irisa.fr/europe2009/gazelle_release_notes.html )', 'application_gazelle_release_notes_url', 'http://gazelle.ihe.net/gazelle_release_notes.html');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (80, 'java.lang.String', 'Flag indicating if the application is a Gazelle application or a GMM (Master Model) application. Values : "SLAVE" for Gazelle, "MASTER" for GMM', 'gazelle_master_model_application', 'SLAVE');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (92, 'java.lang.String', '', 'gazelle_proxy_message', '/message.seam?id=');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (83, 'java.lang.Boolean', 'validate automatically files of logs on mesa tests or not.', 'mesa_test_automatic_validation', 'true');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (82, 'java.lang.Boolean', 'Send or not notifications to system-users if the status of a mesaTest change', 'send_mail_mesa_notification', 'false');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (19, 'java.lang.String', 'Nist XDS EVS', 'service_xdsevs', 'http://ihexds.nist.gov:9080/tf6/services/xdsevs?wsdl');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (93, 'java.lang.String', '', 'gazelle_proxy_step', '/searchMessageStep.seam?id=');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (73, 'java.lang.String', '', 'gazelle_proxy_webservice_endpoint', 'http://kujira2.irisa.fr:8080/gazelle-proxy-gazelle-proxy-ejb/ProxyForTM?wsdl');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10001, 'java.lang.Integer', 'Number of the next invoice, needed for invoice generation', 'nextInvoiceNumber', '1');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (11, 'java.lang.String', 'Application zone (possible values :  "EUROPE", "NA" , "JAPAN") - Rendered pages depend on the zone (homepage, contacts, informations... )', 'application_zone', '');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (33, 'java.lang.String', 'path to the repository where users photo are stored', 'photo_basedir', '');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (70, 'java.lang.String', 'URL corresponding to the Active BPEL server', 'active-bpel_server_url', '');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10002, 'java.lang.String', 'CAS URL', 'cas_url', '');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (88, 'java.lang.String', 'link to the wsdl of schematron validator', 'service_schematron_validator', 'http://131.254.209.12:8080/SchematronValidator-prod-SchematronValidator-ejb/GazelleObjectValidatorWS?wsdl');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (90, 'java.lang.String', 'URL of Gazelle Master Model', 'gazelle_master_model', 'http://gazelle.ihe.net/GMM');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (91, 'java.lang.String', 'the name of the path', 'file_steps_path', 'filesteps');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (72, 'java.lang.String', '', 'gazelle_proxy_url', 'http://gazelle.ihe.net/proxy');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (1, 'java.lang.String', 'URL used to reach the application', 'application_url', 'http://localhost:8080/TM');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (3, 'java.lang.String', 'Admin (name) used to manage the application', 'application_admin_name', '');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (4, 'java.lang.String', 'Admin (email) used to manage the application', 'application_admin_email', '');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (6, 'java.lang.String', 'Default time zone used by the application', 'time_zone', '');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (7, 'java.lang.String', 'URL basename used to reach the application', 'application_url_basename', '');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (21, 'java.lang.String', 'ABSOLUTE PATH TO Gazelle home path. Here is where are stored generated datas, reports and other required binaries for application', 'gazelle_home_path', '');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (34, 'java.lang.String', 'Base URL to access the users photo', 'photo_base_url', '');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (35, 'java.lang.Boolean', 'indicates if the installation (new User, new Institution...) has been performed or not', 'installation_done', 'false');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10003, 'java.lang.Boolean', 'Is CAS Enabled', 'cas_enabled', 'false');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10005, 'java.lang.Integer', '', 'ping_time', '30');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10006, 'java.lang.Boolean', 'Is PR', 'is_product_registry', 'false');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10007, 'java.lang.Boolean', 'Is TM', 'is_test_management', 'true');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10008, 'java.lang.Boolean', '', 'deploy_scheduled', 'false');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10009, 'java.lang.String', '', 'default_color', '#4a75b5');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10010, 'java.lang.Boolean', 'Is GMM', 'is_master_model', 'false');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10011, 'java.lang.Boolean', '', 'help_show', 'false');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10012, 'java.lang.String', '', 'application_gazelle_documentation_url', 'http://gazelle.ihe.net/content/gazelle');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10013, 'java.lang.String', '', 'application_admin_title', 'IHE Test Management');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10014, 'java.lang.String', '', 'use_messages_session_timeout', '60');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10015, 'java.lang.Boolean', '', 'auto_update_results', 'true');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10016, 'java.lang.Boolean', '', 'use_messages', 'true');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10019, 'java.lang.String', '', 'content-security-policy', '');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10020, 'java.lang.String', '', 'content-security-policy-report-only', 'default-src ''self'' *.ihe.net www.epsos.eu; script-src ''self'' ''unsafe-eval'' ''unsafe-inline''; style-src ''self'' ''unsafe-inline'';');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10022, 'java.lang.Boolean', '', 'security-policies', 'true');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10018, 'java.lang.String', '', 'Cache-Control', 'private, no-cache, no-store, must-revalidate, max-age=0');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10021, 'java.lang.String', '', 'Strict-Transport-Security', 'max-age=31536000 ; includeSubDomains');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10024, 'java.lang.String', 'Chrome flag, uses X-Content-Security-Policy values', 'X-WebKit-CSP', '');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10023, 'java.lang.String', 'is an added layer of security that helps to detect and mitigate certain types of attacks, including Cross Site Scripting (XSS) and data injection attacks', 'X-Content-Security-Policy', '');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10017, 'java.lang.String', 'Sites can use this to avoid clickjacking attacks, by ensuring that their content is not embedded into other sites', 'X-Frame-Options', 'SAMEORIGIN');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10025, 'java.lang.String', 'Use X-Content-Security-Policy-Report-Only values', 'X-WebKit-CSP-Report-Only', '');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10026, 'java.lang.String', '', 'X-Content-Security-Policy-Report-Only', 'default-src ''self'' *.ihsql_injection_filter_switche.net www.epsos.eu; script-src ''self'' ''unsafe-eval'' ''unsafe-inline''; style-src ''self'' ''unsafe-inline''');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10027, 'java.lang.Boolean', '', 'sql_injection_filter_switch', 'true');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10028, 'java.lang.String', '', 'gazelle_hl7_validator_url', 'http://gazelle.ihe.net/GazelleHL7Validator');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10044, 'java.lang.String', '', 'x-frame-options', 'SAMEORIGIN');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10045, 'java.lang.String', '', 'cache-control', 'private, no-cache, no-store, must-revalidate, max-age=0');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10048, 'java.lang.String', '', 'strict-transport-security', 'max-age=31536000 ; includeSubDomains');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10051, 'java.lang.String', '', 'atna_msg_specification_url', 'http://gazelle.ihe.net/tls');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10052, 'java.lang.Integer', 'Time between two update of partners in ms', 'partners_check_interval', '30000');
INSERT INTO cmn_application_preference (id, class_name, description, preference_name, preference_value) VALUES (10053, 'java.lang.Integer', 'Time between two update of CAT result in ms', 'cat_result_check_interval', '30000');




--
-- Name: cmn_application_preference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('cmn_application_preference_id_seq', 10053, true);


--
-- Name: cmn_event_tracking_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('cmn_event_tracking_id_seq', 1, false);


--
-- Name: cmn_number_of_results_per_page_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('cmn_number_of_results_per_page_id_seq', 1000, true);


--
-- Data for Name: cmn_path_linking_a_document; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: cmn_path_linking_a_document_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('cmn_path_linking_a_document_id_seq', 61, true);


--
-- Name: hibernate_sequence; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('hibernate_sequence', 18745, true);


--
-- Data for Name: tf_actor; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_domain; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_document; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_document_sections; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_transaction_status_type; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_transaction; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_hl7_message_profile; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_ws_transaction_usage; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_role_in_test; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_test_peer_type; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO tm_test_peer_type (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (1, NULL, NULL, 'gazelle.tm.testing.test.description.NoPeerTest', 'NO_PEER_TEST', 'gazelle.tm.testing.test.label.NoPeerTest');
INSERT INTO tm_test_peer_type (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (2, NULL, NULL, 'gazelle.tm.testing.test.description.P2PTest', 'P2P_TEST', 'gazelle.tm.testing.test.label.P2PTest');
INSERT INTO tm_test_peer_type (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (3, NULL, NULL, 'gazelle.tm.testing.test.description.WorkflowTest', 'WORKFLOW_TEST', 'gazelle.tm.testing.test.label.WorkflowTest');
INSERT INTO tm_test_peer_type (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (4, NULL, NULL, 'gazelle.tm.testing.test.description.Unassigned', 'UNASSIGNED', 'gazelle.tm.testing.test.label.Unassigned');


--
-- Data for Name: tm_test_status; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO tm_test_status (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (1, NULL, NULL, 'The test can be used', 'ready', 'ready');
INSERT INTO tm_test_status (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (2, NULL, NULL, 'The test is deprecated', 'deprecated', 'deprecated');
INSERT INTO tm_test_status (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (3, NULL, NULL, 'the test in not compelete', 'to be completed', 'to be completed');
INSERT INTO tm_test_status (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (4, NULL, NULL, '...', 'storage/substitute', 'storage/substitute');
INSERT INTO tm_test_status (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (5, NULL, NULL, 'the test is imported from kudu and needs verification', 'ready in Kudu', 'ready in kudu');
INSERT INTO tm_test_status (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (6, NULL, NULL, 'the test will be converted at a later time', 'convert later', 'convert later');


--
-- Data for Name: tm_test_type; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO tm_test_type (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (2, NULL, NULL, 'connectathon', 'connectathon', 'connectathon');
INSERT INTO tm_test_type (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (3, '2010-02-10 13:11:05.839043+00', 'epoiseau', 'HITSP', 'HITSP', 'HITSP');
INSERT INTO tm_test_type (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (1, NULL, NULL, 'pre-connectathon', 'pre-connectathon', 'pre-connectathon');
INSERT INTO tm_test_type (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (4, NULL, NULL, 'interoperability-testbed', 'interoperability-testbed', 'interoperability-testbed');


--
-- Data for Name: tm_test; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_test_option; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO tm_test_option (id, last_changed, last_modifier_id, available, description, keyword, label_to_display) VALUES (3, NULL, NULL, false, 'Required if Option Supported', 'RO', 'Required if Option Supported');
INSERT INTO tm_test_option (id, last_changed, last_modifier_id, available, description, keyword, label_to_display) VALUES (4, NULL, NULL, false, 'Test Incomplete', 'x', 'Test Incomplete');
INSERT INTO tm_test_option (id, last_changed, last_modifier_id, available, description, keyword, label_to_display) VALUES (5, NULL, NULL, false, 'Deprecated (not required)', 'D', 'Deprecated (not required)');
INSERT INTO tm_test_option (id, last_changed, last_modifier_id, available, description, keyword, label_to_display) VALUES (6, NULL, NULL, false, 'Placeholder for future year; not required', 'Z', 'Placeholder');
INSERT INTO tm_test_option (id, last_changed, last_modifier_id, available, description, keyword, label_to_display) VALUES (7, NULL, NULL, false, 'Optional for Client', 'C', 'Optional for Client');
INSERT INTO tm_test_option (id, last_changed, last_modifier_id, available, description, keyword, label_to_display) VALUES (8, NULL, NULL, false, 'Required for Server', 'S', 'Required for Server');
INSERT INTO tm_test_option (id, last_changed, last_modifier_id, available, description, keyword, label_to_display) VALUES (9, NULL, NULL, false, 'Optional for client, Required for Server', 'CS', 'Optional for client');
INSERT INTO tm_test_option (id, last_changed, last_modifier_id, available, description, keyword, label_to_display) VALUES (10, NULL, NULL, false, 'Placeholder for future year, test will be optional; not required for the moment', 'ZO', 'Placeholder for future year');
INSERT INTO tm_test_option (id, last_changed, last_modifier_id, available, description, keyword, label_to_display) VALUES (11, NULL, NULL, false, 'Required for Multiple Document Submission Option', 'MD', 'Required for Multiple Document Submission Option');
INSERT INTO tm_test_option (id, last_changed, last_modifier_id, available, description, keyword, label_to_display) VALUES (12, NULL, NULL, false, 'Used for Document Life Cycle Management Option', 'LC', 'Used for Document Life Cycle Management Option');
INSERT INTO tm_test_option (id, last_changed, last_modifier_id, available, description, keyword, label_to_display) VALUES (13, NULL, NULL, false, 'Used for Folder Management Option', 'F', 'Used for Folder Management Option');
INSERT INTO tm_test_option (id, last_changed, last_modifier_id, available, description, keyword, label_to_display) VALUES (2, NULL, NULL, true, 'Optional', 'O', 'Optional');
INSERT INTO tm_test_option (id, last_changed, last_modifier_id, available, description, keyword, label_to_display) VALUES (1, NULL, NULL, true, 'Required', 'R', 'Required');


--
-- Data for Name: tm_test_roles; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_test_steps_option; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO tm_test_steps_option (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (1, NULL, NULL, 'Required', 'R', 'Required');
INSERT INTO tm_test_steps_option (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (2, NULL, NULL, 'Optional', 'O', 'Optional');


--
-- Data for Name: tm_test_steps; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_test_step_message_profile; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: validation_service; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: message_validation_service; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: message_validation_service_sequence; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('message_validation_service_sequence', 1, false);


--
-- Data for Name: pr_crawler_reporting; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: sys_system_type; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: usr_iso_3166_country_code; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('-', NULL, NULL, '', '', NULL, '');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('YT', NULL, 'flags/yt.gif', NULL, 'MAYOTTE', NULL, 'Mayotte');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('AF', NULL, 'flags/af.gif', 'AFG', 'AFGHANISTAN', 4, 'Afghanistan');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('AL', NULL, 'flags/al.gif', 'ALB', 'ALBANIA', 8, 'Albania');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('DZ', NULL, 'flags/dz.gif', 'DZA', 'ALGERIA', 12, 'Algeria');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('AS', NULL, 'flags/as.gif', 'ASM', 'AMERICAN SAMOA', 16, 'American Samoa');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('AD', NULL, 'flags/ad.gif', 'AND', 'ANDORRA', 20, 'Andorra');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('AO', NULL, 'flags/ao.gif', 'AGO', 'ANGOLA', 24, 'Angola');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('AI', NULL, 'flags/ai.gif', 'AIA', 'ANGUILLA', 660, 'Anguilla');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('AQ', NULL, 'flags/aq.gif', NULL, 'ANTARCTICA', NULL, 'Antarctica');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('AG', NULL, 'flags/ag.gif', 'ATG', 'ANTIGUA AND BARBUDA', 28, 'Antigua and Barbuda');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('AR', NULL, 'flags/ar.gif', 'ARG', 'ARGENTINA', 32, 'Argentina');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('AM', NULL, 'flags/am.gif', 'ARM', 'ARMENIA', 51, 'Armenia');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('AW', NULL, 'flags/aw.gif', 'ABW', 'ARUBA', 533, 'Aruba');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('AU', NULL, 'flags/au.gif', 'AUS', 'AUSTRALIA', 36, 'Australia');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('AZ', NULL, 'flags/az.gif', 'AZE', 'AZERBAIJAN', 31, 'Azerbaijan');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('BS', NULL, 'flags/bs.gif', 'BHS', 'BAHAMAS', 44, 'Bahamas');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('BH', NULL, 'flags/bh.gif', 'BHR', 'BAHRAIN', 48, 'Bahrain');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('BD', NULL, 'flags/bd.gif', 'BGD', 'BANGLADESH', 50, 'Bangladesh');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('BB', NULL, 'flags/bb.gif', 'BRB', 'BARBADOS', 52, 'Barbados');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('BY', NULL, 'flags/by.gif', 'BLR', 'BELARUS', 112, 'Belarus');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('BZ', NULL, 'flags/bz.gif', 'BLZ', 'BELIZE', 84, 'Belize');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('BJ', NULL, 'flags/bj.gif', 'BEN', 'BENIN', 204, 'Benin');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('BM', NULL, 'flags/bm.gif', 'BMU', 'BERMUDA', 60, 'Bermuda');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('BT', NULL, 'flags/bt.gif', 'BTN', 'BHUTAN', 64, 'Bhutan');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('BO', NULL, 'flags/bo.gif', 'BOL', 'BOLIVIA', 68, 'Bolivia');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('BA', NULL, 'flags/ba.gif', 'BIH', 'BOSNIA AND HERZEGOVINA', 70, 'Bosnia and Herzegovina');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('BW', NULL, 'flags/bw.gif', 'BWA', 'BOTSWANA', 72, 'Botswana');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('BV', NULL, 'flags/bv.gif', NULL, 'BOUVET ISLAND', NULL, 'Bouvet Island');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('BR', NULL, 'flags/br.gif', 'BRA', 'BRAZIL', 76, 'Brazil');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('IO', NULL, 'flags/io.gif', NULL, 'BRITISH INDIAN OCEAN TERRITORY', NULL, 'British Indian Ocean Territory');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('BN', NULL, 'flags/bn.gif', 'BRN', 'BRUNEI DARUSSALAM', 96, 'Brunei Darussalam');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('BF', NULL, 'flags/bf.gif', 'BFA', 'BURKINA FASO', 854, 'Burkina Faso');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('BI', NULL, 'flags/bi.gif', 'BDI', 'BURUNDI', 108, 'Burundi');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('KH', NULL, 'flags/kh.gif', 'KHM', 'CAMBODIA', 116, 'Cambodia');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('CM', NULL, 'flags/cm.gif', 'CMR', 'CAMEROON', 120, 'Cameroon');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('CA', NULL, 'flags/ca.gif', 'CAN', 'CANADA', 124, 'Canada');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('CV', NULL, 'flags/cv.gif', 'CPV', 'CAPE VERDE', 132, 'Cape Verde');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('KY', NULL, 'flags/ky.gif', 'CYM', 'CAYMAN ISLANDS', 136, 'Cayman Islands');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('CF', NULL, 'flags/cf.gif', 'CAF', 'CENTRAL AFRICAN REPUBLIC', 140, 'Central African Republic');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('TD', NULL, 'flags/td.gif', 'TCD', 'CHAD', 148, 'Chad');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('CL', NULL, 'flags/cl.gif', 'CHL', 'CHILE', 152, 'Chile');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('CN', NULL, 'flags/cn.gif', 'CHN', 'CHINA', 156, 'China');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('CX', NULL, 'flags/cx.gif', NULL, 'CHRISTMAS ISLAND', NULL, 'Christmas Island');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('CC', NULL, 'flags/cc.gif', NULL, 'COCOS (KEELING) ISLANDS', NULL, 'Cocos (Keeling) Islands');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('CO', NULL, 'flags/co.gif', 'COL', 'COLOMBIA', 170, 'Colombia');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('KM', NULL, 'flags/km.gif', 'COM', 'COMOROS', 174, 'Comoros');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('CG', NULL, 'flags/cg.gif', 'COG', 'CONGO', 178, 'Congo');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('CD', NULL, 'flags/cd.gif', 'COD', 'CONGO, THE DEMOCRATIC REPUBLIC OF THE', 180, 'Congo, the Democratic Republic of the');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('TG', NULL, 'flags/tg.gif', 'TGO', 'TOGO', 768, 'Togo');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('CK', NULL, 'flags/ck.gif', 'COK', 'COOK ISLANDS', 184, 'Cook Islands');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('CR', NULL, 'flags/cr.gif', 'CRI', 'COSTA RICA', 188, 'Costa Rica');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('CI', NULL, 'flags/ci.gif', 'CIV', 'COTE D''IVOIRE', 384, 'Cote D''Ivoire');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('HR', NULL, 'flags/hr.gif', 'HRV', 'CROATIA', 191, 'Croatia');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('CU', NULL, 'flags/cu.gif', 'CUB', 'CUBA', 192, 'Cuba');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('DJ', NULL, 'flags/dj.gif', 'DJI', 'DJIBOUTI', 262, 'Djibouti');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('DM', NULL, 'flags/dm.gif', 'DMA', 'DOMINICA', 212, 'Dominica');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('DO', NULL, 'flags/do.gif', 'DOM', 'DOMINICAN REPUBLIC', 214, 'Dominican Republic');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('EC', NULL, 'flags/ec.gif', 'ECU', 'ECUADOR', 218, 'Ecuador');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('EG', NULL, 'flags/eg.gif', 'EGY', 'EGYPT', 818, 'Egypt');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('SV', NULL, 'flags/sv.gif', 'SLV', 'EL SALVADOR', 222, 'El Salvador');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('GQ', NULL, 'flags/qg.gif', 'GNQ', 'EQUATORIAL GUINEA', 226, 'Equatorial Guinea');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('ER', NULL, 'flags/er.gif', 'ERI', 'ERITREA', 232, 'Eritrea');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('ET', NULL, 'flags/et.gif', 'ETH', 'ETHIOPIA', 231, 'Ethiopia');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('FK', NULL, 'flags/fk.gif', 'FLK', 'FALKLAND ISLANDS (MALVINAS)', 238, 'Falkland Islands (Malvinas)');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('FO', NULL, 'flags/fo.gif', 'FRO', 'FAROE ISLANDS', 234, 'Faroe Islands');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('FJ', NULL, 'flags/fj.gif', 'FJI', 'FIJI', 242, 'Fiji');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('GF', NULL, 'flags/gf.gif', 'GUF', 'FRENCH GUIANA', 254, 'French Guiana');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('PF', NULL, 'flags/pf.gif', 'PYF', 'FRENCH POLYNESIA', 258, 'French Polynesia');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('TF', NULL, 'flags/tf.gif', NULL, 'FRENCH SOUTHERN TERRITORIES', NULL, 'French Southern Territories');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('GA', NULL, 'flags/ga.gif', 'GAB', 'GABON', 266, 'Gabon');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('GM', NULL, 'flags/gm.gif', 'GMB', 'GAMBIA', 270, 'Gambia');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('GE', NULL, 'flags/ge.gif', 'GEO', 'GEORGIA', 268, 'Georgia');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('GH', NULL, 'flags/gh.gif', 'GHA', 'GHANA', 288, 'Ghana');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('GI', NULL, 'flags/gi.gif', 'GIB', 'GIBRALTAR', 292, 'Gibraltar');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('GR', NULL, 'flags/gr.gif', 'GRC', 'GREECE', 300, 'Greece');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('GL', NULL, 'flags/gl.gif', 'GRL', 'GREENLAND', 304, 'Greenland');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('GD', NULL, 'flags/gd.gif', 'GRD', 'GRENADA', 308, 'Grenada');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('GP', NULL, 'flags/gp.gif', 'GLP', 'GUADELOUPE', 312, 'Guadeloupe');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('GU', NULL, 'flags/gu.gif', 'GUM', 'GUAM', 316, 'Guam');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('GT', NULL, 'flags/gt.gif', 'GTM', 'GUATEMALA', 320, 'Guatemala');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('GN', NULL, 'flags/gn.gif', 'GIN', 'GUINEA', 324, 'Guinea');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('GW', NULL, 'flags/gw.gif', 'GNB', 'GUINEA-BISSAU', 624, 'Guinea-Bissau');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('GY', NULL, 'flags/gy.gif', 'GUY', 'GUYANA', 328, 'Guyana');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('HT', NULL, 'flags/ht.gif', 'HTI', 'HAITI', 332, 'Haiti');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('HM', NULL, 'flags/hm.gif', NULL, 'HEARD ISLAND AND MCDONALD ISLANDS', NULL, 'Heard Island and Mcdonald Islands');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('VA', NULL, 'flags/va.gif', 'VAT', 'HOLY SEE (VATICAN CITY STATE)', 336, 'Holy See (Vatican City State)');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('HN', NULL, 'flags/hn.gif', 'HND', 'HONDURAS', 340, 'Honduras');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('HK', NULL, 'flags/hk.gif', 'HKG', 'HONG KONG', 344, 'Hong Kong');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('IS', NULL, 'flags/is.gif', 'ISL', 'ICELAND', 352, 'Iceland');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('IN', NULL, 'flags/in.gif', 'IND', 'INDIA', 356, 'India');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('ID', NULL, 'flags/id.gif', 'IDN', 'INDONESIA', 360, 'Indonesia');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('IR', NULL, 'flags/ir.gif', 'IRN', 'IRAN, ISLAMIC REPUBLIC OF', 364, 'Iran, Islamic Republic of');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('IQ', NULL, 'flags/iq.gif', 'IRQ', 'IRAQ', 368, 'Iraq');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('IL', NULL, 'flags/il.gif', 'ISR', 'ISRAEL', 376, 'Israel');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('JM', NULL, 'flags/jm.gif', 'JAM', 'JAMAICA', 388, 'Jamaica');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('JP', NULL, 'flags/jp.gif', 'JPN', 'JAPAN', 392, 'Japan');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('JO', NULL, 'flags/jo.gif', 'JOR', 'JORDAN', 400, 'Jordan');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('KZ', NULL, 'flags/kz.gif', 'KAZ', 'KAZAKHSTAN', 398, 'Kazakhstan');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('KE', NULL, 'flags/ke.gif', 'KEN', 'KENYA', 404, 'Kenya');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('KI', NULL, 'flags/ki.gif', 'KIR', 'KIRIBATI', 296, 'Kiribati');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('KP', NULL, 'flags/kp.gif', 'PRK', 'KOREA, DEMOCRATIC PEOPLE''S REPUBLIC OF', 408, 'Korea, Democratic People''s Republic of');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('KR', NULL, 'flags/kr.gif', 'KOR', 'KOREA, REPUBLIC OF', 410, 'Korea, Republic of');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('KW', NULL, 'flags/kw.gif', 'KWT', 'KUWAIT', 414, 'Kuwait');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('KG', NULL, 'flags/kg.gif', 'KGZ', 'KYRGYZSTAN', 417, 'Kyrgyzstan');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('LA', NULL, 'flags/la.gif', 'LAO', 'LAO PEOPLE''S DEMOCRATIC REPUBLIC', 418, 'Lao People''s Democratic Republic');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('LB', NULL, 'flags/lb.gif', 'LBN', 'LEBANON', 422, 'Lebanon');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('LS', NULL, 'flags/ls.gif', 'LSO', 'LESOTHO', 426, 'Lesotho');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('LR', NULL, 'flags/lr.gif', 'LBR', 'LIBERIA', 430, 'Liberia');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('LY', NULL, 'flags/ly.gif', 'LBY', 'LIBYAN ARAB JAMAHIRIYA', 434, 'Libyan Arab Jamahiriya');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('LI', NULL, 'flags/li.gif', 'LIE', 'LIECHTENSTEIN', 438, 'Liechtenstein');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('MO', NULL, 'flags/mo.gif', 'MAC', 'MACAO', 446, 'Macao');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('MK', NULL, 'flags/mk.gif', 'MKD', 'MACEDONIA, THE FORMER YUGOSLAV REPUBLIC OF', 807, 'Macedonia, the Former Yugoslav Republic of');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('MG', NULL, 'flags/mg.gif', 'MDG', 'MADAGASCAR', 450, 'Madagascar');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('MW', NULL, 'flags/mw.gif', 'MWI', 'MALAWI', 454, 'Malawi');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('MY', NULL, 'flags/my.gif', 'MYS', 'MALAYSIA', 458, 'Malaysia');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('MV', NULL, 'flags/mv.gif', 'MDV', 'MALDIVES', 462, 'Maldives');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('ML', NULL, 'flags/ml.gif', 'MLI', 'MALI', 466, 'Mali');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('MH', NULL, 'flags/mh.gif', 'MHL', 'MARSHALL ISLANDS', 584, 'Marshall Islands');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('MQ', NULL, 'flags/mq.gif', 'MTQ', 'MARTINIQUE', 474, 'Martinique');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('MR', NULL, 'flags/mr.gif', 'MRT', 'MAURITANIA', 478, 'Mauritania');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('MU', NULL, 'flags/mu.gif', 'MUS', 'MAURITIUS', 480, 'Mauritius');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('MX', NULL, 'flags/mx.gif', 'MEX', 'MEXICO', 484, 'Mexico');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('FM', NULL, 'flags/fm.gif', 'FSM', 'MICRONESIA, FEDERATED STATES OF', 583, 'Micronesia, Federated States of');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('MD', NULL, 'flags/md.gif', 'MDA', 'MOLDOVA, REPUBLIC OF', 498, 'Moldova, Republic of');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('MC', NULL, 'flags/mc.gif', 'MCO', 'MONACO', 492, 'Monaco');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('MN', NULL, 'flags/mn.gif', 'MNG', 'MONGOLIA', 496, 'Mongolia');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('MS', NULL, 'flags/ms.gif', 'MSR', 'MONTSERRAT', 500, 'Montserrat');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('MA', NULL, 'flags/ma.gif', 'MAR', 'MOROCCO', 504, 'Morocco');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('MZ', NULL, 'flags/mz.gif', 'MOZ', 'MOZAMBIQUE', 508, 'Mozambique');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('MM', NULL, 'flags/mm.gif', 'MMR', 'MYANMAR', 104, 'Myanmar');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('NA', NULL, 'flags/na.gif', 'NAM', 'NAMIBIA', 516, 'Namibia');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('NR', NULL, 'flags/nr.gif', 'NRU', 'NAURU', 520, 'Nauru');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('NP', NULL, 'flags/np.gif', 'NPL', 'NEPAL', 524, 'Nepal');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('AN', NULL, 'flags/an.gif', 'ANT', 'NETHERLANDS ANTILLES', 530, 'Netherlands Antilles');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('NC', NULL, 'flags/nc.gif', 'NCL', 'NEW CALEDONIA', 540, 'New Caledonia');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('NZ', NULL, 'flags/nz.gif', 'NZL', 'NEW ZEALAND', 554, 'New Zealand');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('NI', NULL, 'flags/ni.gif', 'NIC', 'NICARAGUA', 558, 'Nicaragua');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('NE', NULL, 'flags/ne.gif', 'NER', 'NIGER', 562, 'Niger');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('NG', NULL, 'flags/ng.gif', 'NGA', 'NIGERIA', 566, 'Nigeria');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('NU', NULL, 'flags/nu.gif', 'NIU', 'NIUE', 570, 'Niue');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('NF', NULL, 'flags/nf.gif', 'NFK', 'NORFOLK ISLAND', 574, 'Norfolk Island');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('MP', NULL, 'flags/mp.gif', 'MNP', 'NORTHERN MARIANA ISLANDS', 580, 'Northern Mariana Islands');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('NO', NULL, 'flags/no.gif', 'NOR', 'NORWAY', 578, 'Norway');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('OM', NULL, 'flags/om.gif', 'OMN', 'OMAN', 512, 'Oman');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('PK', NULL, 'flags/pk.gif', 'PAK', 'PAKISTAN', 586, 'Pakistan');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('PW', NULL, 'flags/pw.gif', 'PLW', 'PALAU', 585, 'Palau');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('PS', NULL, 'flags/ps.gif', NULL, 'PALESTINIAN TERRITORY, OCCUPIED', NULL, 'Palestinian Territory, Occupied');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('PA', NULL, 'flags/pa.gif', 'PAN', 'PANAMA', 591, 'Panama');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('PG', NULL, 'flags/pg.gif', 'PNG', 'PAPUA NEW GUINEA', 598, 'Papua New Guinea');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('PY', NULL, 'flags/py.gif', 'PRY', 'PARAGUAY', 600, 'Paraguay');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('PE', NULL, 'flags/pe.gif', 'PER', 'PERU', 604, 'Peru');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('PH', NULL, 'flags/ph.gif', 'PHL', 'PHILIPPINES', 608, 'Philippines');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('PN', NULL, 'flags/pn.gif', 'PCN', 'PITCAIRN', 612, 'Pitcairn');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('PR', NULL, 'flags/pr.gif', 'PRI', 'PUERTO RICO', 630, 'Puerto Rico');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('QA', NULL, 'flags/qa.gif', 'QAT', 'QATAR', 634, 'Qatar');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('RE', NULL, 'flags/re.gif', 'REU', 'REUNION', 638, 'Reunion');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('RU', NULL, 'flags/ru.gif', 'RUS', 'RUSSIAN FEDERATION', 643, 'Russian Federation');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('RW', NULL, 'flags/rw.gif', 'RWA', 'RWANDA', 646, 'Rwanda');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('SH', NULL, 'flags/sh.gif', 'SHN', 'SAINT HELENA', 654, 'Saint Helena');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('KN', NULL, 'flags/kn.gif', 'KNA', 'SAINT KITTS AND NEVIS', 659, 'Saint Kitts and Nevis');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('LC', NULL, 'flags/lc.gif', 'LCA', 'SAINT LUCIA', 662, 'Saint Lucia');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('PM', NULL, 'flags/pm.gif', 'SPM', 'SAINT PIERRE AND MIQUELON', 666, 'Saint Pierre and Miquelon');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('VC', NULL, 'flags/vc.gif', 'VCT', 'SAINT VINCENT AND THE GRENADINES', 670, 'Saint Vincent and the Grenadines');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('WS', NULL, 'flags/ws.gif', 'WSM', 'SAMOA', 882, 'Samoa');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('SM', NULL, 'flags/sm.gif', 'SMR', 'SAN MARINO', 674, 'San Marino');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('ST', NULL, 'flags/st.gif', 'STP', 'SAO TOME AND PRINCIPE', 678, 'Sao Tome and Principe');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('SA', NULL, 'flags/sa.gif', 'SAU', 'SAUDI ARABIA', 682, 'Saudi Arabia');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('SN', NULL, 'flags/sn.gif', 'SEN', 'SENEGAL', 686, 'Senegal');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('CS', NULL, 'flags/cs.gif', NULL, 'SERBIA AND MONTENEGRO', NULL, 'Serbia and Montenegro');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('SC', NULL, 'flags/sc.gif', 'SYC', 'SEYCHELLES', 690, 'Seychelles');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('SL', NULL, 'flags/sl.gif', 'SLE', 'SIERRA LEONE', 694, 'Sierra Leone');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('SG', NULL, 'flags/sg.gif', 'SGP', 'SINGAPORE', 702, 'Singapore');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('SB', NULL, 'flags/sb.gif', 'SLB', 'SOLOMON ISLANDS', 90, 'Solomon Islands');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('SO', NULL, 'flags/so.gif', 'SOM', 'SOMALIA', 706, 'Somalia');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('ZA', NULL, 'flags/za.gif', 'ZAF', 'SOUTH AFRICA', 710, 'South Africa');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('GS', NULL, 'flags/gs.gif', NULL, 'SOUTH GEORGIA AND THE SOUTH SANDWICH ISLANDS', NULL, 'South Georgia and the South Sandwich Islands');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('LK', NULL, 'flags/lk.gif', 'LKA', 'SRI LANKA', 144, 'Sri Lanka');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('SD', NULL, 'flags/sd.gif', 'SDN', 'SUDAN', 736, 'Sudan');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('SR', NULL, 'flags/sr.gif', 'SUR', 'SURINAME', 740, 'Suriname');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('SJ', NULL, 'flags/sj.gif', 'SJM', 'SVALBARD AND JAN MAYEN', 744, 'Svalbard and Jan Mayen');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('SZ', NULL, 'flags/sz.gif', 'SWZ', 'SWAZILAND', 748, 'Swaziland');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('CH', NULL, 'flags/ch.gif', 'CHE', 'SWITZERLAND', 756, 'Switzerland');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('SY', NULL, 'flags/sy.gif', 'SYR', 'SYRIAN ARAB REPUBLIC', 760, 'Syrian Arab Republic');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('TJ', NULL, 'flags/tj.gif', 'TJK', 'TAJIKISTAN', 762, 'Tajikistan');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('TZ', NULL, 'flags/tz.gif', 'TZA', 'TANZANIA, UNITED REPUBLIC OF', 834, 'Tanzania, United Republic of');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('TH', NULL, 'flags/th.gif', 'THA', 'THAILAND', 764, 'Thailand');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('TL', NULL, 'flags/tl.gif', NULL, 'TIMOR-LESTE', NULL, 'Timor-Leste');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('TK', NULL, 'flags/tk.gif', 'TKL', 'TOKELAU', 772, 'Tokelau');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('TO', NULL, 'flags/to.gif', 'TON', 'TONGA', 776, 'Tonga');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('TT', NULL, 'flags/tt.gif', 'TTO', 'TRINIDAD AND TOBAGO', 780, 'Trinidad and Tobago');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('TN', NULL, 'flags/tn.gif', 'TUN', 'TUNISIA', 788, 'Tunisia');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('TR', NULL, 'flags/tr.gif', 'TUR', 'TURKEY', 792, 'Turkey');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('TM', NULL, 'flags/tm.gif', 'TKM', 'TURKMENISTAN', 795, 'Turkmenistan');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('TC', NULL, 'flags/tc.gif', 'TCA', 'TURKS AND CAICOS ISLANDS', 796, 'Turks and Caicos Islands');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('TV', NULL, 'flags/tv.gif', 'TUV', 'TUVALU', 798, 'Tuvalu');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('UG', NULL, 'flags/ug.gif', 'UGA', 'UGANDA', 800, 'Uganda');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('UA', NULL, 'flags/ua.gif', 'UKR', 'UKRAINE', 804, 'Ukraine');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('AE', NULL, 'flags/ae.gif', 'ARE', 'UNITED ARAB EMIRATES', 784, 'United Arab Emirates');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('US', NULL, 'flags/us.gif', 'USA', 'UNITED STATES', 840, 'United States');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('UM', NULL, 'flags/um.gif', NULL, 'UNITED STATES MINOR OUTLYING ISLANDS', NULL, 'United States Minor Outlying Islands');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('UY', NULL, 'flags/uy.gif', 'URY', 'URUGUAY', 858, 'Uruguay');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('UZ', NULL, 'flags/uz.gif', 'UZB', 'UZBEKISTAN', 860, 'Uzbekistan');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('VU', NULL, 'flags/vu.gif', 'VUT', 'VANUATU', 548, 'Vanuatu');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('VE', NULL, 'flags/ve.gif', 'VEN', 'VENEZUELA', 862, 'Venezuela');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('VN', NULL, 'flags/vn.gif', 'VNM', 'VIET NAM', 704, 'Viet Nam');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('VG', NULL, 'flags/vg.gif', 'VGB', 'VIRGIN ISLANDS, BRITISH', 92, 'Virgin Islands, British');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('VI', NULL, 'flags/vi.gif', 'VIR', 'VIRGIN ISLANDS, U.S.', 850, 'Virgin Islands, U.s.');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('WF', NULL, 'flags/wf.gif', 'WLF', 'WALLIS AND FUTUNA', 876, 'Wallis and Futuna');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('EH', NULL, 'flags/eh.gif', 'ESH', 'WESTERN SAHARA', 732, 'Western Sahara');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('YE', NULL, 'flags/ye.gif', 'YEM', 'YEMEN', 887, 'Yemen');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('ZM', NULL, 'flags/zm.gif', 'ZMB', 'ZAMBIA', 894, 'Zambia');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('ZW', NULL, 'flags/zw.gif', 'ZWE', 'ZIMBABWE', 716, 'Zimbabwe');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('AT', true, 'flags/at.gif', 'AUT', 'AUSTRIA', 40, 'Austria');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('BE', true, 'flags/be.gif', 'BEL', 'BELGIUM', 56, 'Belgium');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('CY', true, 'flags/cy.gif', 'CYP', 'CYPRUS', 196, 'Cyprus');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('CZ', true, 'flags/cz.gif', 'CZE', 'CZECH REPUBLIC', 203, 'Czech Republic');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('DK', true, 'flags/dk.gif', 'DNK', 'DENMARK', 208, 'Denmark');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('EE', true, 'flags/ee.gif', 'EST', 'ESTONIA', 233, 'Estonia');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('FI', true, 'flags/fi.gif', 'FIN', 'FINLAND', 246, 'Finland');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('FR', true, 'flags/fr.gif', 'FRA', 'FRANCE', 250, 'France');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('DE', true, 'flags/de.gif', 'DEU', 'GERMANY', 276, 'Germany');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('HU', true, 'flags/hu.gif', 'HUN', 'HUNGARY', 348, 'Hungary');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('IE', true, 'flags/ie.gif', 'IRL', 'IRELAND', 372, 'Ireland');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('IT', true, 'flags/it.gif', 'ITA', 'ITALY', 380, 'Italy');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('LV', true, 'flags/lv.gif', 'LVA', 'LATVIA', 428, 'Latvia');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('LT', true, 'flags/lt.gif', 'LTU', 'LITHUANIA', 440, 'Lithuania');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('LU', true, 'flags/lu.gif', 'LUX', 'LUXEMBOURG', 442, 'Luxembourg');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('MT', true, 'flags/mt.gif', 'MLT', 'MALTA', 470, 'Malta');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('NL', true, 'flags/nl.gif', 'NLD', 'NETHERLANDS', 528, 'Netherlands');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('PL', true, 'flags/pl.gif', 'POL', 'POLAND', 616, 'Poland');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('PT', true, 'flags/pt.gif', 'PRT', 'PORTUGAL', 620, 'Portugal');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('SK', true, 'flags/sk.gif', 'SVK', 'SLOVAKIA', 703, 'Slovakia');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('SI', true, 'flags/si.gif', 'SVN', 'SLOVENIA', 705, 'Slovenia');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('ES', true, 'flags/es.gif', 'ESP', 'SPAIN', 724, 'Spain');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('SE', true, 'flags/se.gif', 'SWE', 'SWEDEN', 752, 'Sweden');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('GB', true, 'flags/gb.gif', 'GBR', 'UNITED KINGDOM', 826, 'United Kingdom');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('RO', true, 'flags/ro.gif', 'ROM', 'ROMANIA', 642, 'Romania');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('BG', true, 'flags/bg.gif', 'BGR', 'BULGARIA', 100, 'Bulgaria');
INSERT INTO usr_iso_3166_country_code (iso, ec, flag_url, iso3, name, numcode, printable_name) VALUES ('TW', NULL, 'flags/tw.gif', 'TWN', 'TAIWAN', 158, 'Taiwan');


--
-- Data for Name: usr_address; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: usr_institution_type; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO usr_institution_type (id, last_changed, last_modifier_id, description, type) VALUES (1, NULL, NULL, 'This institution is public (administration, ...)', 'Public');
INSERT INTO usr_institution_type (id, last_changed, last_modifier_id, description, type) VALUES (2, NULL, NULL, 'A company.', 'Company');
INSERT INTO usr_institution_type (id, last_changed, last_modifier_id, description, type) VALUES (3, NULL, NULL, 'This institution is a sponsor.', 'Sponsor');
INSERT INTO usr_institution_type (id, last_changed, last_modifier_id, description, type) VALUES (4, NULL, NULL, 'Any kind of Healthcare entreprise', 'Healthcare Enterprise');


--
-- Data for Name: usr_institution; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: usr_users; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_system; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: pr_crawler_changed_integration_statements; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: pr_crawler_reporting_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('pr_crawler_reporting_id_seq', 1, false);


--
-- Data for Name: pr_crawler_unmatching_hascode_for_integration_statements; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: pr_crawler_unreachable_integration_statements; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: pr_search_log_report; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: pr_integration_statement_download; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: pr_integration_statement_download_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('pr_integration_statement_download_id_seq', 1, false);


--
-- Data for Name: pr_mail; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: pr_mail_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('pr_mail_id_seq', 1, false);


--
-- Data for Name: pr_search_log_criterion; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: pr_search_criteria_per_report; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: pr_search_log_criterion_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('pr_search_log_criterion_id_seq', 1, false);


--
-- Name: pr_search_log_report_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('pr_search_log_report_id_seq', 1, false);


--
-- Data for Name: pr_search_log_systems_found; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: pr_subtypes_per_system_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('pr_subtypes_per_system_id_seq', 1, false);


--
-- Data for Name: proxy_type; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: proxy_type_sequence; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('proxy_type_sequence', 2, true);


--
-- Data for Name: revinfo; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: sys_dicom_documents_for_systems; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: sys_hl7_documents_for_systems; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: sys_system_subtype; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: sys_system_subtype_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('sys_system_subtype_id_seq', 1, false);


--
-- Data for Name: sys_system_subtypes_per_system_type; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: sys_system_subtypes_per_system_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('sys_system_subtypes_per_system_type_id_seq', 1, false);


--
-- Name: sys_system_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('sys_system_type_id_seq', 41, true);


--
-- Data for Name: sys_table_session; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: sys_table_session_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('sys_table_session_id_seq', 215, true);


--
-- Data for Name: tf_actor_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_actor_default_ports; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tf_actor_default_ports_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_actor_default_ports_id_seq', 239, true);


--
-- Name: tf_actor_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_actor_id_seq', 1190, true);


--
-- Data for Name: tf_integration_profile_status_type; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_integration_profile; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_actor_integration_profile; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_actor_integration_profile_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tf_actor_integration_profile_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_actor_integration_profile_id_seq', 728, true);


--
-- Data for Name: tf_integration_profile_option; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_actor_integration_profile_option; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_actor_integration_profile_option_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tf_actor_integration_profile_option_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_actor_integration_profile_option_id_seq', 1613, true);


--
-- Data for Name: tf_affinity_domain; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_affinity_domain_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tf_affinity_domain_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_affinity_domain_id_seq', 1, false);


--
-- Data for Name: tf_audit_message; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tf_audit_message_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_audit_message_id_seq', 1, false);


--
-- Data for Name: tf_document_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tf_document_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_document_id_seq', 1, false);


--
-- Data for Name: tf_document_sections_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tf_document_sections_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_document_sections_id_seq', 1, false);


--
-- Data for Name: tf_domain_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tf_domain_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_domain_id_seq', 14, true);


--
-- Data for Name: tf_domain_profile; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_domain_profile_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tf_gmm_server_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_gmm_server_id_seq', 1, false);


--
-- Data for Name: tf_hl7_message_profile_affinity_domain; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_hl7_message_profile_affinity_domain_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_hl7_message_profile_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tf_hl7_message_profile_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_hl7_message_profile_id_seq', 175, true);


--
-- Data for Name: tf_hl7_message_profile_table; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_hl7_message_profile_table_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tf_hl7_message_profile_table_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_hl7_message_profile_table_id_seq', 1, true);


--
-- Data for Name: tf_integration_profile_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tf_integration_profile_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_integration_profile_id_seq', 248, true);


--
-- Data for Name: tf_integration_profile_option_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tf_integration_profile_option_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_integration_profile_option_id_seq', 259, true);


--
-- Data for Name: tf_integration_profile_status_type_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tf_integration_profile_status_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_integration_profile_status_type_id_seq', 7, true);


--
-- Data for Name: tf_integration_profile_type; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_integration_profile_type_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tf_integration_profile_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_integration_profile_type_id_seq', 5, true);


--
-- Data for Name: tf_integration_profile_type_link; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_integration_profile_type_link_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_profile_inria_hl7_validation_files; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_profile_inria_hl7_validation_files_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tf_profile_inria_hl7_validation_files_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_profile_inria_hl7_validation_files_id_seq', 1, true);


--
-- Data for Name: tf_transaction_option_type; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO tf_transaction_option_type (id, last_changed, last_modifier_id, description, keyword, name) VALUES (1, '2007-08-19 22:00:00+00', '0', '', 'R', 'Required');
INSERT INTO tf_transaction_option_type (id, last_changed, last_modifier_id, description, keyword, name) VALUES (2, '2007-08-19 22:00:00+00', '0', '', 'O', 'Optional');
INSERT INTO tf_transaction_option_type (id, last_changed, last_modifier_id, description, keyword, name) VALUES (3, '2009-07-13 19:29:38.974411+00', '0', '', 'C', 'Conditional');


--
-- Data for Name: tf_profile_link; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_profile_link_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tf_profile_link_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_profile_link_id_seq', 1730, true);


--
-- Name: tf_revision_tagged_for_sync_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_revision_tagged_for_sync_id_seq', 1, false);


--
-- Data for Name: tf_rule; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_rule_criterion; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tf_rule_criterion_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_rule_criterion_id_seq', 1, false);


--
-- Name: tf_rule_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_rule_id_seq', 1, false);


--
-- Data for Name: tf_rule_list_criterion; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_standard; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_standard_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tf_standard_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_standard_id_seq', 1, false);


--
-- Data for Name: tf_transaction_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tf_transaction_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_transaction_id_seq', 308, true);


--
-- Data for Name: tf_transaction_link; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_transaction_link_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tf_transaction_link_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_transaction_link_id_seq', 510, true);


--
-- Data for Name: tf_transaction_option_type_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tf_transaction_option_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_transaction_option_type_id_seq', 3, true);


--
-- Data for Name: tf_transaction_standard; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_transaction_standard_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tf_transaction_status_type_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tf_transaction_status_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_transaction_status_type_id_seq', 6, true);


--
-- Name: tf_updates_installed_by_sync_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_updates_installed_by_sync_id_seq', 1, false);


--
-- Data for Name: tf_ws_transaction_usage_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tf_ws_transaction_usage_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tf_ws_transaction_usage_id_seq', 1, false);


--
-- Data for Name: tm_configuration_type; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO tm_configuration_type (id, last_changed, last_modifier_id, category, class_name, type_name, used_for_proxy) VALUES (1, NULL, NULL, 'HL7', 'net.ihe.gazelle.tm.configurations.model.HL7.HL7V2InitiatorConfiguration', 'HL7 V2 initiator', false);
INSERT INTO tm_configuration_type (id, last_changed, last_modifier_id, category, class_name, type_name, used_for_proxy) VALUES (2, NULL, NULL, 'HL7', 'net.ihe.gazelle.tm.configurations.model.HL7.HL7V2ResponderConfiguration', 'HL7 V2 responder', true);
INSERT INTO tm_configuration_type (id, last_changed, last_modifier_id, category, class_name, type_name, used_for_proxy) VALUES (3, NULL, NULL, 'HL7', 'net.ihe.gazelle.tm.configurations.model.HL7.HL7V3InitiatorConfiguration', 'HL7 V3 initiator', false);
INSERT INTO tm_configuration_type (id, last_changed, last_modifier_id, category, class_name, type_name, used_for_proxy) VALUES (4, NULL, NULL, 'HL7', 'net.ihe.gazelle.tm.configurations.model.HL7.HL7V3ResponderConfiguration', 'HL7 V3 responder', true);
INSERT INTO tm_configuration_type (id, last_changed, last_modifier_id, category, class_name, type_name, used_for_proxy) VALUES (5, NULL, NULL, 'DICOM', 'net.ihe.gazelle.tm.configurations.model.DICOM.DicomSCUConfiguration', 'DICOM SCU', true);
INSERT INTO tm_configuration_type (id, last_changed, last_modifier_id, category, class_name, type_name, used_for_proxy) VALUES (6, NULL, NULL, 'DICOM', 'net.ihe.gazelle.tm.configurations.model.DICOM.DicomSCPConfiguration', 'DICOM SCP', true);
INSERT INTO tm_configuration_type (id, last_changed, last_modifier_id, category, class_name, type_name, used_for_proxy) VALUES (7, NULL, NULL, 'WS', 'net.ihe.gazelle.tm.configurations.model.WebServiceConfiguration', 'Webservice', true);
INSERT INTO tm_configuration_type (id, last_changed, last_modifier_id, category, class_name, type_name, used_for_proxy) VALUES (8, NULL, NULL, 'SYSLOG', 'net.ihe.gazelle.tm.configurations.model.SyslogConfiguration', 'Syslog', true);


--
-- Data for Name: tm_network_config_for_session; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_system_in_session_status; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO tm_system_in_session_status (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (0, NULL, NULL, 'ready', 'ready', 'ready');
INSERT INTO tm_system_in_session_status (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (1, NULL, NULL, 'busy testing', 'busy testing', 'busy testing');
INSERT INTO tm_system_in_session_status (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (2, NULL, NULL, 'busy configuring', 'busy configuring', 'busy configuring');
INSERT INTO tm_system_in_session_status (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (3, NULL, NULL, 'finished for today', 'finished for today', 'finished for today');
INSERT INTO tm_system_in_session_status (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (4, NULL, NULL, 'installing', 'installing', 'installing');
INSERT INTO tm_system_in_session_status (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (5, NULL, NULL, 'not here yet', 'not here yet', 'not here yet');


--
-- Data for Name: usr_currency; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO usr_currency (keyword, last_changed, last_modifier_id, comment, name) VALUES ('USD', NULL, NULL, 'Currency used for NA registration', 'United States Dollar');
INSERT INTO usr_currency (keyword, last_changed, last_modifier_id, comment, name) VALUES ('EUR', NULL, NULL, 'Currency used for European registration', 'Euro');


--
-- Data for Name: tm_testing_session; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: usr_person; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_system_in_session; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_host; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_configuration; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_abstract_configuration; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_annotation; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_annotation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_annotation_id_seq', 83, true);


--
-- Data for Name: tm_assertion; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_assertion_aipo; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_assertion_aipo_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_assertion_aipo_id_seq', 1, false);


--
-- Name: tm_assertion_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_assertion_id_seq', 1, false);


--
-- Data for Name: tm_standard; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_assertion_standard; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_assertion_standard_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_assertion_standard_id_seq', 1, false);


--
-- Data for Name: tm_assertion_transaction; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_assertion_transaction_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_assertion_transaction_id_seq', 1, false);


--
-- Data for Name: tm_certificate; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_certificate_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_certificate_id_seq', 55, true);


--
-- Name: tm_con_system_status_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_con_system_status_id_seq', 1, false);


--
-- Data for Name: tm_configuration_mapped_with_aipo; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_sop_class; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO tm_sop_class (id, last_changed, last_modifier_id, keyword, name) VALUES (1, NULL, NULL, 'MPPS', '');
INSERT INTO tm_sop_class (id, last_changed, last_modifier_id, keyword, name) VALUES (2, NULL, NULL, 'MWL', '');
INSERT INTO tm_sop_class (id, last_changed, last_modifier_id, keyword, name) VALUES (3, NULL, NULL, 'PRINT', '');
INSERT INTO tm_sop_class (id, last_changed, last_modifier_id, keyword, name) VALUES (4, NULL, NULL, 'QR', '');
INSERT INTO tm_sop_class (id, last_changed, last_modifier_id, keyword, name) VALUES (5, NULL, NULL, 'STORAGE', '');
INSERT INTO tm_sop_class (id, last_changed, last_modifier_id, keyword, name) VALUES (6, NULL, NULL, 'STORAGE COMMITMENT', '');
INSERT INTO tm_sop_class (id, last_changed, last_modifier_id, keyword, name) VALUES (7, NULL, NULL, 'GP-WL', '');
INSERT INTO tm_sop_class (id, last_changed, last_modifier_id, keyword, name) VALUES (8, NULL, NULL, 'GP-PPS', 'General Purpose Performed Procedure Step');
INSERT INTO tm_sop_class (id, last_changed, last_modifier_id, keyword, name) VALUES (9, NULL, NULL, 'IAN', 'Instance Availability Notification');
INSERT INTO tm_sop_class (id, last_changed, last_modifier_id, keyword, name) VALUES (10, NULL, NULL, 'GPSPS', '');
INSERT INTO tm_sop_class (id, last_changed, last_modifier_id, keyword, name) VALUES (11, NULL, NULL, 'WADO', '');


--
-- Data for Name: tm_transport_layer_for_config; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO tm_transport_layer_for_config (id, last_changed, last_modifier_id, keyword, name) VALUES (2, NULL, NULL, 'TCP', 'TCP');
INSERT INTO tm_transport_layer_for_config (id, last_changed, last_modifier_id, keyword, name) VALUES (1, NULL, NULL, 'UDP', 'UDP');


--
-- Data for Name: tm_web_service_type; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_conftype_w_ports_wstype_and_sop_class; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_conf_mapping_w_aipo_w_conftypes; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_conf_mapping_w_aipo_w_conftypes_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_configuration_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_configuration_id_seq', 4724, true);


--
-- Data for Name: tm_configuration_mapped_with_aipo_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_configuration_mapped_with_aipo_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_configuration_mapped_with_aipo_id_seq', 1324, true);


--
-- Data for Name: tm_configuration_type_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_configuration_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_configuration_type_id_seq', 8, true);


--
-- Data for Name: tm_conftype_w_ports_wstype_and_sop_class_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_conftype_w_ports_wstype_and_sop_class_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_conftype_w_ports_wstype_and_sop_class_id_seq', 385, true);


--
-- Data for Name: tm_connectathon_participant_status; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO tm_connectathon_participant_status (id, description, name) VALUES (1, 'gazelle.tm.users.connectathon.participant.description.vendor', 'Vendor');
INSERT INTO tm_connectathon_participant_status (id, description, name) VALUES (2, 'gazelle.tm.users.connectathon.participant.description.monitor', 'Monitor');
INSERT INTO tm_connectathon_participant_status (id, description, name) VALUES (3, 'gazelle.tm.users.connectathon.participant.description.committee', 'Committee');
INSERT INTO tm_connectathon_participant_status (id, description, name) VALUES (4, 'gazelle.tm.users.connectathon.participant.description.visitor', 'Visitor');


--
-- Data for Name: tm_connectathon_participant; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_connectathon_participant_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_connectathon_participant_id_seq', 742, true);


--
-- Name: tm_connectathon_participant_status_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_connectathon_participant_status_id_seq', 4, true);


--
-- Data for Name: tm_path; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_contextual_information; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_contextual_information_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_contextual_information_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_contextual_information_id_seq', 3, true);


--
-- Data for Name: tm_contextual_information_instance; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_contextual_information_instance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_contextual_information_instance_id_seq', 24, true);


--
-- Data for Name: tm_data_type; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO tm_data_type (id, last_changed, last_modifier_id, description, keyword) VALUES (1, NULL, NULL, 'link to sample', 'sample');
INSERT INTO tm_data_type (id, last_changed, last_modifier_id, description, keyword) VALUES (2, NULL, NULL, 'link to the proxy', 'proxy');
INSERT INTO tm_data_type (id, last_changed, last_modifier_id, description, keyword) VALUES (4, NULL, NULL, 'file uploaded by user', 'file');
INSERT INTO tm_data_type (id, last_changed, last_modifier_id, description, keyword) VALUES (3, NULL, NULL, 'link to xds file', 'xds');
INSERT INTO tm_data_type (id, last_changed, last_modifier_id, description, keyword) VALUES (5, NULL, NULL, 'link to entire url', 'url');
INSERT INTO tm_data_type (id, last_changed, last_modifier_id, description, keyword) VALUES (6, NULL, NULL, 'comment', 'comment');
INSERT INTO tm_data_type (id, last_changed, last_modifier_id, description, keyword) VALUES (7, NULL, NULL, 'link to entire EVSClient', 'EVSClient');
INSERT INTO tm_data_type (id, last_changed, last_modifier_id, description, keyword) VALUES (8, NULL, NULL, 'link to entire TLS', 'tls');


--
-- Name: tm_data_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_data_type_id_seq', 8, true);


--
-- Data for Name: tm_demonstration; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_demonstration_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_demonstration_id_seq', 5, true);


--
-- Data for Name: tm_demonstration_system_in_session; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_demonstrations_in_testing_sessions; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_dicom_scp_configuration; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_dicom_scu_configuration; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_domains_in_testing_sessions; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_proxy_configuration_for_session; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_free_ip_ports_for_proxy; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_free_ip_ports_for_proxy_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_free_ip_ports_for_proxy_id_seq', 45005, true);


--
-- Data for Name: tm_free_ips_for_session; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_free_ips_for_session_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_free_ips_for_session_id_seq', 25556, true);


--
-- Data for Name: tm_generic_oids_institution_for_session; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_generic_oids_institution_for_session_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_generic_oids_institution_for_session_id_seq', 4, true);


--
-- Data for Name: tm_generic_oids_ip_config_param_for_session; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_generic_oids_ip_config_param_for_session_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_generic_oids_ip_config_param_for_session_id_seq', 71, true);


--
-- Data for Name: tm_hl7_initiator_configuration; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_hl7_responder_configuration; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_hl7_v3_initiator_configuration; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_hl7_v3_responder_configuration; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_home; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_home_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_home_id_seq', 1, false);


--
-- Name: tm_host_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_host_id_seq', 669, true);


--
-- Data for Name: tm_institution_system; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_institution_system_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_institution_system_id_seq', 547, true);


--
-- Data for Name: tm_invoice; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_invoice_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_invoice_id_seq', 248, true);


--
-- Data for Name: tm_jira_issues_to_test; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_jira_issues_to_test_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_jira_issues_to_test_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_jira_issues_to_test_id_seq', 1, false);


--
-- Data for Name: tm_meta_test; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_meta_test_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_meta_test_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_meta_test_id_seq', 15, true);


--
-- Data for Name: tm_meta_test_test_roles; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_meta_test_test_roles_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_monitor_in_session; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_monitor_in_session_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_monitor_in_session_id_seq', 124, true);


--
-- Data for Name: tm_monitor_test; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_network_config_for_session_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_network_config_for_session_id_seq', 6, true);


--
-- Name: tm_network_config_for_session_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_network_config_for_session_seq', 1, false);


--
-- Data for Name: tm_object_type_status; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO tm_object_type_status (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (1, NULL, NULL, 'The object type can be used', 'ready', 'ready');
INSERT INTO tm_object_type_status (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (2, NULL, NULL, 'The object type is deprecated', 'deprecated', 'deprecated');
INSERT INTO tm_object_type_status (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (3, NULL, NULL, 'the object type in not compelete', 'to be completed', 'to be completed');
INSERT INTO tm_object_type_status (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (4, NULL, NULL, '...', 'storage/substitute', 'storage/substitute');
INSERT INTO tm_object_type_status (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (5, NULL, NULL, 'the object type is imported from kudu and needs verification', 'imported from Kudu', 'Kudu import');


--
-- Data for Name: tm_object_type; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_object_attribute; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_object_attribute_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_object_attribute_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_object_attribute_id_seq', 121, true);


--
-- Data for Name: tm_object_attribute_option; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_object_attribute_option_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_object_attribute_option_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_object_attribute_option_id_seq', 1, true);


--
-- Name: tm_object_class_validator_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_object_class_validator_id_seq', 6, true);


--
-- Data for Name: tm_object_creator; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_object_creator_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_object_creator_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_object_creator_id_seq', 8141, true);


--
-- Data for Name: tm_object_file_type; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_object_file; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_object_file_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_object_file_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_object_file_id_seq', 437, true);


--
-- Data for Name: tm_object_file_type_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_object_file_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_object_file_type_id_seq', 12, true);


--
-- Data for Name: tm_object_instance_validation; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO tm_object_instance_validation (id, last_changed, last_modifier_id, description, value) VALUES (1, NULL, NULL, 'validated', 'valid instance');
INSERT INTO tm_object_instance_validation (id, last_changed, last_modifier_id, description, value) VALUES (2, NULL, NULL, 'not validated', 'invalid instance');
INSERT INTO tm_object_instance_validation (id, last_changed, last_modifier_id, description, value) VALUES (3, NULL, NULL, 'not examinated', 'not yet examinated');


--
-- Data for Name: tm_object_instance; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_object_instance_annotation; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_object_instance_annotation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_object_instance_annotation_id_seq', 83, true);


--
-- Data for Name: tm_object_instance_attribute; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_object_instance_attribute_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_object_instance_attribute_id_seq', 1196, true);


--
-- Data for Name: tm_object_instance_file; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_object_instance_file_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_object_instance_file_id_seq', 1919, true);


--
-- Name: tm_object_instance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_object_instance_id_seq', 993, true);


--
-- Data for Name: tm_object_instance_validation_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_object_instance_validation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_object_instance_validation_id_seq', 3, true);


--
-- Name: tm_object_method_validator_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_object_method_validator_id_seq', 55, true);


--
-- Name: tm_object_parameter_validator_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_object_parameter_validator_id_seq', 72, true);


--
-- Data for Name: tm_object_reader; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_object_reader_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_object_reader_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_object_reader_id_seq', 3913, true);


--
-- Data for Name: tm_object_type_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_object_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_object_type_id_seq', 210, true);


--
-- Data for Name: tm_object_type_status_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_object_type_status_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_object_type_status_id_seq', 5, true);


--
-- Data for Name: tm_oid_institution; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_oid_institution_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_oid_institution_id_seq', 248, true);


--
-- Data for Name: tm_oid_root_definition; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_oid_requirement; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_oid_requirement_aipo; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_oid_requirement_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_oid_requirement_id_seq', 105, true);


--
-- Name: tm_oid_root_definition_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_oid_root_definition_id_seq', 8, true);


--
-- Data for Name: tm_oid_system_assignment; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_oid_system_assignment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_oid_system_assignment_id_seq', 448, true);


--
-- Data for Name: tm_oids_ip_config_param_for_session_for_hl7; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_oids_ip_config_param_for_session_for_hl7_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_oids_ip_config_param_for_session_for_hl7_id_seq', 6, true);


--
-- Data for Name: tm_path_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_path_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_path_id_seq', 1, false);


--
-- Data for Name: tm_patient; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_patient_assigning_autority; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_patient_assigning_authority_sis; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_patient_assigning_autority_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_patient_assigning_autority_id_seq', 13, true);


--
-- Name: tm_patient_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_patient_id_seq', 980, true);


--
-- Data for Name: tm_patient_identifier; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_patient_identifier_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_patient_identifier_id_seq', 3479, true);


--
-- Data for Name: tm_patient_patient_identifier; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_patient_type_code; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_patient_type_code_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_patient_type_code_id_seq', 1, false);


--
-- Data for Name: tm_port_host_pair; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_port_host_pair_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_port_host_pair_id_seq', 1, false);


--
-- Data for Name: tm_profiles_in_testing_sessions; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_proxy_configuration_for_session_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_proxy_configuration_for_session_id_seq', 3, true);


--
-- Name: tm_proxy_configuration_for_session_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_proxy_configuration_for_session_seq', 1, false);


--
-- Data for Name: tm_role_in_test_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_role_in_test_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_role_in_test_id_seq', 12425, true);


--
-- Data for Name: tm_test_participants; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_role_in_test_test_participants; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_role_in_test_test_participants_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_status_results; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO tm_status_results (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (1, NULL, NULL, 'Pass', 'passed', 'gazelle.tm.testing.results.resultStatusPassed');
INSERT INTO tm_status_results (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (2, NULL, NULL, 'Fail', 'failed', 'gazelle.tm.testing.results.resultStatusFailed');
INSERT INTO tm_status_results (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (3, NULL, NULL, ' We think you are going to fail unless you make a large effort', 'atrisk', 'gazelle.tm.testing.results.resultStatusAtRisk');
INSERT INTO tm_status_results (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (4, NULL, NULL, 'Still registered in Gazelle, but have decided no longer to test. Any individual tests that you have run will be available for review, but you have decided not to test anymore', 'withdrawn', 'gazelle.tm.testing.results.resultStatusWithDrawn');
INSERT INTO tm_status_results (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (5, NULL, NULL, 'No grading', 'nograding', 'gazelle.tm.testing.results.noGrading');
INSERT INTO tm_status_results (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (6, NULL, NULL, 'No peer', 'nopeer', 'gazelle.tm.testing.results.noPeer');


--
-- Data for Name: tm_testing_type; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO tm_testing_type (id, last_changed, last_modifier_id, description, image_url, keyword, name) VALUES (1, NULL, NULL, 'A system implements (Integration Profile/Actor/Option) combination as Thorough', 'icons64/status_green.gif', 'T', 'Thorough');
INSERT INTO tm_testing_type (id, last_changed, last_modifier_id, description, image_url, keyword, name) VALUES (2, NULL, NULL, 'A system implements (Integration Profile/Actor/Option) combination as Supportive', 'icons64/status_blue.gif', 'S', 'Supportive');


--
-- Data for Name: tm_system_actor_profiles; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_system_aipo_result_for_a_testing_session; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_sap_result_tr_comment; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_sap_result_tr_comment_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_sap_result_tr_comment_seq', 1, false);


--
-- Data for Name: tm_share_patient_logger; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_share_patient_logger_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_share_patient_logger_id_seq', 606, true);


--
-- Data for Name: tm_sop_class_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_sop_class_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_sop_class_id_seq', 11, true);


--
-- Name: tm_standard_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_standard_id_seq', 1, false);


--
-- Data for Name: tm_status; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO tm_status (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (1, NULL, NULL, 'When the instance is started and currently running', 'started', 'gazelle.tm.testing.status.running');
INSERT INTO tm_status (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (2, NULL, NULL, 'When the instance is completed', 'completed', 'gazelle.tm.testing.status.completed');
INSERT INTO tm_status (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (3, NULL, NULL, 'Test paused', 'paused', 'gazelle.tm.testing.status.paused');
INSERT INTO tm_status (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (4, NULL, NULL, 'When the test is verified', 'verified', 'gazelle.tm.testing.status.verified');
INSERT INTO tm_status (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (5, NULL, NULL, 'test aborted', 'aborted', 'gazelle.tm.testing.status.aborted');
INSERT INTO tm_status (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (6, NULL, NULL, 'partially verified', 'partially verified', 'gazelle.tm.testing.status.partiallyVerified');
INSERT INTO tm_status (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (7, NULL, NULL, 'Test failed', 'failed', 'gazelle.tm.testing.status.failed');
INSERT INTO tm_status (id, last_changed, last_modifier_id, description, keyword, label_to_display) VALUES (8, NULL, NULL, 'Critical test', 'critical', 'gazelle.tm.testing.status.critical');


--
-- Name: tm_status_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_status_id_seq', 8, true);


--
-- Name: tm_status_results_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_status_results_id_seq', 6, true);


--
-- Data for Name: tm_step_inst_msg_process_status; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO tm_step_inst_msg_process_status (id, description, key, label_key_for_display) VALUES (1, 'Message is being processed.', 'PROCESSING', 'net.ihe.gazelle.tm.message.processing');
INSERT INTO tm_step_inst_msg_process_status (id, description, key, label_key_for_display) VALUES (2, 'Message has been processed.', 'PROCESSED', 'net.ihe.gazelle.tm.message.processed');
INSERT INTO tm_step_inst_msg_process_status (id, description, key, label_key_for_display) VALUES (3, 'Message processing encountered an unexpected error.', 'ERROR', 'net.ihe.gazelle.tm.message.error');


--
-- Name: tm_step_inst_msg_process_status_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_step_inst_msg_process_status_id_seq', 3, true);


--
-- Data for Name: tm_step_instance_exec_status; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO tm_step_instance_exec_status (id, description, key, label_key_for_display) VALUES (1, 'Not active yet.', 'INACTIVE', 'net.ihe.gazelle.tm.Inactive');
INSERT INTO tm_step_instance_exec_status (id, description, key, label_key_for_display) VALUES (2, 'Active but no messages have been intercepted yet from sender and receiver', 'WAITING', 'net.ihe.gazelle.tm.Waiting');
INSERT INTO tm_step_instance_exec_status (id, description, key, label_key_for_display) VALUES (3, 'The test step instance was stopped by the system after it had become active at one point.', 'INTERRUPTED', 'net.ihe.gazelle.tm.Interrupted');
INSERT INTO tm_step_instance_exec_status (id, description, key, label_key_for_display) VALUES (4, 'The test step has been skipped by the participant.', 'SKIPPED', 'net.ihe.gazelle.tm.Skipped');
INSERT INTO tm_step_instance_exec_status (id, description, key, label_key_for_display) VALUES (5, 'The test step has been paused by the participant after it had become active.', 'PAUSED', 'net.ihe.gazelle.tm.Paused');
INSERT INTO tm_step_instance_exec_status (id, description, key, label_key_for_display) VALUES (6, 'The test step has been aborted by the participant after it had become active.', 'ABORTED', 'net.ihe.gazelle.tm.Aborted');
INSERT INTO tm_step_instance_exec_status (id, description, key, label_key_for_display) VALUES (7, 'A request message has been intercepted.', 'INITIATED', 'net.ihe.gazelle.tm.Initiated');
INSERT INTO tm_step_instance_exec_status (id, description, key, label_key_for_display) VALUES (8, 'A response message has been intercepted.', 'RESPONDED', 'net.ihe.gazelle.tm.Responded');
INSERT INTO tm_step_instance_exec_status (id, description, key, label_key_for_display) VALUES (9, 'The test step instance is complete.', 'COMPLETED', 'net.ihe.gazelle.tm.Completed');


--
-- Name: tm_step_instance_exec_status_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_step_instance_exec_status_id_seq', 9, true);


--
-- Data for Name: tm_test_steps_instance_status; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO tm_test_steps_instance_status (id, description, label_to_display, keyword) VALUES (1,'...','done','done');
INSERT INTO tm_test_steps_instance_status (id, description, label_to_display, keyword) VALUES (2,'...','failed','failed');
INSERT INTO tm_test_steps_instance_status (id, description, label_to_display, keyword) VALUES (3,'...','activated','activated');
INSERT INTO tm_test_steps_instance_status (id, description, label_to_display, keyword) VALUES (4,'...','disactivated','disactivated');
INSERT INTO tm_test_steps_instance_status (id, description, label_to_display, keyword) VALUES (5,'...','skipped','skipped');
INSERT INTO tm_test_steps_instance_status (id, description, label_to_display, keyword) VALUES (6,'...','verified','verified');
INSERT INTO tm_test_steps_instance_status (id, description, label_to_display, keyword) VALUES (7,'test statut for simu','activated','activated-sim');

--
-- Data for Name: tm_test_steps_instance; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_step_instance_message; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_step_instance_message_sequence; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_step_instance_message_sequence', 1, false);


--
-- Data for Name: validation_status; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO validation_status (id, description, key, label_key_for_display) VALUES (1, 'Validation service could not be called.', 'ERROR', 'gazelle.tm.tee.validation.status.error');
INSERT INTO validation_status (id, description, key, label_key_for_display) VALUES (2, 'Validation service returned errors.', 'FAIL', 'gazelle.tm.tee.validation.status.failed');
INSERT INTO validation_status (id, description, key, label_key_for_display) VALUES (3, 'Validation service returned warnings.', 'WARNING', 'gazelle.tm.tee.validation.status.warning');
INSERT INTO validation_status (id, description, key, label_key_for_display) VALUES (4, 'Validation passed successfully.', 'PASS', 'gazelle.tm.tee.validation.status.passed');


--
-- Data for Name: tm_step_instance_msg_validation; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_step_instance_msg_validation_sequence; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_step_instance_msg_validation_sequence', 1, false);


--
-- Data for Name: tm_subtypes_per_system; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_syslog_configuration; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_system_actor_profiles_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_system_actor_profiles_id_seq', 8485, true);


--
-- Name: tm_system_aipo_result_for_a_testing_session_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_system_aipo_result_for_a_testing_session_id_seq', 5610, true);


--
-- Data for Name: tm_system_event; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_system_event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_system_event_id_seq', 1, false);


--
-- Data for Name: tm_system_events; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_system_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_system_id_seq', 554, true);


--
-- Name: tm_system_in_session_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_system_in_session_id_seq', 654, true);


--
-- Name: tm_system_in_session_status_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_system_in_session_status_id_seq', 5, true);


--
-- Data for Name: tm_system_in_session_user; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_system_in_session_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_system_in_session_user_id_seq', 4741, true);


--
-- Data for Name: tm_test_assertion; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_test_assertion_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_test_assertion_id_seq', 1, false);


--
-- Data for Name: tm_test_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: usr_gazelle_language; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO usr_gazelle_language (id, last_changed, last_modifier_id, description, keyword) VALUES (1, NULL, NULL, 'EN', 'English');
INSERT INTO usr_gazelle_language (id, last_changed, last_modifier_id, description, keyword) VALUES (2, NULL, NULL, 'ES', 'Spanish');
INSERT INTO usr_gazelle_language (id, last_changed, last_modifier_id, description, keyword) VALUES (5, NULL, NULL, 'JA', 'Japanese');
INSERT INTO usr_gazelle_language (id, last_changed, last_modifier_id, description, keyword) VALUES (4, NULL, NULL, 'AR', 'Arabic');
INSERT INTO usr_gazelle_language (id, last_changed, last_modifier_id, description, keyword) VALUES (3, NULL, NULL, 'FR', 'French');


--
-- Data for Name: tm_test_description; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_test_description_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_test_description_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_test_description_id_seq', 1036, true);


--
-- Name: tm_test_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_test_id_seq', 11506, true);


--
-- Data for Name: tm_test_inheritance; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_test_inheritance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_test_inheritance_id_seq', 1, false);


--
-- Data for Name: tm_test_instance_exec_status; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO tm_test_instance_exec_status (id, description, key, label_key_for_display) VALUES (1, 'Not active yet.', 'INACTIVE', 'net.ihe.gazelle.tm.Inactive');
INSERT INTO tm_test_instance_exec_status (id, description, key, label_key_for_display) VALUES (2, 'One or more test step instances are in active status (waiting, initiated, responded).', 'ACTIVE', 'net.ihe.gazelle.tm.Active');
INSERT INTO tm_test_instance_exec_status (id, description, key, label_key_for_display) VALUES (3, 'The test instance was stopped by the system after it had become active at one point.', 'INTERRUPTED', 'net.ihe.gazelle.tm.Interrupted');
INSERT INTO tm_test_instance_exec_status (id, description, key, label_key_for_display) VALUES (4, 'The test instance has been skipped by the participant.', 'SKIPPED', 'net.ihe.gazelle.tm.Skipped');
INSERT INTO tm_test_instance_exec_status (id, description, key, label_key_for_display) VALUES (5, 'The test instance has been paused by the participant after it had become active.', 'PAUSED', 'net.ihe.gazelle.tm.Paused');
INSERT INTO tm_test_instance_exec_status (id, description, key, label_key_for_display) VALUES (6, 'The test instance has been aborted by the participant after it had become active.', 'ABORTED', 'net.ihe.gazelle.tm.Aborted');
INSERT INTO tm_test_instance_exec_status (id, description, key, label_key_for_display) VALUES (7, 'The test step instance is complete.', 'COMPLETED', 'net.ihe.gazelle.tm.Completed');


--
-- Data for Name: tm_test_instance; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_test_instance_event; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_test_instance_event_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_test_instance_event_id_seq', 32036, true);


--
-- Name: tm_test_instance_exec_status_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_test_instance_exec_status_id_seq', 7, true);


--
-- Name: tm_test_instance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_test_instance_id_seq', 14735, true);


--
-- Data for Name: tm_test_instance_participants_status; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_test_instance_participants; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_test_instance_participants_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_test_instance_participants_id_seq', 22621, true);


--
-- Name: tm_test_instance_participants_status_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_test_instance_participants_status_id_seq', 3, true);


--
-- Data for Name: tm_test_instance_path_to_log_file; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_test_instance_path_to_log_file_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_test_instance_path_to_log_file_id_seq', 8429, true);


--
-- Name: tm_test_instance_status_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_test_instance_status_id_seq', 1, false);


--
-- Data for Name: tm_test_instance_test_instance_path_to_log_file; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_test_instance_test_status; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_test_instance_test_steps_instance; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_test_steps_data; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_test_instance_tm_test_steps_data; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_test_option_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_test_option_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_test_option_id_seq', 13, true);


--
-- Data for Name: tm_test_participants_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_test_participants_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_test_participants_id_seq', 1312, true);


--
-- Data for Name: tm_test_peer_type_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_test_peer_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_test_peer_type_id_seq', 4, true);


--
-- Data for Name: tm_test_roles_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_test_roles_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_test_roles_id_seq', 2129610, true);


--
-- Data for Name: tm_test_status_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_test_status_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_test_status_id_seq', 6, true);


--
-- Name: tm_test_step_message_profile_sequence; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_test_step_message_profile_sequence', 1, false);


--
-- Data for Name: tm_test_steps_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_test_steps_data_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_test_steps_data_id_seq', 850, true);


--
-- Name: tm_test_steps_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_test_steps_id_seq', 1498356, true);


--
-- Data for Name: tm_test_steps_input_ci; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_test_steps_input_ci_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_test_steps_instance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_test_steps_instance_id_seq', 37133, true);


--
-- Data for Name: tm_test_steps_instance_input_ci_instance; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_test_steps_instance_output_ci_instance; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_test_steps_instance_status_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_test_steps_instance_status_id_seq', 7, true);


--
-- Data for Name: tm_test_steps_option_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_test_steps_option_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_test_steps_option_id_seq', 2, true);


--
-- Data for Name: tm_test_steps_output_ci; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_test_steps_output_ci_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_test_steps_test_steps_data; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_test_test_description; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_test_test_description_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_test_test_steps; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_test_test_steps_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_test_type_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_test_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_test_type_id_seq', 4, true);


--
-- Data for Name: tm_test_types_sessions; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_user_comment; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_test_user_comment; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_testing_session_admin; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_testing_session_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_testing_session_id_seq', 21, true);


--
-- Name: tm_testing_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_testing_type_id_seq', 2, true);


--
-- Data for Name: tm_transport_layer_for_config_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_transport_layer_for_config_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_transport_layer_for_config_seq', 2, true);


--
-- Name: tm_user_comment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_user_comment_id_seq', 135, true);


--
-- Data for Name: tm_user_photo; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_user_photo_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_user_photo_id_seq', 1, false);


--
-- Data for Name: tm_user_preferences; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_user_preferences_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_user_preferences_id_seq', 902, true);


--
-- Data for Name: tm_web_service_configuration; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: tm_web_service_detail; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO tm_web_service_detail (id, last_changed, last_modifier_id, description, value) VALUES (1, NULL, NULL, 'sq.b', 'ITI-18:Stored Query:sq.b');
INSERT INTO tm_web_service_detail (id, last_changed, last_modifier_id, description, value) VALUES (2, NULL, NULL, 'sq.as', 'ITI-18:Async Stored Query:sq.as');
INSERT INTO tm_web_service_detail (id, last_changed, last_modifier_id, description, value) VALUES (3, NULL, NULL, 'igq', 'ITI-18:Initiating Gateway Stored Query:igq');
INSERT INTO tm_web_service_detail (id, last_changed, last_modifier_id, description, value) VALUES (4, NULL, NULL, 'xcq', 'ITI-38:Cross Gateway Query:xcq');
INSERT INTO tm_web_service_detail (id, last_changed, last_modifier_id, description, value) VALUES (5, NULL, NULL, 'xcr', 'ITI-39:Cross Gateway Retrieve:xcr');
INSERT INTO tm_web_service_detail (id, last_changed, last_modifier_id, description, value) VALUES (6, NULL, NULL, 'pr.b', 'ITI-41:Provide and Register.b:pr.b');
INSERT INTO tm_web_service_detail (id, last_changed, last_modifier_id, description, value) VALUES (7, NULL, NULL, 'pr.as', 'ITI-41:Async Provide and Register.b:pr.as');
INSERT INTO tm_web_service_detail (id, last_changed, last_modifier_id, description, value) VALUES (8, NULL, NULL, 'r.b', 'ITI-42:Register.b:r.b');
INSERT INTO tm_web_service_detail (id, last_changed, last_modifier_id, description, value) VALUES (9, NULL, NULL, 'r.as', 'ITI-42:Async Register.b:r.as');
INSERT INTO tm_web_service_detail (id, last_changed, last_modifier_id, description, value) VALUES (10, NULL, NULL, 'ret.b', 'ITI-43:Retrieve:ret.b');
INSERT INTO tm_web_service_detail (id, last_changed, last_modifier_id, description, value) VALUES (11, NULL, NULL, 'ret.as', 'ITI-43:Async Retrieve:ret.as');
INSERT INTO tm_web_service_detail (id, last_changed, last_modifier_id, description, value) VALUES (12, NULL, NULL, 'X', 'ITI-NA:Not Applicable:X');
INSERT INTO tm_web_service_detail (id, last_changed, last_modifier_id, description, value) VALUES (13, NULL, NULL, 'mpq.as', 'ITI-51:Async MPQ:mpq.as');


--
-- Name: tm_web_service_detail_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_web_service_detail_id_seq', 13, true);


--
-- Data for Name: tm_web_service_type_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: tm_web_service_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('tm_web_service_type_id_seq', 22, true);


--
-- Name: usr_address_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('usr_address_id_seq', 577, true);


--
-- Data for Name: usr_gazelle_language_aud; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: usr_gazelle_language_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('usr_gazelle_language_id_seq', 5, true);


--
-- Data for Name: usr_institution_address; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: usr_institution_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('usr_institution_id_seq', 281, true);


--
-- Name: usr_institution_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('usr_institution_type_id_seq', 4, true);


--
-- Data for Name: usr_message_parameters; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: usr_message_parameters_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('usr_message_parameters_id_seq', 1, false);


--
-- Data for Name: usr_messages; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: usr_messages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('usr_messages_id_seq', 1, false);


--
-- Data for Name: usr_messages_usr_message_parameters; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: usr_messages_usr_users; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: usr_person_function; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO usr_person_function (id, last_changed, last_modifier_id, description, is_billing, keyword, name) VALUES (998, NULL, NULL, 'This person possesses a financial function within the institution', true, 'FINANCIAL', 'gazelle.users.function.financial');
INSERT INTO usr_person_function (id, last_changed, last_modifier_id, description, is_billing, keyword, name) VALUES (2, NULL, NULL, 'This person has a technical function within the institution', false, 'TECHNICAL', 'gazelle.users.function.technical');
INSERT INTO usr_person_function (id, last_changed, last_modifier_id, description, is_billing, keyword, name) VALUES (1, NULL, NULL, 'This person possesses a marketing/Commercial function within the institution', false, 'MARKETING', 'gazelle.users.function.marketing');


--
-- Name: usr_person_function_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('usr_person_function_id_seq', 998, true);


--
-- Name: usr_person_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('usr_person_id_seq', 551, true);


--
-- Data for Name: usr_persons_functions; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Data for Name: usr_role; Type: TABLE DATA; Schema: public; Owner: gazelle
--

INSERT INTO usr_role (id, last_changed, last_modifier_id, description, name) VALUES (1, NULL, NULL, 'Profile with Admin rights (ie. a user allowed to do everything) ', 'admin_role');
INSERT INTO usr_role (id, last_changed, last_modifier_id, description, name) VALUES (3, NULL, NULL, 'Profile with Monitor rights (ie. a user allowed to validate tests) ', 'monitor_role');
INSERT INTO usr_role (id, last_changed, last_modifier_id, description, name) VALUES (4, NULL, NULL, 'Profile with Project Manager rights (ie. a user allowed to do everything except access to financial information) ', 'project-manager_role');
INSERT INTO usr_role (id, last_changed, last_modifier_id, description, name) VALUES (5, NULL, NULL, 'Profile with Accounting rights (ie. a user allowed to update financial and administrative information) ', 'accounting_role');
INSERT INTO usr_role (id, last_changed, last_modifier_id, description, name) VALUES (6, NULL, NULL, 'Profile with Vendor Administrator rights (ie. a user able to manage (CRUD) all its company information (users, systems, contacts, financial)', 'vendor_admin_role');
INSERT INTO usr_role (id, last_changed, last_modifier_id, description, name) VALUES (7, NULL, NULL, 'Profile with Vendor rights (ie. a user with limited rights able to manage its information and the systems he/she creates', 'vendor_role');
INSERT INTO usr_role (id, last_changed, last_modifier_id, description, name) VALUES (8, NULL, NULL, 'Profile with some simple user rights (ie. a user who wants to perform search with his preferences). This role has low privileges', 'user_role');
INSERT INTO usr_role (id, last_changed, last_modifier_id, description, name) VALUES (9, NULL, NULL, 'Profile with Editor rights (ie. a user allowed to update Technical Framework information)  ', 'tf_editor_role');
INSERT INTO usr_role (id, last_changed, last_modifier_id, description, name) VALUES (10, NULL, NULL, 'Profile with Tests Editor rights (ie. a user allowed to update Tests definition)  ', 'tests_editor_role');
INSERT INTO usr_role (id, last_changed, last_modifier_id, description, name) VALUES (11, NULL, NULL, 'Profile with Administrative Editor rights (ie. a user allowed to update administrative -institutions, users, contacts- informations)', 'institutions_editor_role');
INSERT INTO usr_role (id, last_changed, last_modifier_id, description, name) VALUES (12, NULL, NULL, 'Profile with Systems Editor rights (ie. a user allowed to update systems information) ', 'systems_editor_role');
INSERT INTO usr_role (id, last_changed, last_modifier_id, description, name) VALUES (13, '2011-01-18 11:20:26.43915+00', 'epoiseau', 'Role to allow the user to modify the content of a system or add or delete a system', 'vendor_late_registration_role');
INSERT INTO usr_role (id, last_changed, last_modifier_id, description, name) VALUES (14, '2013-08-27 07:07:43.478821+00', 'epoiseau', 'User with the Testing Session Administration rights', 'testing_session_admin_role');


--
-- Name: usr_role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('usr_role_id_seq', 14, true);


--
-- Data for Name: usr_user_role; Type: TABLE DATA; Schema: public; Owner: gazelle
--



--
-- Name: usr_users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('usr_users_id_seq', 1011, true);


--
-- Name: validation_service_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('validation_service_id_seq', 2, true);


--
-- Name: validation_status_id_seq; Type: SEQUENCE SET; Schema: public; Owner: gazelle
--

SELECT pg_catalog.setval('validation_status_id_seq', 4, true);


--
-- PostgreSQL database dump complete
--

