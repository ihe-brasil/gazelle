package net.ihe.gazelle.tm.tee.validation;

import net.ihe.gazelle.common.utils.HibernateConfiguration;
import net.ihe.gazelle.hl7.validator.client.HL7v2Validator;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.Hl7MessageProfile;
import net.ihe.gazelle.tm.tee.model.ValidationService;
import net.ihe.gazelle.tm.tee.model.ValidationServiceEnum;
import net.ihe.gazelle.tm.tee.model.ValidationStatus;
import net.ihe.gazelle.tm.tee.model.ValidationStatusEnum;
import net.ihe.nist.hl7.validator.client.NISTHL7v2Validator;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * This class acts as an interface to Gazelle and NIST validation service
 */
public class MessageValidator {
    private static final Logger LOG = LoggerFactory.getLogger(MessageValidator.class);

    public static void main(String[] args) {

        if (args.length < 7) {
            LOG.error("Please specify service type, domain keyword, actor keyword, transaction keyword, message type, HL7 version and the message " +
                    "to validate");
            return;
        }

        BasicConfigurator.configure();

        HibernateConfiguration.setConfigOnCurrentThread("jdbc:postgresql://" + "localhost" + "/" + "gazelle",
                "auto", false);

        ValidationParameters params = populateData(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
        ValidationResults results = validate(params);

        if (results == null) {
            LOG.error("Could not invoke Validation Service");
        }
    }

    /**
     * Validates passed in HL7 message using supplied parameters
     *
     * @param params validation parameters consisting of service type, domain, actor, transaction, message type, hl7 version
     * @return validation results
     */
    public static synchronized ValidationResults validate(ValidationParameters params) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("synchronized ValidationResults validate");
        }

        if (params.getValidationServiceEnum() == null || params.getServiceUrl() == null ||
                params.getDomainKeyword() == null || params.getDomainKeyword().isEmpty() ||
                params.getActorKeyword() == null || params.getActorKeyword().isEmpty() ||
                params.getTransactionKeyword() == null || params.getTransactionKeyword().isEmpty() ||
                params.getMessageType() == null || params.getMessageType().isEmpty() ||
                params.getHl7Version() == null || params.getHl7Version().isEmpty()) {
            return null;
        }

