package net.ihe.gazelle.tf.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.HQLRestriction;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOption;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOptionQuery;
import net.ihe.gazelle.tf.model.IntegrationProfileStatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ActorIntegrationProfileOptionFilter extends Filter<ActorIntegrationProfileOption> implements
        QueryModifier<ActorIntegrationProfileOption> {

    private static final Logger LOG = LoggerFactory.getLogger(ActorIntegrationProfileOptionFilter.class);

    private static final long serialVersionUID = -2035527794059646463L;
    private List<Integer> excludedAIPOList;

    public ActorIntegrationProfileOptionFilter(Map<String, String> requestParameterMap) {
        super(getHQLCriterions(), requestParameterMap);
        queryModifiers.add(this);
    }

    private static HQLCriterionsForFilter<ActorIntegrationProfileOption> getHQLCriterions() {

        ActorIntegrationProfileOptionQuery query = new ActorIntegrationProfileOptionQuery();
        HQLCriterionsForFilter<ActorIntegrationProfileOption> result = query.getHQLCriterionsForFilter();

        result.addPath("domain", query.actorIntegrationProfile().integrationProfile().domainsProfile().domain());
        result.addPath("integrationProfile", query.actorIntegrationProfile().integrationProfile());
        result.addPath("actor", query.actorIntegrationProfile().actor());

        return result;
    }

    public List<Integer> getExcludedAIPOList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getExcludedAIPOList");
        }
        return excludedAIPOList;
    }

    public void setExcludedAIPOList(List<Integer> setExcludedAIPOList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setExcludedAIPOList");
        }
        this.excludedAIPOList = setExcludedAIPOList;
        modified();
    }

    public void excludeDeprecatedAIPO(HQLQueryBuilder<ActorIntegrationProfileOption> queryBuilder) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("excludeDeprecatedAIPO");
        }
        ActorIntegrationProfileOptionQuery actorIntegrationProfileOptionQuery = new ActorIntegrationProfileOptionQuery();
        HQLRestriction restriction = actorIntegrationProfileOptionQuery.actorIntegrationProfile().integrationProfile()
                .integrationProfileStatusType().keyword()
                .neqRestriction(IntegrationProfileStatusType.DEPRECATED_KEYWORD);
        queryBuilder.addRestriction(restriction);
    }

    @Override
    public void modifyQuery(HQLQueryBuilder<ActorIntegrationProfileOption> queryBuilder,
                            Map<String, Object> filterValuesApplied) {
        if ((this.excludedAIPOList != null) && (this.excludedAIPOList.size() > 0)) {
            ActorIntegrationProfileOptionQuery query = new ActorIntegrationProfileOptionQuery(queryBuilder);
            queryBuilder.addRestriction(query.id().ninRestriction(excludedAIPOList));
        }
        excludeDeprecatedAIPO(queryBuilder);
    }
}
