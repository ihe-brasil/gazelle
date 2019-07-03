package net.ihe.gazelle.pr.bean;

import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.tm.systems.model.IntegrationStatementStatus;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.SystemQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PRSearchQueryModifierMain implements QueryModifier<System> {

    private static final Logger LOG = LoggerFactory.getLogger(PRSearchQueryModifierMain.class);
    private static final long serialVersionUID = 372791513296940591L;

    public PRSearchQueryModifierMain() {
        super();
    }

    @Override
    public void modifyQuery(HQLQueryBuilder<System> queryBuilder, Map<String, Object> filterValuesApplied) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyQuery");
        }
        queryBuilder.addRestriction(IntegrationStatementStatus.systemIntegrationStatementVisible(new SystemQuery(
                queryBuilder)));
        queryBuilder.addOrder("name", true);
    }

}
