-- Query the gazelle database to show all the integration profile where an actor does have an aipo without a none option

select
  actor_integration_profile_id,
  it.keyword as integration_profile,
  a.keyword  as actor
from tf_actor_integration_profile_option aipo
  join tf_actor_integration_profile ait on ait.id = aipo.actor_integration_profile_id
  join tf_integration_profile it on ait.integration_profile_id = it.id
  join tf_actor a on ait.actor_id = a.id
  join tf_integration_profile_option o on aipo.integration_profile_option_id = o.id
group by actor_integration_profile_id, it.keyword, a.keyword
having 'NONE' != ALL(array_agg(o.keyword))
order by it.keyword;

-- Fix database

INSERT INTO tf_actor_integration_profile_option (id, last_changed, last_modifier_id, actor_integration_profile_id, integration_profile_option_id, maybe_supportive, document_section)
SELECT nextval('tf_actor_integration_profile_option_id_seq'), null, null, ait.id, (SELECT id from tf_integration_profile_option where tf_integration_profile_option.keyword = 'NONE'), null, null
from tf_actor_integration_profile_option aipo
  join tf_actor_integration_profile ait on ait.id = aipo.actor_integration_profile_id
  join tf_integration_profile it on ait.integration_profile_id = it.id
  join tf_actor a on ait.actor_id = a.id
  join tf_integration_profile_option o on aipo.integration_profile_option_id = o.id
group by actor_integration_profile_id, it.keyword, a.keyword, ait.id
having 'NONE' != ALL(array_agg(o.keyword));