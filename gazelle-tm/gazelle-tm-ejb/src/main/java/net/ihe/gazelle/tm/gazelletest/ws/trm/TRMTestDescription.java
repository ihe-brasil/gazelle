package net.ihe.gazelle.tm.gazelletest.ws.trm;

import net.ihe.gazelle.tm.gazelletest.model.definition.TestDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TRMTestDescription {
    private static final Logger LOG = LoggerFactory.getLogger(TRMTestDescription.class);

    private String language;
    private String description;

    public void copyFromTestDescription(TestDescription testDescription) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("copyFromTestDescription");
        }
        this.language = testDescription.getGazelleLanguage().getKeyword();
        this.description = testDescription.getDescription();
    }

    public String getDescription() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDescription");
        }
        return description;
    }

    public void setDescription(String description) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDescription");
        }
        this.description = description;
    }

    public String getLanguage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLanguage");
        }
        return language;
    }

    public void setLanguage(String language) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setLanguage");
        }
        this.language = language;
    }

}
