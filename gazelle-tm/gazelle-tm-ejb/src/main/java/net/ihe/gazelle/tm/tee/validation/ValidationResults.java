package net.ihe.gazelle.tm.tee.validation;

import net.ihe.gazelle.tm.tee.model.ValidationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationResults {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationResults.class);

    private ValidationStatus validationStatus;
    private String validationResults;

    public ValidationResults(ValidationStatus validationStatus, String validationResults) {
        this.validationStatus = validationStatus;
        this.validationResults = validationResults;
    }

    public ValidationStatus getValidationStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValidationStatus");
        }
        return validationStatus;
    }

    public void setValidationStatus(ValidationStatus validationStatus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setValidationStatus");
        }
        this.validationStatus = validationStatus;
    }

    public String getValidationResults() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValidationResults");
        }
        return validationResults;
    }

    public void setValidationResults(String validationResults) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setValidationResults");
        }
        this.validationResults = validationResults;
    }
}
