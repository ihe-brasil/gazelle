package net.ihe.gazelle.tm.gazelletest.bean;

import net.ihe.gazelle.tm.gazelletest.model.instance.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum TestStatusStatus {

    TO_COMPLETE_BY_VENDOR("To be completed by vendor", Status.STARTED, Status.PAUSED, Status.PARTIALLY_VERIFIED, Status.FAILED),

    TO_VERIFY_BY_ADMIN("To be verified by admin", Status.COMPLETED, Status.CRITICAL, Status.SUPPORTIVE, Status.COMPLETED_ERRORS),

    VERIFIED("Verified or Self verified", Status.VERIFIED, Status.SELF_VERIFIED),

    COMPLETED("Completed", Status.COMPLETED),

    SUPPORTIVE("Supportive", Status.SUPPORTIVE),

    COMPLETED_ERRORS("Completed with errors ", Status.COMPLETED_ERRORS);

    private static final Logger LOG = LoggerFactory.getLogger(TestStatusStatus.class);

    private Status[] statuses;
    private String labelToDisplay;

    TestStatusStatus(String labelToDisplay, Status... statuses) {
        this.labelToDisplay = labelToDisplay;
        this.statuses = statuses;
    }

    public Status[] getStatuses() {
        return statuses;
    }

    public String getLabelToDisplay() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLabelToDisplay");
        }
        return labelToDisplay;
    }

}
