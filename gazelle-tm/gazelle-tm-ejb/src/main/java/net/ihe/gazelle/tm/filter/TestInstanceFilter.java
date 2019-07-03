package net.ihe.gazelle.tm.filter;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.HQLRestriction;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.tm.filter.modifier.TestMatchingTestingSession;
import net.ihe.gazelle.tm.filter.valueprovider.InstitutionFixer;
import net.ihe.gazelle.tm.filter.valueprovider.TestingSessionFilter;
import net.ihe.gazelle.tm.filter.valueprovider.TestingSessionInitiator;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestType;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceQuery;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.users.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class TestInstanceFilter extends Filter<TestInstance> {

    private static final long serialVersionUID = 7913366350004996437L;
    private static final Logger LOG = LoggerFactory.getLogger(TestInstanceFilter.class);
    private FilterDataModel<?>[] dataModels = null;

    // monitor worklist mode (matching monitor in session tests)
    private boolean monitorWorklist;
    // shows test instances :
    // true : any
    // false: not claimed or claimed by monitorWorklistUser
    private boolean monitorWorklistAny;
    private boolean monitorWorklistClaimed;
    private User monitorWorklistUser;
    private boolean interoperabilityTestsOnly;

    public TestInstanceFilter(boolean monitorWorkList, Map<String, String> requestParameterMap, boolean interoperabilityTestsOnly) {
        super(getHQLCriterions(monitorWorkList, interoperabilityTestsOnly), requestParameterMap);
        monitorWorklist = false;
        monitorWorklistAny = false;
        this.interoperabilityTestsOnly = interoperabilityTestsOnly;
    }

    private static HQLCriterionsForFilter<TestInstance> getHQLCriterions(boolean monitorWorkList, boolean interoperabilityTestsOnly) {
        TestInstanceQuery query = new TestInstanceQuery();

        HQLCriterionsForFilter<TestInstance> result = query.getHQLCriterionsForFilter();

        // Only tests instance matching testing session filters
        result.addQueryModifier(new TestMatchingTestingSession(query.test(), "testingSession"));

        result.addPath("testingSession", query.testingSession(), TestingSessionInitiator.INSTANCE, TestingSession.getSelectedTestingSession());
        result.addQueryModifierForCriterion("testingSession", new TestingSessionFilter(query.testingSession()));

        TMCriterions.addAIPOCriterionsUsingTestInstance(result, query, "testingSession");

        result.addPath("test", query.test());

        result.addPath("institution", query.testInstanceParticipants().systemInSessionUser().systemInSession().system()
                .institutionSystems().institution(), null, InstitutionFixer.INSTANCE);

        result.addPath("system", query.testInstanceParticipants().systemInSessionUser().systemInSession().system()
                .keyword());

        result.addPath("monitor", query.testingSession().monitors().user());

        if (interoperabilityTestsOnly) {
            result.addPath("testTypeReal", query.test().testType(), TestType.getTYPE_INTEROPERABILITY(), TestType.getTYPE_INTEROPERABILITY());
        } else {
            result.addPath("testTypeReal", query.test().testType());
            result.addQueryModifier(new QueryModifier<TestInstance>() {
                /**
                 *
                 */
                private static final long serialVersionUID = -7236147657301191982L;

                @Override
                public void modifyQuery(HQLQueryBuilder<TestInstance> queryBuilder, Map<String, Object> filterValuesApplied) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("modifyQuery");
                    }
                    TestInstanceQuery testInstanceQuery = new TestInstanceQuery(queryBuilder);
                    queryBuilder.addRestriction(testInstanceQuery.test().testType().neqRestriction(TestType.getTYPE_INTEROPERABILITY()));
                }
            });
        }

        if (!monitorWorkList) {
            result.addPath("monitorInSession", query.monitorInSession().user());

        }

        return result;
    }

    @Override
    public void modified() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modified");
        }
        super.modified();
        if (dataModels != null) {
            for (FilterDataModel<?> dataModel : dataModels) {
                dataModel.resetCache();
            }
        }
        Object object = getFilterValues().get("monitor");
        if (object instanceof User) {
            this.monitorWorklistUser = (User) object;
        } else {
            this.monitorWorklistUser = null;
        }
    }

    public void setDataModels(FilterDataModel<?>... dataModels) {
        this.dataModels = dataModels;
    }

    public void setMonitorWorklist(boolean monitorWorklist) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMonitorWorklist");
        }
        this.monitorWorklist = monitorWorklist;
    }

    public void setMonitorWorklistUser(User monitorWorklistUser) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMonitorWorklistUser");
        }
        this.monitorWorklistUser = monitorWorklistUser;
        modified();
    }

    public boolean isMonitorWorklistAny() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isMonitorWorklistAny");
        }
        return monitorWorklistAny;
    }

    public void setMonitorWorklistAny(boolean monitorWorklistAny) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMonitorWorklistAny");
        }
        this.monitorWorklistAny = monitorWorklistAny;
        modified();
    }

    public boolean isMonitorWorklistClaimed() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isMonitorWorklistClaimed");
        }
        return monitorWorklistClaimed;
    }

    public void setMonitorWorklistClaimed(boolean monitorWorklistClaimed) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMonitorWorklistClaimed");
        }
        this.monitorWorklistClaimed = monitorWorklistClaimed;
        modified();
    }

    @Override
    public void appendHibernateFilters(HQLQueryBuilder<TestInstance> queryBuilder, String excludedKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("appendHibernateFilters");
        }
        super.appendHibernateFilters(queryBuilder, excludedKeyword);

        if (!interoperabilityTestsOnly) {
            TestInstanceQuery testInstanceQuery = new TestInstanceQuery(queryBuilder);
            queryBuilder.addRestriction(testInstanceQuery.test().testType().neqRestriction(TestType.getTYPE_INTEROPERABILITY()));
        }
        if (monitorWorklist) {
            TestInstanceQuery testInstanceQuery = new TestInstanceQuery(queryBuilder);
            if (monitorWorklistUser != null) {
                HQLRestriction monitorClaimedTest = testInstanceQuery.monitorInSession().user().id()
                        .eqRestriction(monitorWorklistUser.getId());
                HQLRestriction userTests = null;
                HQLRestriction inMonitorAssignment = testInstanceQuery.test().monitorsInSession().user().id()
                        .eqRestriction(monitorWorklistUser.getId());
                Object testingSessionObject = getRealFilterValue("testingSession");
                if (testingSessionObject != null) {
                    TestingSession testingSession = (TestingSession) testingSessionObject;
                    HQLRestriction inMonitorAssignmentForSession = testInstanceQuery.test().monitorsInSession()
                            .testingSession().id().eqRestriction(testingSession.getId());
                    userTests = HQLRestrictions.and(inMonitorAssignment, inMonitorAssignmentForSession);
                } else {
                    userTests = inMonitorAssignment;
                }
                if (monitorWorklistClaimed) {
                    queryBuilder.addRestriction(monitorClaimedTest);
                } else {
                    if (monitorWorklistAny) {
                        queryBuilder.addRestriction(HQLRestrictions.or(userTests, monitorClaimedTest));
                    } else {
                        HQLRestriction monitorIsNull = testInstanceQuery.monitorInSession().isNullRestriction();
                        queryBuilder.addRestriction(HQLRestrictions.or(monitorClaimedTest,
                                HQLRestrictions.and(userTests, monitorIsNull)));
                    }
                }
            } else if (monitorWorklistClaimed) {
                Object testingSessionObject = getRealFilterValue("testingSession");
                if (testingSessionObject != null) {
                    TestingSession testingSession = (TestingSession) testingSessionObject;
                    HQLRestriction inMonitorAssignmentForSession = testInstanceQuery.test().monitorsInSession()
                            .testingSession().id().eqRestriction(testingSession.getId());
                    HQLRestriction monitorIsNotNull = testInstanceQuery.monitorInSession().user().id().isNotNullRestriction();
                    queryBuilder.addRestriction(HQLRestrictions.and(inMonitorAssignmentForSession, monitorIsNotNull));
                }
            }
        }
    }
}
