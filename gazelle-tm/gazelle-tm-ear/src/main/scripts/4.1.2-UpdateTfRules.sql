--Information
--SELECT rule.id, actor.id from tf_rule_criterion AS rule, tf_actor AS actor WHERE rule.single_actor = actor.keyword;
update tf_rule_criterion set single_actor_id = b.actor_id from (SELECT rule.id AS rule_id, actor.id AS actor_id from tf_rule_criterion AS rule, tf_actor AS actor WHERE rule.single_actor = actor.keyword) AS b where b.rule_id= tf_rule_criterion.id;

--Information
--SELECT rule.id, ip.id from tf_rule_criterion AS rule, tf_integration_profile AS ip WHERE rule.single_integrationprofile = ip.keyword;
update tf_rule_criterion set single_integration_profile_id = b.ip_id from (SELECT rule.id AS rule_id, ip.id AS ip_id from tf_rule_criterion AS rule, tf_integration_profile AS ip WHERE rule.single_integrationprofile = ip.keyword) AS b where b.rule_id= tf_rule_criterion.id;

--Information
--SELECT rule.id, ipo.id from tf_rule_criterion AS rule, tf_integration_profile_option AS ipo WHERE rule.single_option = ipo.keyword;
update tf_rule_criterion set single_option_id = b.ipo_id from (SELECT rule.id AS rule_id, ipo.id AS ipo_id from tf_rule_criterion AS rule, tf_integration_profile_option AS ipo WHERE rule.single_option = ipo.keyword) AS b where b.rule_id= tf_rule_criterion.id;