        switch (params.getValidationServiceEnum()) {
            case GAZELLEHL7V2VALIDATOR:
                return validateWithGazelle(params);

            case NISTHL7V2VALIDATOR:
                return validateWithNIST(params);

            default:
                LOG.error("Invalid service type");
                return null;
        }
    }

    /**
     * Validates passed in HL7 message using supplied parameters and invoking gazelle validator service
     *
     * @param params validation parameters consisting of service type, domain, actor, transaction, message type, hl7 version
     * @return validation results
     */
    private static ValidationResults validateWithGazelle(ValidationParameters params) {
        //
        String profileOid = getProfileOid(params.getDomainKeyword(), params.getActorKeyword(), params.getTransactionKeyword(), params
                .getControlCode(), params.getMessageType(), params.getHl7Version());

        HL7v2Validator validator = new HL7v2Validator(params.getServiceUrl());
        String result = validator.validate(params.getMessageToValidate(), profileOid, StandardCharsets.UTF_8.name());

        return new ValidationResults(mapGazelleValidationStatus(validator.getLastValidationStatus(),
                validator.getWarningsCountForLastValidation()), result);
    }

    private static ValidationResults validateWithNIST(ValidationParameters params) {

        NISTHL7v2Validator client = new NISTHL7v2Validator(params.getServiceUrl());

        String xmlResults = client.validate(params.getMessageToValidate(), params.getProfileOid(), params.getProfileContent(), params
                .getValidationContextContent());

        NISTValidationResultParser resultParser = new NISTValidationResultParser(xmlResults);

        return new ValidationResults(mapNISTValidationStatus(resultParser.getValidationStatus(), resultParser.getWarningCount()), xmlResults);
    }

    /**
     * Retrieves gazelle profile OID using supplied parameters
     *
     * @param domainKeyword      domain
     * @param actorKeyword       actor
     * @param transactionKeyword transaction
     * @param controlCode        control code
     * @param messageType        message type
     * @param hl7Version         hl7 version
     * @return profile OID
     */
    private static String getProfileOid(String domainKeyword, String actorKeyword, String transactionKeyword, String controlCode, String
            messageType, String hl7Version) {
        // the call below should return only one profile in the list based on the specified criteria
        List<Hl7MessageProfile> profileList = Hl7MessageProfile.getHL7MessageProfilesFiltered(domainKeyword, actorKeyword, transactionKeyword,
                hl7Version, messageType, controlCode);

        return (profileList == null ? null : profileList.get(0).getProfileOid());
    }

    private static ValidationParameters populateData(String serviceTypeKey,
                                                     String domainKeyword,
                                                     String actorKeyword,
                                                     String transactionKeyword,
                                                     String messageType,
                                                     String hl7Version,
                                                     String message) {
        ValidationService type = null;
        String messageToValidate = message;

        if (ValidationServiceEnum.NISTHL7V2VALIDATOR.name().equalsIgnoreCase(serviceTypeKey)) {

            type = getValidationServiceByKey(ValidationServiceEnum.NISTHL7V2VALIDATOR);
        } else if (ValidationServiceEnum.GAZELLEHL7V2VALIDATOR.name().equalsIgnoreCase(serviceTypeKey)) {

            type = getValidationServiceByKey(ValidationServiceEnum.GAZELLEHL7V2VALIDATOR);
        }
        ValidationParameters params = null;
        if (type != null) {
            params = new ValidationParameters(domainKeyword, actorKeyword, transactionKeyword, null,
                    messageType, hl7Version, type.getKey(), type.getBaseUrl() + (type.getUrlPath() != null ? type.getUrlPath() : ""),
                    messageToValidate);
        }

        return params;

    }

    private static String getValue(String tag, Element element) {
        NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodes.item(0);
        return node.getNodeValue();
    }

    /**
     * Retrieves validation status for a given status enum
     *
     * @param key status enum
     * @return validation status
     */
    private static ValidationStatus getValidationStatusByKey(ValidationStatusEnum key) {

        EntityManager em = EntityManagerService.provideEntityManager();
        Query query = em.createQuery("FROM ValidationStatus vs where vs.key=:key");
        query.setParameter("key", key);
        query.setHint("org.hibernate.cacheable", true);
        return (ValidationStatus) query.getSingleResult();
    }

    /**
     * Retrieves validation service type for a given service type enum
     *
     * @param key service type enum
     * @return validation service type
     */
    private static ValidationService getValidationServiceByKey(ValidationServiceEnum key) {

        EntityManager em = EntityManagerService.provideEntityManager();
        Query query = em.createQuery("FROM ValidationService vs where vs.key=:key");
        query.setParameter("key", key);
        query.setHint("org.hibernate.cacheable", true);
        return (ValidationService) query.getSingleResult();
    }

    /**
     * Translates Gazelle validation status into TEE validation status
     *
     * @param gazelleValidationStatus validation status returned by Gazelle validation service
     * @return TEE validation status
     */
    private static ValidationStatus mapGazelleValidationStatus(String gazelleValidationStatus, int warningCount) {
        /*
         * In HapiValidator.java of GazelleHL7v2Validator-ejb module, the validation status is set as follows:
		 * INVALID_REQUEST	No profile OID provided OR The given profile OID does not match any HL7 message profile
		 * ABORTED			The HL7 message profile is not parsable or The HL7 message is not parsable
		 * PASSED			Error count is zero
		 * FAILED			Error count is greater than zero
		 * There is no status value defined if there are warnings. It will have to be derived from the warning count.
		 */
        ValidationStatus status = getValidationStatusByKey(ValidationStatusEnum.FAIL);
        if (gazelleValidationStatus == null ||
                gazelleValidationStatus.equalsIgnoreCase("INVALID_REQUEST") ||
                gazelleValidationStatus.equalsIgnoreCase("ABORTED")) {
            status = getValidationStatusByKey(ValidationStatusEnum.ERROR);
        }
        if (gazelleValidationStatus != null) {
            if (gazelleValidationStatus.equalsIgnoreCase("PASSED") && warningCount == 0) {
                status = getValidationStatusByKey(ValidationStatusEnum.PASS);
            }
            if (gazelleValidationStatus.equalsIgnoreCase("PASSED") && warningCount > 0) {
                status = getValidationStatusByKey(ValidationStatusEnum.WARNING);
            }
            if (gazelleValidationStatus.equalsIgnoreCase("FAILED")) {
                status = getValidationStatusByKey(ValidationStatusEnum.FAIL);
            }
        }
        return status;
    }

    private static ValidationStatus mapNISTValidationStatus(String nistValidationStatus, int warningCount) {
        /*
         * In NIST Validator, the validation status is set as follows:
		 * INCONCLUSIVE		No documentation is available to describe this value
		 * PASSED			Error count is zero
		 * FAILED			Error count is greater than zero
		 * There is no status value defined if there are warnings. It will have to be derived from the warning count.
		 */
        ValidationStatus status = getValidationStatusByKey(ValidationStatusEnum.FAIL);
        if (nistValidationStatus == null) {
            status = getValidationStatusByKey(ValidationStatusEnum.ERROR);
        } else if (nistValidationStatus.equalsIgnoreCase("INCONCLUSIVE")) {
            status = getValidationStatusByKey(ValidationStatusEnum.ERROR);
        } else if (nistValidationStatus.equalsIgnoreCase("PASSED") && warningCount == 0) {
            status = getValidationStatusByKey(ValidationStatusEnum.PASS);
        } else if (nistValidationStatus.equalsIgnoreCase("PASSED") && warningCount > 0) {
            status = getValidationStatusByKey(ValidationStatusEnum.WARNING);
        } else if (nistValidationStatus.equalsIgnoreCase("FAILED")) {
            status = getValidationStatusByKey(ValidationStatusEnum.FAIL);
        }
        return status;
    }

}