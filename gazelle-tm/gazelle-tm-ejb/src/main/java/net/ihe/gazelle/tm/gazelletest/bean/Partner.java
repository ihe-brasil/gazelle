package net.ihe.gazelle.tm.gazelletest.bean;

import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.TestingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Partner implements Comparable<Partner> {

    private static final Logger LOG = LoggerFactory.getLogger(Partner.class);

    private System system;

    private boolean tested;

    private TestingType testingType;

    private String skype;
    private String tableKeyword;

    public Partner(System system, boolean tested, TestingType testingType, String skype, String tableKeyword) {
        super();
        this.system = system;
        this.tested = tested;
        this.testingType = testingType;
        this.skype = skype;
        this.tableKeyword = tableKeyword;
    }

    public System getSystem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystem");
        }
        return system;
    }

    public boolean isTested() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isTested");
        }
        return tested;
    }

    public String getTestingType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestingType");
        }
        return testingType.getKeyword();
    }

    public String getEmail() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEmail");
        }
        return system.getOwnerUser().getEmail();
    }

    public String getSkype() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSkype");
        }
        return skype;
    }

    public String getTableKeyword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTableKeyword");
        }
        return tableKeyword;
    }

    @Override
    public int compareTo(Partner o) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("compareTo");
        }
        return system.getKeyword().compareTo(o.system.getKeyword());
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
}
