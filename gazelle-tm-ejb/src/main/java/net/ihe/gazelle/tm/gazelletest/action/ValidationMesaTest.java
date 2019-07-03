package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.action.DateDisplay;
import net.ihe.gazelle.common.util.File;
import net.ihe.gazelle.tm.gazelletest.model.instance.Status;
import net.ihe.gazelle.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author abderrazek boufahja
 */
public class ValidationMesaTest {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationMesaTest.class);

    public static int FILE_AUTOMATICALLY_VERIFIED = 2;

    public static int FILE_AUTOMATICALLY_FAILED = 0;

    public static int FILE_NOT_AUTOMATICALLY_VERIFIED = 1;

    public ValidationMesaTest() {
    }

    public static List<Pair<Integer, String>> validateListFile(List<File> listf, String testKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateListFile");
        }
        List<Pair<Integer, String>> res = new ArrayList<Pair<Integer, String>>();
        for (File f : listf) {
            Pair<Integer, String> pp = new Pair<Integer, String>(validateFile(f, testKeyword), f.getName());
            res.add(pp);
        }
        return res;
    }

    public static String generateCommentFromValidationFiles(List<Pair<Integer, String>> lres) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateCommentFromValidationFiles");
        }
        StringBuilder res = new StringBuilder();
        res.append("( ").append(DateDisplay.instance().displayDateTime(new Date(), true)).append(" ) : ");
        for (Pair<Integer, String> pp : lres) {
            if (res.length() > 0) {
                res.append("\n");
            }
            if (pp.getObject1().equals(FILE_AUTOMATICALLY_VERIFIED)) {
                res.append("- file : ").append(pp.getObject2()).append(" was automatically verified.");
            } else if (pp.getObject1().equals(FILE_AUTOMATICALLY_FAILED)) {
                res.append("- file : ").append(pp.getObject2()).append(" was automatically failed.");
            } else if (pp.getObject1().equals(FILE_NOT_AUTOMATICALLY_VERIFIED)) {
                res.append("- file : ").append(pp.getObject2()).append(" can not be automatically verified.");
            }
        }
        if (res.length() > 0) {
            res.append("\n");
        }
        Status ss = getStatusOfListValidation(lres);
        if (ss == null) {
            res.append("test can not be automatically verified.");
        } else {
            if (ss.equals(Status.getSTATUS_FAILED())) {
                res.append("test was automatically failed.");
            } else if (ss.equals(Status.getSTATUS_VERIFIED())) {
                res.append("test was automatically verified.");
            } else if (ss.equals(Status.getSTATUS_PARTIALLY_VERIFIED())) {
                res.append("test was automatically partially verified.");
            }
        }
        return res.toString();
    }

    public static Status getStatusOfListValidation(List<Pair<Integer, String>> lres) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getStatusOfListValidation");
        }
        int i = 1;
        for (Pair<Integer, String> pp : lres) {
            if (pp.getObject1() != null) {
                i = i * pp.getObject1().intValue();
            }
        }
        if (i == 0) {
            return Status.getSTATUS_FAILED();
        }
        if (i == (int) (Math.pow(2, lres.size()))) {
            return Status.getSTATUS_VERIFIED();
        }
        if ((i > 1) && (i < (int) (Math.pow(2, lres.size())))) {
            return Status.getSTATUS_PARTIALLY_VERIFIED();
        }
        return null;
    }

    public static int validateFile(File f, String testKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("int validateFile");
        }
        int res = ValidationMesaTest.FILE_NOT_AUTOMATICALLY_VERIFIED;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            ByteArrayInputStream byteArray = new ByteArrayInputStream(f.getData());

            Document doc = db.parse(byteArray);
            doc.getDocumentElement().normalize();

            if (doc.getDocumentElement().getNodeName().equals("MESA_Result")) {
                if (getValueForFirstLevelElement(doc, "Test").trim().equals(testKeyword.trim())
                        && getValueForFirstLevelElement(doc, "Result").trim().toLowerCase().equals("pass")) {
                    res = FILE_AUTOMATICALLY_VERIFIED;
                } else {
                    res = FILE_AUTOMATICALLY_FAILED;
                }
            }
        } catch (Exception e) {
            res = FILE_NOT_AUTOMATICALLY_VERIFIED;
        }
        return res;
    }

    private static String getValueForFirstLevelElement(Document doc, String firstElementValue) {
        NodeList nodeLst = doc.getElementsByTagName(firstElementValue);
        for (int s = 0; s < nodeLst.getLength(); s++) {
            Node node = nodeLst.item(s);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (element.getTextContent() != null && !element.getTextContent().trim().equals("")) {
                    return element.getTextContent();
                }
            }
        }
        return null;
    }

}
