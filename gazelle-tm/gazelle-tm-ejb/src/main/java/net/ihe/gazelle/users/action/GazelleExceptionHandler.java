package net.ihe.gazelle.users.action;

import org.jboss.seam.Component;
import org.jboss.seam.exception.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GazelleExceptionHandler extends ExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GazelleExceptionHandler.class);

    @Override
    public boolean isHandler(Exception e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isHandler");
        }
        SessionTimeoutManagerLocal sessionTimeoutManager = (SessionTimeoutManagerLocal) Component
                .getInstance("sessionTimeoutManager");
        return sessionTimeoutManager.isHandler(e);
    }

    @Override
    public void handle(Exception e) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("handle");
        }
        SessionTimeoutManagerLocal sessionTimeoutManager = (SessionTimeoutManagerLocal) Component
                .getInstance("sessionTimeoutManager");
        sessionTimeoutManager.handle(e);
    }

    @Override
    public boolean isLogEnabled() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isLogEnabled");
        }
        return false;
    }
}
