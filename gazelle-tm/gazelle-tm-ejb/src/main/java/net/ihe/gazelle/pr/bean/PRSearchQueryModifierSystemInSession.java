package net.ihe.gazelle.pr.bean;

import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.tm.systems.model.IntegrationStatementStatus;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.SystemInSessionQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PRSearchQueryModifierSystemInSession implements QueryModifier<SystemInSession> {
    private static final Logger LOG = LoggerFactory.getLogger(PRSearchQueryModifierSystemInSession.class);

    private static final long serialVersionUID = -3636453010819254958L;

    @Override
    public void modifyQuery(HQLQueryBuilder<SystemInSession> queryBuilder, Map<String, Object> filterValuesApplied) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyQuery");
        }
        SystemInSessionQuery query = new SystemInSessionQuery(queryBuilder);
        queryBuilder.addRestriction(IntegrationStatementStatus.systemIntegrationStatementVisible(query.system()));
    }

}
