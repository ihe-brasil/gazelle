package net.ihe.gazelle.tm.application.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Session {
    private static final Logger LOG = LoggerFactory.getLogger(Session.class);
    private String label;

    private String size;

    private long realSize;

    private List<SessionItem> items = new ArrayList<SessionItem>();

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

    public List<SessionItem> getItems() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getItems");
        }
        return items;
    }

    public void setItems(List<SessionItem> items) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setItems");
        }
        this.items = items;
    }

    public void sortItems() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("sortItems");
        }
        Collections.sort(items);
    }

}
