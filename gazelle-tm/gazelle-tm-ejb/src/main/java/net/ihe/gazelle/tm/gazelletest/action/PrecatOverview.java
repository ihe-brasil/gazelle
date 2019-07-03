package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.restrictions.HQLRestrictionPathEqual;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOption;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOptionEntity;
import net.ihe.gazelle.tm.filter.TMCriterions;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestRoles;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestRolesQuery;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestType;
import net.ihe.gazelle.tm.gazelletest.model.instance.Status;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.SystemEntity;
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
import java.util.List;
import java.util.Map;

@Name("precatOverview")
@Scope(ScopeType.PAGE)
public class PrecatOverview implements Serializable, QueryModifier<TestRoles> {

    private static final Logger LOG = LoggerFactory.getLogger(PrecatOverview.class);
    private static final long serialVersionUID = -3199206193915828743L;

    private Filter<TestRoles> filter;

    private FilterDataModel<TestRoles> testRolesDataModel;

    private Status status;

    @Create
    public void create() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("create");
        }

    }

    @Remove
    @Destroy
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    public Status getStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getStatus");
        }
        return status;
    }

    public void setStatus(Status status) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setStatus");
        }
        this.status = status;
    }

    public Filter<TestRoles> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();

            TestRolesQuery testRolesQuery = new TestRolesQuery();
            HQLCriterionsForFilter<TestRoles> criterions = testRolesQuery.getHQLCriterionsForFilter();

            criterions.addPath("testing_session", testRolesQuery.roleInTest().testParticipantsList().aipo()
                    .systemActorProfiles().system().systemsInSession().testingSession());
            criterions.addPath("institution", testRolesQuery.roleInTest().testParticipantsList().aipo()
                    .systemActorProfiles().system().institutionSystems().institution());
            criterions.addPath("system", testRolesQuery.roleInTest().testParticipantsList().aipo()
                    .systemActorProfiles().system());
            criterions.addPath("test", testRolesQuery.test());
            TMCriterions.addAIPOCriterionsUsingTest(criterions, testRolesQuery.test(), "testing_session");

            criterions.addQueryModifier(this);

            filter = new Filter<TestRoles>(criterions, requestParameterMap);
        }
        return filter;
    }

    public FilterDataModel<TestRoles> getTestRolesDataModel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestRolesDataModel");
        }
        if (testRolesDataModel == null) {
            testRolesDataModel = new FilterDataModel<TestRoles>(getFilter()) {
                /**
                 *
                 */
                private static final long serialVersionUID = -2045795884108795983L;

                @Override
                protected Object getId(TestRoles t) {
                    // TODO Auto-generated method stub
                    return t.getId();
                }

                @Override
                protected List<?> getRealCache(HQLQueryBuilder<TestRoles> queryBuilder) {
                    TestRolesQuery query = new TestRolesQuery(queryBuilder);
                    return query.getQueryBuilder().getMultiSelect(query.test(),
                            query.roleInTest().testParticipantsList().aipo().systemActorProfiles().system(),
                            query.roleInTest().testParticipantsList().actorIntegrationProfileOption());
                }
            };
        }
        return testRolesDataModel;
    }

    @Override
    public void modifyQuery(HQLQueryBuilder<TestRoles> queryBuilder, Map<String, Object> filterValuesApplied) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyQuery");
        }
        TestRolesQuery query = new TestRolesQuery(queryBuilder);

        query.roleInTest().testParticipantsList().tested().eq(true);
        query.test().testStatus().keyword().eq("ready");
        query.test().testType().keyword().eq(TestType.TYPE_MESA_STRING);

        if (status != null) {
            ActorIntegrationProfileOptionEntity<ActorIntegrationProfileOption> actorIntegrationProfileOptionPath1 = query
                    .roleInTest().testInstanceParticipants().actorIntegrationProfileOption();
            ActorIntegrationProfileOptionEntity<ActorIntegrationProfileOption> actorIntegrationProfileOptionPath2 = query
                    .roleInTest().testParticipantsList().actorIntegrationProfileOption();
            query.addRestriction(new HQLRestrictionPathEqual(actorIntegrationProfileOptionPath1.toString(),
                    actorIntegrationProfileOptionPath2.toString()));

            SystemEntity<System> systemPath1 = query.roleInTest().testInstanceParticipants().systemInSessionUser()
                    .systemInSession().system();
            SystemEntity<System> systemPath2 = query.roleInTest().testParticipantsList().aipo().systemActorProfiles()
                    .system();
            query.addRestriction(new HQLRestrictionPathEqual(systemPath1.toString(), systemPath2.toString()));

            query.roleInTest().testInstanceParticipants().testInstance().lastStatus().eq(status);
        }
        // HQLRestrictionPathEqual
    }
}
