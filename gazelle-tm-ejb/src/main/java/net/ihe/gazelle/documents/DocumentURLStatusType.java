package net.ihe.gazelle.documents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum DocumentURLStatusType {

    CHECKING("Checking"),
    THIS_URL_IS_NOT_WELL_FORMED("This url is not well formed"),
    THIS_URL_IS_NOT_RESPONDING("This url is not responding"),
    THIS_URL_IS_POINTING_TO_A_PDF("This url is pointing to a PDF"),
    PLEASE_PROVIDE_A_URL("Please provide a URL"),
    THIS_URL_IS_NOT_POINTING_TO_A_PDF_FILE("This url is not pointing to a PDF file"),
    CANNOT_RETRIEVE_THE_PDF_FILE("Cannot retrieve the PDF file"),
    CANNOT_COMPUTE_DOCUMENT_MD5("Cannot compute Document MD5"),
    OK("OK");

    private static final Logger LOG = LoggerFactory.getLogger(DocumentURLStatusType.class);

    private String friendlyName;

    DocumentURLStatusType(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getFriendlyName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFriendlyName");
        }
        return friendlyName;
    }
}
