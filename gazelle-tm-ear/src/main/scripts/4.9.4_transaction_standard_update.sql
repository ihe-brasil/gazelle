alter table tf_transaction_standard_aud drop CONSTRAINT tf_transaction_standard_aud_standard_id_transaction_id_key;
-- Don't forget to check the foreign key tf_standard et tf_transaction and replace it by your's
alter table tf_transaction_standard_aud drop CONSTRAINT fk6302beab2fa28b00;
alter table tf_transaction_standard_aud drop CONSTRAINT fk6302beab60fd5c54;

alter table tf_transaction_standard_aud add CONSTRAINT tf_transaction_standard_aud_pkey PRIMARY KEY (rev, standard_id, transaction_id);
alter table tf_standard_aud drop constraint tf_standard_aud_pkey;
alter table tf_standard_aud add constraint tf_standard_aud_pkey primary key (rev);
alter table tf_standard_aud drop constraint tf_standard_aud_keyword_key;