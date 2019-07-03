package net.ihe.gazelle.tm.tee.dao;

import net.ihe.gazelle.tm.tee.exception.TestExecutionException;
import net.ihe.gazelle.tm.tee.model.*;
import net.ihe.gazelle.tm.tee.status.dao.TestStepsInstanceExecutionStatusDAO;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * Contains DAO methods for TestStepInstMessageProcessStatus entity
 *
 * @author tnabeel
 */
public class StepInstanceMsgStatusDAO {

    private static final Logger LOG = LoggerFactory.getLogger(StepInstanceMsgStatusDAO.class);

    private EntityManager entityManager;

    public StepInstanceMsgStatusDAO(EntityManager entityManager) {
        Validate.notNull(entityManager);
        this.entityManager = entityManager;
    }

    public EntityManager getEntityManager() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEntityManager");
        }
        return entityManager;
    }

    /**
     * This method sets the processing status of TmStepInstanceMessage record accordingly
     *
     * @param entityManager
     * @param tmStepInstanceMessage
     * @param processStatus
     */
    public void updateMsgProcessStatus(TmStepInstanceMessage tmStepInstanceMessage, TestStepInstMessageProcessStatusEnum processStatus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateMsgProcessStatus");
        }
        TestStepInstMessageProcessStatus status = findTestStepInstMessageProcessStatus(processStatus);
        tmStepInstanceMessage.setProcessStatus(status);
        getEntityManager().merge(tmStepInstanceMessage);
    }

    /**
     * This method marks the process status of TmStepInstanceMessage record in error and sets the error message accordingly.
     *
     * @param entityManager
     * @param tmStepInstanceMessage
     * @param errorMessage
     */
    public void updateMsgProcessStatusToError(TmStepInstanceMessage tmStepInstanceMessage, String errorMessage) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateMsgProcessStatusToError");
        }
        TestStepInstMessageProcessStatus status = findTestStepInstMessageProcessStatus(TestStepInstMessageProcessStatusEnum.ERROR);
        tmStepInstanceMessage.setProcessStatus(status);
        tmStepInstanceMessage.setErrorMessage(errorMessage);
        getEntityManager().merge(tmStepInstanceMessage);
        if (tmStepInstanceMessage.getTmTestStepInstance() != null) {
            if (tmStepInstanceMessage.getDirection() == MessageDirection.REQUEST) {
                tmStepInstanceMessage.getTmTestStepInstance().setExecutionStatus(new TestStepsInstanceExecutionStatusDAO()
                        .getTestStepExecutionStatusByEnum(TestStepInstanceExecutionStatusEnum.INITIATED));
            } else {
                tmStepInstanceMessage.getTmTestStepInstance().setExecutionStatus(new TestStepsInstanceExecutionStatusDAO()
                        .getTestStepExecutionStatusByEnum(TestStepInstanceExecutionStatusEnum.RESPONDED));
            }
            getEntityManager().merge(tmStepInstanceMessage.getTmTestStepInstance());
        }
    }

    /**
     * Retrieves the ProxyType entity based on the provided proxyTypeEnum
     *
     * @param proxyTypeEnum
     * @return
     */
    public TestStepInstMessageProcessStatus findTestStepInstMessageProcessStatus(TestStepInstMessageProcessStatusEnum statusEnum) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findTestStepInstMessageProcessStatus");
        }
        Query query = getEntityManager().createNamedQuery("findTestStepInstMessageProcessStatusByKey");
        query.setParameter("key", statusEnum);
        query.setHint("org.hibernate.cacheable", true);

        try {
            return (TestStepInstMessageProcessStatus) query.getSingleResult();
        } catch (javax.persistence.NoResultException e) {
            @SuppressWarnings("unchecked")
            List<TestStepInstMessageProcessStatus> list = getEntityManager().createNamedQuery("findAllTestStepInstMessageProcessStatuses")
                    .getResultList();
            throw new TestExecutionException("Could not find TestStepInstMessageProcessStatus '" + statusEnum.name() + "'.  Available " +
                    "testStepInstMessageProcessStatuses in database: \n" + list, e);
        }
    }
}
