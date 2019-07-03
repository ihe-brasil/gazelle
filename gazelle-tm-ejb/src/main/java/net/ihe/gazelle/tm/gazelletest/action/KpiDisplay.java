package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.list.GazelleListDataModel;
import net.ihe.gazelle.dao.GazelleDAO;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.beans.HQLStatistic;
import net.ihe.gazelle.hql.beans.HQLStatisticItem;
import net.ihe.gazelle.hql.paths.HQLSafePathBasic;
import net.ihe.gazelle.tf.model.Domain;
import net.ihe.gazelle.tf.model.IntegrationProfile;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestParticipants;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestRoles;
import net.ihe.gazelle.tm.gazelletest.model.instance.MonitorInSessionQuery;
import net.ihe.gazelle.tm.gazelletest.model.instance.Status;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceQuery;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.SystemInSessionQuery;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.users.model.User;
import net.ihe.gazelle.users.model.UserQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class KpiDisplay<T> {

    private static final Logger LOG = LoggerFactory.getLogger(KpiDisplay.class);
    protected List<Test> testsDisplay;
    protected List<TestInstance> testInstancesDisplay;
    protected List<TestInstance> testInstancesInSessionDisplay;
    protected List<User> monitorsDisplay;
    protected List<SystemInSession> systemsDisplay;

    protected Filter<T> filter;

    public static <E> List<E> removeDuplicate(List<E> list) {
        Set<E> testParticipants = new HashSet<>();
        testParticipants.addAll(list);
        list.clear();
        list.addAll(testParticipants);
        return list;
    }

    public Filter<T> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        return filter;
    }

    public GazelleListDataModel<HQLStatistic<T>> getElements() {
        List<HQLStatistic<T>> res = filter.getListWithStatistics();
        GazelleListDataModel<HQLStatistic<T>> dm = new GazelleListDataModel<HQLStatistic<T>>(res);
        return dm;
    }

    public List<Test> getTestsDisplay() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestsDisplay");
        }
        return testsDisplay;
    }

    public List<TestInstance> getTestInstancesDisplay() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestInstancesDisplay");
        }
        return testInstancesDisplay;
    }

    public List<TestInstance> getTestInstancesInSessionDisplay() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestInstancesInSessionDisplay");
        }
        return testInstancesInSessionDisplay;
    }

    public void setTestInstancesInSessionDisplay(List<TestInstance> testInstancesInSessionDisplay) {
        this.testInstancesInSessionDisplay = testInstancesInSessionDisplay;
    }

    public List<User> getMonitorsDisplay() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMonitorsDisplay");
        }
        return monitorsDisplay;
    }

    public List<SystemInSession> getSystemsDisplay() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemsDisplay");
        }
        return systemsDisplay;
    }

    public void searchMonitors(HQLStatistic<T> item, int statisticItemIndex) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("searchMonitors");
        }
        List list = filter.getListWithStatisticsItems(item, statisticItemIndex);
        List<Integer> l = new ArrayList<>(list);
        if (l.get(0) == null) {
            l.remove(0);
        }
        UserQuery q = new UserQuery();
        q.id().in(l);
        monitorsDisplay = q.getList();
    }

    public void searchTests(HQLStatistic<T> item, int statisticItemIndex) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("searchTests");
        }
        List list = filter.getListWithStatisticsItems(item, statisticItemIndex);
        HQLQueryBuilder<Test> builder = new HQLQueryBuilder<Test>(Test.class);
        builder.addIn("id", list);
        testsDisplay = builder.getList();
    }

    public void searchTestInstances(HQLStatistic<T> item, int statisticItemIndex) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("searchTestInstances");
        }
        List list = filter.getListWithStatisticsItems(item, statisticItemIndex);
        HQLQueryBuilder<TestInstance> builder = new HQLQueryBuilder<TestInstance>(TestInstance.class);
        builder.addIn("id", list);
        testInstancesDisplay = builder.getList();
    }

    public void searchSystemInSessions(HQLStatistic<T> item, int statisticItemIndex) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("searchSystemInSessions");
        }
        List list = filter.getListWithStatisticsItems(item, statisticItemIndex);
        SystemInSessionQuery q = new SystemInSessionQuery();
        q.id().in(list);
        q.system().keyword().order(true);
        systemsDisplay = q.getListDistinctOrdered();
    }

    public void searchMonitorsInSession(HQLStatistic<T> item, int statisticItemIndex) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("searchMonitorsInSession");
        }
        List list = filter.getListWithStatisticsItems(item, statisticItemIndex);
        List<Integer> l = new ArrayList<>(list);
        if (l.size() > 0 && (l.get(0) == null)) {
            l.remove(0);
        }
        MonitorInSessionQuery q = new MonitorInSessionQuery();
        q.testingSession().id().eq(((TestingSession) filter.getFilterValues().get("testing_session")).getId());
        q.user().id().in(l);
        q.user().username().order(true);
        monitorsDisplay = q.user().getListDistinctOrdered();
    }

    public void searchTestInstancesInSession(HQLStatistic<T> item, int statisticItemIndex) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("searchTestInstancesInSession");
        }
        List list = filter.getListWithStatisticsItems(item, statisticItemIndex);
        List<Integer> l = new ArrayList<>(list);
        if (l.size() > 0 && (l.get(0) == null)) {
            l.remove(0);
        }
        TestInstanceQuery q = new TestInstanceQuery();
        q.testingSession().id().eq(((TestingSession) filter.getFilterValues().get("testing_session")).getId());
        q.id().in(l);
        q.id().order(true);
        setTestInstancesInSessionDisplay(q.getListDistinctOrdered());
    }

    protected void addTiStatistics(List<HQLStatisticItem> statisticItems, HQLSafePathBasic<Integer> tiPath,
                                   HQLSafePathBasic<Status> tiStatusPath) {
        statisticItems.add(tiPath.statisticItem());
        statisticItems.add(tiPath.statisticItem(tiStatusPath.eqRestriction(Status.STARTED)));
        statisticItems.add(tiPath.statisticItem(tiStatusPath.eqRestriction(Status.COMPLETED)));
        statisticItems.add(tiPath.statisticItem(tiStatusPath.eqRestriction(Status.PAUSED)));
        statisticItems.add(tiPath.statisticItem(tiStatusPath.eqRestriction(Status.VERIFIED)));
        statisticItems.add(tiPath.statisticItem(tiStatusPath.eqRestriction(Status.ABORTED)));
        statisticItems.add(tiPath.statisticItem(tiStatusPath.eqRestriction(Status.PARTIALLY_VERIFIED)));
        statisticItems.add(tiPath.statisticItem(tiStatusPath.eqRestriction(Status.FAILED)));
        statisticItems.add(tiPath.statisticItem(tiStatusPath.eqRestriction(Status.CRITICAL)));
    }

    public List<TestParticipants> getProfilesForThisTest(Test test) {
        GazelleDAO gazelleDAO = new GazelleDAO();
        RoleInTestManager ritManager = new RoleInTestManager();
        List<TestParticipants> testParticipantsList = new ArrayList<>();
        List<TestRoles> testRolesListForATest = gazelleDAO.getTestRolesListForATest(test);
        for (TestRoles testRoles : testRolesListForATest) {
            testParticipantsList.addAll(ritManager.getTestParticipantsForTestRole(testRoles));
        }
        testParticipantsList = removeDuplicate(testParticipantsList);
        return testParticipantsList;
    }

    public String getProfilesForThisTestToString(Test test) {
        List<TestParticipants> testParticipants = getProfilesForThisTest(test);
        List<IntegrationProfile> ips = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        for (TestParticipants testParticipant : testParticipants) {
            if (testParticipant.getTested() != null && testParticipant.getTested()) {
                ips.add(testParticipant.getActorIntegrationProfileOption().getActorIntegrationProfile().getIntegrationProfile());
            }
        }
        ips = removeDuplicate(ips);
        Collections.sort(ips);
        for (IntegrationProfile ip : ips) {
            sb.append(ip.getKeyword());
            sb.append(":");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public String getDomainsForThisTestToString(Test test) {
        List<TestParticipants> testParticipants = getProfilesForThisTest(test);
        List<Domain> domains = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (TestParticipants testParticipant : testParticipants) {
            if (testParticipant.getTested() != null && testParticipant.getTested()) {
                domains.addAll(testParticipant.getActorIntegrationProfileOption().getActorIntegrationProfile().getIntegrationProfile()
                        .getDomainsForDP());
            }
        }
        domains = removeDuplicate(domains);
        Collections.sort(domains);
        for (Domain domain : domains) {
            sb.append(domain.getKeyword());
            sb.append(":");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public String styleClassIfNoMonitor(HQLStatistic<T> item) {

        Integer nbSystems = item.getCounts()[1];
        Integer nbMonitors = item.getCounts()[11];
        String styleClass = "";
        if (nbSystems != null && nbSystems > 0 && nbMonitors != null && nbMonitors.equals(0)) {
            styleClass = "gzl-warn-row";
        }
        return styleClass;
    }

}
