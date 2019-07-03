package net.ihe.gazelle.tm.tee.parser;

import net.ihe.gazelle.tm.tee.exception.TestExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * This class is responsible for extracting HL7 V2 message meta-data
 *
 * @author rizwan.tanoli@aegis.net
 * @author tnabeel
 */
public class HL7V2MessageParser extends HL7MessageParser {

    /**
     * Field locations are defined in the HL7 Specification. Since this class is looking for the field values inside a Java Array,
     * the location of the field differs from the HL7 Spec by a factor of -1.
     */
    public static final int LOCATION_OF_LINE_SEPARATOR_CHAR = 3;
    public static final int LOCATION_OF_HL7_VERSION_STRING = 11;
    public static final int LOCATION_OF_MSG_TYPE_STRING = 8;
    // segments in the "classic HL7 v2 syntax" are seperated
    // by a carriage return. This regex splits the method
    // by carriage return (OS independent).
    // reference: http://docs.oracle.com/javase/1.5.0/docs/api/java/io/BufferedReader.html#readLine%28%29
    public static final String CARRIAGE_RETURN_REGEX = "\r\n|\r|\n";
    // name of the Message Header Segment = MSH
    public static final String MSH_SEGMENT = "MSH";
    private static final Logger LOG = LoggerFactory.getLogger(HL7V2MessageParser.class);

    public HL7V2MessageParser(String messageContents) {
        super(messageContents);
    }

    @Override
    public String loadMessageContents() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("loadMessageContents");
        }
        int numberOfLines = 0;
        StringBuilder message = new StringBuilder();
        String line;

        BufferedReader reader = new BufferedReader(new StringReader(getMessageContents()));
        try {
            while ((line = reader.readLine()) != null) {
                if (numberOfLines == 0) {
                    message.append(line);
                } else {
                    message.append("\r").append(line);
                }
                numberOfLines++;
            }
            return message.toString();
        } catch (IOException e) {
            throw new TestExecutionException("loadMessageContents() failed", e);
        }

    }

    /**
     * Extracts the MSH segment from the supplied HL7 Message. MSH segment is a required segment for every HL7 Message.
     * It is the first segment in every HL7 Message. MSH = Message Header Segment
     */
    @Override
    public String extractMSHSegment() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractMSHSegment");
        }
        String mshSegement = null;
        String[] segments = getMessageContents().split(CARRIAGE_RETURN_REGEX);
        if (0 < segments.length) {
            if (segments[0].substring(0, 3).equalsIgnoreCase(MSH_SEGMENT)) {
                mshSegement = segments[0];
            } else {
                throw new TestExecutionException("MSH Segment is not the first segment. Please check message structure.");
            }
        }
        return mshSegement;
    }

    /**
     * Extracts the HL7 version number from the supplied MSH Segment. HL7 Version number is Field # 12 in the MSH Segment.
     */
    @Override
    public String extractHL7Version(HL7Message message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractHL7Version");
        }
        String hl7Version = null;
        if (message.getMshSegment() != null) {
            String lineSeparator = Character.toString(message.getMshSegment().charAt(LOCATION_OF_LINE_SEPARATOR_CHAR));
            hl7Version = message.getMshSegment().split("\\" + lineSeparator)[LOCATION_OF_HL7_VERSION_STRING];
        } else {
            throw new IllegalStateException("FATAL ERROR: MSH Segment is NULL.");
        }
        return hl7Version;
    }

    /**
     * Extracts the HL7 Message Type from the supplied MSH Segment. HL7 Message type is Field # 9 in the MSH Segment.
     */
    @Override
    public String extractMessageType(HL7Message message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractMessageType");
        }
        String msgType = null;
        if (message.getMshSegment() != null) {
            String lineSeparator = Character.toString(message.getMshSegment().charAt(LOCATION_OF_LINE_SEPARATOR_CHAR));
            msgType = message.getMshSegment().split("\\" + lineSeparator)[LOCATION_OF_MSG_TYPE_STRING];
            if (msgType != null) {
                String[] tokens = msgType.split("\\^");
                if (tokens != null) {
                    if (tokens.length > 1) {
                        msgType = tokens[0] + "_" + tokens[1];
                    }
                } else {
                    throw new RuntimeException("Message Type missing in MSH segment in message.");
                }
            }
        } else {
            throw new RuntimeException("MSH Segment missing in message.");
        }
        return msgType;

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
