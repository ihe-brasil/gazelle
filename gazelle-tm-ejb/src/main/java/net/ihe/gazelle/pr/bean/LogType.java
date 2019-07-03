package net.ihe.gazelle.pr.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum LogType {

    SEARCH("Searches"),

    DOWNLOAD("Downloads");
    private static final Logger LOG = LoggerFactory.getLogger(LogType.class);

    String label;

    LogType(String label) {
        this.label = label;
    }

    public String getLabel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLabel");
        }
        return label;
    }

}