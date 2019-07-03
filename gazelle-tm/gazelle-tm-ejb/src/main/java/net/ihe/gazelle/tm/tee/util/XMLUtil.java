package net.ihe.gazelle.tm.tee.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Contains utility methods for handling XML
 *
 * @author tnabeel
 */
public class XMLUtil {

    private static final Logger LOG = LoggerFactory.getLogger(XMLUtil.class);

    public static Document loadDocument(byte[] xmlContents) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(
                    xmlContents));
            return document;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getPrettyContents(byte[] xmlContents) {
        try {

            return getPrettyContents(loadDocument(xmlContents));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getPrettyContents(String xmlContents) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("String getPrettyContents");
        }
        try {
            return getPrettyContents(xmlContents.getBytes(StandardCharsets.UTF_8.name()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getPrettyContents(Document document) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("String getPrettyContents");
        }
        return getPrettyContents(new DOMSource(document));
    }

    public static String getPrettyContents(Source source) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("String getPrettyContents");
        }
        TransformerFactory tfactory = TransformerFactory.newInstance();
        Transformer serializer;
        try {
            serializer = tfactory.newTransformer();
            //Setup indenting to "pretty print"
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            serializer.setOutputProperty(OutputKeys.METHOD, "xml");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-number", "1");

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            serializer.transform(source, new StreamResult(bos));
            bos.close();
            return new String(bos.toByteArray(), StandardCharsets.UTF_8.name());
        } catch (TransformerException e) {
            throw new RuntimeException("getNodeContents failed", e);
        }
    }
}
