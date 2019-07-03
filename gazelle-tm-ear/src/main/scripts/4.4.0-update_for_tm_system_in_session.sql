--SELECT id from tm_system_in_session where registration_status is null;
UPDATE tm_system_in_session SET registration_status=0 WHERE registration_status is null;


