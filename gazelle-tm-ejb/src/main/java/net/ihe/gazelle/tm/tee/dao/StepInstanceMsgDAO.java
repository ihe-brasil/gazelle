package net.ihe.gazelle.tm.tee.dao;

import net.ihe.gazelle.tm.gazelletest.model.instance.TestStepsInstance;
import net.ihe.gazelle.tm.tee.execution.ProxyMsgData;
import net.ihe.gazelle.tm.tee.model.*;
import net.ihe.gazelle.tm.tee.util.CollectionUtil;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * Contains DAO methods for TmStepInstanceMessage entity
 *
 * @author tnabeel
 */
public class StepInstanceMsgDAO {
    private static final Logger LOG = LoggerFactory.getLogger(StepInstanceMsgDAO.class);

    private EntityManager entityManager;

    public StepInstanceMsgDAO(EntityManager entityManager) {
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
     * Persists a non-existent tmStepInstanceMessage to the database
     *
     * @param tmStepInstanceMessage
     */
    public void createTmStepInstanceMessage(
            TmStepInstanceMessage tmStepInstanceMessage) {
        tmStepInstanceMessage.setLastChanged(new Date());
        getEntityManager().persist(tmStepInstanceMessage);
    }

    /**
     * Updates the database tmStepInstanceMessage record with the provided
     * tmStepInstanceMessage
     *
     * @param tmStepInstanceMessage
     */
    public void updateTmStepInstanceMessage(
            TmStepInstanceMessage tmStepInstanceMessage) {
        tmStepInstanceMessage.setLastChanged(new Date());
        getEntityManager().merge(tmStepInstanceMessage);
    }

    /**
     * Retrieves TmStepInstanceMessage based on proxyMsgId, proxyTypeEnum, and
     * proxyHostName
     *
     * @param proxyMsgId
     * @param proxyTypeEnum
     * @param proxyHostName
     * @return
     */
    public TmStepInstanceMessage findTmStepInstanceMsgByProxyMsgIdAndTypeAndHost(
            int proxyMsgId, ProxyTypeEnum proxyTypeEnum, String proxyHostName) {
        Query query = getEntityManager().createNamedQuery(
                "findTmStepInstanceMsgByProxyMsgIdAndTypeAndHost");
        query.setParameter("proxyMsgId", proxyMsgId);
        query.setParameter("proxyTypeEnum", proxyTypeEnum);
        query.setParameter("proxyHostName", proxyHostName);
        @SuppressWarnings("unchecked")
        List<TmStepInstanceMessage> list = query.getResultList();
        if (LOG.isInfoEnabled()) {
            LOG.info("findTmStepInstanceMsgByProxyMsgIdAndTypeAndHost found "
                    + (CollectionUtil.isNotEmpty(list) ? list.size() : 0)
                    + " records for proxyMsgId=" + proxyMsgId + " proxyTypeEnum="
                    + proxyTypeEnum + " proxyHostName='" + proxyHostName + "'");
        }
        return CollectionUtil.isNotEmpty(list) ? list.get(0) : null;
    }

    /**
     * Retrieves TmStepInstanceMessage based on proxyTypeEnum, proxyHostName,
     * messageDirection, and connectionId
     *
     * @param proxyTypeEnum
     * @param proxyHostName
     * @param messageDirection
     * @param connectionId
     * @return
     */
    public TmStepInstanceMessage findTmStepInstanceMsgByProxyTypeAndHostAndDirectionAndConnId(
            ProxyTypeEnum proxyTypeEnum, String proxyHostName,
            MessageDirection messageDirection, int connectionId) {
        Query query = getEntityManager().createNamedQuery(
                "findTmStepInstanceMsgByProxyTypeAndHostAndDirectionAndConnId");
        query.setParameter("proxyTypeEnum", proxyTypeEnum);
        query.setParameter("proxyHostName", proxyHostName);
        query.setParameter("messageDirection", messageDirection);
        query.setParameter("connectionId", connectionId);
        @SuppressWarnings("unchecked")
        List<TmStepInstanceMessage> list = query.getResultList();
        if (LOG.isInfoEnabled()) {
            LOG.info("findTmStepInstanceMsgByProxyTypeAndHostAndDirectionAndConnId found "
                    + (CollectionUtil.isNotEmpty(list) ? list.size() : 0)
                    + " records for messageDirection="
                    + messageDirection
                    + " proxyTypeEnum="
                    + proxyTypeEnum
                    + " proxyHostName='"
                    + proxyHostName + "' connectionId=" + connectionId);
        }
        return CollectionUtil.isNotEmpty(list) ? list.get(0) : null;
    }

    /**
     * Returns true if a TmStepInstanceMessage record exists with the same
     * values for the provided proxyMsgId, proxyTypeEnum, and proxyHostName. Can
     * be used to determine whether to reprocess a JMS message that has already
     * been processed from the same Proxy server.
     *
     * @param proxyMsgId
     * @param proxyTypeEnum
     * @param proxyHostName
     * @return
     */
    public boolean tmStepInstanceMessageExists(int proxyMsgId,
                                               ProxyTypeEnum proxyTypeEnum, String proxyHostName) {
        return findTmStepInstanceMsgByProxyMsgIdAndTypeAndHost(proxyMsgId,
                proxyTypeEnum, proxyHostName) != null;
    }

    /**
     * Creates a new TmStepInstanceMessage based on the provided attributes and
     * persists it to the database
     *
     * @param proxyMsgData
     * @return
     */
    public TmStepInstanceMessage createTmStepInstanceMessage(ProxyMsgData proxyMsgData) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createTmStepInstanceMessage");
        }
        try {
            StepInstanceMsgStatusDAO stepInstanceMsgStatusDAO = new StepInstanceMsgStatusDAO(getEntityManager());
            TestStepInstMessageProcessStatus STATUS_PROCESSING = stepInstanceMsgStatusDAO.findTestStepInstMessageProcessStatus
                    (TestStepInstMessageProcessStatusEnum.PROCESSING);

            if (tmStepInstanceMessageExists(proxyMsgData.getProxyMsgId(), proxyMsgData.getProxyType(),
                    proxyMsgData.getProxyHostName())) {
                throw new EntityExistsException(
                        "TmStepInstanceMessage (proxyMsgId=" + proxyMsgData.getProxyMsgId()
                                + " proxyTypeEnum=" + proxyMsgData.getProxyType()
                                + " proxyHostName=" + proxyMsgData.getProxyHostName()
                                + ") already exists.");
            }
            TmStepInstanceMessage tmStepInstanceMessage = new TmStepInstanceMessage();
            tmStepInstanceMessage.setProxyMsgId(proxyMsgData.getProxyMsgId());
            tmStepInstanceMessage.setProxyType(new ProxyTypeDAO(
                    getEntityManager()).findProxyType(proxyMsgData.getProxyType()));
            tmStepInstanceMessage.setDirection(proxyMsgData.getMessageDirection());
            tmStepInstanceMessage.setConnectionId(proxyMsgData.getConnectionId());
            tmStepInstanceMessage.setProxyHostName(proxyMsgData.getProxyHostName());
            tmStepInstanceMessage.setProcessStatus(STATUS_PROCESSING);
            tmStepInstanceMessage.setMessageContents(proxyMsgData.getMessageContents().getBytes(StandardCharsets.UTF_8.name()));
            tmStepInstanceMessage.setSenderPort(proxyMsgData.getSenderPort());
            tmStepInstanceMessage.setProxyPort(proxyMsgData.getProxyPort());
            tmStepInstanceMessage.setReceiverPort(proxyMsgData.getReceiverPort());
            createTmStepInstanceMessage(tmStepInstanceMessage);
            return tmStepInstanceMessage;
        } catch (EntityExistsException e) {
            LOG.error("createTmStepInstanceMessage failed: " + e.getMessage());
            return null;
        } catch (UnsupportedEncodingException e) {
            LOG.error("createTmStepInstanceMessage failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves the list of TmStepInstanceMessage instances that have the same
     * resolved testStepInstanceId
     *
     * @param testInstanceId
     * @return
     */
    public List<TmStepInstanceMessage> findTmStepInstanceMessagesByTsiIdOrderByDirAsc(
            Integer testStepInstanceId) {
        Query query = getEntityManager().createNamedQuery(
                "findTmStepInstanceMessagesByTsiIdOrderByDirAsc");
        query.setParameter("tsiId", testStepInstanceId);

        @SuppressWarnings("unchecked")
        List<TmStepInstanceMessage> list = query.getResultList();
        LOG.debug("findTmStepInstanceMessagesByTsiId found "
                + (CollectionUtil.isNotEmpty(list) ? list.size() : 0)
                + " records for testStepInstanceId=" + testStepInstanceId);

        return list;
    }

    public List<TmStepInstanceMessage> findTmStepInstanceMessagesByTsiIdOrderByDirDesc(
            Integer testStepInstanceId) {
        Query query = getEntityManager().createNamedQuery(
                "findTmStepInstanceMessagesByTsiIdOrderByDirDesc");
        query.setParameter("tsiId", testStepInstanceId);

        @SuppressWarnings("unchecked")
        List<TmStepInstanceMessage> list = query.getResultList();
        LOG.debug("findTmStepInstanceMessagesByTsiId found "
                + (CollectionUtil.isNotEmpty(list) ? list.size() : 0)
                + " records for testStepInstanceId=" + testStepInstanceId);

        return list;
    }

    /**
     * Deletes all the TmStepInstanceMessage records for supplied
     * TestStepsInstance
     *
     * @param instance
     */
    public void deleteAllTmStepInstanceMessagesForTsi(TestStepsInstance instance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteAllTmStepInstanceMessagesForTsi");
        }
        List<TmStepInstanceMessage> messages = findTmStepInstanceMessagesByTsiIdOrderByDirDesc(instance
                .getId());

        if (!messages.isEmpty()) {
            for (int i = 0; messages.size() > i; i++) {
                deleteTsiMessage(messages.get(i));
            }
        }
    }

    public void deleteTsiMessage(TmStepInstanceMessage msg) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteTsiMessage");
        }
        if (msg != null) {
            entityManager.remove(msg);
        }
    }

}
