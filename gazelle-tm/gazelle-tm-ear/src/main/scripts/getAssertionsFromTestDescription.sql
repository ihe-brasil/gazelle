psql -d gazelle -Ugazelle -t -A -F"," -c "
 SELECT DISTINCT
'TEST' as linked_entity,
'linkId' as id,
  tm_test.keyword, 
  regexp_matches(tm_test_description.description , '(\w+-\d+)','g') as assertionId
FROM 
  public.tf_domain, 
  public.tf_integration_profile, 
  public.tf_actor_integration_profile, 
  public.tf_actor_integration_profile_option, 
  public.tm_test,
  public.tm_test_participants, 
  public.tm_role_in_test, 
  public.tm_test_roles, 
  public.tf_domain_profile, 
  public.tm_role_in_test_test_participants,
public.tm_test_test_description,
public.tm_test_description
WHERE 
  tf_actor_integration_profile.integration_profile_id = tf_integration_profile.id AND
  tf_actor_integration_profile_option.actor_integration_profile_id = tf_actor_integration_profile.id AND
  tm_test_participants.actor_integration_profile_option_id = tf_actor_integration_profile_option.id AND
  tm_test_roles.role_in_test_id = tm_role_in_test.id AND
  tm_test_roles.test_id = tm_test.id AND
  tf_domain_profile.domain_id = tf_domain.id AND
  tf_domain_profile.integration_profile_id = tf_integration_profile.id AND
  tm_role_in_test_test_participants.role_in_test_id = tm_role_in_test.id AND
  tm_role_in_test_test_participants.test_participants_id = tm_test_participants.id AND
  tf_domain.keyword = 'KSA' AND tm_test_test_description.test_id = tm_test.id AND
  tm_test_test_description.test_description_id = tm_test_description.id AND
  tm_test_description.description LIKE '%[%-%]%'
ORDER BY
  tm_test.keyword ASC;" > assertions_from_test.csv
    
/*
sed "/RAD-/d; s/^/insert into am_assertions_links (linked_entity,id,linked_entity_keyword,assertion_id) values (/; s/TEST/'TEST'/; s/linkId/nextval('am_assertions_links_id_seq')/; s/$/));/ ; s/{/(select id from am_oasis_test_assertion where assertion_id = '/; s/}/'/; s/),/),'/; s/,(/\',(/" assertions_from_test.csv
*/
