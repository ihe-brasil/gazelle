package net.ihe.gazelle.tm.filter;

import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOptionAttributes;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOptionEntity;
import net.ihe.gazelle.tm.application.action.ApplicationManager;
import net.ihe.gazelle.tm.filter.modifier.IntegrationProfilesFromTestingSession;
import net.ihe.gazelle.tm.filter.modifier.TestParticipantTested;
import net.ihe.gazelle.tm.filter.valueprovider.TestingSessionFilter;
import net.ihe.gazelle.tm.filter.valueprovider.TestingSessionFixer;
import net.ihe.gazelle.tm.filter.valueprovider.TestingSessionInitiator;
import net.ihe.gazelle.tm.gazelletest.model.definition.*;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceAttributes;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceParticipantsAttributes;
import net.ihe.gazelle.tm.systems.model.SystemActorProfiles;
import net.ihe.gazelle.tm.systems.model.SystemActorProfilesQuery;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.tm.systems.model.TestingSessionEntity;
import org.jboss.seam.security.Identity;

import java.util.Map;

public class TMCriterions {

    private TMCriterions() {
        super();
    }

    private static void addAIPOCriterionsBasic(HQLCriterionsForFilter result,
                                               ActorIntegrationProfileOptionAttributes aipoAttributes, String testingSessionKey) {
        result.addPath("actor", aipoAttributes.actorIntegrationProfile().actor());
        result.addPath("domain", aipoAttributes.actorIntegrationProfile().integrationProfile().domainsProfile()
                .domain());
        result.addPath("integrationProfile", aipoAttributes.actorIntegrationProfile().integrationProfile());
        result.addPath("integrationProfileOption", aipoAttributes.integrationProfileOption());
        if (testingSessionKey != null) {
            result.addQueryModifierForCriterion("domain", new IntegrationProfilesFromTestingSession(aipoAttributes
                    .actorIntegrationProfile().integrationProfile(), testingSessionKey));
            result.addQueryModifierForCriterion("integrationProfile", new IntegrationProfilesFromTestingSession(
                    aipoAttributes.actorIntegrationProfile().integrationProfile(), testingSessionKey));
        }
    }

    public static void addAIPOCriterions(HQLCriterionsForFilter result,
                                         ActorIntegrationProfileOptionAttributes aipoAttributes) {
        addAIPOCriterionsBasic(result, aipoAttributes, null);
        result.addPath("transaction", aipoAttributes.actorIntegrationProfile().profileLinks().transaction());
    }

    private static void addAIPOTestParticipantTested(HQLCriterionsForFilter result,
                                                     TestParticipantsAttributes tpAttributes) {
        TestParticipantTested testParticipantTested = new TestParticipantTested(tpAttributes);
        result.addQueryModifierForCriterion("actor", testParticipantTested);
        result.addQueryModifierForCriterion("domain", testParticipantTested);
        result.addQueryModifierForCriterion("integrationProfile", testParticipantTested);
        result.addQueryModifierForCriterion("integrationProfileOption", testParticipantTested);
    }

    public static void addAIPOCriterionsUsingTest(HQLCriterionsForFilter result, TestAttributes testAttributes,
                                                  String testingSessionKey) {
        TestParticipantsAttributes tpAttributes = testAttributes.testRoles().roleInTest().testParticipantsList();
        ActorIntegrationProfileOptionEntity aipoAttributes = tpAttributes.actorIntegrationProfileOption();

        addAIPOCriterionsBasic(result, aipoAttributes, testingSessionKey);

        result.addPath("transaction", testAttributes.testStepsList().transaction());

        addAIPOTestParticipantTested(result, tpAttributes);
    }

    public static void addAIPOCriterionsUsingTestInstance(HQLCriterionsForFilter result,
                                                          TestInstanceAttributes testInstanceAttributes, String testingSessionKey) {

        TestInstanceParticipantsAttributes testInstanceParticipants = testInstanceAttributes.testInstanceParticipants();

        ActorIntegrationProfileOptionEntity aipoAttributes = testInstanceParticipants.actorIntegrationProfileOption();
        addAIPOCriterionsBasic(result, aipoAttributes, testingSessionKey);
        result.addPath("transaction", testInstanceAttributes.test().testStepsList().transaction());

        TestParticipantsAttributes tpAttributes = testInstanceAttributes.testInstanceParticipants().roleInTest()
                .testParticipantsList();
        addAIPOTestParticipantTested(result, tpAttributes);

    }

    public static void addTestCriterions(HQLCriterionsForFilter result, TestAttributes testAttributes,
                                         boolean onlyConnectathon) {
        result.addPath("lastModifier", testAttributes.lastModifierId());
        result.addPath("testPeerType", testAttributes.testPeerType());
        if (ApplicationManager.instance().isTestsDisplayAsNotLoggedIn() && !Identity.instance().isLoggedIn()) {
            result.addPath("testStatus", testAttributes.testStatus(), TestStatus.getSTATUS_READY(), TestStatus.getSTATUS_READY());
        } else {
            result.addPath("testStatus", testAttributes.testStatus(), TestStatus.getSTATUS_READY());
        }
        if (onlyConnectathon) {
            TestQuery query = new TestQuery();
            query.testType().eq(TestType.getTYPE_CONNECTATHON());
            query.setMaxResults(1);
            Test uniqueResult = query.getUniqueResult();

            query = new TestQuery();
            int res = query.testType().getListDistinct().size();
            if (uniqueResult != null) {
                result.addPath("testType", testAttributes.testType(), TestType.getTYPE_CONNECTATHON());
            } else if (res == 1) {
                result.addPath("testType", testAttributes.testType(), query.testType().getListDistinct().get(0));
            } else {
                result.addPath("testType", testAttributes.testType());
            }
        } else {
            result.addPath("testType", testAttributes.testType());
        }
        result.addPath("testVersion", testAttributes.version());
    }

    public static void addTestCriterion(HQLCriterionsForFilter result, TestAttributes testAttributes,
                                        boolean onlyConnectathon) {
        result.addPath("testValidated", testAttributes.validated());
        result.addPath("testAuthor", testAttributes.author());
    }

    public static void addTestingSession(HQLCriterionsForFilter result, String keyword,
                                         TestingSessionEntity<TestingSession> testingSession) {
        result.addPath(keyword, testingSession, TestingSessionInitiator.INSTANCE, TestingSessionFixer.INSTANCE);
        result.addQueryModifierForCriterion(keyword, new TestingSessionFilter(testingSession));
    }

    public static void addActiveTestingSession(HQLCriterionsForFilter result, String keyword,
                                               TestingSessionEntity<TestingSession> testingSession) {
        result.addPath(keyword, testingSession, TestingSessionInitiator.INSTANCE, TestingSessionFixer.INSTANCE);
        result.addQueryModifierForCriterion(keyword, new TestingSessionFilter(testingSession));
        result.addQueryModifier(new QueryModifier<SystemActorProfiles>() {
            @Override
            public void modifyQuery(HQLQueryBuilder<SystemActorProfiles> hqlQueryBuilder, Map<String, Object> map) {
                SystemActorProfilesQuery sapQuery = new SystemActorProfilesQuery();
                hqlQueryBuilder.addRestriction(sapQuery.system()
                        .systemsInSession().testingSession().hiddenSession().eqRestriction(false));
            }
        });
    }

}
