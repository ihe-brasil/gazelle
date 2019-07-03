INSERT INTO tm_step_instance_exec_status (id,key,description,label_key_for_display) values
(1,'INACTIVE','Not active yet.','net.ihe.gazelle.tm.Inactive');

INSERT INTO tm_step_instance_exec_status (id,key,description,label_key_for_display) values
(2,'WAITING','Active but no messages have been intercepted yet from sender and receiver','net.ihe.gazelle.tm.Waiting');

INSERT INTO tm_step_instance_exec_status (id,key,description,label_key_for_display) values
(3,'INTERRUPTED','The test step instance was stopped by the system after it had become active at one point.','net.ihe.gazelle.tm.Interrupted');

INSERT INTO tm_step_instance_exec_status (id,key,description,label_key_for_display) values
(4,'SKIPPED','The test step has been skipped by the participant.','net.ihe.gazelle.tm.Skipped');

INSERT INTO tm_step_instance_exec_status (id,key,description,label_key_for_display) values
(5,'PAUSED','The test step has been paused by the participant after it had become active.','net.ihe.gazelle.tm.Paused');

INSERT INTO tm_step_instance_exec_status (id,key,description,label_key_for_display) values
(6,'ABORTED','The test step has been aborted by the participant after it had become active.','net.ihe.gazelle.tm.Aborted');

INSERT INTO tm_step_instance_exec_status (id,key,description,label_key_for_display) values
(7,'INITIATED','A request message has been intercepted.','net.ihe.gazelle.tm.Initiated');

INSERT INTO tm_step_instance_exec_status (id,key,description,label_key_for_display) values
(8,'RESPONDED','A response message has been intercepted.','net.ihe.gazelle.tm.Responded');

INSERT INTO tm_step_instance_exec_status (id,key,description,label_key_for_display) values
(9,'COMPLETED','The test step instance is complete.','net.ihe.gazelle.tm.Completed');