-- this script has to be executed if some test steps do not have any version logged in the audit table. In that case, TM reports such error message
-- in the logs: "a problem on the revNumbers of teststeps" or "there are no revNumbers for tespsteps"

-- rev column in tm_test_steps_aud is a foreign key to revinfo.rev; we must first populate the revinfo table
CREATE OR REPLACE FUNCTION populate_revinfo()
  RETURNS VOID AS
$BODY$
DECLARE revnumber INTEGER := (select last_value from hibernate_sequence) + 1;
  DECLARE endnumber INTEGER := revnumber + (select count(id) from tm_test_steps where id not in (select id from tm_test_steps_aud)) ;
BEGIN
  LOOP
    EXIT when revnumber = endnumber;
    insert into revinfo (rev, revtstmp) values (revnumber, 1550915431299);
    revnumber := revnumber + 1;
  END LOOP;
  RETURN;
END
$BODY$
LANGUAGE 'plpgsql';

-- execute the function we have just created
select populate_revinfo();

-- populate the tm_test_steps_aud for tm_test_steps which id is not present in tm_test_steps_aud table
insert into tm_test_steps_aud (id, rev, revtype, description, message_type, secured, step_index, test_roles_initiator_id, test_roles_responder_id, test_steps_option_id, transaction_id, ws_transaction_usage_id)
  SELECT id, nextval('hibernate_sequence'), 1, description, message_type, secured, step_index, test_roles_initiator_id, test_roles_responder_id, test_steps_option_id, transaction_id, ws_transaction_usage_id
  from tm_test_steps where id not in (select id from tm_test_steps_aud);

-- we might have test instances with empty test steps, link them to the latest revision of the test step
update tm_test_steps_instance set test_steps_version = (select rev from tm_test_steps_aud where id = test_steps_id order by rev desc limit 1) where test_steps_version is null ;