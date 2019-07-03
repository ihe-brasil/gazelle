package net.ihe.gazelle.statusMessage;

import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.international.StatusMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NullStatusMessage extends StatusMessages {

    private static final long serialVersionUID = 2967600903164370609L;
    private static final Logger LOG = LoggerFactory.getLogger(NullStatusMessage.class);

    @Override
    protected List<StatusMessage> getMessages() {
        return new ArrayList<StatusMessage>();
    }

    @Override
    protected Map<String, List<StatusMessage>> getKeyedMessages() {
        // TODO Auto-generated method stub
        return new HashMap<String, List<StatusMessage>>();
    }

    @Override
    public void clear() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("clear");
        }
    }

    @Override
    public void clearKeyedMessages(String id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("clearKeyedMessages");
        }
    }

    @Override
    public void clearGlobalMessages() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("clearGlobalMessages");
        }
    }

    @Override
    public void add(Severity severity, String key, String detailKey, String messageTemplate,
                    String messageDetailTemplate, Object... params) {
    }

    @Override
    public void addToControl(String id, Severity severity, String key, String messageTemplate, Object... params) {
    }

    @Override
    public void add(String messageTemplate, Object... params) {
    }

    @Override
    public void add(Severity severity, String messageTemplate, Object... params) {
    }

    @Override
    public void addToControl(String id, String messageTemplate, Object... params) {
    }

    @Override
    public void addToControl(String id, Severity severity, String messageTemplate, Object... params) {
    }

    @Override
    public void addFromResourceBundle(String key, Object... params) {
    }

    @Override
    public void addFromResourceBundle(Severity severity, String key, Object... params) {
    }

    @Override
    public void addFromResourceBundleOrDefault(String key, String defaultMessageTemplate, Object... params) {
    }

    @Override
    public void addFromResourceBundleOrDefault(Severity severity, String key, String defaultMessageTemplate,
                                               Object... params) {
    }

    @Override
    public void addToControlFromResourceBundle(String id, String key, Object... params) {
    }

    @Override
    public void addToControlFromResourceBundle(String id, Severity severity, String key, Object... params) {
    }

    @Override
    public void addToControlFromResourceBundleOrDefault(String id, String key, String defaultMessageTemplate,
                                                        Object... params) {
    }

    @Override
    public void addToControlFromResourceBundleOrDefault(String id, Severity severity, String key,
                                                        String defaultMessageTemplate, Object... params) {
    }

    @Override
    protected void doRunTasks() {
    }

}
