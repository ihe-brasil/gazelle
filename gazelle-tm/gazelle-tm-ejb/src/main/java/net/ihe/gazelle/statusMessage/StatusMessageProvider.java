package net.ihe.gazelle.statusMessage;

import org.jboss.seam.Component;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.international.StatusMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusMessageProvider extends StatusMessages {

    private static final long serialVersionUID = -1852543427129673432L;
    private static final Logger LOG = LoggerFactory.getLogger(StatusMessageProvider.class);

    public static StatusMessages instance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("StatusMessages instance");
        }
        StatusMessages statusMessage = null;
        if (Contexts.isSessionContextActive()) {
            Component component = Component.forName(StatusMessages.COMPONENT_NAME);
            if ((component != null) && !component.getScope().isContextActive()) {
                throw new IllegalStateException("No active " + component.getScope().name() + " context");
            }
            // Attempting to get the instance anyway for backwards compatibility with some potential hack situations.
            statusMessage = (StatusMessages) Component.getInstance(COMPONENT_NAME);
        } else {
            statusMessage = new NullStatusMessage();
        }
        return statusMessage;
    }
}
