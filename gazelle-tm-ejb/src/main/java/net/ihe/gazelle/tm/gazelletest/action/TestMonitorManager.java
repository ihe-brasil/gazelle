package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.filter.TMCriterions;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestQuery;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestStatus;
import net.ihe.gazelle.tm.gazelletest.model.instance.MonitorInSession;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author abderrazek boufahja
 */
@Name("testMonitorManager")
@Scope(ScopeType.PAGE)
@Synchronized(timeout = 10000)
public class TestMonitorManager implements Serializable, QueryModifier<Test> {

    private static final long serialVersionUID = -7774525146362549777L;
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TestMonitorManager.class);

    private List<Test> testsSelected;

    private List<MonitorInSession> listAllMonitorsForSelectedSession;

    private List<MonitorInSession> listMonitorsForSelectedTests;

    private List<MonitorInSession> listMonitorsForSelectedTestsByAdmin;

    private TestingSession testingSession;

    private Filter<Test> filter;

    private FilterDataModel<Test> tests;

    // getter && settter ////////////////////////////////////////////////////

    public List<MonitorInSession> getListMonitorsForSelectedTestsByAdmin() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListMonitorsForSelectedTestsByAdmin");
        }
        return listMonitorsForSelectedTestsByAdmin;
    }

    public void setListMonitorsForSelectedTestsByAdmin(List<MonitorInSession> listMonitorsForSelectedTestsByAdmin) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setListMonitorsForSelectedTestsByAdmin");
        }
        this.listMonitorsForSelectedTestsByAdmin = listMonitorsForSelectedTestsByAdmin;
    }

    public List<MonitorInSession> getListMonitorsForSelectedTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListMonitorsForSelectedTests");
        }
        return listMonitorsForSelectedTests;
    }

    public void setListMonitorsForSelectedTests(List<MonitorInSession> listMonitorsForSelectedTests) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setListMonitorsForSelectedTests");
        }
        this.listMonitorsForSelectedTests = listMonitorsForSelectedTests;
    }

    public List<MonitorInSession> getListAllMonitorsForSelectedSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListAllMonitorsForSelectedSession");
        }
        return listAllMonitorsForSelectedSession;
    }

    public void setListAllMonitorsForSelectedSession(List<MonitorInSession> listAllMonitorsForSelectedSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setListAllMonitorsForSelectedSession");
        }
        this.listAllMonitorsForSelectedSession = listAllMonitorsForSelectedSession;
    }

    public List<Test> getTestsSelected() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestsSelected");
        }
        if (this.testsSelected == null) {
            this.testsSelected = new ArrayList<Test>();
        }
        return testsSelected;
    }

    public void setTestsSelected(List<Test> testsSelected) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestsSelected");
        }
        this.testsSelected = testsSelected;
    }

    public TestingSession getTestingSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestingSession");
        }
        return testingSession;
    }

    public void setTestingSession(TestingSession testingSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestingSession");
        }
        this.testingSession = testingSession;
    }

    public Filter<Test> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        return filter;
    }

    public void setFilter(Filter<Test> filter) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFilter");
        }
        this.filter = filter;
    }

    public FilterDataModel<Test> getTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTests");
        }
        return tests;
    }

    public void setTests(FilterDataModel<Test> tests) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTests");
        }
        this.tests = tests;
    }

    // methods ////////////////////////////////////////////////////

    @Create
    public void reset() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("reset");
        }
        this.testingSession = TestingSession.getSelectedTestingSession();
        this.listAllMonitorsForSelectedSession = MonitorInSession
                .getAllActivatedMonitorsForATestingSession(testingSession);
        this.listMonitorsForSelectedTests = new ArrayList<MonitorInSession>();
        this.testsSelected = new ArrayList<Test>();

        filter = new Filter<Test>(getCriterions());
        tests = new FilterDataModel<Test>(filter) {
            @Override
            protected Object getId(Test t) {
                // TODO Auto-generated method stub
                return t.getId();
            }
        };
    }

    @Override
    public void modifyQuery(HQLQueryBuilder<Test> queryBuilder, Map<String, Object> filterValuesApplied) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyQuery");
        }
        TestQuery query = new TestQuery(queryBuilder);
        query.testRoles().roleInTest().testParticipantsList().actorIntegrationProfileOption().actorIntegrationProfile()
                .integrationProfile().in(testingSession.getIntegrationProfilesUnsorted());
        query.testType().in(testingSession.getTestTypes());

    }

    private List<Test> getAllDisplayedTests() {
        return (List<Test>) getTests().getAllItems(FacesContext.getCurrentInstance());
    }

    private HQLCriterionsForFilter<Test> getCriterions() {
        TestQuery query = new TestQuery();
        HQLCriterionsForFilter<Test> criterions = query.getHQLCriterionsForFilter();
        criterions.addQueryModifier(this);
        TMCriterions.addAIPOCriterionsUsingTest(criterions, query, null);
        criterions.addPath("monitor", query.monitorsInSession().user());
        criterions.addQueryModifierForCriterion("monitor", new QueryModifier<Test>() {

            /**
             *
             */
            private static final long serialVersionUID = -6065569847211349424L;

            @Override
            public void modifyQuery(HQLQueryBuilder<Test> queryBuilder, Map<String, Object> filterValuesApplied) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("modifyQuery");
                }
                TestQuery query = new TestQuery(queryBuilder);
                query.monitorsInSession().testingSession().eq(testingSession);
            }
        });
        criterions.addPath("test", query.testRoles().test());

        criterions.addPath("testPeerType", query.testRoles().test().testPeerType());
        criterions.addPath("testStatus", query.testRoles().test().testStatus(), TestStatus.getSTATUS_READY());
        criterions.addPath("testType", query.testRoles().test().testType());

        return criterions;
    }

    public boolean testsSelectedContain(Test test) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("testsSelectedContain");
        }
        if (this.getTestsSelected().contains(test)) {
            return true;
        }
        return false;
    }

    public void addTestToSelection(Test test) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addTestToSelection");
        }
        this.getTestsSelected().add(test);
    }

    public void deleteTestFromSelection(Test test) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteTestFromSelection");
        }
        this.getTestsSelected().remove(test);
    }

    public void addTestToSelectionAndUpdateListMonitor(Test test) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addTestToSelectionAndUpdateListMonitor");
        }
        this.addTestToSelection(test);
        this.updateListMonitorForSelectedTests();
    }

    public void deleteTestFromSelectionAndUpdateListMonitor(Test test) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteTestFromSelectionAndUpdateListMonitor");
        }
        this.deleteTestFromSelection(test);
        this.updateListMonitorForSelectedTests();
    }

    private void updateListMonitorForSelectedTests() {
        this.listMonitorsForSelectedTests = new ArrayList<MonitorInSession>();
        this.listMonitorsForSelectedTestsByAdmin = new ArrayList<MonitorInSession>();
        for (MonitorInSession mis : this.listAllMonitorsForSelectedSession) {
            if (this.testsSelected.size() > 0) {
                boolean isWellMonitor = true;
                for (Test test : this.testsSelected) {
                    if (!mis.getTestList().contains(test)) {
                        isWellMonitor = false;
                        break;
                    }
                }
                if (isWellMonitor) {
                    this.listMonitorsForSelectedTests.add(mis);
                }
            }
        }
        this.listMonitorsForSelectedTestsByAdmin.addAll(this.listMonitorsForSelectedTests);
        Collections.sort(this.listMonitorsForSelectedTests);
        Collections.sort(this.listMonitorsForSelectedTestsByAdmin);
    }

    public void saveAddedMonitorsToTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveAddedMonitorsToTests");
        }
        StringBuffer madded = new StringBuffer();
        StringBuffer mremoved = new StringBuffer();
        for (MonitorInSession mis : this.listMonitorsForSelectedTestsByAdmin) {
            if (!this.listMonitorsForSelectedTests.contains(mis)) {
                List<Test> ltmis = mis.getTestList();
                if (ltmis == null) {
                    ltmis = new ArrayList<Test>();
                }
                ltmis.removeAll(this.testsSelected);
                ltmis.addAll(this.testsSelected);
                mis.setTestList(ltmis);
                EntityManager em = EntityManagerService.provideEntityManager();
                mis = em.merge(mis);
                em.flush();
                this.listMonitorsForSelectedTests.add(mis);
                if (madded.length() == 0) {
                    madded.append(mis.getUser().getUserNameAndSurName());
                } else {
                    madded.append(", ").append(mis.getUser().getUserNameAndSurName());
                }
            }
        }
        for (MonitorInSession mis : this.listMonitorsForSelectedTests) {
            if (!this.listMonitorsForSelectedTestsByAdmin.contains(mis)) {
                List<Test> ltmis = mis.getTestList();
                ltmis.removeAll(testsSelected);
                mis.setTestList(ltmis);
                EntityManager em = EntityManagerService.provideEntityManager();
                mis = em.merge(mis);
                em.flush();
                if (mremoved.length() == 0) {
                    mremoved.append(mis.getUser().getUserNameAndSurName());
                } else {
                    mremoved.append(", ").append(mis.getUser().getUserNameAndSurName());
                }
            }
        }
        Collections.sort(this.listMonitorsForSelectedTestsByAdmin);
        this.listMonitorsForSelectedTests = new ArrayList<MonitorInSession>();
        this.listMonitorsForSelectedTests.addAll(listMonitorsForSelectedTestsByAdmin);

        if (!madded.toString().equals("")) {
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "These monitors were added for the selected tests : " + madded);
        }
        if (!mremoved.toString().equals("")) {
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "These monitors are removed from the selected tests : " + mremoved);
        }
        if (madded.toString().equals("") && mremoved.toString().equals("")) {
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "No modification on the list of monitors was found.");
        }
    }

    public void selectAllTestsAndUpdatListMonitor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("selectAllTestsAndUpdatListMonitor");
        }
        this.testsSelected.clear();
        this.testsSelected.addAll(this.getAllDisplayedTests());
        this.updateListMonitorForSelectedTests();
    }

    public void diselectAllTestsAndUpdatListMonitor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("diselectAllTestsAndUpdatListMonitor");
        }
        this.testsSelected.clear();
        this.updateListMonitorForSelectedTests();
    }

    public void updateListTestsAndMonitors() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateListTestsAndMonitors");
        }
        this.testsSelected = new ArrayList<Test>();
        this.updateListMonitorForSelectedTests();
    }

    public void resetListMonitors() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetListMonitors");
        }
        this.listMonitorsForSelectedTestsByAdmin = new ArrayList<MonitorInSession>(this.listMonitorsForSelectedTests);
    }

    public String getStatus(Test test) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getStatus");
        }
        if (test != null) {
            if (test.getTestStatus() != null) {
                return test.getTestStatus().getKeyword();
            }
        }
        return null;
    }

    public String getTestPeerType(Test test) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestPeerType");
        }
        if (test != null) {
            if (test.getTestPeerType() != null) {
                return test.getTestPeerType().getKeyword();
            }
        }
        return null;
    }

    public String getTestType(Test test) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestType");
        }
        if (test != null) {
            if (test.getTestType() != null) {
                return test.getTestType().getKeyword();
            }
        }
        return null;
    }

    // destroy ////////////////////////////////////////////////////

    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

}
