package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.hql.HQLRestriction;
import net.ihe.gazelle.hql.beans.HQLStatisticItem;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.paths.HQLSafePathBasic;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.tm.filter.TMCriterions;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestQuery;
import net.ihe.gazelle.tm.gazelletest.model.instance.Status;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Remove;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Name("kpiTestsInstances")
@Scope(ScopeType.PAGE)
public class KpiTestsInstances extends KpiDisplay<Test> implements Serializable {

    private static final long serialVersionUID = -429665831360950956L;
    private static final Logger LOG = LoggerFactory.getLogger(KpiTestsInstances.class);

    @Create
    public void create() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("create");
        }
        TestQuery query = new TestQuery();
        HQLCriterionsForFilter<Test> criterions = query.getHQLCriterionsForFilter();

        TMCriterions.addTestingSession(criterions, "testing_session", query.instances().testingSession());
        TMCriterions.addTestCriterions(criterions, query, true);
        TMCriterions.addAIPOCriterionsUsingTest(criterions, query, "testing_session");

        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> requestParametersMap = fc.getExternalContext().getRequestParameterMap();
        filter = new Filter<Test>(criterions, requestParametersMap);

        List<HQLStatisticItem> statisticItems = new ArrayList<HQLStatisticItem>();
        statisticItems.add(query.instances().monitorInSession().user().id().statisticItem());

        statisticItems.add(query.instances().testInstanceParticipants().systemInSessionUser().systemInSession().id()
                .statisticItem());

        HQLSafePathBasic<Integer> tiPath = query.instances().id();
        HQLSafePathBasic<Status> tiStatusPath = query.instances().lastStatus();

        addTiStatistics(statisticItems, tiPath, tiStatusPath);
        HQLRestriction testingSessionRestriction = query.monitorsInSession().testingSession().id().eqRestriction(
                ((TestingSession) filter.getFilterValues().get("testing_session")).getId()
        );
        HQLRestriction isActiveRestriction = query.monitorsInSession().isActivated().eqRestriction(true);
        HQLRestriction restrictions = HQLRestrictions.and(testingSessionRestriction, isActiveRestriction);

        statisticItems.add(query.monitorsInSession().user().id().statisticItem(restrictions));

        filter.setStatisticItems(statisticItems);
    }

    public String getTestingSessionId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestingSessionId");
        }
        Object value = filter.getFilterValues().get("testing_session");
        if (value == null) {
            return "";
        } else {
            TestingSession testingSession = (TestingSession) value;
            return Integer.toString(testingSession.getId());
        }
    }

    @Remove
    @Destroy
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

}
