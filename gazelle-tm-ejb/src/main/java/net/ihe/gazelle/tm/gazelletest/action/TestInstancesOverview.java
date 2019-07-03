package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.HQLRestriction;
import net.ihe.gazelle.hql.restrictions.HQLRestrictionIn;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.tm.datamodel.TestInstanceDataModel;
import net.ihe.gazelle.tm.filter.TestInstanceFilter;
import net.ihe.gazelle.tm.gazelletest.model.instance.Status;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.users.model.User;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Remove;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.*;

@Name("testInstancesOverview")
@Scope(ScopeType.PAGE)
public class TestInstancesOverview implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(TestInstancesOverview.class);
    private static final long serialVersionUID = -4962080849183941322L;

    private TestInstanceFilter filter;

    private TestInstanceDataModel tests;
    private TestInstanceDataModel testsCritical;
    private TestInstanceDataModel testsCompleted;
    private TestInstanceDataModel testsPartiallyVerified;

    private boolean interoperabilityTestsOnly;

    private Set<Status> statuses = new HashSet<Status>();

    @Create
    public void create() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("create");
        }
    }

    private Collection<Status> getStatusSet(Status status) {
        Collection<Status> result = new ArrayList<Status>(1);
        result.add(status);
        return result;
    }

    @Remove
    @Destroy
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }
    }

    public TestInstanceFilter getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        return filter;
    }

    public TestInstanceDataModel getTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTests");
        }
        return tests;
    }

    public TestInstanceDataModel getTestsCritical() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestsCritical");
        }
        return testsCritical;
    }

    public TestInstanceDataModel getTestsCompleted() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestsCompleted");
        }
        return testsCompleted;
    }

    public TestInstanceDataModel getTestsPartiallyVerified() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestsPartiallyVerified");
        }
        return testsPartiallyVerified;
    }

    public void setMonitorWorkList(boolean isMonitorWorklist) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMonitorWorkList");
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();
        if (isMonitorWorklist) {
            filter = new TestInstanceFilter(isMonitorWorklist, requestParameterMap, interoperabilityTestsOnly) {
                /**
                 *
                 */
                private static final long serialVersionUID = 4700006843975440701L;

                @Override
                public boolean isCountEnabled() {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("isCountEnabled");
                    }
                    return false;
                }
            };

            User user = User.loggedInUser();
            if (user != null) {
                filter.getFilterValues().put("monitor", user);
                filter.setMonitorWorklistUser(user);
            }

            testsCritical = new TestInstanceDataModel(filter, getStatusSet(Status.getSTATUS_CRITICAL()));
            testsCompleted = new TestInstanceDataModel(filter, getStatusSet(Status.getSTATUS_COMPLETED()));
            testsPartiallyVerified = new TestInstanceDataModel(filter,
                    getStatusSet(Status.getSTATUS_PARTIALLY_VERIFIED()));

            List<Status> validStatuses = new ArrayList<Status>();
            validStatuses.add(Status.getSTATUS_CRITICAL());
            validStatuses.add(Status.getSTATUS_COMPLETED());
            validStatuses.add(Status.getSTATUS_PARTIALLY_VERIFIED());
            tests = new TestInstanceDataModel(filter, validStatuses);

            filter.setDataModels(testsCritical, testsCompleted, testsPartiallyVerified);

            filter.setMonitorWorklist(true);
        } else {
            filter = new TestInstanceFilter(isMonitorWorklist, requestParameterMap, interoperabilityTestsOnly);
            tests = new TestInstanceDataModel(filter, statuses);
            filter.setMonitorWorklist(false);
        }
    }

    public void resetFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetFilter");
        }
        getFilter().clear();
        statuses.clear();
    }

    public int countTests(String status) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("countTests");
        }
        HQLQueryBuilder<TestInstance> queryBuilder = new HQLQueryBuilder<TestInstance>(TestInstance.class);
        tests.appendFiltersPublic(FacesContext.getCurrentInstance(), queryBuilder);

        HQLRestriction restrictionStatus = null;

        List<HQLRestriction> restrictions = queryBuilder.getRestrictions();
        for (HQLRestriction hqlRestriction : restrictions) {
            if (hqlRestriction instanceof HQLRestrictionIn) {
                HQLRestrictionIn hqlRestrictionIn = (HQLRestrictionIn) hqlRestriction;
                if (hqlRestrictionIn.getPath().equals("lastStatus")) {
                    restrictionStatus = hqlRestrictionIn;
                }
            }
        }
        if (restrictionStatus != null) {
            restrictions.remove(restrictionStatus);
        }

        restrictionStatus = HQLRestrictions.eq("lastStatus", Status.getStatusByKeyword(status));
        restrictions.add(restrictionStatus);

        return queryBuilder.getCount();
    }

    public Boolean getFilterPartiallyVerified() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilterPartiallyVerified");
        }
        return getFilterStatus(Status.getSTATUS_PARTIALLY_VERIFIED());
    }

    public void setFilterPartiallyVerified(Boolean value) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFilterPartiallyVerified");
        }
        setFilterStatus(value, Status.getSTATUS_PARTIALLY_VERIFIED());
    }

    private Boolean getFilterStatus(Status status) {
        return statuses.contains(status);
    }

    private void setFilterStatus(Boolean value, Status status) {
        if ((value != null) && value) {
            statuses.add(status);
        } else {
            statuses.remove(status);
        }
        filter.modified();
    }

    public Boolean getFilterToBeVerifiedTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilterToBeVerifiedTests");
        }
        return getFilterStatus(Status.getSTATUS_COMPLETED());
    }

    public void setFilterToBeVerifiedTests(Boolean value) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFilterToBeVerifiedTests");
        }
        setFilterStatus(value, Status.getSTATUS_COMPLETED());
    }

    public Boolean getFilterVerifiedTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilterVerifiedTests");
        }
        return getFilterStatus(Status.getSTATUS_VERIFIED());
    }

    public void setFilterVerifiedTests(Boolean value) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFilterVerifiedTests");
        }
        setFilterStatus(value, Status.getSTATUS_VERIFIED());
    }

    public Boolean getFilterCriticalTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilterCriticalTests");
        }
        return getFilterStatus(Status.getSTATUS_CRITICAL());
    }

    public void setFilterCriticalTests(Boolean value) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFilterCriticalTests");
        }
        setFilterStatus(value, Status.getSTATUS_CRITICAL());
    }

    public void setInteroperabilityTestsOnly(boolean value) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setInteroperabilityTestsOnly");
        }
        interoperabilityTestsOnly = value;
    }

    public Boolean getFilterAbortedTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilterAbortedTests");
        }
        return getFilterStatus(Status.getSTATUS_ABORTED());
    }

    public void setFilterAbortedTests(Boolean value) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFilterAbortedTests");
        }
        setFilterStatus(value, Status.getSTATUS_ABORTED());
    }

    public Boolean getFilterRunningTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilterRunningTests");
        }
        return getFilterStatus(Status.getSTATUS_STARTED());
    }

    public void setFilterRunningTests(Boolean value) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFilterRunningTests");
        }
        setFilterStatus(value, Status.getSTATUS_STARTED());
    }

    public Boolean getFilterPausedTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilterPausedTests");
        }
        return getFilterStatus(Status.getSTATUS_PAUSED());
    }

    public void setFilterPausedTests(Boolean value) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFilterPausedTests");
        }
        setFilterStatus(value, Status.getSTATUS_PAUSED());
    }

    public Boolean getFilterFailedTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilterFailedTests");
        }
        return getFilterStatus(Status.getSTATUS_FAILED());
    }

    public void setFilterFailedTests(Boolean value) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFilterFailedTests");
        }
        setFilterStatus(value, Status.getSTATUS_FAILED());
    }

    public int getTotalTestTodo() {
        int total = 0;
        setInteroperabilityTestsOnly(false);
        setMonitorWorkList(true);
        if (TestingSession.getSelectedTestingSession().getIsCriticalStatusEnabled()) {
            if (testsCritical != null) {
                total = total + testsCritical.size();
            }
        }
        if (testsCompleted != null) {
            total = total + testsCompleted.size();
        }
        if (testsPartiallyVerified != null) {
            total = total + testsPartiallyVerified.size();
        }
        return total;
    }

    public String getMonitorName() {
        if (filter != null) {
            User selectedMonitor = (User) filter.getFilterValues().get("monitor");
            if (selectedMonitor != null) {
                return selectedMonitor.toString();
            }
        }
        return null;
    }
}
