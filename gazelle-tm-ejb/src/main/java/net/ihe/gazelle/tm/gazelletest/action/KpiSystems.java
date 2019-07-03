package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.beans.HQLStatisticItem;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.paths.HQLSafePathBasic;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.tm.filter.TMCriterions;
import net.ihe.gazelle.tm.filter.modifier.TestMatchingTestingSession;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestEntity;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestType;
import net.ihe.gazelle.tm.gazelletest.model.instance.Status;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.SystemInSessionQuery;
import net.ihe.gazelle.tm.systems.model.SystemInSessionRegistrationStatus;
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

@Name("kpiSystems")
@Scope(ScopeType.PAGE)
public class KpiSystems extends KpiDisplay<SystemInSession> implements Serializable, QueryModifier<SystemInSession> {

    private static final Logger LOG = LoggerFactory.getLogger(KpiSystems.class);

    private static final long serialVersionUID = -5233829352944063573L;

    @Create
    public void create() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("create");
        }

        SystemInSessionQuery query = new SystemInSessionQuery();

        HQLCriterionsForFilter<SystemInSession> criterions = query.getHQLCriterionsForFilter();

        TMCriterions.addTestingSession(criterions, "testing_session", query.testingSession());
        TMCriterions.addAIPOCriterions(criterions, query.systemInSessionUsers().testInstanceParticipants()
                .actorIntegrationProfileOption());

        TestEntity<Test> testEntity = query.systemInSessionUsers().testInstanceParticipants().testInstance().test();
        criterions.addPath("test", testEntity);
        criterions.addQueryModifierForCriterion("test", new TestMatchingTestingSession(testEntity, "testing_session"));

        criterions.addPath("institution", query.system().institutionSystems().institution());
        criterions.addPath("demonstration", query.demonstrationsSystemInSession().demonstration().name());

        criterions.addQueryModifier(this);

        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> requestParametersMap = fc.getExternalContext().getRequestParameterMap();
        filter = new Filter<SystemInSession>(criterions, requestParametersMap);

        List<HQLStatisticItem> statisticItems = new ArrayList<HQLStatisticItem>();

        statisticItems.add(query.systemInSessionUsers().testInstanceParticipants().testInstance().test().id()
                .statisticItem());
        statisticItems.add(query.systemInSessionUsers().testInstanceParticipants().testInstance().monitorInSession()
                .user().id().statisticItem());

        HQLSafePathBasic<Integer> tiPath = query.systemInSessionUsers().testInstanceParticipants().testInstance().id();
        HQLSafePathBasic<Status> tiStatusPath = query.systemInSessionUsers().testInstanceParticipants().testInstance()
                .lastStatus();

        addTiStatistics(statisticItems, tiPath, tiStatusPath);

        filter.setStatisticItems(statisticItems);
    }

    @Override
    public void modifyQuery(HQLQueryBuilder<SystemInSession> queryBuilder, Map<String, Object> filterValuesApplied) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyQuery");
        }
        SystemInSessionQuery query = new SystemInSessionQuery();
        // Exclude dropped systems
        queryBuilder.addRestriction(HQLRestrictions.or(query.system().systemsInSession().registrationStatus()
                .neqRestriction(SystemInSessionRegistrationStatus.DROPPED), query.system().systemsInSession()
                .registrationStatus().isNullRestriction()));
        queryBuilder.addRestriction(query.system().systemsInSession().testingSession()
                .eqRestriction(TestingSession.getSelectedTestingSession()));
        // GZL-4405: Exclude pre-connectathon tests from count
        List<TestType> testTypes = TestType.getTestTypesWithoutMESA();
        TestEntity<Test> testEntity = query.systemInSessionUsers().testInstanceParticipants().testInstance().test();
        queryBuilder.addRestriction(testEntity.testType().inRestriction(testTypes));
    }

    @Remove
    @Destroy
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

}
