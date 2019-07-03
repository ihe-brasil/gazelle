pg_dump -U gazelle master-model --column-inserts  -t tf_rule  --data-only > tf_rule.sql
pg_dump -U gazelle master-model --column-inserts  -t tf_rule_criterion  --data-only > tf_rule_criterion.sql
pg_dump -U gazelle master-model --column-inserts  -t tf_rule_list_criterion  --data-only > tf_rule_list_criterion.sql

delete from tf_rule;
delete from tf_rule_criterion;
delete from tf_rule_list_criterion;

psql -U gazelle gazelle < tf_rule_criterion.sql
psql -U gazelle gazelle < tf_rule_list_criterion.sql
psql -U gazelle gazelle < tf_rule.sql

ALTER TABLE ONLY public.tf_rule_criterion
    ADD CONSTRAINT fk1778828b2d58306c FOREIGN KEY (aiporule_id) REFERENCES public.tf_rule(id);


delete from tf_rule where cause_id not in (select id from tf_rule_criterion);
delete from tf_rule where consequence_id not in (select id from tf_rule_criterion);
delete from tf_rule where cause_id in (select id from tf_rule_criterion where list_or is not null and id not in (select tf_rule_criterion_id from tf_rule_list_criterion ));
delete from tf_rule where consequence_id in (select id from tf_rule_criterion where list_or is not null and id not in (select tf_rule_criterion_id from tf_rule_list_criterion ));
delete from tf_rule_list_criterion where aipocriterions_id in (select id from  tf_rule_criterion where aiporule_id not in (select id from tf_rule));
delete from tf_rule_criterion where aiporule_id not in (select id from tf_rule);
delete from tf_rule_list_criterion where aipocriterions_id in (select id from  tf_rule_criterion where aiporule_id not in (select id from tf_rule where cause_id not in (select id from tf_rule_criterion)));
delete from tf_rule_list_criterion where aipocriterions_id in (select id from  tf_rule_criterion where aiporule_id not in (select id from tf_rule where consequence_id not in (select id from tf_rule_criterion)));

ALTER TABLE ONLY public.tf_rule
    ADD CONSTRAINT fk_1nipbe194qol7ts0abf705jv4 FOREIGN KEY (consequence_id) REFERENCES public.tf_rule_criterion(id);

ALTER TABLE ONLY public.tf_rule
    ADD CONSTRAINT fk_9cynyuydbfmqhresmj8n1uuqo FOREIGN KEY (cause_id) REFERENCES public.tf_rule_criterion(id);




SELECT dtype, id, single_actor, single_integrationprofile, single_option, list_or, 'INSERT INTO tf_rule_criterion (dtype, id, single_actor, single_integrationprofile, single_option, list_or, aiporule_id, single_actor_id, single_integration_profile_id, single_option_id) VALUES ('
|| dtype
|| ''', '''
as fooo, ''''
|| id
|| ''', '''
|| single_actor
|| ''', '''
|| single_integrationprofile
|| ''', '''
|| single_option
|| ''', '''
|| list_or as foo
  from tf_rule_criterion where aiporule_id is null ;

|| '''), get_profile_id('''
|| single_integrationprofile
|| '''), get_integration_profile_option_id('''
|| single_option
|| '''));' from tf_rule_criterion where aiporule_id is null ;



|| '''));' from tf_rule_criterion where aiporule_id is not null ;


INSERT INTO tf_rule_criterion (dtype, id, single_actor, single_integrationprofile, single_option, list_or, aiporule_id, single_actor_id, single_integration_profile_id, single_option_id) VALUES ('AipoSingle', 5178, 'CARE_MGR', 'XTHM-WD', 'NONE', NULL, 1271, 112, 291, 1);
