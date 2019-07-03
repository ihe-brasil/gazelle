package net.ihe.gazelle.tm.tee.parser;

import net.ihe.gazelle.tm.tee.exception.TestExecutionException;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides factory method for instantiating the right HL7MsgParser
 *
 * @author tnabeel
 */
public class HL7MsgParserFactory {

    private static final Logger LOG = LoggerFactory.getLogger(HL7MsgParserFactory.class);
    private static final HL7MsgParserFactory me = new HL7MsgParserFactory();

    private HL7MsgParserFactory() {
    }

    public static HL7MsgParserFactory getInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("HL7MsgParserFactory getInstance");
        }
        return me;
    }

    public HL7MessageParser createHL7MsgParser(String messageContents) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createHL7MsgParser");
        }
        Validate.notNull(messageContents);
        Validate.notEmpty(messageContents);
        HL7MessageParser parser = null;
        if (isXML(messageContents)) {
//            parser = new HL7V3MessageParser(messageContents);
            throw new TestExecutionException("createHL7MsgParser() failed: XML message not supported at this time");
        } else {
            parser = new HL7V2MessageParser(messageContents);
        }
        return parser;
    }

    private boolean isXML(String messageContents) {
        return false;
    }

}
