package net.ihe.gazelle.tm.filter.modifier;

import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOptionEntity;
import net.ihe.gazelle.tf.model.IntegrationProfile;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestEntity;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestParticipantsAttributes;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestType;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class TestMatchingTestingSession implements QueryModifier {

    private static final long serialVersionUID = 4314266412727863555L;
    private static final Logger LOG = LoggerFactory.getLogger(TestMatchingTestingSession.class);

    private String testPath;
    private String testingSessionKey;

    public TestMatchingTestingSession(TestEntity<Test> testEntity, String testingSessionKey) {
        super();
        this.testPath = testEntity.getPath();
        this.testingSessionKey = testingSessionKey;
    }

    @Override
    public void modifyQuery(HQLQueryBuilder queryBuilder, Map filterValuesApplied) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyQuery");
        }
        TestEntity<Test> testEntity = new TestEntity<Test>(testPath, queryBuilder);

        List<TestType> testTypes = null;
        if (testingSessionKey != null) {
            Object testingSessionObject = filterValuesApplied.get(testingSessionKey);
            if ((testingSessionObject != null) && (testingSessionObject instanceof TestingSession)) {
                testTypes = ((TestingSession) testingSessionObject).getTestTypes();

                List<IntegrationProfile> integrationProfiles = ((TestingSession) testingSessionObject)
                        .getIntegrationProfilesUnsorted();

                TestParticipantsAttributes tpAttributes = testEntity.testRoles().roleInTest().testParticipantsList();
                ActorIntegrationProfileOptionEntity aipoAttributes = tpAttributes.actorIntegrationProfileOption();
                aipoAttributes.actorIntegrationProfile().integrationProfile().in(integrationProfiles);

            }
        }
        if (testTypes == null) {
            testTypes = TestType.getTestTypesWithoutMESA();
        }

        testEntity.testType().in(testTypes);
    }
}
