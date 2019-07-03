package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.HQLRestriction;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.tf.model.Domain;
import net.ihe.gazelle.tf.model.IntegrationProfile;
import net.ihe.gazelle.tm.filter.SystemInSessionFilter1;
import net.ihe.gazelle.tm.filter.TMCriterions;
import net.ihe.gazelle.tm.filter.modifier.IntegrationProfilesFromTestingSession;
import net.ihe.gazelle.tm.filter.modifier.TestMatchingTestingSession;
import net.ihe.gazelle.tm.filter.modifier.TestParticipantTested;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestEntity;
import net.ihe.gazelle.tm.report.action.ReportManager;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.*;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Remove;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@Name("systemInSessionOverview")
@Scope(ScopeType.PAGE)
public class SystemInSessionOverview implements Serializable, QueryModifier<SystemActorProfiles> {
    private static final Logger LOG = LoggerFactory.getLogger(SystemInSessionOverview.class);
    private static final long serialVersionUID = 1394117177837966208L;
    private SystemInSessionFilter1 filter;
    private FilterDataModel<SystemInSession> systemsInSession;

    private Filter<SystemActorProfiles> filterSAP;
    private FilterDataModel<SystemActorProfiles> systemActorProfiles;

    private TestingType testingType;

    private List<System> systemsWithoutTestingSession;

    private boolean displaySystemsWithoutTestingSession;

    public List<System> getSystemsWithoutTestingSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemsWithoutTestingSession");
        }
        SystemInSessionQuery sisQuery = new SystemInSessionQuery();
        List<Integer> sisIds = sisQuery.system().id().getListDistinct();

        SystemQuery query = new SystemQuery();
        HQLRestriction restriction = query.id().ninRestriction(sisIds);
        query.addRestriction(restriction);
        setSystemsWithoutTestingSession(query.getListDistinct());

        return systemsWithoutTestingSession;
    }

    public void setSystemsWithoutTestingSession(List<System> systemsWithoutTestingSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSystemsWithoutTestingSession");
        }
        this.systemsWithoutTestingSession = systemsWithoutTestingSession;
    }

    public boolean isDisplaySystemsWithoutTestingSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isDisplaySystemsWithoutTestingSession");
        }
        return displaySystemsWithoutTestingSession;
    }

    public void setDisplaySystemsWithoutTestingSession(boolean displaySystemsWithoutTestingSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDisplaySystemsWithoutTestingSession");
        }
        this.displaySystemsWithoutTestingSession = displaySystemsWithoutTestingSession;
    }

    public TestingType getTestingType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestingType");
        }
        return testingType;
    }

    public void setTestingType(TestingType testingType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestingType");
        }
        this.testingType = testingType;
    }

    @Remove
    @Destroy
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    public SystemInSessionFilter1 getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();
            filter = new SystemInSessionFilter1(requestParameterMap);
        }
        return filter;
    }

    public void refreshFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("refreshFilter");
        }
        getFilter().modified();
        getFilterSAP().modified();
        getSystemsInSession().resetCache();
        getSystemActorProfiles().resetCache();
    }

    public FilterDataModel<SystemInSession> getSystemsInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemsInSession");
        }
        if (systemsInSession == null) {
            systemsInSession = new FilterDataModel<SystemInSession>(getFilter()) {
                @Override
                protected Object getId(SystemInSession t) {
                    // TODO Auto-generated method stub
                    return t.getId();
                }
            };
        }
        return systemsInSession;
    }

    public List<SystemInSession> getListOfSystemInSessionAsList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfSystemInSessionAsList");
        }
        List<SystemInSession> allSystemInSession = (List<SystemInSession>) this.getSystemsInSession().getAllItems(
                FacesContext.getCurrentInstance());
        return allSystemInSession;
    }

    public Filter<SystemActorProfiles> getFilterSAP() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilterSAP");
        }
        if (filterSAP == null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();

            SystemActorProfilesQuery sapQuery = new SystemActorProfilesQuery();

            HQLCriterionsForFilter<SystemActorProfiles> hqlCriterionsForFilter = sapQuery.getHQLCriterionsForFilter();

            TMCriterions.addActiveTestingSession(hqlCriterionsForFilter, "testing_session", sapQuery.system()
                    .systemsInSession().testingSession());
            TMCriterions.addAIPOCriterions(hqlCriterionsForFilter, sapQuery.actorIntegrationProfileOption());
            hqlCriterionsForFilter
                    .addQueryModifier(new IntegrationProfilesFromTestingSession(sapQuery
                            .actorIntegrationProfileOption().actorIntegrationProfile().integrationProfile(),
                            "testing_session"));

            TestEntity<Test> testPath = sapQuery.aipo().testParticipants().roleInTest().testRoles().test();
            hqlCriterionsForFilter.addPath("test", testPath);

            hqlCriterionsForFilter.addQueryModifierForCriterion("test", new TestMatchingTestingSession(testPath,
                    "testing_session"));
            hqlCriterionsForFilter.addQueryModifierForCriterion("test", new TestParticipantTested(sapQuery.aipo()
                    .testParticipants()));

            hqlCriterionsForFilter.addPath("system", sapQuery.system());
            hqlCriterionsForFilter.addPath("institution", sapQuery.system().institutionSystems().institution()
                    .keyword());
            hqlCriterionsForFilter.addPath("demonstration", sapQuery.system().systemsInSession()
                    .demonstrationsSystemInSession().demonstration().name());

            hqlCriterionsForFilter.addPath("wanted_test_type", sapQuery.wantedTestingType());
            hqlCriterionsForFilter.addPath("test_type", sapQuery.testingType());
            hqlCriterionsForFilter.addPath("test_type_reviewed", sapQuery.testingTypeReviewed());
            hqlCriterionsForFilter.addPath("reviewable", sapQuery.actorIntegrationProfileOption().maybeSupportive());

            hqlCriterionsForFilter.addQueryModifier(this);

            filterSAP = new Filter<SystemActorProfiles>(hqlCriterionsForFilter, requestParameterMap);
        }
        return filterSAP;
    }

    public void initForSupportiveAIPOs() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initForSupportiveAIPOs");
        }
        getFilterSAP().getFilterValues().put("reviewable", Boolean.TRUE);
    }

    public FilterDataModel<SystemActorProfiles> getSystemActorProfiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemActorProfiles");
        }
        if (systemActorProfiles == null) {
            systemActorProfiles = new FilterDataModel<SystemActorProfiles>(getFilterSAP()) {
                @Override
                protected Object getId(SystemActorProfiles t) {
                    return t.getId();
                }
            };
        }
        return systemActorProfiles;
    }

    @Override
    public void modifyQuery(HQLQueryBuilder<SystemActorProfiles> queryBuilder, Map<String, Object> filterValuesApplied) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyQuery");
        }
        SystemActorProfilesQuery query = new SystemActorProfilesQuery(queryBuilder);

        queryBuilder.addRestriction(HQLRestrictions.or(query.system().systemsInSession().registrationStatus()
                .neqRestriction(SystemInSessionRegistrationStatus.DROPPED), query.system().systemsInSession()
                .registrationStatus().isNullRestriction()));
        queryBuilder.addRestriction(query.system().systemsInSession().testingSession()
                .eqRestriction(TestingSession.getSelectedTestingSession()));

    }

    public void changeAllSelectedSAPTestingType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("changeAllSelectedSAPTestingType");
        }
        int nbChange = 0;
        List<SystemActorProfiles> sapList = (List<SystemActorProfiles>) getSystemActorProfiles().getAllItems(
                FacesContext.getCurrentInstance());

        for (SystemActorProfiles sap : sapList) {
            if (getTestingType().getKeyword().equals("S")
                    && !sap.getActorIntegrationProfileOption().getMaybeSupportive()) {
                // Do nothing
                String actor = sap.getActorIntegrationProfileOption().getActorIntegrationProfile().getActor()
                        .getKeyword();
                String ip = sap.getActorIntegrationProfileOption().getActorIntegrationProfile().getIntegrationProfile()
                        .getKeyword();
                String option = sap.getActorIntegrationProfileOption().getIntegrationProfileOption().getKeyword();
                FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                        "Test type from " + actor + " " + ip + " " + option + " can not be set to Supportive");
            } else {
                sap.setTestingType(getTestingType());
                sap.setTestingTypeReviewed(true);
                nbChange++;
            }
        }
        setTestingType(null);
        FacesMessages.instance().add(StatusMessage.Severity.INFO, nbChange + " System Actor Profiles updated");
    }

    public void listSystemsSummary() throws IOException {
        Domain domain = getDomainFromFilter();
        if (domain != null) {
            List<System> systemsList = new ArrayList<>();
            List<SystemActorProfiles> systemActorProfilesList = getSystemActorProfiles().getAllItems(FacesContext.getCurrentInstance());
            for (SystemActorProfiles systemActorProfiles : systemActorProfilesList) {
                systemsList.add(systemActorProfiles.getSystem());
            }
            systemsList = removeDuplicateSystems(systemsList);

            TestingSession activeSession = TestingSession.getSelectedTestingSession();
            Collection<IntegrationProfile> listOfIntegrationProfile = activeSession.getIntegrationProfilesUnsorted();
            List<IntegrationProfile> listOfIntegrationProfileCopy = new ArrayList<>(listOfIntegrationProfile);
            for (IntegrationProfile integrationProfile : listOfIntegrationProfile) {
                try {
                    List<Domain> ipDomain = IntegrationProfile.getDomainOfIntegrationProfileList(integrationProfile);
                    for (Domain dom : ipDomain) {
                        if (!dom.getKeyword().equals(domain.getKeyword())) {
                            listOfIntegrationProfileCopy.remove(integrationProfile);
                        }
                    }
                } catch (Exception e) {
                    LOG.error("" + e.getMessage());
                }
            }

            ReportManager rm = new ReportManager();
            rm.listSystemsSummary(listOfIntegrationProfileCopy, systemsList);
        }
    }

    public Domain getDomainFromFilter() {
        return (Domain) getFilterSAP().getFilterValues().get("domain");
    }

    public List<System> removeDuplicateSystems(List<System> systemList) {
        Set<System> systems = new HashSet<System>();
        systems.addAll(systemList);
        systemList.clear();
        systemList.addAll(systems);
        return systemList;
    }
}
