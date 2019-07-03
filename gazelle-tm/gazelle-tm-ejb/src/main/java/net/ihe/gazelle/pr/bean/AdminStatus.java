package net.ihe.gazelle.pr.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum AdminStatus {

    NEED_VERIFICATION("Need verification"),

    REFERENCED("Referenced"),

    NOT_REFERENCED("Not referenced"),

    WILL_BE_CRAWLED("Will be crawled");

    private static final Logger LOG = LoggerFactory.getLogger(AdminStatus.class);

    private String label;

    AdminStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLabel");
        }
        return label;
    }

}
