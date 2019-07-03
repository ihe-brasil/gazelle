update tm_test_instance_exec_status set label_key_for_display='net.ihe.gazelle.tm.Skipped' where id=4 and key='SKIPPED';
update tm_test_instance_exec_status set label_key_for_display='net.ihe.gazelle.tm.Paused' where id=5 and key='PAUSED';
update tm_test_instance_exec_status set label_key_for_display='net.ihe.gazelle.tm.Aborted' where id=6 and key='ABORTED';
update tm_test_instance_exec_status set label_key_for_display='net.ihe.gazelle.tm.Completed' where id=7 and key='COMPLETED';