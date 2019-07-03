package net.ihe.gazelle.tm.tee.dao;

import net.ihe.gazelle.tm.gazelletest.model.instance.TestStepsInstance;
import net.ihe.gazelle.tm.tee.exception.TestExecutionException;
import net.ihe.gazelle.tm.tee.model.MessageDirection;
import net.ihe.gazelle.tm.tee.model.MessageValidationService;
import net.ihe.gazelle.tm.tee.model.TmTestStepMessageProfile;
import net.ihe.gazelle.tm.tee.util.CollectionUtil;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * Contains DAO methods for TmTestStepMessageProfile entity
 *
 * @author tnabeel
 */
public class TestStepMsgProfileDAO {

    private static final Logger LOG = LoggerFactory.getLogger(TestStepMsgProfileDAO.class);

    private EntityManager entityManager;

    public TestStepMsgProfileDAO(EntityManager entityManager) {
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
     * @param stepMsgProfileId
     * @return
     */
    public TmTestStepMessageProfile findTestStepMsgProfileById(int stepMsgProfileId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findTestStepMsgProfileById");
        }
        return getEntityManager().find(TmTestStepMessageProfile.class, stepMsgProfileId);
    }

    /**
     * @param stepMsgProfileId
     * @return
     */
    public TmTestStepMessageProfile findTestStepMsgProfileById(Integer stepMsgProfileId, boolean mustFind) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findTestStepMsgProfileById");
        }
        TmTestStepMessageProfile stepProfile = findTestStepMsgProfileById(stepMsgProfileId);
        if (stepProfile == null && mustFind) {
            throw new TestExecutionException("Could not find TmTestStepMessageProfile " + stepMsgProfileId);
        }
        return stepProfile;
    }

    /**
     * Returns the TmTestStepMessageProfile instance for the given testStepInstance id and messageDirection
     *
     * @param tsiId
     * @param messageType
     * @param messageDirection
     * @return
     */
    public TmTestStepMessageProfile findTestStepMsgProfileByTsiIdAndMsgTypeAndDirection(int tsiId, String messageType, MessageDirection
            messageDirection) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findTestStepMsgProfileByTsiIdAndMsgTypeAndDirection");
        }
        Query query = null;
        if (messageDirection == MessageDirection.REQUEST) {
            query = getEntityManager().createNamedQuery("findTestStepMessageProfileByTsiIdAndMsgTypeAndDirection");
            query.setParameter("messageType", messageType);
        } else {
            query = getEntityManager().createNamedQuery("findTestStepMessageProfileByTsiIdAndDirection");
        }
        query.setParameter("tsiId", tsiId);
        query.setParameter("direction", messageDirection);

        @SuppressWarnings("unchecked")
        List<TmTestStepMessageProfile> list = query.getResultList();

        return CollectionUtil.isNotEmpty(list) ? list.get(0) : null;
    }

    /**
     * Returns the TmTestStepMessageProfile instance for the given testStepInstance and messageDirection
     *
     * @param testStepInstance
     * @param messageType
     * @param messageDirection
     * @return
     */
    public TmTestStepMessageProfile findTestStepMsgProfileByTestStepInstanceAndMsgTypeAndDirection(TestStepsInstance testStepInstance,
                                                                                                   String messageType, MessageDirection
                                                                                                           messageDirection) {

        if (testStepInstance != null) {
            return findTestStepMsgProfileByTsiIdAndMsgTypeAndDirection(testStepInstance.getId(), messageType, messageDirection);
        } else {
            return null;
        }
    }

    /**
     * Deletes the supplied {@link TmTestStepMessageProfile} record.
     *
     * @param msgType
     */
    public void deleteTmTestStepMessageProfile(TmTestStepMessageProfile msgType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteTmTestStepMessageProfile");
        }

        if (msgType == null) {

            return;
        }

        if (msgType.getTmStepInstanceMessages() != null) {
            if (msgType.getTmStepInstanceMessages().size() > 0) {

                return;
            }
        }

        //before we delete the TmTestStepMessageProfile object,
        //we need to delete all associated MessageValidationService records
        for (MessageValidationService srv : msgType.getMessageValidationServices()) {
            srv = entityManager.find(MessageValidationService.class, srv.getId());
            entityManager.remove(srv);
            entityManager.flush();
        }

        msgType = entityManager.find(TmTestStepMessageProfile.class, msgType.getId());
        entityManager.remove(msgType);
        entityManager.flush();
    }

}
