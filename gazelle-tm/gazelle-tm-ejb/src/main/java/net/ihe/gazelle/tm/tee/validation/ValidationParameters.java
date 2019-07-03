package net.ihe.gazelle.tm.tee.validation;

import net.ihe.gazelle.tm.tee.model.ValidationServiceEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationParameters {
    private static final Logger LOG = LoggerFactory.getLogger(ValidationParameters.class);

    private String domainKeyword;
    private String actorKeyword;
    private String transactionKeyword;
    private String controlCode;
    private String messageType;
    private String hl7Version;
    private ValidationServiceEnum validationServiceEnum;
    private String serviceUrl;
    private String messageToValidate;

    private String profileOid;
    private String profileContent;
    private String validationContextContent;

    public ValidationParameters(String domainKeyword, String actorKeyword, String transactionKeyword, String controlCode,
                                String messageType, String hl7Version, ValidationServiceEnum validationServiceEnum, String serviceUrl, String
                                        messageToValidate) {
        this(domainKeyword, actorKeyword, transactionKeyword, controlCode, messageType, hl7Version, validationServiceEnum, serviceUrl, null, null,
                null, messageToValidate);
    }

    public ValidationParameters(String domainKeyword, String actorKeyword, String transactionKeyword, String controlCode,
                                String messageType, String hl7Version, ValidationServiceEnum validationServiceEnum, String serviceUrl,
                                String profileOid, String profileContent, String validationContextContent, String messageToValidate) {
        this.domainKeyword = domainKeyword;
        this.actorKeyword = actorKeyword;
        this.transactionKeyword = transactionKeyword;
        this.controlCode = controlCode;
        this.messageType = messageType;
        this.hl7Version = hl7Version;
        this.validationServiceEnum = validationServiceEnum;
        this.serviceUrl = serviceUrl;
        this.profileOid = profileOid;
        this.profileContent = profileContent;
        this.validationContextContent = validationContextContent;
        this.messageToValidate = messageToValidate;
    }

    public String getDomainKeyword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDomainKeyword");
        }
        return domainKeyword;
    }

    public void setDomainKeyword(String domainKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDomainKeyword");
        }
        this.domainKeyword = domainKeyword;
    }

    public String getActorKeyword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorKeyword");
        }
        return actorKeyword;
    }

    public void setActorKeyword(String actorKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setActorKeyword");
        }
        this.actorKeyword = actorKeyword;
    }

    public void setActorId(String actorKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setActorId");
        }
        this.actorKeyword = actorKeyword;
    }

    public String getTransactionKeyword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTransactionKeyword");
        }
        return transactionKeyword;
    }

    public void setTransactionKeyword(String transactionKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTransactionKeyword");
        }
        this.transactionKeyword = transactionKeyword;
    }

    public String getControlCode() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getControlCode");
        }
        return controlCode;
    }

    public void setControlCode(String controlCode) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setControlCode");
        }
        this.controlCode = controlCode;
    }

    public String getMessageType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMessageType");
        }
        return messageType;
    }

    public void setMessageType(String messageType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMessageType");
        }
        this.messageType = messageType;
    }

    public String getHl7Version() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getHl7Version");
        }
        return hl7Version;
    }

    public void setHl7Version(String hl7Version) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setHl7Version");
        }
        this.hl7Version = hl7Version;
    }

    public ValidationServiceEnum getValidationServiceEnum() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValidationServiceEnum");
        }
        return validationServiceEnum;
    }

    public void setValidationServiceEnum(ValidationServiceEnum validationServiceEnum) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setValidationServiceEnum");
        }
        this.validationServiceEnum = validationServiceEnum;
    }

    public String getServiceUrl() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getServiceUrl");
        }
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setServiceUrl");
        }
        this.serviceUrl = serviceUrl;
    }

    public String getMessageToValidate() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMessageToValidate");
        }
        return messageToValidate;
    }

    public void setMessageToValidate(String messageToValidate) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMessageToValidate");
        }
        this.messageToValidate = messageToValidate;
    }

    public String getProfileContent() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProfileContent");
        }
        return profileContent;
    }

    public void setProfileContent(String profileContent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setProfileContent");
        }
        this.profileContent = profileContent;
    }

    public String getValidationContextContent() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValidationContextContent");
        }
        return validationContextContent;
    }

    public void setValidationContextContent(String validationContextContent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setValidationContextContent");
        }
        this.validationContextContent = validationContextContent;
    }

    public String getProfileOid() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProfileOid");
        }
        return profileOid;
    }

    public void setProfileOid(String profileOid) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setProfileOid");
        }
        this.profileOid = profileOid;
    }

    @Override
    public String toString() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("toString");
        }
        return "ValidationParameters [domainKeyword=" + domainKeyword + ", actorKeyword=" + actorKeyword
                + ", transactionKeyword=" + transactionKeyword + ", controlCode=" + controlCode + ", messageType="
                + messageType + ", hl7Version=" + hl7Version + ", validationServiceEnum=" + validationServiceEnum + ", serviceUrl=" + serviceUrl
                + ",profileOid=" + profileOid + ", profileContent is null: " + (profileContent == null) + ",\n validationContextContent=" +
                validationContextContent + "]";
    }

}
