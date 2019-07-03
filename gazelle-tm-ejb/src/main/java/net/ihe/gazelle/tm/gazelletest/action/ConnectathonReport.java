package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.HQLRestriction;
import net.ihe.gazelle.hql.beans.HQLStatistic;
import net.ihe.gazelle.hql.beans.HQLStatisticItem;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.tf.model.Actor;
import net.ihe.gazelle.tf.model.Domain;
import net.ihe.gazelle.tf.model.IntegrationProfile;
import net.ihe.gazelle.tm.gazelletest.bean.SAPResultDetailItem;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestType;
import net.ihe.gazelle.tm.gazelletest.model.instance.*;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.SystemQuery;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.InstitutionQuery;
import net.ihe.gazelle.users.model.Role;
import net.ihe.gazelle.users.model.User;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.ResourceBundle;
import org.jboss.seam.security.AuthorizationException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

@Name("connectathonReport")
@Scope(ScopeType.PAGE)
public class ConnectathonReport implements Serializable {

    static final Comparator<Object[]> STATISTICS_COMPARATOR = new Comparator<Object[]>() {

        @Override
        public int compare(Object[] o1, Object[] o2) {
            return (((Number) o1[1]).intValue() - ((Number) o2[1]).intValue());
        }

    };
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(ConnectathonReport.class);
    private TestingSession selectedTestingSession;
    private List<SystemInSession> systemsInSession;
    private Institution selectedInstitution;
    // needed to numbered chapters
    private int index = 0;
    private List<Object[]> statisticsPerDomain;
    private List<Object[]> statisticsPerIntegrationProfile;
    private List<Object[]> statisticsPerActor;
    private List<Object[]> statisticsPerSystemPartner;
    private List<Object[]> statisticsPerInstitutionPartner;
    private List<Object[]> statisticsPerMonitor;
    private List<SystemAIPOResultForATestingSession> systemAIPOResults;
    private Date generationDate;
    private List<SAPResultDetailItem> aipoDetails;
    private String reportId;

