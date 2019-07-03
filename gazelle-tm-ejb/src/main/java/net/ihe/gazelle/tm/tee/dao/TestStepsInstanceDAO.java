package net.ihe.gazelle.tm.tee.dao;

import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestStepsInstance;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.tee.exception.TestExecutionException;
import net.ihe.gazelle.tm.tee.model.TestInstanceExecutionStatusEnum;
import net.ihe.gazelle.tm.tee.model.TestStepInstanceExecutionStatusEnum;
import net.ihe.gazelle.tm.tee.util.CollectionUtil;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Arrays;
import java.util.List;

/**
 * Contains DAO methods for TestStepsInstance entity.
 *
 * @author tnabeel
 */
public class TestStepsInstanceDAO {

    private static final String ACTIVE_TSI_FOR_SIS_FINDER = "select tsi.id from tm_test_instance ti join tm_test_instance_exec_status tiStatus on " +
            "ti.execution_status_id=tiStatus.id join tm_test_instance_test_steps_instance titsi on ti.id=titsi.test_instance_id join " +
            "tm_test_steps_instance tsi on titsi.test_steps_instance_id=tsi.id join tm_test_steps step on tsi.test_steps_id=step.id join " +
            "tm_step_instance_exec_status tsiStatus on tsi.execution_status_id=tsiStatus.id join tm_system_in_session senderSis on senderSis.id=tsi" +
            ".system_in_session_initiator_id where ti.proxy_used=true and tiStatus.key='ACTIVE' and tsiStatus.key in ('WAITING') and senderSis.id=?" +
            " order by ti.id desc, tsi.id asc;";

    private static final Logger LOG = LoggerFactory.getLogger(TestStepsInstanceDAO.class);

    private EntityManager entityManager;

    public TestStepsInstanceDAO(EntityManager entityManager) {
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
     * Updates the database with changes in provided in testStepInstance
     *
     * @param testStepInstance
     */
    public void updateTestStepInstance(TestStepsInstance testStepInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateTestStepInstance");
        }
        Validate.notNull(testStepInstance);
        getEntityManager().merge(testStepInstance);
    }

    /**
     * Returns the TestStepsInstance that corresponds to the given parameters
     *
     * @param testInstanceId
     * @param senderHostIPAddress
     * @param testInstanceExecutinoStatus
     * @param tsiStatuses                 list of TestStepInstance execution statuses
     * @return
     */
    public TestStepsInstance findTsiByTiIdAndSenderHostIPandExecStatus(int testInstanceId, String senderHostIPAddress,
                                                                       TestInstanceExecutionStatusEnum testInstanceExecutionStatus,
                                                                       TestStepInstanceExecutionStatusEnum... tsiStatuses) {

        Query query = getEntityManager().createNamedQuery("findTsiByTiIdAndSenderHostIPandExecStatus");
        query.setParameter("tiId", testInstanceId);
        query.setParameter("senderHostIPAddress", senderHostIPAddress);
        query.setParameter("tiStatus", testInstanceExecutionStatus);
        List<TestStepInstanceExecutionStatusEnum> tsiStatusList = Arrays.asList(tsiStatuses);
        query.setParameter("tsiStatuses", tsiStatusList);

        @SuppressWarnings("unchecked")
        List<TestStepsInstance> list = (List<TestStepsInstance>) query.getResultList();

        return CollectionUtil.isNotEmpty(list) ? list.get(0) : null;
    }

    /**
     * Returns the TestStepsInstance that corresponds to the given parameters
     *
     * @param testStepInstanceId
     * @return
     */
    public TestStepsInstance findTsiByTsiId(int testStepInstanceId, boolean mustFind) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findTsiByTsiId");
        }
        TestStepsInstance tsi = getEntityManager().find(TestStepsInstance.class, testStepInstanceId);
        if (tsi == null && mustFind) {
            throw new TestExecutionException("Could not find TestStepsInstance " + testStepInstanceId);
        }
        return tsi;
    }

    /**
     * Returns the TestStepsInstance that corresponds to the given parameters
     *
     * @param testInstanceId
     * @return
     */
    public List<TestStepsInstance> findTsisByTiId(int testInstanceId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findTsisByTiId");
        }
        Query query = getEntityManager().createNamedQuery("findTsisByTiId");
        query.setParameter("tiId", testInstanceId);

        @SuppressWarnings("unchecked")
        List<TestStepsInstance> list = (List<TestStepsInstance>) query.getResultList();

        return list;
    }

    /**
     * Returns the next Test Step Instance that's ready to be activated
     *
     * @param testInstanceId
     * @param currentTestStepIndex
     * @return the next TestStepsInstance to activate
     */
    public TestStepsInstance findNextTsiToActivate(Integer testInstanceId, Integer currentTestStepIndex) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findNextTsiToActivate");
        }
        Query query = getEntityManager().createNamedQuery("findNextTsiToActivate");
        query.setParameter("tiId", testInstanceId);
        query.setParameter("currentStepIndex", currentTestStepIndex);

        @SuppressWarnings("unchecked")
        List<TestStepsInstance> list = (List<TestStepsInstance>) query.getResultList();

        return CollectionUtil.isNotEmpty(list) ? list.get(0) : null;
    }

    /**
     * This method returns the first INACTIVE step instance in the provided test instance for the provided initiator systemInSession
     *
     * @param testInstance
     * @param initiatorSystemInSession
     * @return the first TestStepsInstance that can be activated
     */
    public TestStepsInstance findFirstTsiToActivate(TestInstance testInstance, SystemInSession initiatorSystemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findFirstTsiToActivate");
        }
        Validate.notNull(testInstance);
        Validate.notNull(initiatorSystemInSession);

        Query query = getEntityManager().createNamedQuery("findFirstTsiToActivate");
        query.setParameter("tiId", testInstance.getId());
        query.setParameter("sisId", initiatorSystemInSession.getId());

        @SuppressWarnings("unchecked")
        List<TestStepsInstance> list = (List<TestStepsInstance>) query.getResultList();

        return CollectionUtil.isNotEmpty(list) ? list.get(0) : null;

    }

    /**
     * This method returns active test step instance for the provided systemInSession
     *
     * @param systemInSession
     * @return
     */
    public TestStepsInstance findActiveTsiForSis(SystemInSession systemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findActiveTsiForSis");
        }
        Query query = getEntityManager().createNativeQuery(ACTIVE_TSI_FOR_SIS_FINDER);
        query.setParameter(1, systemInSession.getId());
        List<?> list = query.getResultList();
        if (!CollectionUtil.isEmpty(list)) {
            Integer tsiId = (Integer) list.get(0);
            if (tsiId != null) {
                return findTsiByTsiId(tsiId, true);
            }
        }
        return null;
    }
}
