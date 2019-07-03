-- Run the following command on GMM in order to extract the list of insert to execute on the target installation
-- It is best to perform that task once the tests have been imported on the target machine

SELECT 'INSERT INTO tf_hl7_message_profile (id, last_changed, last_modifier_id, hl7_version, message_order_control_code, message_type, profile_content, profile_oid, profile_type, actor_id, domain_id, transaction_id) VALUES ('
|| 'nextval(''tf_hl7_message_profile_id_seq'') , now(), '''
|| last_modifier_id
|| ''', '''
|| hl7_version
|| ''', '''
|| message_order_control_code
|| ''', '''
|| message_type
|| ''', NULL ,'''
|| profile_oid
|| ''', '''
|| profile_type
|| ''', get_actor_id('''
|| get_actor_keyword(actor_id)
|| '''), get_domain_id('''
|| get_domain_keyword(domain_id)
|| '''), get_transaction_id('''
|| get_transaction_keyword(transaction_id)
|| '''));' from tf_hl7_message_profile;

-- Use the following command to update the entries when new actors have been added to the target system

SELECT 'UPDATE tf_hl7_message_profile set actor_id = get_actor_id('''
|| get_actor_keyword(actor_id)
|| '''), domain_id = get_domain_id('''
|| get_domain_keyword(domain_id)
|| '''), transaction_id = get_transaction_id('''
|| get_transaction_keyword(transaction_id)
|| ''') where profile_oid = '''
|| profile_oid
|| ''';' from tf_hl7_message_profile;