    @Create
    public void initializeDocument() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeDocument");
        }
        Map<String, String> urlParams = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        if (urlParams.containsKey("testingSession")) {
            try {
                Integer testingSessionId = Integer.valueOf(urlParams.get("testingSession"));
                selectedTestingSession = entityManager.find(TestingSession.class, testingSessionId);
            } catch (NumberFormatException e) {
                LOG.error("testingSession param must be an integer");
                return;
            }
        } else {
            selectedTestingSession = TestingSession.getSelectedTestingSession();
        }
        if (urlParams.containsKey("institution")) {
            Integer institutionId = Integer.parseInt(urlParams.get("institution"));
            InstitutionQuery query = new InstitutionQuery();
            query.id().eq(institutionId);
            selectedInstitution = query.getUniqueResult();
        } else {
            selectedInstitution = User.loggedInUser().getInstitution();
        }
        if (urlParams.containsKey("system")) {
            Integer systemId = Integer.parseInt(urlParams.get("system"));
            SystemQuery systemQuery = new SystemQuery();
            systemQuery.id().eq(systemId);
            SystemInSession sis = SystemInSession.getSystemInSessionForSession(systemQuery.getUniqueResult(),
                    selectedTestingSession);
            if (sis != null) {
                systemsInSession = new ArrayList<>();
                systemsInSession.add(sis);
            }
        } else {
            systemsInSession = SystemInSession.getAcceptedSystemsInSessionForCompanyForSession(entityManager,
                    selectedInstitution, selectedTestingSession);
        }
        User user = User.loggedInUser();
        if (!user.getRoles().contains(Role.getADMINISTRATOR_ROLE())
                && !user.getInstitution().equals(selectedInstitution)) {
            throw new AuthorizationException("You are not allowed to generate a report for this organization");
        }
        generationDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        reportId = selectedTestingSession.getYear() + "." + selectedTestingSession.getZone() + "."
                + selectedTestingSession.getType() + "." + selectedInstitution.getKeyword() + "."
                + sdf.format(generationDate);
    }

    public int updateIndex() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateIndex");
        }
        return ++index;
    }

    public void getResultsForSystemInSession(SystemInSession currentSIS) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getResultsForSystemInSession");
        }
        SystemAIPOResultForATestingSessionQuery query = new SystemAIPOResultForATestingSessionQuery();
        query.testSession().eq(selectedTestingSession);
        query.systemActorProfile().system().eq(currentSIS.getSystem());

        query.addRestriction(HQLRestrictions.or(query.enabled().eqRestriction(true), query.enabled()
                .isNullRestriction()));

        systemAIPOResults = query.getList();
    }

    public String getDisclaimer() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDisclaimer");
        }
        String disclaimer = ResourceBundle.instance().getString("gazelle.tm.cat.report.disclaimer");
        disclaimer = disclaimer.replace("$COMPANY_NAME$", selectedInstitution.getName());
        disclaimer = disclaimer.replace("$TS_NAME$", selectedTestingSession.getDescription());
        return disclaimer;
    }

    @SuppressWarnings("unchecked")
    public void prepareStatistics(SystemInSession systemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("prepareStatistics");
        }
        // get the list of test instances in which is involved the systemInSession (passed/failed/partially verified)
        HQLQueryBuilder<TestInstance> builder = new HQLQueryBuilder<TestInstance>(TestInstance.class);
        builder.addEq("testInstanceParticipants.systemInSessionUser.systemInSession.id", systemInSession.getId());

        builder.addIn("test.testType", TestType.getTestTypesWithoutMESA());

        String tiStatusPath = "lastStatus";
        builder.addRestriction(HQLRestrictions.or(HQLRestrictions.eq(tiStatusPath, Status.VERIFIED),
                HQLRestrictions.eq(tiStatusPath, Status.FAILED),
                HQLRestrictions.eq(tiStatusPath, Status.PARTIALLY_VERIFIED)));
        List<Integer> testInstanceIds = (List<Integer>) builder.getListDistinct("id");

        statisticsPerDomain = getTiPerDomain(systemInSession, testInstanceIds);
        statisticsPerIntegrationProfile = getTiPerIntegrationProfile(systemInSession, testInstanceIds);
        statisticsPerActor = getTiPerActor(systemInSession, testInstanceIds);
        statisticsPerMonitor = getTiPerMonitor(systemInSession, testInstanceIds);
        statisticsPerSystemPartner = getTiPerSystemPartner(systemInSession, testInstanceIds);
        statisticsPerInstitutionPartner = getTiPerOrganizationPartner(systemInSession, testInstanceIds);
    }

    private List<Object[]> getTiPerDomain(SystemInSession systemInSession, List<Integer> testInstanceIds) {
        HQLQueryBuilder<TestInstanceParticipants> builder = new HQLQueryBuilder<>(
                TestInstanceParticipants.class);
        builder.addEq("systemInSessionUser.systemInSession.id", systemInSession.getId());
        builder.addRestriction(HQLRestrictions.in("testInstance.id", testInstanceIds));
        //TODO: Add restrictions to remove aborted and paused tests instances
        String path = "actorIntegrationProfileOption.actorIntegrationProfile.integrationProfile.domainsProfile.domain";
        return builder.getStatistics(path);
    }

    private List<Object[]> getTiPerIntegrationProfile(SystemInSession systemInSession, List<Integer> testInstanceIds) {
        HQLQueryBuilder<TestInstanceParticipants> builder = new HQLQueryBuilder<>(
                TestInstanceParticipants.class);
        builder.addEq("systemInSessionUser.systemInSession.id", systemInSession.getId());
        builder.addRestriction(HQLRestrictions.in("testInstance.id", testInstanceIds));
        String path = "actorIntegrationProfileOption.actorIntegrationProfile.integrationProfile";
        return builder.getStatistics(path);
    }

    private List<Object[]> getTiPerActor(SystemInSession systemInSession, List<Integer> testInstanceIds) {
        HQLQueryBuilder<TestInstanceParticipants> builder = new HQLQueryBuilder<>(
                TestInstanceParticipants.class);
        builder.addEq("systemInSessionUser.systemInSession.id", systemInSession.getId());
        builder.addRestriction(HQLRestrictions.in("testInstance.id", testInstanceIds));
        String path = "actorIntegrationProfileOption.actorIntegrationProfile.actor";
        return builder.getStatistics(path);
    }

    private List<Object[]> getTiPerMonitor(SystemInSession systemInSession, List<Integer> testInstanceIds) {
        HQLQueryBuilder<TestInstanceParticipants> builder = new HQLQueryBuilder<>(
                TestInstanceParticipants.class);
        builder.addEq("systemInSessionUser.systemInSession.id", systemInSession.getId());
        builder.addRestriction(HQLRestrictions.in("testInstance.id", testInstanceIds));
        String path = "testInstance.monitorInSession";
        return builder.getStatistics(path);
    }

    private List<Object[]> getTiPerSystemPartner(SystemInSession systemInSession, List<Integer> testInstanceIds) {
        HQLQueryBuilder<TestInstance> builderForStats = new HQLQueryBuilder<>(TestInstance.class);
        builderForStats.addIn("id", testInstanceIds);
        String path = "testInstanceParticipants.systemInSessionUser.systemInSession.system";
        List<Object[]> statistics = builderForStats.getStatistics(path);
        if ((statistics != null) && !statistics.isEmpty()) {
            List<Object[]> statisticsToReturn = new ArrayList<>();
            for (Object[] stat : statistics) {
                if (!((System) stat[0]).getKeyword().equals(systemInSession.getSystem().getKeyword())) {
                    statisticsToReturn.add(stat);
                }
            }
            return statisticsToReturn;
        } else {

            return null;
        }
    }

    private List<Object[]> getTiPerOrganizationPartner(SystemInSession systemInSession, List<Integer> testInstanceIds) {
        HQLQueryBuilder<TestInstance> builderForStats = new HQLQueryBuilder<>(TestInstance.class);
        builderForStats.addIn("id", testInstanceIds);
        String path = "testInstanceParticipants.systemInSessionUser.user.institution";
        List<Object[]> statistics = builderForStats.getStatistics(path);
        if ((statistics != null) && !statistics.isEmpty()) {
            List<Institution> systemInstitution = System.getInstitutionsForASystem(systemInSession.getSystem());
            List<Object[]> statisticsToReturn = new ArrayList<>();
            for (Object[] stat : statistics) {
                if (!systemInstitution.contains(stat[0])) {
                    statisticsToReturn.add(stat);
                }
            }
            return statisticsToReturn;
        } else {

            return null;
        }
    }

    public List<HQLStatistic<SystemInSession>> getTestInstancesSummary(SystemInSession systemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestInstancesSummary");
        }
        HQLQueryBuilder<SystemInSession> builder = new HQLQueryBuilder<>(SystemInSession.class);
        builder.addEq("id", systemInSession.getId());
        List<HQLStatisticItem> statisticItems = new ArrayList<>();
        String tiPath = "systemInSessionUsers.testInstanceParticipants.testInstance.id";
        String tiStatusPath = "systemInSessionUsers.testInstanceParticipants.testInstance.lastStatus";
        String testTypePath = "systemInSessionUsers.testInstanceParticipants.testInstance.test.testType";
        HQLRestriction restrictionOnTestType = HQLRestrictions.in(testTypePath, TestType.getTestTypesWithoutMESA());
        // # tests
        statisticItems.add(new HQLStatisticItem("systemInSessionUsers.testInstanceParticipants.testInstance.test.id",
                restrictionOnTestType));
        // # test instances performed
        statisticItems.add(new HQLStatisticItem(tiPath, HQLRestrictions.and(
                restrictionOnTestType,
                HQLRestrictions.or(HQLRestrictions.eq(tiStatusPath, Status.VERIFIED),
                        HQLRestrictions.eq(tiStatusPath, Status.FAILED),
                        HQLRestrictions.eq(tiStatusPath, Status.PARTIALLY_VERIFIED)))));
        // # test instances Verified
        statisticItems.add(new HQLStatisticItem(tiPath, HQLRestrictions.and(restrictionOnTestType,
                HQLRestrictions.eq(tiStatusPath, Status.VERIFIED))));
        // # test instances Failed
        statisticItems.add(new HQLStatisticItem(tiPath, HQLRestrictions.and(restrictionOnTestType,
                HQLRestrictions.eq(tiStatusPath, Status.FAILED))));
        // # test instances Partially Verified
        statisticItems.add(new HQLStatisticItem(tiPath, HQLRestrictions.and(restrictionOnTestType,
                HQLRestrictions.eq(tiStatusPath, Status.PARTIALLY_VERIFIED))));

        return builder.getListWithStatistics(statisticItems);
    }

    public TestingSession getSelectedTestingSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestingSession");
        }
        return selectedTestingSession;
    }

    public void setSelectedTestingSession(TestingSession selectedTestingSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestingSession");
        }
        this.selectedTestingSession = selectedTestingSession;
    }

    public String getTestTitle(SAPResultDetailItem item) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestTitle");
        }
        StringBuffer title = new StringBuffer();
        if (!item.isMetaTest()) {
            title.append(item.getTitle());
        } else if (item.getTitle2().contains("All tests")) {
            title.append("Meta test: ");
            title.append(item.getTitle2());
        } else {
            title.append(item.getTitle2());
            title.append(" (");
            title.append(item.getTitle());
            title.append(")");
        }
        return title.toString();
    }

    public String getTextForTestInstanceDetails(SystemAIPOResultForATestingSession aipoResult) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTextForTestInstanceDetails");
        }
        ConnectathonResult connectathonResultBean = (ConnectathonResult) Component.getInstance("connectathonResult");
        aipoDetails = connectathonResultBean.computeDetail(aipoResult);
        if ((aipoDetails == null) || aipoDetails.isEmpty()) {
            return "No test has been defined for this Integration Profile/Actor/Option";
        } else {
            return "See below the details of the test instances performed for this Integration Profile/Actor/Option";
        }
    }

    public JFreeChart getPieChart(String chartType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPieChart");
        }
        DefaultPieDataset dataSet = new DefaultPieDataset();
        if (chartType == null) {
            return null;
        } else if (chartType.equals("domain") && (statisticsPerDomain != null)) {
            ArrayList<Object[]> dom = new ArrayList<>(statisticsPerDomain);
            Collections.sort(dom, STATISTICS_COMPARATOR);
            for (Object[] stat : dom) {
            	if (stat != null && stat.length>1 && stat[0] != null && stat[1] != null) {
            		dataSet.setValue(((Domain) stat[0]).getKeyword(), ((Number) stat[1]).doubleValue());
            	}
            }
        } else if (chartType.equals("actor") && (statisticsPerActor != null)) {
            ArrayList<Object[]> act = new ArrayList<>(statisticsPerActor);
            Collections.sort(act, STATISTICS_COMPARATOR);
            for (Object[] stat : act) {
            	if (stat != null && stat.length>1 && stat[0] != null && stat[1] != null) {
            		dataSet.setValue(((Actor) stat[0]).getKeyword(), ((Number) stat[1]).doubleValue());
            	}
            }
        } else if (chartType.equals("integrationProfile") && (statisticsPerIntegrationProfile != null)) {
            ArrayList<Object[]> ip = new ArrayList<>(statisticsPerIntegrationProfile);
            Collections.sort(ip, STATISTICS_COMPARATOR);
            for (Object[] stat : ip) {
            	if (stat != null && stat.length>1 && stat[0] != null && stat[1] != null) {
            		dataSet.setValue(((IntegrationProfile) stat[0]).getKeyword(), ((Number) stat[1]).doubleValue());
            	}
            }
        } else if (chartType.equals("organization") && (statisticsPerInstitutionPartner != null)) {
            ArrayList<Object[]> partners = new ArrayList<>(statisticsPerInstitutionPartner);
            Collections.sort(partners, STATISTICS_COMPARATOR);
            for (Object[] stat : partners) {
            	if (stat != null && stat.length>1 && stat[0] != null && stat[1] != null) {
            		dataSet.setValue(((Institution) stat[0]).getKeyword(), ((Number) stat[1]).doubleValue());
            	}
            }
        } else if (chartType.equals("system") && (statisticsPerSystemPartner != null)) {
            ArrayList<Object[]> sp = new ArrayList<>(statisticsPerSystemPartner);
            Collections.sort(sp, STATISTICS_COMPARATOR);
            for (Object[] stat : sp) {
            	if (stat != null && stat.length>1 && stat[0] != null && stat[1] != null) {
            		dataSet.setValue(((System) stat[0]).getKeyword(), ((Number) stat[1]).doubleValue());
            	}
            }
        } else if (chartType.equals("monitor") && (statisticsPerMonitor != null)) {
            ArrayList<Object[]> monitors = new ArrayList<>(statisticsPerMonitor);
            Collections.sort(monitors, STATISTICS_COMPARATOR);
            for (Object[] stat : monitors) {
                if ((stat[0] != null) && (((MonitorInSession) stat[0]).getUser() != null)) {
                    dataSet.setValue(((MonitorInSession) stat[0]).getUser().getUsername(),
                            ((Number) stat[1]).doubleValue());
                } else if (stat[0] != null) {
                    dataSet.setValue("Monitor" + ((MonitorInSession) stat[0]).getId(), ((Number) stat[1]).doubleValue());
                }
            }
        } else {
            return null;
        }
        JFreeChart chart = ChartFactory.createPieChart(null, dataSet, false, true, false);
        return chart;
    }

    public String getReportUrl(TestingSession testingSession, Institution institution, System system) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getReportUrl");
        }
        if ((testingSession == null) || (institution == null)) {
            return null;
        }

        StringBuilder url = new StringBuilder("/testing/reporting/reporting4TestingSession.seam?testingSession=");//DONE
        url.append(testingSession.getId());
        url.append("&institution=");
        url.append(institution.getId());
        if (system != null) {
            url.append("&system=");
            url.append(system.getId());
        }
        return url.toString();
    }

    public String getReportUrlForVendor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getReportUrlForVendor");
        }
        User currentUser = User.loggedInUser();
        TestingSession testingSession = TestingSession.getSelectedTestingSession();
        return getReportUrl(testingSession, currentUser.getInstitution(), null);
    }

    public List<SystemInSession> getSystemsInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemsInSession");
        }
        return systemsInSession;
    }

    public void setSystemsInSession(List<SystemInSession> systemsInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSystemsInSession");
        }
        this.systemsInSession = systemsInSession;
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

    public List<Object[]> getStatisticsPerDomain() {
        return statisticsPerDomain;
    }

    public List<Object[]> getStatisticsPerIntegrationProfile() {
        return statisticsPerIntegrationProfile;
    }

    public List<Object[]> getStatisticsPerActor() {
        return statisticsPerActor;
    }

    public List<Object[]> getStatisticsPerSystemPartner() {
        return statisticsPerSystemPartner;
    }

    public List<Object[]> getStatisticsPerInstitutionPartner() {
        return statisticsPerInstitutionPartner;
    }

    public List<Object[]> getStatisticsPerMonitor() {
        return statisticsPerMonitor;
    }

    public List<SystemAIPOResultForATestingSession> getSystemAIPOResults() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemAIPOResults");
        }
        return systemAIPOResults;
    }

    public Date getGenerationDate() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getGenerationDate");
        }
        return generationDate;
    }

    public List<SAPResultDetailItem> getAipoDetails() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAipoDetails");
        }
        return aipoDetails;
    }

    public String getReportId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getReportId");
        }
        return reportId;
    }
}
