package net.ihe.gazelle.tm.filter.valueprovider;

import net.ihe.gazelle.hql.criterion.ValueProvider;
import net.ihe.gazelle.menu.Authorizations;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestingSessionFixer implements ValueProvider {

    public static final TestingSessionFixer INSTANCE = new TestingSessionFixer();
    private static final long serialVersionUID = 1467097923877807016L;
    private static final Logger LOG = LoggerFactory.getLogger(TestingSessionFixer.class);

    @Override
    public Object getValue() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValue");
        }
        if (Authorizations.ADMIN.isGranted() || Authorizations.MONITOR.isGranted() || Authorizations
                .TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION.isGranted()) {
            return null;
        }
        return TestingSession.getSelectedTestingSession();
    }

}
