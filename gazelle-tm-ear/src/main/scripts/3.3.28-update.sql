ALTER TABLE tm_user_comment DROP COLUMN user_id;
ALTER TABLE tm_user_comment ADD COLUMN user_id varchar(32);
UPDATE tm_user_comment SET user_id = last_modifier_id;
