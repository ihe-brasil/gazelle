UPDATE cmn_application_preference SET preference_name='active-bpel_server_url_not_used'  where preference_name='active-bpel_server_url';
UPDATE cmn_application_preference SET preference_name='application_build_time_not_used'  where preference_name='application_build_time';
UPDATE cmn_application_preference SET preference_name='application_profile_not_used'  where preference_name='application_profile';
UPDATE cmn_application_preference SET preference_name='cache-control_not_used'  where preference_name='cache-control';
UPDATE cmn_application_preference SET preference_name='cache_proxy_channel_status_not_used'  where preference_name='cache_proxy_channel_status';
UPDATE cmn_application_preference SET preference_name='certificates_generated_by_host_not_used'  where preference_name='certificates_generated_by_host';
UPDATE cmn_application_preference SET preference_name='content-security-policy_not_used'  where preference_name='content-security-policy';
UPDATE cmn_application_preference SET preference_name='content-security-policy-report-only_not_used'  where preference_name='content-security-policy-report-only';
UPDATE cmn_application_preference SET preference_name='dicom3tools_path_not_used'  where preference_name='dicom3tools_path';
UPDATE cmn_application_preference SET preference_name='gazelle_master_model_application_not_used'  where preference_name='gazelle_master_model_application';
UPDATE cmn_application_preference SET preference_name='key_length_encryption_not_used'  where preference_name='key_length_encryption';
UPDATE cmn_application_preference SET preference_name='logo_link_url_not_used'  where preference_name='logo_link_url';
UPDATE cmn_application_preference SET preference_name='logo_url_not_used'  where preference_name='logo_url';
UPDATE cmn_application_preference SET preference_name='mb_detailed_result_xsl_not_used'  where preference_name='mb_detailed_result_xsl';
UPDATE cmn_application_preference SET preference_name='photo_basedir_not_used'  where preference_name='photo_basedir';
UPDATE cmn_application_preference SET preference_name='rules_db_not_used'  where preference_name='rules_db';
UPDATE cmn_application_preference SET preference_name='service_cda_mb_validator_not_used'  where preference_name='service_cda_mb_validator';
UPDATE cmn_application_preference SET preference_name='service_schematron_validator_not_used'  where preference_name='service_schematron_validator';
UPDATE cmn_application_preference SET preference_name='service_xdsevs_not_used'  where preference_name='service_xdsevs';
UPDATE cmn_application_preference SET preference_name='service_xdw_validator_not_used'  where preference_name='service_xdw_validator';
UPDATE cmn_application_preference SET preference_name='strict-transport-security_not_used'  where preference_name='strict-transport-security';
UPDATE cmn_application_preference SET preference_name='use_new_menu_not_used'  where preference_name='tbduse_new_menu';
UPDATE cmn_application_preference SET preference_name='xdw_detailed_result_xsl_not_used'  where preference_name='xdw_detailed_result_xsl';
UPDATE cmn_application_preference SET preference_name='x-frame-options_not_used'  where preference_name='x-frame-options';


UPDATE cmn_application_preference SET preference_value='private, no-cache, no-store, must-revalidate, max-age=0' where preference_name='Cache-Control';
UPDATE cmn_application_preference SET preference_value='max-age=31536000 ; includeSubDomains' where preference_name='Strict-Transport-Security';
UPDATE cmn_application_preference SET preference_value='SAMEORIGIN' where preference_name='X-Frame-Options';


