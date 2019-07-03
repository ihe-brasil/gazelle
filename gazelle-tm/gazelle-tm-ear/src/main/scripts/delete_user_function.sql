DROP FUNCTION delete_user_safe(text);
CREATE FUNCTION delete_user_safe(username_value text) RETURNS text
    LANGUAGE plpgsql
    AS $$
DECLARE
  selected_user_id INTEGER ;

BEGIN
  SELECT INTO selected_user_id id FROM usr_users WHERE username = username_value;
  IF selected_user_id IS NOT NULL THEN
    delete from usr_messages_usr_users WHERE users_id = selected_user_id;
    delete from usr_user_role WHERE user_id = selected_user_id;
    delete from tm_user_preferences WHERE user_id = selected_user_id;
    delete from usr_users WHERE id = selected_user_id;
END IF;

return selected_user_id ;
END;
$$;


DROP FUNCTION merge_user_safe(from_user_name text, to_user_name text);
CREATE FUNCTION merge_user_safe(from_user_name text, to_user_name text) RETURNS text
    LANGUAGE plpgsql
    AS $$
DECLARE
  from_user_id INTEGER ;
  from_user_logins INTEGER;
  to_user_id INTEGER ;
  tmp INTEGER ;
  role INTEGER;
  cnt_role INTEGER;

BEGIN
  SELECT INTO from_user_id id FROM usr_users WHERE username = from_user_name;
  SELECT INTO from_user_logins counter_logins FROM usr_users WHERE username = from_user_name;
  SELECT INTO to_user_id id FROM usr_users WHERE username = to_user_name;

  IF from_user_id IS NOT NULL THEN
    IF to_user_id IS NOT NULL THEN
      UPDATE usr_messages_usr_users set users_id = to_user_id WHERE users_id = from_user_id;
      FOR role IN SELECT role_id FROM usr_user_role WHERE user_id = from_user_id
      LOOP
        SELECT INTO cnt_role count(role_id) from usr_user_role WHERE user_id = to_user_id and role_id = role;
        IF cnt_role = 0 THEN
        INSERT INTO usr_user_role (user_id, role_id) VALUES(to_user_id, role );
        END IF ;
      END LOOP;
      UPDATE tm_system_in_session_user set user_id = to_user_id WHERE user_id = from_user_id;
      UPDATE tm_monitor_in_session set user_id = to_user_id WHERE user_id = from_user_id;
      UPDATE tm_system set owner_user_id = to_user_id WHERE owner_user_id = from_user_id;
      UPDATE tm_simulator set owner_user_id = to_user_id WHERE owner_user_id = from_user_id;
      UPDATE usr_users set counter_logins = counter_logins + from_user_logins WHERE id = to_user_id;
      UPDATE usr_users set email =  lower(email)  WHERE id = to_user_id;
      SELECT INTO tmp delete_user_safe(from_user_name);
    END IF;
  END IF;


return to_user_id ;
END;
$$;
