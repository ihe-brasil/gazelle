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
INSERT INTO usr_role (id, last_changed, last_modifier_id, description, name) VALUES (13, '2011-01-18 12:20:26.43915', 'epoiseau', 'Role to allow the user to modify the content of a system or add or delete a system', 'vendor_late_registration_role');
INSERT INTO usr_role (id, last_changed, last_modifier_id, description, name) VALUES (14, '2013-08-27 09:07:43.478821', 'epoiseau', 'User with the Testing Session Administration rights', 'testing_session_admin_role');

SELECT setval('usr_role_id_seq', 14);  