package net.ihe.gazelle.tm.tee.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for extracting HL7 V3 message meta-data
 *
 * @author rizwan.tanoli@aegis.net
 * @author tnabeel
 */
public class HL7V3MessageParser extends HL7MessageParser {

    private static final Logger LOG = LoggerFactory.getLogger(HL7V3MessageParser.class);

    protected HL7V3MessageParser(String messageContents) {
        super(messageContents);
    }

    @Override
    public String loadMessageContents() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("loadMessageContents");
        }
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String extractHL7Version(HL7Message message) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String extractMessageType(HL7Message message) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String extractMSHSegment() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractMSHSegment");
        }
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String extractEncodingCharacters(HL7Message message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractEncodingCharacters");
        }
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String extractSendingApplication(HL7Message message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractSendingApplication");
        }
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String extractSendingFacility(HL7Message message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractSendingFacility");
        }
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String extractReceivingApplication(HL7Message message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractReceivingApplication");
        }
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String extractReceivingFacility(HL7Message message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractReceivingFacility");
        }
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String extractDateTimeOfMessage(HL7Message message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractDateTimeOfMessage");
        }
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String extractSecurity(HL7Message message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractSecurity");
        }
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String extractMessageControlID(HL7Message message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractMessageControlID");
        }
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String extractProcessingID(HL7Message message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractProcessingID");
        }
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String extractSequenceNumber(HL7Message message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractSequenceNumber");
        }
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String extractContinuationPointer(HL7Message message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractContinuationPointer");
        }
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String extractAcceptAcknowledgmentType(HL7Message message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractAcceptAcknowledgmentType");
        }
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String extractApplicationAcknowledgmentType(HL7Message message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractApplicationAcknowledgmentType");
        }
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String extractCountryCode(HL7Message message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractCountryCode");
        }
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String extractCharacterSet(HL7Message message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractCharacterSet");
        }
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String extractPrincipalLanguageOfMessage(HL7Message message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractPrincipalLanguageOfMessage");
        }
        // TODO Auto-generated method stub
        return null;
    }

}
