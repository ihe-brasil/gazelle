select actor_integration_profile_option_id, keyword, count(*) from (
select distinct role_in_test_id, actor_integration_profile_option_id, is_tested, keyword from tm_role_in_test_test_participants rittp, tm_role_in_test rit, tm_test_participants tp  where tp.id = rittp.test_participants_id and rittp.role_in_test_id = rit.id and test_participants_id  in (select id from tm_test_participants where actor_integration_profile_option_id in (
select  actor_integration_profile_option_id
from tm_test_participants
group by actor_integration_profile_option_id
HAVING count(*) > 1 ))) as foo group by actor_integration_profile_option_id, keyword having count(*) > 1;
