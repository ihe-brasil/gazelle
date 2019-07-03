package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.hql.HQLRestriction;
import net.ihe.gazelle.hql.beans.HQLStatistic;
import net.ihe.gazelle.hql.beans.HQLStatisticItem;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.tm.filter.TMCriterions;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestPeerType;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestQuery;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.SystemInSessionAttributes;
import net.ihe.gazelle.tm.systems.model.SystemInSessionQuery;
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

@Name("kpiTests")
@Scope(ScopeType.PAGE)
public class KpiTests extends KpiDisplay<Test> implements Serializable {

    private static final long serialVersionUID = -429665831360950956L;
    private static final Logger LOG = LoggerFactory.getLogger(KpiTests.class);

    @Create
    public void create() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("create");
        }

        SystemInSessionQuery sisQuery = new SystemInSessionQuery();
        TestQuery query = new TestQuery();
        HQLCriterionsForFilter<Test> criterions = query.getHQLCriterionsForFilter();

        HQLRestriction isParticipantsTested = query.testRoles().roleInTest().testParticipantsList().tested().eqRestriction(true);
        HQLRestriction isRolePlayedByATool = query.testRoles().roleInTest().isRolePlayedByATool().eqRestriction(false);
        HQLRestriction participantsRestrictions = HQLRestrictions.and(isParticipantsTested, isRolePlayedByATool);

        SystemInSessionAttributes<SystemInSession> systemInSessionSystemInSessionAttributes = query.testRoles().roleInTest().testParticipantsList()
                .aipo().systemActorProfiles().system().systemsInSession();
        TMCriterions.addTestingSession(criterions, "testing_session", systemInSessionSystemInSessionAttributes.testingSession());
        TMCriterions.addTestCriterions(criterions, query, true);
        TMCriterions.addAIPOCriterionsUsingTest(criterions, query, "testing_session");

        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> requestParametersMap = fc.getExternalContext().getRequestParameterMap();
        filter = new Filter<Test>(criterions, requestParametersMap);

        List<HQLStatisticItem> statisticItems = new ArrayList<HQLStatisticItem>();

        statisticItems.add(systemInSessionSystemInSessionAttributes.id()
                .statisticItem(participantsRestrictions));


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

    @Override
    public String styleClassIfNoMonitor(HQLStatistic<Test> item) {

        Integer nbSystems = item.getCounts()[0];
        Integer nbMonitors = item.getCounts()[1];
        String styleClass = "";
        if (nbSystems != null && nbMonitors != null) {
            if (nbSystems == 1 && nbMonitors.equals(0) && !item.getItem().getTestPeerType().equals(TestPeerType.getNO_PEER_TEST())) {
                styleClass = "gzl-warn-row";
            } else if (nbSystems >= 2 && nbMonitors.equals(0) && !item.getItem().getTestPeerType().equals(TestPeerType.getNO_PEER_TEST())) {
                styleClass = "gzl-error-row";
            } else if (nbSystems >= 1 && nbMonitors.equals(0) && item.getItem().getTestPeerType().equals(TestPeerType.getNO_PEER_TEST())) {
                styleClass = "gzl-error-row";
            }
        }
        return styleClass;
    }
}
//    rouge si (S => 2, M=0 et testType = peerToPeer) ou (S = 1, M= 0 et testType = noPeer)
//        [09:40:01] Anne-Gaelle: orange si S = 1, M=0 et testType != noPeer
