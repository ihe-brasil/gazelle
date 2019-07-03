INSERT INTO pat_patient (id, dtype,
                         first_name,
                         last_name,
                         alternate_first_name,
                         alternate_last_name,
                         gender_code,
                         country_code,
                         second_name,
                         alternate_second_name,
                         third_name,
                         alternate_third_name,
                         mother_maiden_name,
                         alternate_mothers_maiden_name,
                         character_set,
                         creation_date,
                         date_of_birth,
                         race_code,
                         religion_code,
                         creator_id,
                         editable,
                         date_of_death,
                         derived_from,
                        test_keyword)
  SELECT
    id,
    'tm_patient',
    first_name,
    last_name,
    alternate_first_name,
    alternate_last_name,
    gender_code,
    country_code,
    second_name,
    alternate_second_name,
    third_name,
    alternate_third_name,
    mother_maiden_name,
    alternate_mothers_maiden_name,
    character_set,
    creation_date,
    date_of_birth,
    race_code,
    religion_code,
    creator_id,
    editable,
    date_of_death,
    derived_from,
    test_keyword
  FROM tm_patient;

INSERT INTO pat_patient_address (id, address_type, is_main_address, country_code, street, street_number, zip_code, city, address_line, state, patient_id)
  SELECT
    nextval('pat_patient_address_id_seq'),
    6,
    TRUE,
    country_code,
    street,
    NULL,
    zip_code,
    city,
    street,
    state,
    id
  FROM tm_patient;


ALTER TABLE tm_patient_patient_identifier DROP CONSTRAINT fk65bd03239e18473e;

DROP TABLE tm_patient;