package net.ihe.gazelle.tm.tee.execution;

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestStepsInstance;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.tee.dao.StepInstanceMsgDAO;
import net.ihe.gazelle.tm.tee.dao.StepInstanceMsgStatusDAO;
import net.ihe.gazelle.tm.tee.exception.TestExecutionException;
import net.ihe.gazelle.tm.tee.model.TestStepInstMessageProcessStatusEnum;
import net.ihe.gazelle.tm.tee.model.TmStepInstanceMessage;
import net.ihe.gazelle.tm.tee.util.ExceptionUtil;
import org.jboss.seam.annotations.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;

/**
 * This class is responsible for managing the operations of the Test Execution Engine
 *
 * @author tnabeel
 */
@Stateless
@Name("teeManager")
//Commented the scope out as Stateless session beans should only be bound to the STATELESS context
//@Scope(ScopeType.APPLICATION)
@GenerateInterface("TEEManagerLocal")
public class TEEManager implements Serializable, TEEManagerLocal {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(TEEManager.class);

    @Resource
    private SessionContext sessionContext;

    /**
     * This method performs all the operations of the Test Execution Engine
     *
     * @param proxyMsgId
     * @param proxyTypeEnum
     * @param proxyHostName
     * @param messageDirection
     * @param connectionId
     */
    public void processProxyMessage(ProxyMsgData proxyMsgData) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("processProxyMessage");
        }

        EntityManager entityManager = EntityManagerService.provideEntityManager();
        TmStepInstanceMessage tmStepInstanceMessage = null;
        try {

            StepInstanceMsgDAO stepInstanceMsgDAO = new StepInstanceMsgDAO(entityManager);

            // Create tm_step_instance_message mapping record
            tmStepInstanceMessage = stepInstanceMsgDAO.createTmStepInstanceMessage(proxyMsgData);
            if (tmStepInstanceMessage == null) {
                return;
            }

            // Resolve the rest of the fields in TmStepInstanceMessage
            switch (proxyMsgData.getMessageDirection()) {
                case REQUEST:
                    new RequestDataResolver(entityManager).resolveExecutionData(proxyMsgData, tmStepInstanceMessage);
                    break;
                case RESPONSE:
                    new ResponseDataResolver(entityManager).resolveExecutionData(proxyMsgData, tmStepInstanceMessage);
                    break;
            }

            // Update the TmStepInstanceMessage record with all the resolved fields
            stepInstanceMsgDAO.updateTmStepInstanceMessage(tmStepInstanceMessage);

            // Call the validation services
            validateMessageAndPersistResults(entityManager, proxyMsgData, tmStepInstanceMessage);

            // Update the execution status of the current test step instance, the next step instance, and the overall test instance.
            updateExecutionStatuses(entityManager, proxyMsgData, tmStepInstanceMessage);

        } catch (TestExecutionException e) {
            LOG.error(getClass().getSimpleName() + ".processProxyMessage() encountered error for proxyMsgId=" + proxyMsgData.getProxyMsgId() + " " +
                    "proxyType=" + proxyMsgData.getProxyType() + " proxyHostName=" + proxyMsgData.getProxyHostName() + " messageDirection=" +
                    proxyMsgData.getMessageDirection() + " connId=" + proxyMsgData.getConnectionId(), e);
            try {
                updateMsgProcessStatusToError(entityManager, tmStepInstanceMessage, e.getMessage());
            } catch (Exception ex) {
                LOG.error("Encountered exception during updateMsgProcessStatusToError() for proxyMsgId=" + proxyMsgData.getProxyMsgId() + " " +
                        "proxyType=" + proxyMsgData.getProxyType() + " proxyHostName=" + proxyMsgData.getProxyHostName() + " messageDirection=" +
                        proxyMsgData.getMessageDirection() + " connId=" + proxyMsgData.getConnectionId(), ex);
            }
        } finally {

        }

    }

    /**
     * This method invokes MessageValidator and persists the results to the database.
     *
     * @param entityManager
     * @param proxyMsgData
     * @param tmStepInstanceMessage
     */
    private void validateMessageAndPersistResults(EntityManager entityManager, ProxyMsgData proxyMsgData,
                                                  TmStepInstanceMessage tmStepInstanceMessage) {
        try {
            TsiMsgValidator tsiMsgValidator = new TsiMsgValidator(entityManager);
            tsiMsgValidator.validateMessageAndPersistResults(proxyMsgData, tmStepInstanceMessage);
        } catch (Exception e) {
            throw new TestExecutionException("Validation error: " + ExceptionUtil.getWrappedMessage(e, false, false, true), e);
        }
    }

    /**
     * This method updates the Test Step Instance and the Test Instance execution statuses appropriately.
     *
     * @param entityManager
     * @param proxyMsgData
     * @param tmStepInstanceMessage
     */
    private void updateExecutionStatuses(EntityManager entityManager, ProxyMsgData proxyMsgData,
                                         TmStepInstanceMessage tmStepInstanceMessage) {

        ExecStatusUpdater execStatusUpdater = new ExecStatusUpdater(entityManager);

        // Mark the message record as processed
        updateMsgProcessStatus(entityManager, tmStepInstanceMessage, TestStepInstMessageProcessStatusEnum.PROCESSED);

        // Mark the TestStepInstance record as complete if appropriate
        execStatusUpdater.updateCurrentTestStepInstanceStatus(proxyMsgData, tmStepInstanceMessage);

        // Mark the TestInstance record as complete if appropriate
        execStatusUpdater.updateCurrentTestInstanceStatus(proxyMsgData, tmStepInstanceMessage);
    }

    /**
     * This method sets the processing status of TmStepInstanceMessage record accordingly
     *
     * @param entityManager
     * @param tmStepInstanceMessage
     * @param processStatus
     */
    private void updateMsgProcessStatus(EntityManager entityManager, TmStepInstanceMessage tmStepInstanceMessage,
                                        TestStepInstMessageProcessStatusEnum processStatus) {
        if (tmStepInstanceMessage != null) {
            StepInstanceMsgStatusDAO stepInstanceMsgStatusDAO = new StepInstanceMsgStatusDAO(entityManager);
            stepInstanceMsgStatusDAO.updateMsgProcessStatus(tmStepInstanceMessage, processStatus);
        }
    }

    /**
     * This method marks the process status of TmStepInstanceMessage record in error and sets the error message accordingly.
     *
     * @param entityManager
     * @param tmStepInstanceMessage
     * @param errorMessage
     */
    private void updateMsgProcessStatusToError(EntityManager entityManager, TmStepInstanceMessage tmStepInstanceMessage, String errorMessage) {
        if (tmStepInstanceMessage != null) {
            StepInstanceMsgStatusDAO stepInstanceMsgStatusDAO = new StepInstanceMsgStatusDAO(entityManager);
            stepInstanceMsgStatusDAO.updateMsgProcessStatusToError(tmStepInstanceMessage, errorMessage);
        }
    }

    /**
     * This method retrieves all the TmStepInstanceMessage objects that belong to the
     * provided TestStepsInstance object.
     */
    public List<TmStepInstanceMessage> getTmStepInstanceMessages(TestStepsInstance forStepInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTmStepInstanceMessages");
        }
        return new StepInstanceMsgDAO(EntityManagerService.provideEntityManager()).findTmStepInstanceMessagesByTsiIdOrderByDirAsc(forStepInstance
                .getId());
    }

    /**
     * This method activates the first step instance in the provided test instance for the provided initiator systemInSession
     *
     * @param testInstance
     * @param initiatorSystemInSession
     */
    public void activateFirstTestStepInstance(TestInstance testInstance, SystemInSession initiatorSystemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("activateFirstTestStepInstance");
        }
        new FirstTsiActivator(EntityManagerService.provideEntityManager()).activateFirstTestStepInstance(testInstance, initiatorSystemInSession);
    }

    public SessionContext getSessionContext() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSessionContext");
        }
        return sessionContext;
    }

    public void setSessionContext(SessionContext sessionContext) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSessionContext");
        }
        this.sessionContext = sessionContext;
    }

}
