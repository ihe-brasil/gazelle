package net.ihe.gazelle.tm.tee.parser;

import net.ihe.gazelle.tm.tee.util.FileUtil;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * JUnit tests for parsing of HL7 messages via HL7MsgParserFactory, HL7MessageParser, and HL7Message
 *
 * @author rizwan.tanoli@aegis.net
 * @author tnabeel
 */
public class HL7MessageParserTest {

    @Test
    public void parseJeffMsg() throws Exception {
        String messageContents = FileUtil.loadFileContents("src/test/resources/hl7Messages/QBP_Q22_QBP_Q21.txt");
        HL7MessageParser parser = HL7MsgParserFactory.getInstance().createHL7MsgParser(messageContents);
        HL7Message message = parser.parseMessage();
        System.out.println("HL7 Version=" + message.gethL7Version() + " MessageType=" + message.getMessageType() + "\n");
        assertNotNull(message.gethL7Version());
        assertNotNull(message.getMessageType());
        assertEquals("QBP_Q22", message.getMessageType());
    }

    @Test
    public void parseMsg11Asc() throws Exception {
        String messageContents = FileUtil.loadFileContents("src/test/resources/hl7Messages/11.asc");
        HL7MessageParser parser = HL7MsgParserFactory.getInstance().createHL7MsgParser(messageContents);
        HL7Message message = parser.parseMessage();
        System.out.println("HL7 Version=" + message.gethL7Version() + " MessageType=" + message.getMessageType() + "\n");
        assertNotNull(message.gethL7Version());
        assertNotNull(message.getMessageType());
    }

    @Test
    public void parseMsg18Asc() throws Exception {
        String messageContents = FileUtil.loadFileContents("src/test/resources/hl7Messages/18.asc");
        HL7MessageParser parser = HL7MsgParserFactory.getInstance().createHL7MsgParser(messageContents);
        HL7Message message = parser.parseMessage();
        System.out.println("HL7 Version=" + message.gethL7Version() + " MessageType=" + message.getMessageType() + "\n");
        assertNotNull(message.gethL7Version());
        assertNotNull(message.getMessageType());
    }

    @Test
    public void parseMsg21Asc() throws Exception {
        String messageContents = FileUtil.loadFileContents("src/test/resources/hl7Messages/21.asc");
        HL7MessageParser parser = HL7MsgParserFactory.getInstance().createHL7MsgParser(messageContents);
        HL7Message message = parser.parseMessage();
        System.out.println("HL7 Version=" + message.gethL7Version() + " MessageType=" + message.getMessageType() + "\n");
        assertNotNull(message.gethL7Version());
        assertNotNull(message.getMessageType());
    }

    @Test
    public void parseMsgAckA01() throws Exception {
        String messageContents = FileUtil.loadFileContents("src/test/resources/hl7Messages/ACK_A01.asc");
        HL7MessageParser parser = HL7MsgParserFactory.getInstance().createHL7MsgParser(messageContents);
        HL7Message message = parser.parseMessage();
        System.out.println("HL7 Version=" + message.gethL7Version() + " MessageType=" + message.getMessageType() + "\n");
        assertNotNull(message.gethL7Version());
        assertNotNull(message.getMessageType());
    }
}
