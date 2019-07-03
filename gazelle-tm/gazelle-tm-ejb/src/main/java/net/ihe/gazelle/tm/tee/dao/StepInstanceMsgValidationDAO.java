package net.ihe.gazelle.tm.tee.dao;

import net.ihe.gazelle.tm.tee.exception.TestExecutionException;
import net.ihe.gazelle.tm.tee.model.MessageValidationService;
import net.ihe.gazelle.tm.tee.model.TmStepInstanceMessage;
import net.ihe.gazelle.tm.tee.model.TmStepInstanceMsgValidation;
import net.ihe.gazelle.tm.tee.validation.ValidationResults;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Contains DAO methods for TmStepInstanceMessageValidation entity
 *
 * @author tnabeel
 */
public class StepInstanceMsgValidationDAO {

    private static final Logger LOG = LoggerFactory.getLogger(StepInstanceMsgValidationDAO.class);
    private EntityManager entityManager;

    public StepInstanceMsgValidationDAO(EntityManager entityManager) {
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
     * Creates tm_step_instance_msg_validation record in the database
     *
     * @param tmStepInstanceMessage
     * @param messageValidationService
     * @param validationResults
     */
    public void createTmStepInstanceMsgValidation(TmStepInstanceMessage tmStepInstanceMessage, MessageValidationService messageValidationService,
                                                  ValidationResults validationResults) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createTmStepInstanceMsgValidation");
        }
        TmStepInstanceMsgValidation tmStepInstanceMsgValidation = new TmStepInstanceMsgValidation();
        tmStepInstanceMsgValidation.setMessageValidationService(messageValidationService);
        tmStepInstanceMsgValidation.setTmStepInstanceMessage(tmStepInstanceMessage);
        try {
            if (validationResults.getValidationResults() != null) {
                tmStepInstanceMsgValidation.setValidationReport(validationResults.getValidationResults().getBytes(StandardCharsets.UTF_8.name()));
            }
        } catch (UnsupportedEncodingException e) {
            throw new TestExecutionException("createTmStepInstanceMsgValidation() failed", e);
        }
        tmStepInstanceMsgValidation.setValidationStatus(validationResults.getValidationStatus());
        createTmStepInstanceMsgValidation(tmStepInstanceMsgValidation);
    }

    /**
     * Persists a non-existent tmStepInstanceMsgValidation to the database
     *
     * @param tmStepInstanceMsgValidation
     */
    public void createTmStepInstanceMsgValidation(TmStepInstanceMsgValidation tmStepInstanceMsgValidation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createTmStepInstanceMsgValidation");
        }
        tmStepInstanceMsgValidation.setLastChanged(new Date());
        getEntityManager().persist(tmStepInstanceMsgValidation);
    }

    /**
     * Updates the database tmStepInstanceMsgValidation record with the provided tmStepInstanceMsgValidation
     *
     * @param tmStepInstanceMsgValidation
     */
    public void updateTmStepInstanceMsgValidation(TmStepInstanceMsgValidation tmStepInstanceMsgValidation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateTmStepInstanceMsgValidation");
        }
        tmStepInstanceMsgValidation.setLastChanged(new Date());
        getEntityManager().merge(tmStepInstanceMsgValidation);
    }

    public TmStepInstanceMsgValidation find(int id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("find");
        }

        Query query = getEntityManager().createNamedQuery("findTmStepInstanceMsgValidationByTsimvId");
        query.setParameter("id", id);
        return (TmStepInstanceMsgValidation) query.getSingleResult();

    }

}
