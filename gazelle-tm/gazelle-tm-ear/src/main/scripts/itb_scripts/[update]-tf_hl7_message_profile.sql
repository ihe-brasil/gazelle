/* This update script updates the profile_content column in database table tf_hl7_message_profile with the  */
/* contents of the gazelle profile XML file(s). The script requires that the profile XML files must be present  */
/* in the gazelle_hl7_profiles folder of the postgres data directory. For example, in windows, the data directory  */
/* is C:\Program Files\PostgreSQL\9.1\data.  */


/* PIX Feed (Patient Admit) request */
/* load profile XML for domain = 'ITI', actor = PAT_IDENTITY_SRC, transaction = 'ITI-8', message_type = 'ADT_A01' and hl7_version = '2.3.1' */
UPDATE tf_hl7_message_profile profile
SET profile_content=(SELECT f_readfile('gazelle_hl7_profiles/ADT_A01.xml')) 
WHERE profile.domain_id = 2 and
	profile.actor_id = 45 and
	profile.transaction_id = 50 and
	profile.message_type = 'ADT_A01' and
	profile.hl7_version = '2.3.1';


/* PIX Feed (Patient Register) request  */
/* load profile XML for domain = 'ITI', actor = PAT_IDENTITY_SRC, transaction = 'ITI-8', message_type = 'ADT_A04' and hl7_version = '2.3.1' */
UPDATE tf_hl7_message_profile profile
SET profile_content=(SELECT f_readfile('gazelle_hl7_profiles/ADT_A04.xml')) 
WHERE profile.domain_id = 2 and
	profile.actor_id = 45 and
	profile.transaction_id = 50 and
	profile.message_type = 'ADT_A04' and
	profile.hl7_version = '2.3.1';


/* PIX Feed (Patient Pre-admit) request  */
/* load profile XML for domain = 'ITI', actor = PAT_IDENTITY_SRC, transaction = 'ITI-8', message_type = 'ADT_A05' and hl7_version = '2.3.1' */
UPDATE tf_hl7_message_profile profile
SET profile_content=(SELECT f_readfile('gazelle_hl7_profiles/ADT_A05.xml')) 
WHERE profile.domain_id = 2 and
	profile.actor_id = 45 and
	profile.transaction_id = 50 and
	profile.message_type = 'ADT_A05' and
	profile.hl7_version = '2.3.1';


/* PIX Feed (Patient Update) request  */
/* load profile XML for domain = 'ITI', actor = PAT_IDENTITY_SRC, transaction = 'ITI-8', message_type = 'ADT_A08' and hl7_version = '2.3.1' */
UPDATE tf_hl7_message_profile profile
SET profile_content=(SELECT f_readfile('gazelle_hl7_profiles/ADT_A08.xml')) 
WHERE profile.domain_id = 2 and
	profile.actor_id = 45 and
	profile.transaction_id = 50 and
	profile.message_type = 'ADT_A08' and
	profile.hl7_version = '2.3.1';


/* PIX Feed (Patient Merge) request  */
/* load profile XML for domain = 'ITI', actor = PAT_IDENTITY_SRC, transaction = 'ITI-8', message_type = 'ADT_A40' and hl7_version = '2.3.1' */
UPDATE tf_hl7_message_profile profile
SET profile_content=(SELECT f_readfile('gazelle_hl7_profiles/ADT_A40.xml')) 
WHERE profile.domain_id = 2 and
	profile.actor_id = 45 and
	profile.transaction_id = 50 and
	profile.message_type = 'ADT_A40' and
	profile.hl7_version = '2.3.1';


/* PIX Feed (Patient Admit) acknowledgement */
/* load profile XML for domain = 'ITI', actor = PAT_IDENTITY_X_REF_MGR, transaction = 'ITI-8', message_type = 'ACK_A01' and hl7_version = '2.3.1' */
UPDATE tf_hl7_message_profile profile
SET profile_content=(SELECT f_readfile('gazelle_hl7_profiles/ACK_A01.xml')) 
WHERE profile.domain_id = 2 and
	profile.actor_id = 27 and
	profile.transaction_id = 50 and
	profile.message_type = 'ACK_A01' and
	profile.hl7_version = '2.3.1';


/* PIX Feed (Patient Register) acknowledgement  */
/* load profile XML for domain = 'ITI', actor = PAT_IDENTITY_X_REF_MGR, transaction = 'ITI-8', message_type = 'ACK_A04' and hl7_version = '2.3.1' */
UPDATE tf_hl7_message_profile profile
SET profile_content=(SELECT f_readfile('gazelle_hl7_profiles/ACK_A04.xml')) 
WHERE profile.domain_id = 2 and
	profile.actor_id = 27 and
	profile.transaction_id = 50 and
	profile.message_type = 'ACK_A04' and
	profile.hl7_version = '2.3.1';


