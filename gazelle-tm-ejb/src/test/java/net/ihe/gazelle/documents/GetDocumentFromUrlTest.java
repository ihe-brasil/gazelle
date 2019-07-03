package net.ihe.gazelle.documents;

import org.junit.Test;

import java.net.URL;
import static org.junit.Assert.assertEquals;

/**
 * The class <code>GetDocumentFromUrlTest</code> contains tests for the class <code>{@link GetDocumentFromUrl}</code>.
 *
 * @author jean-francois
 * @version $Revision: 1.0 $
 * @generatedBy CodePro at 24/09/12 14:09
 */
public class GetDocumentFromUrlTest {
    /**
     * Run the void getPdfFromUrl(URL) method test.
     *
     * @throws Exception
     * @generatedBy CodePro at 24/09/12 14:09
     */
    @Test
    public void testGetPdfFromUrl_and_compute_md5() throws Exception {
        URL url = new URL("http://ihe.net/Technical_Framework/upload/IHE_CARD_TF_Rev4-0_Vol1_2011-08-05.pdf");
        GetDocumentFromUrl fixture = new GetDocumentFromUrl(url);
        fixture.getDocument();
        fixture.saveDocument();
        assertEquals("4b2a1d50eba2d74c31bfc79ba0403826", fixture.computeDocumentMD5());
    }
}