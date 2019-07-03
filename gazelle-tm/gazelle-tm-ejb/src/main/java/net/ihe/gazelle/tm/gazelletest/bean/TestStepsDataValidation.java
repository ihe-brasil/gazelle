package net.ihe.gazelle.tm.gazelletest.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestStepsDataValidation {
    private static final Logger LOG = LoggerFactory.getLogger(TestStepsDataValidation.class);
    private final String permalink;

    private final String display;

    private final String status;

    public TestStepsDataValidation(String permalink, String display, String status) {
        super();
        this.permalink = permalink;
        this.display = display;
        this.status = status;
    }

    @Override
    public String toString() {
        return display + ", " + status + ", " + permalink;
    }

    public String getPermalink() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPermalink");
        }
        return permalink;
    }

    public String getDisplay() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDisplay");
        }
        return display;
    }

    public String getStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getStatus");
        }
        return status;
    }
}