/* PIX Feed (Patient Pre-admit) acknowledgement */
/* load profile XML for domain = 'ITI', actor = PAT_IDENTITY_X_REF_MGR, transaction = 'ITI-8', message_type = 'ACK_A05' and hl7_version = '2.3.1' */
UPDATE tf_hl7_message_profile profile
SET profile_content=(SELECT f_readfile('gazelle_hl7_profiles/ACK_A05.xml')) 
WHERE profile.domain_id = 2 and
	profile.actor_id = 27 and
	profile.transaction_id = 50 and
	profile.message_type = 'ACK_A05' and
	profile.hl7_version = '2.3.1';


/* PIX Feed (Patient Update) acknowledgement */
/* load profile XML for domain = 'ITI', actor = PAT_IDENTITY_X_REF_MGR, transaction = 'ITI-8', message_type = 'ACK_A08' and hl7_version = '2.3.1' */
UPDATE tf_hl7_message_profile profile
SET profile_content=(SELECT f_readfile('gazelle_hl7_profiles/ACK_A08.xml')) 
WHERE profile.domain_id = 2 and
	profile.actor_id = 27 and
	profile.transaction_id = 50 and
	profile.message_type = 'ACK_A08' and
	profile.hl7_version = '2.3.1';


/* PIX Feed (Patient Merge) acknowledgement */
/* load profile XML for domain = 'ITI', actor = PAT_IDENTITY_X_REF_MGR, transaction = 'ITI-8', message_type = 'ACK_A40' and hl7_version = '2.3.1' */
UPDATE tf_hl7_message_profile profile
SET profile_content=(SELECT f_readfile('gazelle_hl7_profiles/ACK_A40.xml')) 
WHERE profile.domain_id = 2 and
	profile.actor_id = 27 and
	profile.transaction_id = 50 and
	profile.message_type = 'ACK_A40' and
	profile.hl7_version = '2.3.1';


/* PIX Update request */
/* load profile XML for domain = 'ITI', actor = PAT_IDENTITY_X_REF_MGR, transaction = 'ITI-10', message_type = 'ADT_A31' and hl7_version = '2.5' */
UPDATE tf_hl7_message_profile profile
SET profile_content=(SELECT f_readfile('gazelle_hl7_profiles/ADT_A31.xml')) 
WHERE profile.domain_id = 2 and
	profile.actor_id = 27 and
	profile.transaction_id = 52 and
	profile.message_type = 'ADT_A31' and
	profile.hl7_version = '2.5';


/* PIX Update acknowledgement */
/* load profile XML for domain = 'ITI', actor = PDC, transaction = 'ITI-30', message_type = 'ACK' and hl7_version = '2.5' */
UPDATE tf_hl7_message_profile profile
SET profile_content=(SELECT f_readfile('gazelle_hl7_profiles/ACK_ALL.xml')) 
WHERE profile.domain_id = 2 and
	profile.actor_id = 36 and
	profile.transaction_id = 91 and
	profile.message_type = 'ACK' and
	profile.hl7_version = '2.5';


/* PIX Query request */
/* load profile XML for domain = 'ITI', actor = PAT_IDENTITY_CONSUMER, transaction = 'ITI-9', message_type = 'QBP_Q23' and hl7_version = '2.5' */
UPDATE tf_hl7_message_profile profile
SET profile_content=(SELECT f_readfile('gazelle_hl7_profiles/QBP_Q23.xml')) 
WHERE profile.domain_id = 2 and
	profile.actor_id = 67 and
	profile.transaction_id = 51 and
	profile.message_type = 'QBP_Q23' and
	profile.hl7_version = '2.5';


/* PIX Query response */
/* load profile XML for domain = 'ITI', actor = PAT_IDENTITY_X_REF_MGR, transaction = 'ITI-9', message_type = 'RSP_K23' and hl7_version = '2.5' */
UPDATE tf_hl7_message_profile profile
SET profile_content=(SELECT f_readfile('gazelle_hl7_profiles/RSP_K23.xml')) 
WHERE profile.domain_id = 2 and
	profile.actor_id = 27 and
	profile.transaction_id = 51 and
	profile.message_type = 'RSP_K23' and
	profile.hl7_version = '2.5';


/* PDQ Query request */
/* load profile XML for domain = 'ITI', actor = PDC, transaction = 'ITI-21', message_type = 'QBP_Q22' and hl7_version = '2.5' */
UPDATE tf_hl7_message_profile profile
SET profile_content=(SELECT f_readfile('gazelle_hl7_profiles/QBP_Q22.xml')) 
WHERE profile.domain_id = 2 and
	profile.actor_id = 36 and
	profile.transaction_id = 70 and
	profile.message_type = 'QBP_Q22' and
	profile.hl7_version = '2.5';


/* PDQ Query response */
/* load profile XML for domain = 'ITI', actor = PDS, transaction = 'ITI-21', message_type = 'RSP_K22' and hl7_version = '2.5' */
UPDATE tf_hl7_message_profile profile
SET profile_content=(SELECT f_readfile('gazelle_hl7_profiles/RSP_K22.xml')) 
WHERE profile.domain_id = 2 and
	profile.actor_id = 37 and
	profile.transaction_id = 70 and
	profile.message_type = 'RSP_K22' and
	profile.hl7_version = '2.5';


	
	