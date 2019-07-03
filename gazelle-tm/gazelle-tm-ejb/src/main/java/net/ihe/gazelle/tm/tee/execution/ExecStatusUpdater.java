package net.ihe.gazelle.tm.tee.execution;

import net.ihe.gazelle.tm.gazelletest.model.instance.TestStepsInstance;
import net.ihe.gazelle.tm.tee.dao.StepInstanceMsgDAO;
import net.ihe.gazelle.tm.tee.dao.TestStepsInstanceDAO;
import net.ihe.gazelle.tm.tee.model.TestInstanceExecutionStatusEnum;
import net.ihe.gazelle.tm.tee.model.TestStepInstanceExecutionStatus;
import net.ihe.gazelle.tm.tee.model.TestStepInstanceExecutionStatusEnum;
import net.ihe.gazelle.tm.tee.model.TmStepInstanceMessage;
import net.ihe.gazelle.tm.tee.status.dao.TestInstanceDAO;
import net.ihe.gazelle.tm.tee.status.dao.TestInstanceExecutionStatusDAO;
import net.ihe.gazelle.tm.tee.status.dao.TestStepsInstanceExecutionStatusDAO;
import net.ihe.gazelle.tm.tee.util.TEEConstants;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;

/**
 * This class is responsible for updating the Execution Status of Test Step Instances and Test Instances at appropriate times.
 *
 * @author tnabeel
 */
