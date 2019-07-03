package net.ihe.gazelle.tm.filter.valueprovider;

import net.ihe.gazelle.hql.criterion.ValueProvider;
import net.ihe.gazelle.tm.gazelletest.model.instance.MonitorInSession;
import net.ihe.gazelle.users.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorInitiator implements ValueProvider {
    private static final Logger LOG = LoggerFactory.getLogger(MonitorInitiator.class);

    private static final long serialVersionUID = -6007497591735345831L;

    @Override
    public Object getValue() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValue");
        }
        if (MonitorInSession.isConnectedUserMonitorForSelectedSession()) {
            return User.loggedInUser();
        }
        return null;
    }

}
