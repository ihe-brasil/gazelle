package net.ihe.gazelle.tm.tee.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides methods for extraction of metadata from an HL7 Message by primarily looking at the MSH Segment of the message.
 *
 * @author rizwan.tanoli@aegis.net
 * @author tnabeel
 */
public abstract class HL7MessageParser {

    private static final Logger LOG = LoggerFactory.getLogger(HL7MessageParser.class);

    private String messageContents;

    protected HL7MessageParser(String messageContents) {
        this.messageContents = messageContents;
    }

    protected abstract String loadMessageContents();

    protected abstract String extractMSHSegment();

    protected abstract String extractHL7Version(HL7Message message);

    protected abstract String extractMessageType(HL7Message message);

    protected abstract String extractEncodingCharacters(HL7Message message);

    protected abstract String extractSendingApplication(HL7Message message);

    protected abstract String extractSendingFacility(HL7Message message);

    protected abstract String extractReceivingApplication(HL7Message message);

    protected abstract String extractReceivingFacility(HL7Message message);

    protected abstract String extractDateTimeOfMessage(HL7Message message);

    protected abstract String extractSecurity(HL7Message message);

    protected abstract String extractMessageControlID(HL7Message message);

    protected abstract String extractProcessingID(HL7Message message);

    protected abstract String extractSequenceNumber(HL7Message message);

    protected abstract String extractContinuationPointer(HL7Message message);

    protected abstract String extractAcceptAcknowledgmentType(HL7Message message);

    protected abstract String extractApplicationAcknowledgmentType(HL7Message message);

    protected abstract String extractCountryCode(HL7Message message);

    protected abstract String extractCharacterSet(HL7Message message);

    protected abstract String extractPrincipalLanguageOfMessage(HL7Message message);

    /**
     * This method has to be called by clients of HL7MessageParser in order for parser
     * to extract needed fields from the message.
     *
     * @return
     */
    public HL7Message parseMessage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("parseMessage");
        }
        HL7Message hl7Message = new HL7Message();
        hl7Message.setMessageContents(loadMessageContents());
        hl7Message.setMshSegment(extractMSHSegment());
        hl7Message.sethL7Version(extractHL7Version(hl7Message));
        hl7Message.setMessageType(extractMessageType(hl7Message));
        hl7Message.setEncodingCharacters(extractEncodingCharacters(hl7Message));
        hl7Message.setSendingApplication(extractSendingApplication(hl7Message));
        hl7Message.setSendingFacility(extractSendingFacility(hl7Message));
        hl7Message.setReceivingApplication(extractReceivingApplication(hl7Message));
        hl7Message.setReceivingFacility(extractReceivingFacility(hl7Message));
        hl7Message.setDateTimeOfMessage(extractDateTimeOfMessage(hl7Message));
        hl7Message.setSecurity(extractSecurity(hl7Message));
        hl7Message.setMessageControlID(extractMessageControlID(hl7Message));
        hl7Message.setProcessingID(extractProcessingID(hl7Message));
        hl7Message.setSequenceNumber(extractSequenceNumber(hl7Message));
        hl7Message.setContinuationPointer(extractContinuationPointer(hl7Message));
        hl7Message.setAcceptAcknowledgmentType(extractAcceptAcknowledgmentType(hl7Message));
        hl7Message.setApplicationAcknowledgmentType(extractApplicationAcknowledgmentType(hl7Message));
        hl7Message.setCountryCode(extractCountryCode(hl7Message));
        hl7Message.setCharacterSet(extractCharacterSet(hl7Message));
        hl7Message.setPrincipalLanguageOfMessage(extractPrincipalLanguageOfMessage(hl7Message));
        return hl7Message;
    }

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

}