public class ExecStatusUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(ExecStatusUpdater.class);

    private EntityManager entityManager;

    protected ExecStatusUpdater(EntityManager entityManager) {
        Validate.notNull(entityManager);
        this.entityManager = entityManager;
    }

    private EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * This method updates the TestStepInstance ExecutionStatus to INITIATED, RESPONDED, COMPLETED, etc.
     *
     * @param proxyMsgData
     * @param tmStepInstanceMessage
     */
    public void updateCurrentTestStepInstanceStatus(ProxyMsgData proxyMsgData, TmStepInstanceMessage tmStepInstanceMessage) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateCurrentTestStepInstanceStatus");
        }
        Validate.notNull(tmStepInstanceMessage);
        if (tmStepInstanceMessage.getTmTestStepInstance() == null) {
            return;
        }
        TestStepsInstanceExecutionStatusDAO tsiExecStatusDAO = new TestStepsInstanceExecutionStatusDAO();
        TestStepsInstanceDAO tsiDAO = new TestStepsInstanceDAO(getEntityManager());
        switch (proxyMsgData.getMessageDirection()) {
            case REQUEST:
                updateCurrentTsiExecStatus(tsiExecStatusDAO, tsiDAO, tmStepInstanceMessage, TestStepInstanceExecutionStatusEnum.INITIATED);
                break;
            case RESPONSE:
                TestStepsInstance testStepInstance = tmStepInstanceMessage.getTmTestStepInstance();
                updateCurrentTsiExecStatus(tsiExecStatusDAO, tsiDAO, tmStepInstanceMessage, TestStepInstanceExecutionStatusEnum.RESPONDED);
                int expectedMsgCount = getExpectedMsgCount(testStepInstance);
                List<TmStepInstanceMessage> allStepInstanceMessagesForTsi = new StepInstanceMsgDAO(entityManager)
                        .findTmStepInstanceMessagesByTsiIdOrderByDirAsc(tmStepInstanceMessage.getTmTestStepInstance().getId());
                if (allStepInstanceMessagesForTsi.size() >= expectedMsgCount) {
                    if (new ResponseTracker(getEntityManager()).receivedResponseForEveryRequest(proxyMsgData, tmStepInstanceMessage,
                            allStepInstanceMessagesForTsi)) {
                        if (testStepInstance.getTestSteps().getAutoComplete()) {
                            updateCurrentTsiExecStatus(tsiExecStatusDAO, tsiDAO, tmStepInstanceMessage, TestStepInstanceExecutionStatusEnum
                                    .COMPLETED);
                        }
                        activateNextTestStepInstanceStatus(proxyMsgData, tmStepInstanceMessage);
                    }
                }
                break;
        }
    }

    /**
     * Updates the execution status of the provided tmStepInstanceMessage's testStepInstance to the specified status
     *
     * @param tsiExecStatusDAO
     * @param tsiDAO
     * @param tmStepInstanceMessage
     * @param status                The status to update to
     */
    private void updateCurrentTsiExecStatus(TestStepsInstanceExecutionStatusDAO tsiExecStatusDAO, TestStepsInstanceDAO tsiDAO,
                                            TmStepInstanceMessage tmStepInstanceMessage, TestStepInstanceExecutionStatusEnum status) {
        Validate.notNull(tsiExecStatusDAO);
        Validate.notNull(tsiDAO);
        Validate.notNull(status);
        TestStepInstanceExecutionStatus testStepInstanceExecStatus = tsiExecStatusDAO.getTestStepExecutionStatusByEnum(status);
        Validate.notNull(testStepInstanceExecStatus);
        tmStepInstanceMessage.getTmTestStepInstance().setExecutionStatus(testStepInstanceExecStatus);
        tsiDAO.updateTestStepInstance(tmStepInstanceMessage.getTmTestStepInstance());

    }

    /**
     * Updates the execution status of the provided tmStepInstanceMessage's testInstance to completed if all the testInstance's tmStepInstances are in
     * completed status
     *
     * @param tsiDAO
     * @param proxyMsgData
     * @param tmStepInstanceMessage
     */
    protected void updateCurrentTestInstanceStatus(ProxyMsgData proxyMsgData, TmStepInstanceMessage tmStepInstanceMessage) {
        Validate.notNull(tmStepInstanceMessage);
        Validate.notNull(tmStepInstanceMessage.getTmTestStepInstance());
        Validate.notNull(tmStepInstanceMessage.getTmTestStepInstance().getTestInstance());

        net.ihe.gazelle.tm.tee.dao.TestStepsInstanceDAO tsiDAO = new net.ihe.gazelle.tm.tee.dao.TestStepsInstanceDAO(getEntityManager());

        List<TestStepsInstance> tsiList = tsiDAO.findTsisByTiId(tmStepInstanceMessage.getTmTestStepInstance().getTestInstance().getId());
        boolean allStepsCompleted = false;
        for (TestStepsInstance testStepsInstance : tsiList) {
            allStepsCompleted = (testStepsInstance.getExecutionStatus() != null && testStepsInstance.getExecutionStatus().getKey().isDone());
            if (!allStepsCompleted) {
                break;
            }
        }
        if (allStepsCompleted) {

            tmStepInstanceMessage.getTmTestStepInstance().getTestInstance().setExecutionStatus(new TestInstanceExecutionStatusDAO()
                    .getTestInstanceExecutionStatusByEnum(TestInstanceExecutionStatusEnum.COMPLETED));
            new TestInstanceDAO().mergeTestInstance(tmStepInstanceMessage.getTmTestStepInstance().getTestInstance());
        } else {

        }
    }

    private int getExpectedMsgCount(TestStepsInstance testStepInstance) {
        Integer expectedMsgCount = testStepInstance.getTestSteps().getExpectedMessageCount();
        if (expectedMsgCount != null) {
            return expectedMsgCount;
        } else {
            return TEEConstants.DEFAULT_EXPECTED_STEP_INSTANCE_MESSAGE_COUNT;
        }
    }

    /**
     * This method activates the next Test Step Instance in the sequence if it's status is INACTIVE or PAUSED and if it's
     * autoTrigger flag is turned on.
     *
     * @param proxyMsgData
     * @param tmStepInstanceMessage
     */
    private void activateNextTestStepInstanceStatus(ProxyMsgData proxyMsgData, TmStepInstanceMessage tmStepInstanceMessage) {
        TestStepsInstanceDAO tsiDAO = new TestStepsInstanceDAO(getEntityManager());

        Integer currentStepIndex = tmStepInstanceMessage.getTmTestStepInstance().getTestSteps().getStepIndex();
        TestStepsInstance tsiToActivate = tsiDAO.findNextTsiToActivate(tmStepInstanceMessage.getTmTestStepInstance().getTestInstance().getId(),
                currentStepIndex);
        if (tsiToActivate != null) {
            TestStepsInstanceExecutionStatusDAO tsiExecStatusDAO = new TestStepsInstanceExecutionStatusDAO();
            TestStepInstanceExecutionStatus execStatusWaiting = tsiExecStatusDAO.getTestStepExecutionStatusByEnum
                    (TestStepInstanceExecutionStatusEnum.WAITING);
            Validate.notNull(execStatusWaiting);
            tsiToActivate.setLastChanged(new Date());
            tsiToActivate.setExecutionStatus(execStatusWaiting);
            tsiDAO.updateTestStepInstance(tsiToActivate);
        }

    }

}
