package net.ihe.gazelle.tm.filter.modifier;

import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.tf.model.IntegrationProfile;
import net.ihe.gazelle.tf.model.IntegrationProfileEntity;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class IntegrationProfilesFromTestingSession implements QueryModifier {

    private static final long serialVersionUID = -4563409077430911357L;
    private static final Logger LOG = LoggerFactory.getLogger(IntegrationProfilesFromTestingSession.class);
    private String integrationProfilePath;
    private String testingSessionKey;

    public IntegrationProfilesFromTestingSession(IntegrationProfileEntity<IntegrationProfile> integrationProfileEntity,
                                                 String testingSessionKey) {
        super();
        this.integrationProfilePath = integrationProfileEntity.getPath();
        this.testingSessionKey = testingSessionKey;
    }

    @Override
    public void modifyQuery(HQLQueryBuilder queryBuilder, Map filterValuesApplied) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyQuery");
        }
        if (testingSessionKey != null) {
            Object testingSessionObject = filterValuesApplied.get(testingSessionKey);
            if ((testingSessionObject != null) && (testingSessionObject instanceof TestingSession)) {
                List<IntegrationProfile> integrationProfiles = ((TestingSession) testingSessionObject)
                        .getIntegrationProfilesUnsorted();
                queryBuilder.addIn(integrationProfilePath, integrationProfiles);
            }
        }

    }
}
