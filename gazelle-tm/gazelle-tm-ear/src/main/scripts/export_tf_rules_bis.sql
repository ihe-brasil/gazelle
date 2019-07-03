-- This command will create insert command for each row on tf_rule table
SELECT 'INSERT INTO tf_rule (id, cause_id, consequence_id, comment) VALUES ('
|| id
|| ', '
|| COALESCE(cause_id, cause_id, -1)
|| ', '
|| COALESCE(consequence_id, consequence_id, -1)
|| ', '''
|| COALESCE(comment, comment, '')
|| ''');' from tf_rule order by id;

-- This command will create insert command for each row on tf_rule_criterion table
SELECT 'INSERT INTO tf_rule_criterion (dtype, id, single_actor, single_integrationprofile, single_option, list_or, aiporule_id, single_actor_id, single_integration_profile_id, single_option_id) VALUES ('''
|| dtype
|| ''', '
|| id
|| ', '''
|| COALESCE(single_actor, single_actor, '')
|| ''', '''
|| COALESCE(single_integrationprofile, single_integrationprofile, '')
|| ''', '''
|| COALESCE(single_option, single_option, '')
|| ''', '
|| COALESCE(list_or, list_or, false )
|| ', '
|| COALESCE(aiporule_id, aiporule_id, -1)
|| ', '
|| (SELECT CASE WHEN (SELECT COUNT(*) FROM tf_actor where id = single_actor_id) = 0 THEN 'NULL' else (SELECT 'get_actor_id_bis(''' || (SELECT COALESCE(keyword, keyword, 'NULL') FROM tf_actor where id = single_actor_id ) || ''')') END)
|| ', '
|| (SELECT CASE WHEN (SELECT COUNT(*) FROM tf_integration_profile where id = single_integration_profile_id) = 0 THEN 'NULL' else (SELECT 'get_profile_id_bis(''' || (SELECT COALESCE(keyword, keyword, 'NULL') FROM tf_integration_profile where id = single_integration_profile_id ) || ''')') END)
|| ', '
|| (SELECT CASE WHEN (SELECT COUNT(*) FROM tf_integration_profile_option where id = single_option_id) = 0 THEN 'NULL' else (SELECT 'get_integration_profile_option_bis(''' || (SELECT COALESCE(keyword, keyword, 'NULL') FROM tf_integration_profile_option where id = single_option_id ) || ''')') END)
|| ');' from tf_rule_criterion order by id;


-- This command will create insert command for each row on tf_rule_list_criterion table
SELECT 'INSERT INTO tf_rule_list_criterion (tf_rule_criterion_id, aipocriterions_id, tf_rule_list_criterion_id) VALUES ('
|| COALESCE(tf_rule_criterion_id, tf_rule_criterion_id, -1)
|| ', '
|| COALESCE(aipocriterions_id, aipocriterions_id, -1)
|| ', '
|| tf_rule_list_criterion_id
|| ');' from tf_rule_list_criterion order by tf_rule_list_criterion_id;


-- Once all insert files have been created using the three commands, the data are ready to be imported.
-- However some constraints have to be droped from datatables in order to insert the data.
-- Constraints that need to be dropped :
--      Criterion reference to rules
--      List join table reference to criterion
--      Rules references to criterions
-- Use the following command to drop the constraint. Do not forget to change the id and database name!
ALTER TABLE public.tf_rule_criterion DROP CONSTRAINT fk1778828b2d58306c;

-- Functions also have to be imported from create_fucntions.sql file in order to retrieve ids from keywords.
-- One last thing is to create false objects into tf_actor, tf_integration_profile and tf_integration_profile_option in order to reference them when
-- an object does not exist on target instance. All imported data referencing those objects will be deleted later on.
INSERT INTO tf_actor (id, keyword, name) VALUES (-1, 'TO DELETE', 'TO DELETE');
INSERT INTO tf_integration_profile (id, keyword, name) VALUES (-1, 'TO DELETE', 'TO DELETE');
INSERT INTO tf_integration_profile_option (id, keyword, name, description) VALUES (-1, 'TO DELETE', 'TO DELETE', 'TO DELETE');

-- Now datas can be imported in the target database !
-- Be carefull to add datas in the right order : tf_rule_criterion first, then tf_rule and tf_rule_list_criterion in any order

psql -U gazelle gazelle < tf_rule_criterion.sql
psql -U gazelle gazelle < tf_rule_list_criterion.sql
psql -U gazelle gazelle < tf_rule.sql

-- All datas have now been imported. It is time to delete all not compliant records

--Delete criterions that references unknow model objects.
DELETE FROM tf_rule_criterion WHERE single_actor_id = -1 or single_integration_profile_id = -1 or single_option_id = -1;
--Delete criterions that are not list and which references no model elements.
delete from tf_rule_criterion where ((dtype = 'AipoSingle') AND (single_actor_id is NULL AND single_integration_profile_id is NULL AND single_option_id is NULL));

--Delete criterion that are lists whith AND and that references criterions that have been removed
DELETE from tf_rule_criterion
where ((dtype = 'AipoList')
AND list_or = false
AND id IN (SELECT tf_rule_criterion_id FROM tf_rule_list_criterion where aipocriterions_id NOT IN (SELECT id from tf_rule_criterion) )) ;

-- Delete from join table all elements that is not in criterions table.
DELETE FROM tf_rule_list_criterion where aipocriterions_id NOT IN (SELECT id from tf_rule_criterion) ;

-- Delete list of or with no more elements in join table
delete from tf_rule_criterion where dtype='AipoList' and list_or=true and id not in (Select tf_rule_criterion_id from tf_rule_list_criterion);

-- Delete rules that have a cause or a consequence that has been removed.
DELETE from tf_rule
where ((cause_id not in (Select id from tf_rule_criterion))
OR (consequence_id not in (Select id from tf_rule_criterion)));

-- Delete criterions that reference a rule that does notexist anymore
-- THIS LINK IS NTO SAFE TO USE, LET IT BE
-- DELETE FROM tf_rule_criterion WHERE aiporule_id NOT IN (select id from tf_rule);

-- Delete from join table element that references deleted criterions
DELETE FROM tf_rule_list_criterion where aipocriterions_id NOT IN (SELECT id from tf_rule_criterion) ;
DELETE FROM tf_rule_list_criterion where tf_rule_criterion_id NOT IN (SELECT id from tf_rule_criterion) ;


-- Once all datas have been imported, do not forget to recreate the constraint on tables :
ALTER TABLE ONLY public.tf_rule_criterion
    ADD CONSTRAINT fk_aiporule_id_constraint FOREIGN KEY (aiporule_id) REFERENCES public.tf_rule(id);

ALTER TABLE ONLY public.tf_rule_list_criterion
    ADD CONSTRAINT fk_rule_criterion_id_constraint FOREIGN KEY (tf_rule_criterion_id) REFERENCES public.tf_rule_criterion(id);

ALTER TABLE ONLY public.tf_rule_list_criterion
    ADD CONSTRAINT fk_aipocriterions_id_constraint FOREIGN KEY (aipocriterions_id) REFERENCES public.tf_rule_criterion(id);

ALTER TABLE ONLY public.tf_rule
    ADD CONSTRAINT fk_cause_id_criterion FOREIGN KEY (cause_id) REFERENCES public.tf_rule_criterion(id);

ALTER TABLE ONLY public.tf_rule
    ADD CONSTRAINT fk_consequence_id_criterion FOREIGN KEY (consequence_id) REFERENCES public.tf_rule_criterion(id);

-- Also delete fake objects we created to respect constraints :
DELETE FROM tf_actor WHERE id = -1;
DELETE FROM tf_integration_profile WHERE id = -1;
DELETE FROM tf_integration_profile_option WHERE id = -1;