SELECT 'INSERT INTO tm_oid_root_definition_label (id, label) VALUES ('
|| id
|| ', '''
|| COALESCE(label, label, '')
|| ''');' from tm_oid_root_definition_label;



SELECT 'INSERT INTO tm_oid_root_definition (id, root_oid, last_value, label_id) VALUES ('
|| id
|| ', '''
|| COALESCE(root_oid, root_oid, '')
|| ''', '
|| 0
|| ', '
|| COALESCE(label_id, label_id, -1)
|| ');' from tm_oid_root_definition;


SELECT 'INSERT INTO tm_oid_requirement (id, label, oid_root_definition_id, prefix) VALUES ('
|| id
|| ', '''
|| COALESCE(label, label, '')
|| ''', '
|| COALESCE(oid_root_definition_id, oid_root_definition_id, -1)
|| ', '''
|| COALESCE(prefix, prefix, '')
|| ''');' from tm_oid_requirement;



SELECT 'INSERT INTO tm_oid_requirement_aipo (oid_requirement_id, aipo_id) VALUES ('
|| COALESCE(oid_requirement_id, oid_requirement_id, -1)
|| ', '
||(SELECT CASE WHEN (SELECT COUNT(*) FROM tf_actor_integration_profile_option where id = aipo_id) = 0 THEN 'NULL' else ('(SELECT id FROM tf_actor_integration_profile_option WHERE actor_integration_profile_id = (SELECT id FROM tf_actor_integration_profile where actor_id = get_actor_id_bis(''' ||(SELECT keyword from tf_actor WHERE id =(SELECT actor_id FROM tf_actor_integration_profile WHERE id =(SELECT actor_integration_profile_id FROM tf_actor_integration_profile_option WHERE id = aipo_id) ))||''') AND  integration_profile_id = get_profile_id_bis('''||(SELECT keyword from tf_integration_profile WHERE id =(SELECT integration_profile_id FROM tf_actor_integration_profile WHERE id = (SELECT actor_integration_profile_id FROM tf_actor_integration_profile_option WHERE id = aipo_id)))||'''))'
||' AND integration_profile_option_id = get_integration_profile_option_bis('''|| (SELECT keyword FROM tf_integration_profile_option WHERE id = (SELECT integration_profile_option_id FROM tf_actor_integration_profile_option WHERE id = aipo_id)) ||''')'
||')') END)
|| ');' from tm_oid_requirement_aipo;




-- ONCE DATAS ARE INSERTED
ALTER SEQUENCE tm_oid_root_definition_label_id_seq RESTART (SELECT max(id) from tm_oid_root_definition_label);
ALTER SEQUENCE tm_oid_root_definition_id_seq RESTART (SELECT max(id) from tm_oid_root_definition);
ALTER SEQUENCE tm_oid_requirement_id_seq RESTART (SELECT max(id) from tm_oid_requirement);