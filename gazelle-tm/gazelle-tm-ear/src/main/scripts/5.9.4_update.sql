-- Show sequence diagram to true by default in user preferences.
UPDATE tm_user_preferences SET show_sequence_diagram=true where show_sequence_diagram is null;

-- Remove preferences referencing QRCodes and Monitor Application.
DELETE FROM cmn_application_preference WHERE preference_name = 'enable_qr_codes';
DELETE FROM cmn_application_preference WHERE preference_name = 'gazelle_monitor_app_url';