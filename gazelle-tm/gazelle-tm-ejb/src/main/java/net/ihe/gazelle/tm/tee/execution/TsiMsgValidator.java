package net.ihe.gazelle.tm.tee.execution;

import net.ihe.gazelle.tf.model.Hl7MessageProfile;
import net.ihe.gazelle.tm.tee.dao.StepInstanceMsgValidationDAO;
import net.ihe.gazelle.tm.tee.model.MessageValidationService;
import net.ihe.gazelle.tm.tee.model.TmStepInstanceMessage;
import net.ihe.gazelle.tm.tee.validation.MessageValidator;
import net.ihe.gazelle.tm.tee.validation.ValidationParameters;
import net.ihe.gazelle.tm.tee.validation.ValidationResults;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;

/**
 * This class is responsible for constructing and providing ValidationParameters to MessageValidator and persisting
 * the ValidationResults from MessageValidator to the database.
 *
 * @author tnabeel
 */
public class TsiMsgValidator {

    private static final Logger LOG = LoggerFactory.getLogger(TsiMsgValidator.class);

    private EntityManager entityManager;

    protected TsiMsgValidator(EntityManager entityManager) {
        Validate.notNull(entityManager);
        this.entityManager = entityManager;
    }

    private EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * This method validates the captured tmStepInstanceMessage contents using one or more validators and persists
     * the validation results to the database.
     *
     * @param proxyMsgData
     * @param tmStepInstanceMessage
     */
    protected void validateMessageAndPersistResults(ProxyMsgData proxyMsgData,
                                                    TmStepInstanceMessage tmStepInstanceMessage) {

        Validate.notNull(tmStepInstanceMessage.getTmTestStepMessageProfile());
        Validate.notNull(tmStepInstanceMessage.getTmTestStepMessageProfile().getTfHl7MessageProfile());

        // Initialize ValidationParameters
        Hl7MessageProfile hl7MessageProfile = tmStepInstanceMessage.getTmTestStepMessageProfile().getTfHl7MessageProfile();
        Validate.notNull(hl7MessageProfile.getDomain().getKeyword());
        Validate.notEmpty(hl7MessageProfile.getDomain().getKeyword());

        Validate.notNull(hl7MessageProfile.getActor().getKeyword());
        Validate.notEmpty(hl7MessageProfile.getActor().getKeyword());

        Validate.notNull(hl7MessageProfile.getTransaction().getKeyword());
        Validate.notEmpty(hl7MessageProfile.getTransaction().getKeyword());

        Validate.notNull(hl7MessageProfile.getMessageType());
        Validate.notEmpty(hl7MessageProfile.getMessageType());

        Validate.notNull(hl7MessageProfile.getHl7Version());
        Validate.notEmpty(hl7MessageProfile.getHl7Version());

        for (MessageValidationService messageValidationService : tmStepInstanceMessage.getTmTestStepMessageProfile().getMessageValidationServices()) {
            ValidationParameters validationParams = new ValidationParameters(hl7MessageProfile.getDomain().getKeyword(),
                    hl7MessageProfile.getActor().getKeyword(),
                    hl7MessageProfile.getTransaction().getKeyword(),
                    hl7MessageProfile.getMessageOrderControlCode(),
                    hl7MessageProfile.getMessageType(),
                    hl7MessageProfile.getHl7Version(),
                    messageValidationService.getValidationService().getKey(),
                    messageValidationService.getValidationService().getBaseUrl() + (messageValidationService.getValidationService().getUrlPath() !=
                            null ? messageValidationService.getValidationService().getUrlPath() : ""),
                    hl7MessageProfile.getProfileOid(), hl7MessageProfile.getProfileContent(), tmStepInstanceMessage.getTmTestStepMessageProfile()
                    .getValidationContextContent(),
                    tmStepInstanceMessage.getMessageContentsAsString());

            // Invoke MessageValidator
            ValidationResults validationResults = MessageValidator.validate(validationParams);

            Validate.notNull(validationResults);
            Validate.notNull(validationResults.getValidationStatus());

            // Persist ValidationResults
            new StepInstanceMsgValidationDAO(getEntityManager()).createTmStepInstanceMsgValidation(tmStepInstanceMessage, messageValidationService,
                    validationResults);
        }
    }

}
