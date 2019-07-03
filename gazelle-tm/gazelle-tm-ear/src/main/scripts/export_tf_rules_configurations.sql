SELECT 'INSERT INTO tm_web_service_type (id, description, needs_oid, profile_id) VALUES ('
|| id
|| ', '''
|| COALESCE(description, description, '')
|| ''', '
|| COALESCE(needs_oid, needs_oid, false )
|| ', '
||(SELECT CASE WHEN (SELECT COUNT(*) FROM tf_integration_profile where id = profile_id) = 0 THEN 'NULL' else (SELECT 'get_profile_id_bis(''' || (SELECT COALESCE(keyword, keyword, 'NULL') FROM tf_integration_profile where id = profile_id ) || ''')') END)
|| ');' from tm_web_service_type order by id;



SELECT 'INSERT INTO tm_conftype_w_ports_wstype_and_sop_class (id, configurationType_id, port_non_secure, port_secure, web_service_type_id, ws_transaction_usage_id, sop_class_id, transport_layer_id, transaction_description, comment) VALUES('
|| id
|| ', '
|| COALESCE(configurationType_id, configurationType_id, -1)
|| ', '
|| COALESCE(port_non_secure, port_non_secure, -1)
|| ', '
|| COALESCE(port_secure, port_secure, -1)
|| ', (SELECT CASE WHEN (SELECT COUNT(*) FROM tm_web_service_type where id = '
||COALESCE(web_service_type_id, web_service_type_id, -1)
|| ') = 0 THEN -1 else '
||COALESCE(web_service_type_id, web_service_type_id, -1)
||' END)'
|| ', (SELECT CASE WHEN (SELECT COUNT(*) FROM tf_ws_transaction_usage where id = '
|| COALESCE(ws_transaction_usage_id, ws_transaction_usage_id, -1)
|| ') = 0 THEN -1 else '
||COALESCE(ws_transaction_usage_id, ws_transaction_usage_id, -1)
||' END)'
|| ', '
|| COALESCE(sop_class_id, sop_class_id, -1)
|| ', '
|| COALESCE(transport_layer_id, transport_layer_id, -1)
|| ', '''
|| COALESCE(transaction_description, transaction_description, '')
|| ''', '''
|| COALESCE(comment, comment, '')
|| ''');' from tm_conftype_w_ports_wstype_and_sop_class;

-- REPLACE IN OBTAINED FILE '-1 END' BY 'NULL END'


SELECT 'INSERT INTO tm_configuration_mapped_with_aipo (id, aipo_id) VALUES ('
||id
||', '
||(SELECT CASE WHEN (SELECT COUNT(*) FROM tf_actor_integration_profile_option where id = aipo_id) = 0 THEN 'NULL' else ('(SELECT id FROM tf_actor_integration_profile_option WHERE actor_integration_profile_id = (SELECT id FROM tf_actor_integration_profile where actor_id = get_actor_id_bis(''' ||(SELECT keyword from tf_actor WHERE id =(SELECT actor_id FROM tf_actor_integration_profile WHERE id =(SELECT actor_integration_profile_id FROM tf_actor_integration_profile_option WHERE id = aipo_id) ))||''') AND  integration_profile_id = get_profile_id_bis('''||(SELECT keyword from tf_integration_profile WHERE id =(SELECT integration_profile_id FROM tf_actor_integration_profile WHERE id = (SELECT actor_integration_profile_id FROM tf_actor_integration_profile_option WHERE id = aipo_id)))||'''))'
||' AND integration_profile_option_id = get_integration_profile_option_bis('''|| (SELECT keyword FROM tf_integration_profile_option WHERE id = (SELECT integration_profile_option_id FROM tf_actor_integration_profile_option WHERE id = aipo_id)) ||''')'
||')') END)
||');' from tm_configuration_mapped_with_aipo order by id;



SELECT 'INSERT INTO tm_conf_mapping_w_aipo_w_conftypes (tm_conf_mapped_id, tm_conftypes_id) VALUES ('
|| tm_conf_mapped_id
|| ', '
|| tm_conftypes_id
|| ');' from tm_conf_mapping_w_aipo_w_conftypes;




INSERT INTO tf_integration_profile (id, keyword, name) VALUES (-1, 'TO DELETE', 'TO DELETE');
INSERT INTO tf_integration_profile_option (id, keyword, name, description) VALUES (-1, 'TO DELETE', 'TO DELETE', 'TO DELETE');
INSERT INTO tm_web_service_type (id) VALUES (-1);
INSERT INTO tf_transaction (id,description, keyword, name) VALUES (-1, 'TO DELETE', 'TO DELETE', 'TO DELETE' );
INSERT INTO tf_ws_transaction_usage (id, transaction_id) VALUES (-1, -1);
INSERT INTO tm_sop_class (id, keyword) VALUES (-1, 'TO DELETE');
INSERT INTO tm_transport_layer_for_config (id, keyword) VALUES (-1, 'TO DELETE');

UPDATE tm_conftype_w_ports_wstype_and_sop_class SET configurationType_id = NULL where configurationType_id = -1;
UPDATE tm_conftype_w_ports_wstype_and_sop_class SET port_non_secure = NULL where port_non_secure = -1;
UPDATE tm_conftype_w_ports_wstype_and_sop_class SET port_secure = NULL where port_secure = -1;
UPDATE tm_conftype_w_ports_wstype_and_sop_class SET sop_class_id = NULL where sop_class_id = -1;
UPDATE tm_conftype_w_ports_wstype_and_sop_class SET transport_layer_id = NULL where transport_layer_id = -1;

DELETE FROM tm_conftype_w_ports_wstype_and_sop_class where web_service_type_id in (SELECT id FROM tm_web_service_type WHERE profile_id = -1);
DELETE FROM tm_conf_mapping_w_aipo_w_conftypes where tm_conftypes_id in (SELECT id FROM tm_conftype_w_ports_wstype_and_sop_class where ws_transaction_usage_id = -1 or web_service_type_id = -1);
DELETE FROM tm_conftype_w_ports_wstype_and_sop_class where ws_transaction_usage_id = -1 or web_service_type_id = -1;
DELETE FROM tm_web_service_type where profile_id = -1;


DELETE FROM tf_integration_profile WHERE id = -1;
DELETE FROM tf_integration_profile_option WHERE id = -1;
DELETE FROM tm_web_service_type WHERE id = -1;
DELETE FROM tf_transaction WHERE id = -1;
DELETE FROM tf_ws_transaction_usage WHERE id = -1;
DELETE FROM tm_sop_class WHERE id = -1;
DELETE FROM tm_transport_layer_for_config WHERE id = -1;