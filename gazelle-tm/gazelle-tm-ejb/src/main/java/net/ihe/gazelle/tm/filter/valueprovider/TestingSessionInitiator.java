package net.ihe.gazelle.tm.filter.valueprovider;

import net.ihe.gazelle.hql.criterion.ValueProvider;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestingSessionInitiator implements ValueProvider {

    public static final TestingSessionInitiator INSTANCE = new TestingSessionInitiator();
    private static final Logger LOG = LoggerFactory.getLogger(TestingSessionInitiator.class);
    private static final long serialVersionUID = -8047544369456796713L;

    @Override
    public Object getValue() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValue");
        }
        return TestingSession.getSelectedTestingSession();
    }

}
