package net.ihe.gazelle.tm.filter.modifier;

import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestParticipants;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestParticipantsAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class TestParticipantTested implements QueryModifier {

    private static final long serialVersionUID = -6639873388238588215L;
    private static final Logger LOG = LoggerFactory.getLogger(TestParticipantTested.class);
    private String tpPath;

    public TestParticipantTested(TestParticipantsAttributes<TestParticipants> testParticipantsEntity) {
        super();
        this.tpPath = testParticipantsEntity.getPath();
    }

    @Override
    public void modifyQuery(HQLQueryBuilder queryBuilder, Map filterValuesApplied) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyQuery");
        }
        TestParticipantsAttributes<TestParticipants> testParticipantsEntity = new TestParticipantsAttributes<TestParticipants>(
                tpPath, queryBuilder);
        testParticipantsEntity.tested().eq(true);
    }

}
