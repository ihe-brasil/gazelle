package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.filter.TMCriterions;
import net.ihe.gazelle.tm.filter.modifier.TestMatchingTestingSession;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestEntity;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestQuery;
import net.ihe.gazelle.tm.gazelletest.model.instance.MonitorInSession;
import net.ihe.gazelle.tm.gazelletest.model.instance.MonitorInSessionQuery;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Remove;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Name("monitorSearchFilter")
@Scope(ScopeType.PAGE)
public class MonitorSearchFilter implements Serializable {

    private static final long serialVersionUID = -782240547894724760L;

    private static final Logger LOG = LoggerFactory.getLogger(MonitorSearchFilter.class);

    private FilterDataModel<MonitorInSession> monitors;

    private List<Test> monitorTests;

    private MonitorInSession displayedMonitor;

    private Filter<Test> testFilter;

    private MonitorInSession selectedMonitor;

    public MonitorInSession getSelectedMonitor() {
        return selectedMonitor;
    }

    public void setSelectedMonitor(MonitorInSession selectedMonitor) {
        this.selectedMonitor = selectedMonitor;
    }

    @Create
    public void create() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("create");
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();
        Filter<MonitorInSession> filter = new Filter<MonitorInSession>(getCriterions(), requestParameterMap);
        monitors = new FilterDataModel<MonitorInSession>(filter) {
            @Override
            protected Object getId(MonitorInSession t) {
                // TODO Auto-generated method stub
                return t.getId();
            }
        };
    }

    private HQLCriterionsForFilter<MonitorInSession> getCriterions() {
        MonitorInSessionQuery query = new MonitorInSessionQuery();
        HQLCriterionsForFilter<MonitorInSession> result = query.getHQLCriterionsForFilter();

        TestEntity<Test> testEntity = query.monitorTests().test();

        TMCriterions.addTestingSession(result, "testing_session", query.testingSession());
        TMCriterions.addAIPOCriterionsUsingTest(result, testEntity, "testing_session");
        result.addPath("firstname", query.user().firstname());
        result.addPath("lastname", query.user().lastname());
        result.addPath("username", query.user().username());
        result.addPath("test", testEntity);
        result.addPath("active", query.isActivated(), true);
        result.addQueryModifierForCriterion("test", new TestMatchingTestingSession(testEntity, "testing_session"));

        return result;
    }

    @Remove
    @Destroy
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    public FilterDataModel<MonitorInSession> getMonitors() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMonitors");
        }
        return monitors;
    }

    public void getMonitorInSessionById() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMonitorInSessionById");
        }
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String monitorId = params.get("id");
        if ((monitorId != null) && !monitorId.isEmpty()) {
            try {
                Integer id = Integer.parseInt(monitorId);
                EntityManager em = EntityManagerService.provideEntityManager();
                displayedMonitor = em.find(MonitorInSession.class, id);
            } catch (NumberFormatException e) {
                LOG.error("monitorId is empty or not well-formed: " + monitorId);
                displayedMonitor = null;
            }
        } else {
            displayedMonitor = null;
        }
    }

    public MonitorInSession getDisplayedMonitor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDisplayedMonitor");
        }
        return displayedMonitor;
    }

    public void showTestsForMonitor(MonitorInSession monitor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showTestsForMonitor");
        }
        setSelectedMonitor(monitor);
        monitorTests = new ArrayList<Test>(monitor.getTestList());
    }

    public List<Test> getTestsDisplay() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestsDisplay");
        }
        return monitorTests;
    }

    public FilterDataModel<Test> listTestsDisplay() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listTestsDisplay");
        }
        return new FilterDataModel<Test>(getFilter()) {
            @Override
            protected Object getId(Test test) {
                return test.getId();
            }
        };
    }

    public Filter<Test> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (testFilter == null) {
            TestQuery q = new TestQuery();
            HQLCriterionsForFilter<Test> result = q.getHQLCriterionsForFilter();
            result.addQueryModifier(new QueryModifier<Test>() {
                @Override
                public void modifyQuery(HQLQueryBuilder<Test> hqlQueryBuilder, Map<String, Object> map) {
                    if (selectedMonitor != null) {
                        TestQuery q = new TestQuery();
                        hqlQueryBuilder.addRestriction(q.monitorsInSession().id().eqRestriction(selectedMonitor.getId()));
                    }
                }
            });
            this.testFilter = new Filter<Test>(result);
        }
        return this.testFilter;
    }
}
