package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.SystemInSessionQuery;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.users.model.Institution;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.async.QuartzDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Name("testInstancesOverviewManager")
@Scope(ScopeType.PAGE)
@Synchronized(timeout = 10000)
public class TestInstancesOverviewManager implements Serializable {

    private static final long serialVersionUID = -7827661163911263947L;

    private static final Logger LOG = LoggerFactory.getLogger(TestInstancesOverviewManager.class);
    private Filter<SystemInSession> filter;

    private Institution selectedInstitution = null;

    public TestingSession getSelectedTestingSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestingSession");
        }
        return TestingSession.getSelectedTestingSession();
    }

    public Institution getSelectedInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedInstitution");
        }
        return selectedInstitution;
    }

    public void setSelectedInstitution(Institution selectedInstitution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedInstitution");
        }
        this.selectedInstitution = selectedInstitution;
    }

    public Filter<SystemInSession> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            filter = new Filter<SystemInSession>(getHQLCriterions());
        }
        return filter;
    }

    private HQLCriterionsForFilter<SystemInSession> getHQLCriterions() {
        SystemInSessionQuery query = new SystemInSessionQuery();
        final HQLCriterionsForFilter<SystemInSession> hqlCriterionsForFilter = query.getHQLCriterionsForFilter();
        hqlCriterionsForFilter.addPath("testingSession", query.testingSession(), getSelectedTestingSession());
        hqlCriterionsForFilter.addPath("institution", query.system().institutionSystems().institution());
        hqlCriterionsForFilter.addPath("system", query.system());
        hqlCriterionsForFilter.addQueryModifier(new QueryModifier<SystemInSession>() {
            @Override
            public void modifyQuery(HQLQueryBuilder<SystemInSession> hqlQueryBuilder, Map<String, Object> map) {
                if (selectedInstitution != null) {
                    SystemInSessionQuery q = new SystemInSessionQuery();
                    hqlQueryBuilder.addRestriction(q.system().ownerUser().institution().id().eqRestriction(selectedInstitution.getId()));
                }
            }
        });
        return hqlCriterionsForFilter;
    }

    public FilterDataModel<SystemInSession> listSystemInSessionsForCompany() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listSystemInSessionsForCompany");
        }
        return new FilterDataModel<SystemInSession>(getFilter()) {
            @Override
            protected Object getId(SystemInSession systemInSession) {
                return systemInSession.getId();
            }
        };
    }

    public int countSystemInSession() {
        return listSystemInSessionsForCompany().getAllItems(FacesContext.getCurrentInstance()).size();
    }

    public void acceptListedSystems(boolean accepted) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("acceptListedSystems");
        }
        FilterDataModel<SystemInSession> list = listSystemInSessionsForCompany();
        List<Integer> testingSessionid = new ArrayList<Integer>();
        for (SystemInSession systemInSession : list.getAllItems(FacesContext.getCurrentInstance())) {
            if (!testingSessionid.contains(systemInSession.getTestingSession().getId())) {
                testingSessionid.add(systemInSession.getTestingSession().getId());
            }
            systemInSession.setAcceptedToSession(accepted);
            EntityManagerService.provideEntityManager().merge(systemInSession);
        }

        QuartzDispatcher.instance().scheduleAsynchronousEvent("updateConnectathonResultsList", testingSessionid);
    }
}
