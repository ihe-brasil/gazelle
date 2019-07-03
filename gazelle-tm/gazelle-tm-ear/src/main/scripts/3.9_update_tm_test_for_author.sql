UPDATE tm_test SET author='';

UPDATE tm_test SET author=b.last_modifier_id FROM (SELECT a.id, a.rev, a.revtype, a.last_modifier_id from tm_test_aud a, tm_test b where a.revtype=0 and b.keyword=a.keyword and a.id = b.id) AS b where tm_test.id=b.id;

UPDATE tm_test SET author=b.last_modifier_id FROM (SELECT a.id,min(a.rev), a.revtype, a.last_modifier_id from tm_test_aud a, tm_test b where a.revtype=1 and b.keyword=a.keyword and a.id = b.id GROUP by a.id,a.revtype,a.last_modifier_id) AS b where tm_test.id=b.id and tm_test.author = '';


