-- delete tm_system_actor_profiles with deleted actor id
DELETE FROM tm_system_actor_profiles
WHERE actor_integration_profile_option_id = 332;
DELETE FROM tm_system_actor_profiles
WHERE actor_integration_profile_option_id = 330;

-- Add system_type_id where missing
UPDATE tm_system
SET system_type_id = 4
WHERE system_type_id IS NULL;

-- Add integration_statement_status where missing
UPDATE tm_system
SET integration_statement_status = 0
WHERE integration_statement_status IS NULL;