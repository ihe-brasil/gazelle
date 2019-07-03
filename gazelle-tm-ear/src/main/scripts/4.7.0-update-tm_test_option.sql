UPDATE tm_test_option SET available=false;
UPDATE tm_test_option SET available=true where keyword='O';
UPDATE tm_test_option SET available=true where keyword='R';

alter table tm_test_option_aud add column available boolean;