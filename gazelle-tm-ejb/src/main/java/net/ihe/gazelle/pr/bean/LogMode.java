package net.ihe.gazelle.pr.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum LogMode {

    ONLY_FILTERED("Show only filtered"),

    WITH_UNFILTERED("Show filtered and unfiltered");

    private static final Logger LOG = LoggerFactory.getLogger(LogMode.class);
    String label;

    LogMode(String label) {
        this.label = label;
    }

    public String getLabel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLabel");
        }
        return label;
    }

}
