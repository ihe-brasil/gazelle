UPDATE tm_testing_session SET testing_session_type_id=1 where type='Connectathon';
UPDATE tm_testing_session SET testing_session_type_id=3 where type='Interoperability';
UPDATE tm_testing_session SET testing_session_type_id=1 where description like '%Connectathon%';
UPDATE tm_testing_session SET testing_session_type_id=1 where description like '%Connecathon%';
UPDATE tm_testing_session SET testing_session_type_id=3,type='Interoperability' where description like '%Interoperability%';
