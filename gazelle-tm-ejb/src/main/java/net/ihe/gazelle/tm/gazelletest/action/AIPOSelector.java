package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.HQLRestriction;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.tf.model.*;
import net.ihe.gazelle.tf.model.constraints.AipoSingle;
import net.ihe.gazelle.tm.application.action.ApplicationManager;
import net.ihe.gazelle.tm.filter.TMCriterions;
import net.ihe.gazelle.tm.systems.action.SystemManagerLocal;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Remove;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Name("aipoSelector")
@Scope(ScopeType.PAGE)
public class AIPOSelector implements Serializable, QueryModifier<ActorIntegrationProfileOption> {

    private static final long serialVersionUID = -5578663433219898074L;
    private static final Logger LOG = LoggerFactory.getLogger(AIPOSelector.class);
    private Filter<ActorIntegrationProfileOption> filter;
    private FilterDataModel<ActorIntegrationProfileOption> aipos;
    private TestingSession testingSession = null;

    @Create
    public void create() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("create");
        }
        ActorIntegrationProfileOptionQuery query = new ActorIntegrationProfileOptionQuery();
        HQLCriterionsForFilter<ActorIntegrationProfileOption> criterions = query.getHQLCriterionsForFilter();

        TMCriterions.addAIPOCriterions(criterions, query);

        if (ApplicationManager.instance().isProductRegistry()) {
            setTestingSession(null);
        } else {
            SystemManagerLocal systemManagerLocal = (SystemManagerLocal) Component.getInstance("systemManager");
            if (systemManagerLocal != null) {
                SystemInSession selectedSystemInSession = systemManagerLocal.getSelectedSystemInSession();
                if (selectedSystemInSession != null) {
                    setTestingSession(selectedSystemInSession.getTestingSession());
                } else {
                    setTestingSession(TestingSession.getSelectedTestingSession());
                }
            }
        }

        criterions.addQueryModifier(this);

        filter = new Filter<ActorIntegrationProfileOption>(criterions) {
            private static final long serialVersionUID = -6633193043649081306L;

            @Override
            public boolean isCountEnabled() {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("isCountEnabled");
                }
                return false;
            }

        };

        aipos = new FilterDataModel<ActorIntegrationProfileOption>(filter) {
            @Override
            protected Object getId(ActorIntegrationProfileOption t) {
                // TODO Auto-generated method stub
                return t.getId();
            }
        };
    }

    public TestingSession getTestingSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestingSession");
        }
        return testingSession;
    }

    public void setTestingSession(TestingSession testingSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestingSession");
        }

        this.testingSession = testingSession;
    }

    @Override
    public void modifyQuery(HQLQueryBuilder<ActorIntegrationProfileOption> queryBuilder,
                            Map<String, Object> filterValuesApplied) {
        ActorIntegrationProfileOptionQuery query = new ActorIntegrationProfileOptionQuery(queryBuilder);

        IntegrationProfileStatusPerApplication integrationProfileStatusPerApplication = IntegrationProfileStatusPerApplication.GMM;
        if (ApplicationManager.instance().isTestManagement()) {
            integrationProfileStatusPerApplication = IntegrationProfileStatusPerApplication.TM;
        }
        if (ApplicationManager.instance().isProductRegistry()) {
            integrationProfileStatusPerApplication = IntegrationProfileStatusPerApplication.PR;
        }

        List<HQLRestriction> restrictions = new ArrayList<HQLRestriction>();
        for (String keyword : integrationProfileStatusPerApplication.keywords) {
            restrictions.add(query.actorIntegrationProfile().integrationProfile().integrationProfileStatusType()
                    .keyword().eqRestriction(keyword));
        }
        if (restrictions.size() > 0) {
            query.addRestriction(HQLRestrictions.or(restrictions.toArray(new HQLRestriction[restrictions.size()])));
        }

        if (getTestingSession() != null) {
            query.actorIntegrationProfile().integrationProfile().in(testingSession.getIntegrationProfilesUnsorted());
        }

    }

    public void initUsingCriterion(AipoSingle criterion) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initUsingCriterion");
        }
        String actorNull = StringUtils.trimToNull(criterion.getActor());
        String ipNull = StringUtils.trimToNull(criterion.getIntegrationProfile());
        String optionNull = StringUtils.trimToNull(criterion.getOption());

        filter.clear();
        if (actorNull != null) {
            ActorQuery actorQuery = new ActorQuery();
            actorQuery.keyword().eq(actorNull);
            Actor actor = actorQuery.getUniqueResult();
            if (actor != null) {
                filter.getFilterValues().put("actor", actor);
            }
        }
        if (ipNull != null) {
            IntegrationProfileQuery ipQuery = new IntegrationProfileQuery();
            ipQuery.keyword().eq(ipNull);
            IntegrationProfile ip = ipQuery.getUniqueResult();
            if (ip != null) {
                filter.getFilterValues().put("integrationProfile", ip);
            }
        }
        if (optionNull != null) {
            IntegrationProfileOptionQuery optionQuery = new IntegrationProfileOptionQuery();
            optionQuery.keyword().eq(optionNull);
            IntegrationProfileOption option = optionQuery.getUniqueResult();
            if (option != null) {
                filter.getFilterValues().put("integrationProfileOption", option);
            }
        }
    }

    @Remove
    @Destroy
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    public Filter<ActorIntegrationProfileOption> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        return filter;
    }

    public FilterDataModel<ActorIntegrationProfileOption> getAipos() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAipos");
        }
        return aipos;
    }

    private static enum IntegrationProfileStatusPerApplication {

        TM("TI", "FT"),
        PR("TI", "FT", "DEPRECATED"),
        GMM();

        private String[] keywords;

        IntegrationProfileStatusPerApplication(String... keywords) {
            this.keywords = keywords;
        }

    }

}
