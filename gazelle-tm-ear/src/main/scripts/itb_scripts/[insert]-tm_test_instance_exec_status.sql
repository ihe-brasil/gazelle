INSERT INTO tm_test_instance_exec_status (id,key,description,label_key_for_display) values
(1,'INACTIVE','Not active yet.','net.ihe.gazelle.tm.Inactive');

INSERT INTO tm_test_instance_exec_status (id,key,description,label_key_for_display) values
(2,'ACTIVE','One or more test step instances are in active status (waiting, initiated, responded).','net.ihe.gazelle.tm.Active');

INSERT INTO tm_test_instance_exec_status (id,key,description,label_key_for_display) values
(3,'INTERRUPTED','The test instance was stopped by the system after it had become active at one point.','net.ihe.gazelle.tm.Interrupted');

INSERT INTO tm_test_instance_exec_status (id,key,description,label_key_for_display) values
(4,'SKIPPED','The test instance has been skipped by the participant.','net.ihe.gazelle.tm.Skip');

INSERT INTO tm_test_instance_exec_status (id,key,description,label_key_for_display) values
(5,'PAUSED','The test instance has been paused by the participant after it had become active.','net.ihe.gazelle.tm.Pause');

INSERT INTO tm_test_instance_exec_status (id,key,description,label_key_for_display) values
(6,'ABORTED','The test instance has been aborted by the participant after it had become active.','net.ihe.gazelle.tm.Abort');

INSERT INTO tm_test_instance_exec_status (id,key,description,label_key_for_display) values
(7,'COMPLETED','The test step instance is complete.','net.ihe.gazelle.tm.Complete');