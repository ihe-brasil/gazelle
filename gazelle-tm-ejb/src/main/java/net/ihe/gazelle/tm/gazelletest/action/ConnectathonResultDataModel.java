package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.tm.gazelletest.model.instance.SystemAIPOResultForATestingSession;
import net.ihe.gazelle.tm.gazelletest.model.instance.SystemAIPOResultForATestingSessionQuery;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectathonResultDataModel extends FilterDataModel<SystemAIPOResultForATestingSession> {

    private static final long serialVersionUID = 7927806087657389604L;

    private static final Logger LOG = LoggerFactory.getLogger(ConnectathonResultDataModel.class);

    private Integer selectedTestPeerTypeId;

    public ConnectathonResultDataModel(Filter<SystemAIPOResultForATestingSession> filter) {
        super(filter);
    }

    public Integer getSelectedTestPeerTypeId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestPeerTypeId");
        }
        return selectedTestPeerTypeId;
    }

    public void setSelectedTestPeerTypeId(Integer selectedTestPeerTypeId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestPeerTypeId");
        }
        this.selectedTestPeerTypeId = selectedTestPeerTypeId;
        resetCache();
    }

    @Override
    public void appendFiltersFields(HQLQueryBuilder<SystemAIPOResultForATestingSession> queryBuilder) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("appendFiltersFields");
        }
        if ((selectedTestPeerTypeId != null) && (selectedTestPeerTypeId != -1)) {
            SystemAIPOResultForATestingSessionQuery query = new SystemAIPOResultForATestingSessionQuery(queryBuilder);
            queryBuilder.addRestriction(query.systemActorProfile().aipo().testParticipants().roleInTest().testRoles()
                    .test().testStatus().keyword().eqRestriction("ready"));

            queryBuilder.addRestriction(query.systemActorProfile().aipo().testParticipants().roleInTest().testRoles()
                    .test().testType().inRestriction(TestingSession.getSelectedTestingSession().getTestTypes()));

            queryBuilder.addRestriction(query.systemActorProfile().aipo().testParticipants().roleInTest().testRoles()
                    .test().testPeerType().id().eqRestriction(selectedTestPeerTypeId));
        }
    }

    @Override
    protected Object getId(SystemAIPOResultForATestingSession t) {
        // TODO Auto-generated method stub
        return t.getId();
    }
}
