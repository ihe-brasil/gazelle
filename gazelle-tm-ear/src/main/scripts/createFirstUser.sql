insert into usr_institution (id,keyword,name,url,institution_type_id) values (nextval('usr_institution_id_seq'),'IHE','IHE','http://gazelle.ihe.net',1);

insert into usr_users (id,email,password,username,institution_id,activated,is_institution_informations_entered,blocked,counter_logins,counter_failed_login_attempts,firstname) values (nextval('usr_users_id_seq'),'admin@gazelle.com','ab4f63f9ac65152575886860dde480a1','admin',(SELECT  id from usr_institution where keyword = 'IHE'),true,true,false,0,0,'admin');

insert into usr_user_role (user_id,role_id) values ((select id from usr_users where username = 'admin'), (select id from usr_role where name ='admin_role'));

