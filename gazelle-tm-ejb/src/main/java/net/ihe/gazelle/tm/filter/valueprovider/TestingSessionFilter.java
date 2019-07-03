package net.ihe.gazelle.tm.filter.valueprovider;

import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.menu.Authorizations;
import net.ihe.gazelle.tm.gazelletest.model.instance.MonitorInSessionQuery;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.tm.systems.model.TestingSessionEntity;
import net.ihe.gazelle.users.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class TestingSessionFilter implements QueryModifier {

    private static final Logger LOG = LoggerFactory.getLogger(TestingSessionFilter.class);
    private static final long serialVersionUID = 5221150633060422866L;
    private String path;

    public TestingSessionFilter(TestingSessionEntity<TestingSession> testingSession) {
        super();
        this.path = testingSession.toString();
    }

    @Override
    public void modifyQuery(HQLQueryBuilder queryBuilder, Map filterValuesApplied) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyQuery");
        }
        if (Authorizations.TM.isGranted() && Authorizations.MONITOR.isGranted() && !Authorizations.ADMIN.isGranted() && !Authorizations
                .TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION.isGranted()) {
            MonitorInSessionQuery query = new MonitorInSessionQuery();
            query.user().id().eq(User.loggedInUser().getId());
            List<Integer> testingSessionIds = query.testingSession().id().getListDistinct();
            queryBuilder.addIn(path + ".id", testingSessionIds);
        }
    }

}
