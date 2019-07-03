package net.ihe.gazelle.tm.application.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionItem implements Comparable<SessionItem> {
    private static final Logger LOG = LoggerFactory.getLogger(SessionItem.class);
    private String label;

    private String size;

    private long realSize;

    private String value;

    public SessionItem(String label, String size, long realSize, String value) {
        super();
        this.label = label;
        this.size = size;
        this.value = value;
        this.realSize = realSize;
    }

    public String getLabel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLabel");
        }
        return label;
    }

    public void setLabel(String label) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setLabel");
        }
        this.label = label;
    }

    public String getSize() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSize");
        }
        return size;
    }

    public void setSize(String size) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSize");
        }
        this.size = size;
    }

    public String getValue() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValue");
        }
        return value;
    }

    public void setValue(String value) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setValue");
        }
        this.value = value;
    }

    public long getRealSize() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRealSize");
        }
        return realSize;
    }

    public void setRealSize(long realSize) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setRealSize");
        }
        this.realSize = realSize;
    }

    @Override
    public int compareTo(SessionItem o) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("compareTo");
        }
        if (realSize < o.realSize) {
            return 1;
        }
        if (realSize > o.realSize) {
            return -1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
}
