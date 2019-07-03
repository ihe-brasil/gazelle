package net.ihe.gazelle.tm.filter;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.tm.gazelletest.model.instance.MonitorInSession;
import net.ihe.gazelle.tm.gazelletest.model.instance.MonitorInSessionQuery;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

//FIXME Never use, need to be removed in future release
@Deprecated
public class MonitorWorklistFilter extends Filter<MonitorInSession> {

    private static final long serialVersionUID = 8840497331827577948L;
    private static final Logger LOG = LoggerFactory.getLogger(MonitorWorklistFilter.class);

    public MonitorWorklistFilter() {
        super(getHQLCriterions());
    }

    private static HQLCriterionsForFilter<MonitorInSession> getHQLCriterions() {
        MonitorInSessionQuery query = new MonitorInSessionQuery();
        HQLCriterionsForFilter<MonitorInSession> result = query.getHQLCriterionsForFilter();
        result.addPath("user", query.user());

        result.addQueryModifier(new QueryModifier<MonitorInSession>() {
            /**
             *
             */
            private static final long serialVersionUID = 3971916855314052233L;

            @Override
            public void modifyQuery(HQLQueryBuilder<MonitorInSession> queryBuilder, Map<String, Object> filterValuesApplied) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("modifyQuery");
                }
                MonitorInSessionQuery query = new MonitorInSessionQuery(queryBuilder);
                queryBuilder.addRestriction(query.testingSession().eqRestriction(TestingSession.getSelectedTestingSession()));
            }
        });

        return result;
    }

    @Override
    public boolean isCountEnabled() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isCountEnabled");
        }
        return false;
    }

}
