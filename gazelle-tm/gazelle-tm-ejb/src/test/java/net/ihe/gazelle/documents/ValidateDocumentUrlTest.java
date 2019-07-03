package net.ihe.gazelle.documents;

import net.ihe.gazelle.tf.model.Document;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class ValidateDocumentUrlTest {
    @Test
    public void testUrlResponds_and_doesnt_point_to_pdf() throws IOException {
        ValidateDocumentUrl urlChecker = new ValidateDocumentUrl();
        urlChecker.setUrl("http://www.google.fr");
        assertEquals(true, urlChecker.connect());
        assertEquals(true, urlChecker.isThisUrlRespondingHTTP_OK());
        assertEquals(false, urlChecker.isthisUrlPointingToApdf());
        urlChecker.disconnect();
    }

    @Ignore
    @Test
    public void testUrldoesntRespond() throws IOException {
        ValidateDocumentUrl urlChecker = new ValidateDocumentUrl();
        urlChecker.setUrl("http://www.ggle.zz");
        urlChecker.connect();
        assertEquals(false, urlChecker.isThisUrlRespondingHTTP_OK());
        urlChecker.disconnect();
    }

    @Test
    public void testUrlResponds_and_points_to_pdf() throws IOException {
        ValidateDocumentUrl urlChecker = new ValidateDocumentUrl();
        urlChecker.setUrl("http://ihe.net/Technical_Framework/upload/IHE_PAT_TF_Rev2-0_Vol1_TI_2010-07-23.pdf");
        assertEquals(new URL("http://ihe.net/Technical_Framework/upload/IHE_PAT_TF_Rev2-0_Vol1_TI_2010-07-23.pdf"),
                urlChecker.getUrl());
        urlChecker.connect();

        assertEquals(true, urlChecker.isThisUrlRespondingHTTP_OK());
        assertEquals(true, urlChecker.isthisUrlPointingToApdf());
        urlChecker.disconnect();
    }

    @Test
    public void test_checkurl_no_pdf() throws IOException {
        Document doc = new Document();
        DocumentUrlManager docUrlManager = new DocumentUrlManager(doc);
        DocumentURLStatusType response = docUrlManager.checkUrlStatus("http://www.google.fr");
        assertEquals(DocumentURLStatusType.THIS_URL_IS_NOT_POINTING_TO_A_PDF_FILE, response);
    }

    @Test
    public void test_checkurl_wrong_url() throws IOException {
        Document doc = new Document();
        DocumentUrlManager docUrlManager = new DocumentUrlManager(doc);
        DocumentURLStatusType response = docUrlManager.checkUrlStatus("http://www.projectsmart.co.uk/docs/chaos-repor");
        assertEquals(DocumentURLStatusType.THIS_URL_IS_NOT_RESPONDING, response);
    }

    @Ignore
    @Test
    public void test_checkurl_OK() throws IOException {
        Document doc = new Document();
        DocumentUrlManager docUrlManager = new DocumentUrlManager(doc);
        DocumentURLStatusType response = docUrlManager
                .checkUrlStatus("http://www.projectsmart.co.uk/docs/chaos-report.pdf");
        assertEquals(DocumentURLStatusType.OK, response);
    }

    @Test
    public void test_checkurl_malformed() throws IOException {
        Document doc = new Document();
        DocumentUrlManager docUrlManager = new DocumentUrlManager(doc);
        DocumentURLStatusType response = docUrlManager.checkUrlStatus("	htt://192.168.10.1/tutos");
        assertEquals(DocumentURLStatusType.THIS_URL_IS_NOT_WELL_FORMED, response);
    }

    @Test
    public void test_checkurl_null() throws IOException {
        Document doc = new Document();
        DocumentUrlManager docUrlManager = new DocumentUrlManager(doc);
        DocumentURLStatusType response = docUrlManager.checkUrlStatus(null);
        assertEquals(DocumentURLStatusType.PLEASE_PROVIDE_A_URL, response);
    }

    @Test
    public void test_checkurl_ftp() throws IOException {
        Document doc = new Document();
        DocumentUrlManager docUrlManager = new DocumentUrlManager(doc);
        DocumentURLStatusType response = docUrlManager
                .checkUrlStatus("ftp://ftp.ihe.net/DocumentPublication/CurrentPublished/Radiology/IHE_RAD_TF_Rev11.0_Vol1_FT_2012-07-24.pdf");
        assertEquals(DocumentURLStatusType.THIS_URL_IS_NOT_WELL_FORMED, response);
    }

    @Test
    public void test_checkurl_not_responding() throws IOException {
        Document doc = new Document();
        DocumentUrlManager docUrlManager = new DocumentUrlManager(doc);
        DocumentURLStatusType response = docUrlManager.checkUrlStatus("	http://192.168.10.1/tutos");
        assertEquals(DocumentURLStatusType.THIS_URL_IS_NOT_RESPONDING, response);
    }

    @Test
    public void test_getNameFromFilePathWithoutExtension_withoutFileName() {
        Document doc = new Document();
        DocumentUrlManager docUrlManager = new DocumentUrlManager(doc);
        assertEquals("", docUrlManager.getNameFromFilePathWithoutExtension(""));
    }

    @Test
    public void test_getNameFromFilePathWithoutExtension_null() {
        Document doc = new Document();
        DocumentUrlManager docUrlManager = new DocumentUrlManager(doc);
        assertEquals("", docUrlManager.getNameFromFilePathWithoutExtension(null));
    }

    @Test
    public void test_getNameFromFilePathWithoutExtension_withFileName() {
        Document doc = new Document();
        DocumentUrlManager docUrlManager = new DocumentUrlManager(doc);
        assertEquals("toto", docUrlManager.getNameFromFilePathWithoutExtension("toto.pdf"));
    }

    @Test
    public void test_getNameFromFilePathWithoutExtension_withFileName2() {
        Document doc = new Document();
        DocumentUrlManager docUrlManager = new DocumentUrlManager(doc);
        assertEquals("toto", docUrlManager.getNameFromFilePathWithoutExtension("tata/tutu/toto.pdf"));
    }

    @Test
    public void test_getNameFromFilePathWithoutExtension_withoutExtention() {
        Document doc = new Document();
        DocumentUrlManager docUrlManager = new DocumentUrlManager(doc);
        assertEquals("toto", docUrlManager.getNameFromFilePathWithoutExtension("toto"));
    }

    @Test
    public void test_getNameFromFilePathWithoutExtension_withoutExtention2() {
        Document doc = new Document();
        DocumentUrlManager docUrlManager = new DocumentUrlManager(doc);
        assertEquals("", docUrlManager.getNameFromFilePathWithoutExtension("/"));
    }
}
