package net.ihe.gazelle.tm.gazelletest.bean;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.tm.gazelletest.model.definition.MetaTest;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestRoles;
import net.ihe.gazelle.tm.gazelletest.model.instance.Status;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.tm.tee.model.TestInstanceExecutionStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SAPResultDetailItem implements Comparable<SAPResultDetailItem> {

    private static final List<TestInstance> EMPTY_LIST = new ArrayList<TestInstance>(0);
    private static final Logger LOG = LoggerFactory.getLogger(SAPResultDetailItem.class);
    protected Map<Status, List<TestInstance>> instances = new HashMap<Status, List<TestInstance>>();

    //add Map to capture TestInstances by Execution Status - ITB
    protected Map<TestInstanceExecutionStatusEnum, List<TestInstance>> instancesByExecutionStatus = new HashMap<TestInstanceExecutionStatusEnum,
            List<TestInstance>>();
    private AIPOSystemPartners partners;

    public SAPResultDetailItem(TestingSession testingSession) {
        super();
        this.partners = new AIPOSystemPartners(testingSession);
    }

    protected boolean isSupported(Test test, TestRoles testRoles) {
        int testId1 = testRoles.getTest().getId();
        int testId2 = test.getId();
        return testId1 == testId2;
    }

    public void tryAddTestInstance(TestInstance testInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("tryAddTestInstance");
        }
        if (isSupported(testInstance.getTest())) {
            Status status = testInstance.getLastStatus();

            //added for ITB
            TestInstanceExecutionStatusEnum execStatus = (null != testInstance.getExecutionStatus()) ? testInstance
                    .getExecutionStatus().getKey() : null;
            List<TestInstance> list = instances.get(status);
            if (execStatus == null) {
                execStatus = TestInstanceExecutionStatusEnum.ACTIVE;
            }
            List<TestInstance> listByExecStat = instancesByExecutionStatus.get(execStatus);
            if (list == null) {
                list = new ArrayList<TestInstance>();
                instances.put(status, list);
            }
            if (listByExecStat == null) {
                listByExecStat = new ArrayList<TestInstance>();
                instancesByExecutionStatus.put(execStatus, listByExecStat);
            }
            //GZL-4565
            if (!listByExecStat.contains(testInstance)) {
                listByExecStat.add(testInstance);
            }
            if (!list.contains(testInstance)) {
                list.add(testInstance);
            }
        }
    }

    protected abstract boolean isSupported(Test test);

    protected abstract String getComparableTitle();

    public abstract String getTitle();

    public abstract String getLink();

    public abstract String getCount();

    public abstract String getOpt();

    public abstract boolean isMetaTest();

    public abstract String getTitle2();

    public abstract String getLink2();

    public abstract List<TestRoles> getTestRolesList();

    public abstract boolean isAllowComment();

    public abstract String getComment();

    public abstract void setComment(String comment);

    public List<TestInstance> getInstances(Status status) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstances");
        }
        List<TestInstance> result = instances.get(status);
        if (result == null) {
            return EMPTY_LIST;
        }
        return result;
    }

    public List<TestInstance> getInstancesVerified() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstancesVerified");
        }
        return getInstances(Status.VERIFIED);
    }

    public List<TestInstance> getInstancesWaiting() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstancesWaiting");
        }
        List<TestInstance> instances = new ArrayList<TestInstance>();
        instances.addAll(getInstances(Status.COMPLETED));
        instances.addAll(getInstances(Status.CRITICAL));
        return instances;
    }

    public List<TestInstance> getInstancesProgress() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstancesProgress");
        }
        List<TestInstance> instances = new ArrayList<TestInstance>();
        instances.addAll(getInstances(Status.STARTED));
        instances.addAll(getInstances(Status.PAUSED));
        instances.addAll(getInstances(Status.PARTIALLY_VERIFIED));
        return instances;
    }

    public List<TestInstance> getInstancesFailed() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstancesFailed");
        }
        return getInstances(Status.FAILED);
    }

    @Override
    public int compareTo(SAPResultDetailItem o) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("compareTo");
        }
        return getComparableTitle().compareTo(o.getComparableTitle());
    }

    public String getTestPermalink(Test inTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestPermalink");
        }
        if (inTest != null) {
            return ApplicationPreferenceManager.instance().getApplicationUrl() + "test.seam?id=" + inTest.getId();
        }
        return null;
    }

    public String getPermalinkToMetaTest(MetaTest metaTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPermalinkToMetaTest");
        }
        if ((metaTest != null) && (metaTest.getId() != null)) {
            return ApplicationPreferenceManager.instance().getApplicationUrl() + "testing/metatest/metaTest.seam?id=" //DONE
                    + metaTest.getId();
        }
        return null;
    }

    public AIPOSystemPartners getPartners() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPartners");
        }
        return partners;
    }

    public void setPartners(AIPOSystemPartners partners) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setPartners");
        }
        this.partners = partners;
    }

    //======== [START] methods added 2013-11-07 as part of ITB effort

    /**
     * retrieve List<TestInstance> based on execution status
     *
     * @param execStatus
     * @return
     */
    public List<TestInstance> getInstancesByExecutionStatus(TestInstanceExecutionStatusEnum execStatus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstancesByExecutionStatus");
        }
        List<TestInstance> result = instancesByExecutionStatus.get(execStatus);
        if (result == null) {
            return EMPTY_LIST;
        }
        return result;
    }

    public List<TestInstance> getInstancesByAllExecutionStatuses() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstancesByAllExecutionStatuses");
        }

        List<TestInstance> list = new ArrayList<TestInstance>();

        list.addAll(getInstancesByExecutionStatus(TestInstanceExecutionStatusEnum.ACTIVE));
        list.addAll(getInstancesByExecutionStatus(TestInstanceExecutionStatusEnum.PAUSED));
        list.addAll(getInstancesByExecutionStatus(TestInstanceExecutionStatusEnum.ABORTED));
        list.addAll(getInstancesByExecutionStatus(TestInstanceExecutionStatusEnum.SKIPPED));
        list.addAll(getInstancesByExecutionStatus(TestInstanceExecutionStatusEnum.INACTIVE));

        return list;
    }

    public List<TestInstance> getInstancesActiveOrPaused() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstancesActiveOrPaused");
        }
        List<TestInstance> list = new ArrayList<TestInstance>();
        list.addAll(getInstancesByExecutionStatus(TestInstanceExecutionStatusEnum.ACTIVE));
        list.addAll(getInstancesByExecutionStatus(TestInstanceExecutionStatusEnum.PAUSED));
        return list;
    }

    public List<TestInstance> getInstancesAborted() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstancesAborted");
        }
        return getInstancesByExecutionStatus(TestInstanceExecutionStatusEnum.ABORTED);
    }

    public List<TestInstance> getInstancesCompleted() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstancesCompleted");
        }
        return getInstancesByExecutionStatus(TestInstanceExecutionStatusEnum.COMPLETED);
    }

    public List<TestInstance> getInstancesSkipped() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstancesSkipped");
        }
        return getInstancesByExecutionStatus(TestInstanceExecutionStatusEnum.SKIPPED);
    }

    //======== [END] methods added 2013-11-07 as part of ITB effort
}
