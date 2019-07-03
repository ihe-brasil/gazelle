/**
 *
 */

package net.ihe.gazelle.tm.tee.validation;

/**
 * @author rchitnis
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class NISTValidationResultParser extends DefaultHandler {

    private static final Logger LOG = LoggerFactory.getLogger(NISTValidationResultParser.class);

    protected byte[] result;
    protected String validationStatus;
    protected String currentText;
    protected String errorCountAsString;
    protected String warningCountAsString;

    /**
     * Sets the result and parse the document
     *
     * @param result
     */
    public NISTValidationResultParser(String result) {
        if (result != null) {
            this.result = result.getBytes(StandardCharsets.UTF_8);
            parse();
        } else {
            this.result = null;
        }
    }

    /**
     * Returns the validation status extracted from the XML string
     *
     * @return null if the "mes:ResultOfTest" element is not present in the XML string
     */
    public String getValidationStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValidationStatus");
        }
        return this.validationStatus;
    }

    /**
     * returns the number of errors
     *
     * @return -1 if the "mes:ErrorCount" element is not present in the XML string
     */
    public int getErrorCount() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getErrorCount");
        }
        if (errorCountAsString == null || errorCountAsString.isEmpty()) {
            return -1;
        } else {
            return Integer.parseInt(errorCountAsString);
        }
    }

    /**
     * returns the number of warnings
     *
     * @return -1 if the "mes:WarningCount" element is not present in the XML String
     */
    public int getWarningCount() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getWarningCount");
        }
        if (warningCountAsString == null || warningCountAsString.isEmpty()) {
            return -1;
        } else {
            return Integer.parseInt(warningCountAsString);
        }
    }

    protected void parse() {
        try {
            SAXParserFactory saxFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxFactory.newSAXParser();
            ByteArrayInputStream bais = new ByteArrayInputStream(result);
            Reader reader = new InputStreamReader(bais);
            InputSource source = new InputSource(reader);
            source.setEncoding(StandardCharsets.UTF_8.name());
            // parse file
            saxParser.parse(source, this);
        } catch (SAXException e) {
            LOG.error(e.getMessage());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        } catch (ParserConfigurationException e) {
            LOG.error(e.getMessage());
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("endElement");
        }
        if (qName.equals("mes:ResultOfTest")) {
            validationStatus = currentText;
        } else if (qName.equals("mes:ErrorCount")) {
            errorCountAsString = currentText;
        } else if (qName.equals("mes:WarningCount")) {
            warningCountAsString = currentText;
        }
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        currentText = new String(ch, start, length);
    }

}
