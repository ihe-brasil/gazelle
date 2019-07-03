package net.ihe.gazelle.tm.filter;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.tm.filter.modifier.TestMatchingTestingSession;
import net.ihe.gazelle.tm.filter.modifier.TestParticipantTested;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestEntity;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.SystemInSessionQuery;
import net.ihe.gazelle.tm.systems.model.SystemInSessionRegistrationStatus;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.users.model.Role;
import net.ihe.gazelle.users.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SystemInSessionFilter1 extends Filter<SystemInSession> implements QueryModifier<SystemInSession> {
    private static final long serialVersionUID = -6659360202016280689L;
    private static final Logger LOG = LoggerFactory.getLogger(SystemInSessionFilter1.class);

    public SystemInSessionFilter1(Map<String, String> requestParameterMap) {
        super(getHQLCriterions(), requestParameterMap);
        queryModifiers.add(this);
    }

    private static HQLCriterionsForFilter<SystemInSession> getHQLCriterions() {
        SystemInSessionQuery query = new SystemInSessionQuery();

        HQLCriterionsForFilter<SystemInSession> result = query.getHQLCriterionsForFilter();

        TMCriterions.addActiveTestingSession(result, "testing_session", query.testingSession());
        TMCriterions.addAIPOCriterions(result, query.system().systemActorProfiles().actorIntegrationProfileOption());
        TestEntity<Test> testPath = query.system().systemActorProfiles().aipo().testParticipants().roleInTest()
                .testRoles().test();

        result.addPath("test", testPath);
        result.addQueryModifierForCriterion("test", new TestMatchingTestingSession(testPath, "testing_session"));
        result.addQueryModifierForCriterion("test", new TestParticipantTested(query.system().systemActorProfiles()
                .aipo().testParticipants()));

        result.addPath("institution", query.system().institutionSystems().institution());
        result.addPath("demonstration", query.demonstrationsSystemInSession().demonstration().name());
        result.addPath("registrationStatus", query.registrationStatus());
        result.addPath("is_tool", query.system().isTool());
        query.getHQLCriterionsForFilter();

        return result;
    }

    @Override
    public void modifyQuery(HQLQueryBuilder<SystemInSession> queryBuilder, Map<String, Object> filterValuesApplied) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyQuery");
        }
        SystemInSessionQuery query = new SystemInSessionQuery(queryBuilder);

        queryBuilder.addRestriction(HQLRestrictions.or(query.system().systemsInSession().registrationStatus()
                .neqRestriction(SystemInSessionRegistrationStatus.DROPPED), query.system().systemsInSession()
                .registrationStatus().isNullRestriction()));
        if (!User.loggedInUser().hasRole(Role.getADMINISTRATOR_ROLE())) {
            queryBuilder.addRestriction(query.system().systemsInSession().testingSession()
                    .eqRestriction(TestingSession.getSelectedTestingSession()));
        }

    }
}
