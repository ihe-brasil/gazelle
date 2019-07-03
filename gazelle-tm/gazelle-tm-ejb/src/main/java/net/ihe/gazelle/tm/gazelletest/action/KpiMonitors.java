package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.hql.beans.HQLStatisticItem;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.paths.HQLSafePathBasic;
import net.ihe.gazelle.tm.filter.TMCriterions;
import net.ihe.gazelle.tm.filter.modifier.TestMatchingTestingSession;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestEntity;
import net.ihe.gazelle.tm.gazelletest.model.instance.MonitorInSession;
import net.ihe.gazelle.tm.gazelletest.model.instance.MonitorInSessionQuery;
import net.ihe.gazelle.tm.gazelletest.model.instance.Status;
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

@Name("kpiMonitors")
@Scope(ScopeType.PAGE)
public class KpiMonitors extends KpiDisplay<MonitorInSession> implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(KpiMonitors.class);
    private static final long serialVersionUID = 7341977222674982652L;

    @Create
    public void create() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("create");
        }
        MonitorInSessionQuery query = new MonitorInSessionQuery();
        HQLCriterionsForFilter<MonitorInSession> criterions = query.getHQLCriterionsForFilter();

        TMCriterions.addTestingSession(criterions, "testing_session", query.testingSession());
        TMCriterions.addAIPOCriterionsUsingTestInstance(criterions, query.testInstances(), "testing_session");

        criterions.addPath("firstname", query.user().firstname());
        criterions.addPath("lastname", query.user().lastname());
        criterions.addPath("username", query.user().username());
        criterions.addPath("active", query.isActivated(), true);

        TestEntity<Test> testEntity = query.monitorTests().test();
        criterions.addPath("test", testEntity);
        criterions.addQueryModifierForCriterion("test", new TestMatchingTestingSession(testEntity, "testing_session"));

        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> requestParametersMap = fc.getExternalContext().getRequestParameterMap();
        filter = new Filter<MonitorInSession>(criterions, requestParametersMap);

        List<HQLStatisticItem> statisticItems = new ArrayList<HQLStatisticItem>();

        statisticItems.add(query.testInstances().test().id().statisticItem());
        statisticItems.add(query.testInstances().testInstanceParticipants().systemInSessionUser().systemInSession()
                .id().statisticItem());

        HQLSafePathBasic<Integer> tiPath = query.testInstances().id();
        HQLSafePathBasic<Status> tiStatusPath = query.testInstances().lastStatus();

        addTiStatistics(statisticItems, tiPath, tiStatusPath);

        filter.setStatisticItems(statisticItems);
    }

    @Remove
    @Destroy
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

}
