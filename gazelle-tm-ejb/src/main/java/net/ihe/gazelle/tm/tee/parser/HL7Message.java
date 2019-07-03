package net.ihe.gazelle.tm.tee.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base HL7 Message representing common attributes for HL7 messages
 *
 * @author tnabeel
 */
public class HL7Message {

    private static final Logger LOG = LoggerFactory.getLogger(HL7Message.class);
    private String messageContents;
    private String hL7Version;
    private String mshSegment;
    private String messageType;
    private String encodingCharacters;
    private String sendingApplication;
    private String sendingFacility;
    private String receivingApplication;
    private String receivingFacility;
    private String dateTimeOfMessage;
    private String security;
    private String messageControlID;
    private String processingID;
    private String sequenceNumber;
    private String continuationPointer;
    private String acceptAcknowledgmentType;
    private String applicationAcknowledgmentType;
    private String countryCode;
    private String characterSet;
    private String principalLanguageOfMessage;

    public String getMessageContents() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMessageContents");
        }
        return messageContents;
    }

    public void setMessageContents(String messageContents) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMessageContents");
        }
        this.messageContents = messageContents;
    }

    public String gethL7Version() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("gethL7Version");
        }
        return hL7Version;
    }

    public void sethL7Version(String hL7Version) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("sethL7Version");
        }
        this.hL7Version = hL7Version;
    }

    public String getMshSegment() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMshSegment");
        }
        return mshSegment;
    }

    public void setMshSegment(String mshSegment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMshSegment");
        }
        this.mshSegment = mshSegment;
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

    public String getEncodingCharacters() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEncodingCharacters");
        }
        return encodingCharacters;
    }

    public void setEncodingCharacters(String encodingCharacters) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setEncodingCharacters");
        }
        this.encodingCharacters = encodingCharacters;
    }

    public String getSendingApplication() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSendingApplication");
        }
        return sendingApplication;
    }

    public void setSendingApplication(String sendingApplication) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSendingApplication");
        }
        this.sendingApplication = sendingApplication;
    }

    public String getSendingFacility() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSendingFacility");
        }
        return sendingFacility;
    }

    public void setSendingFacility(String sendingFacility) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSendingFacility");
        }
        this.sendingFacility = sendingFacility;
    }

    public String getReceivingApplication() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getReceivingApplication");
        }
        return receivingApplication;
    }

    public void setReceivingApplication(String receivingApplication) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setReceivingApplication");
        }
        this.receivingApplication = receivingApplication;
    }

    public String getReceivingFacility() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getReceivingFacility");
        }
        return receivingFacility;
    }

    public void setReceivingFacility(String receivingFacility) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setReceivingFacility");
        }
        this.receivingFacility = receivingFacility;
    }

    public String getDateTimeOfMessage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDateTimeOfMessage");
        }
        return dateTimeOfMessage;
    }

    public void setDateTimeOfMessage(String dateTimeOfMessage) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDateTimeOfMessage");
        }
        this.dateTimeOfMessage = dateTimeOfMessage;
    }

    public String getSecurity() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSecurity");
        }
        return security;
    }

    public void setSecurity(String security) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSecurity");
        }
        this.security = security;
    }

    public String getMessageControlID() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMessageControlID");
        }
        return messageControlID;
    }

    public void setMessageControlID(String messageControlID) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMessageControlID");
        }
        this.messageControlID = messageControlID;
    }

    public String getProcessingID() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProcessingID");
        }
        return processingID;
    }

    public void setProcessingID(String processingID) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setProcessingID");
        }
        this.processingID = processingID;
    }

    public String getSequenceNumber() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSequenceNumber");
        }
        return sequenceNumber;
    }

    public void setSequenceNumber(String sequenceNumber) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSequenceNumber");
        }
        this.sequenceNumber = sequenceNumber;
    }

    public String getContinuationPointer() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getContinuationPointer");
        }
        return continuationPointer;
    }

    public void setContinuationPointer(String continuationPointer) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setContinuationPointer");
        }
        this.continuationPointer = continuationPointer;
    }

    public String getAcceptAcknowledgmentType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAcceptAcknowledgmentType");
        }
        return acceptAcknowledgmentType;
    }

    public void setAcceptAcknowledgmentType(String acceptAcknowledgmentType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setAcceptAcknowledgmentType");
        }
        this.acceptAcknowledgmentType = acceptAcknowledgmentType;
    }

    public String getApplicationAcknowledgmentType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getApplicationAcknowledgmentType");
        }
        return applicationAcknowledgmentType;
    }

    public void setApplicationAcknowledgmentType(String applicationAcknowledgmentType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setApplicationAcknowledgmentType");
        }
        this.applicationAcknowledgmentType = applicationAcknowledgmentType;
    }

    public String getCountryCode() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCountryCode");
        }
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCountryCode");
        }
        this.countryCode = countryCode;
    }

    public String getCharacterSet() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCharacterSet");
        }
        return characterSet;
    }

    public void setCharacterSet(String characterSet) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCharacterSet");
        }
        this.characterSet = characterSet;
    }

    public String getPrincipalLanguageOfMessage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPrincipalLanguageOfMessage");
        }
        return principalLanguageOfMessage;
    }

    public void setPrincipalLanguageOfMessage(String principalLanguageOfMessage) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setPrincipalLanguageOfMessage");
        }
        this.principalLanguageOfMessage = principalLanguageOfMessage;
    }

}
