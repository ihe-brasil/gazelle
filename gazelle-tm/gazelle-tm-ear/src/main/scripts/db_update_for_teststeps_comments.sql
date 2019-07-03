-- alter table tm_test_instance drop constraint fk20c71e5c2964340;
-- alter table tm_test_instance_event drop constraint fkd518bef7c5b079a9;
-- alter table tm_test_instance_status drop constraint fke5c50f35c5b079a9;
-- alter table tm_test_steps_instance drop constraint fk6afc6734c5b079a9;
-- drop table tm_status;
INSERT INTO tm_data_type (id, last_changed, last_modifier_id, description, keyword) VALUES (6, NULL, NULL, 'comment', 'comment');
INSERT INTO tm_data_type (id, last_changed, last_modifier_id, description, keyword) VALUES (7, NULL, NULL, 'url', 'url');
INSERT INTO tm_data_type (id, last_changed, last_modifier_id, description, keyword) VALUES (8, NULL, NULL, 'xds', 'xds');
ALTER TABLE tm_test_steps_data ALTER COLUMN comment TYPE text;
ALTER TABLE tm_test_steps_data ALTER COLUMN value TYPE text;

