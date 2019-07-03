package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.report.ReportExporterManager;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.Actor;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOption;
import net.ihe.gazelle.tf.model.Domain;
import net.ihe.gazelle.tf.model.IntegrationProfile;
import net.ihe.gazelle.tm.gazelletest.model.definition.*;
import net.ihe.gazelle.tm.gazelletest.model.instance.MonitorInSession;
import net.ihe.gazelle.tm.gazelletest.model.instance.MonitorInSessionQuery;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.Role;
import net.ihe.gazelle.users.model.User;
import net.sf.jasperreports.engine.JRException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.*;


@Name("monitorInSessionAdministrationManager")
@Scope(ScopeType.SESSION)
@GenerateInterface("MonitorInSessionAdministrationManagerLocal")
public class MonitorInSessionAdministrationManager implements MonitorInSessionAdministrationManagerLocal, Serializable,
        QueryModifier<MonitorInSession> {

    public static final String SEARCH_BY_DOMAIN_LABEL_TO_DISPLAY = "gazelle.common.button.searchByDomain";
    public static final String SEARCH_BY_PROFILE_LABEL_TO_DISPLAY = "gazelle.common.button.searchByIntegrationProfile";
    public static final String SEARCH_BY_ACTOR_LABEL_TO_DISPLAY = "gazelle.common.button.searchByActor";
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(MonitorInSessionAdministrationManager.class);
    private MonitorInSession selectedMonitorInSession;

    private Domain selectedDomain;
    private IntegrationProfile selectedIntegrationProfile;
    private Actor selectedActor;

    private String selectedCriterion;

    private List<User> selectedMonitors;

    private List<Test> availableTests;
    private List<Test> selectedTests;

    private Filter<MonitorInSession> filter;

    private boolean showMonitorInSessionListPanel = true;
    private boolean showAddMonitorInSessionPanel = false;
    private boolean showEditMonitorInSessionPanel = false;
    private boolean showViewMonitorInSessionPanel = false;
    private boolean showAddTestsPanel = false;

    private List<Test> testsWithNoMonitors;

    private Institution selectedInstitution;

    @Override
    public Institution getSelectedInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedInstitution");
        }
        return selectedInstitution;
    }

    @Override
    public void setSelectedInstitution(Institution selectedInstitution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedInstitution");
        }
        this.selectedInstitution = selectedInstitution;
    }

    @Override
    public FilterDataModel<MonitorInSession> getFoundMonitorInSessionList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFoundMonitorInSessionList");
        }
        return new FilterDataModel<MonitorInSession>(getFilter()) {
            @Override
            protected Object getId(MonitorInSession monitorInSession) {
                return monitorInSession.getId();
            }
        };
    }

    public Filter<MonitorInSession> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            filter = new Filter<MonitorInSession>(getHQLCriterions());
        }
        return filter;
    }

    private HQLCriterionsForFilter<MonitorInSession> getHQLCriterions() {
        MonitorInSessionQuery query = new MonitorInSessionQuery();
        HQLCriterionsForFilter<MonitorInSession> criterionsForFilter = query.getHQLCriterionsForFilter();
        criterionsForFilter.addQueryModifier(this);
        return criterionsForFilter;
    }

    @Override
    public List<String> getPossibleSearchCriterionList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleSearchCriterionList");
        }
        List<String> possibleSearchCriterionList = new ArrayList<String>();
        possibleSearchCriterionList.add(SEARCH_BY_DOMAIN_LABEL_TO_DISPLAY);
        possibleSearchCriterionList.add(SEARCH_BY_PROFILE_LABEL_TO_DISPLAY);
        possibleSearchCriterionList.add(SEARCH_BY_ACTOR_LABEL_TO_DISPLAY);
        return possibleSearchCriterionList;
    }

    @Override
    public String getSEARCH_BY_DOMAIN_LABEL_TO_DISPLAY() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSEARCH_BY_DOMAIN_LABEL_TO_DISPLAY");
        }
        return SEARCH_BY_DOMAIN_LABEL_TO_DISPLAY;
    }

    @Override
    public String getSEARCH_BY_PROFILE_LABEL_TO_DISPLAY() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSEARCH_BY_PROFILE_LABEL_TO_DISPLAY");
        }
        return SEARCH_BY_PROFILE_LABEL_TO_DISPLAY;
    }

    @Override
    public String getSEARCH_BY_ACTOR_LABEL_TO_DISPLAY() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSEARCH_BY_ACTOR_LABEL_TO_DISPLAY");
        }
        return SEARCH_BY_ACTOR_LABEL_TO_DISPLAY;
    }

    @Override
    public String getSelectedCriterion() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedCriterion");
        }
        return selectedCriterion;
    }

    @Override
    public void setSelectedCriterion(String selectedCriterion) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedCriterion");
        }
        this.selectedCriterion = selectedCriterion;
    }

    @Override
    public List<Test> getSelectedTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTests");
        }
        return selectedTests;
    }

    @Override
    public void setSelectedTests(List<Test> selectedTests) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTests");
        }
        this.selectedTests = selectedTests;
    }

    @Override
    public List<Test> getAvailableTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAvailableTests");
        }
        return availableTests;
    }

    @Override
    public void setAvailableTests(List<Test> availableTests) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setAvailableTests");
        }
        this.availableTests = availableTests;
    }

    @Override
    public Domain getSelectedDomain() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedDomain");
        }
        return selectedDomain;
    }

    @Override
    public void setSelectedDomain(Domain selectedDomain) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedDomain");
        }
        this.selectedDomain = selectedDomain;
    }

    @Override
    public IntegrationProfile getSelectedIntegrationProfile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedIntegrationProfile");
        }
        return selectedIntegrationProfile;
    }

    @Override
    public void setSelectedIntegrationProfile(IntegrationProfile selectedIntegrationProfile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedIntegrationProfile");
        }
        this.selectedIntegrationProfile = selectedIntegrationProfile;
    }

    @Override
    public Actor getSelectedActor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedActor");
        }
        return selectedActor;
    }

    @Override
    public void setSelectedActor(Actor selectedActor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedActor");
        }
        this.selectedActor = selectedActor;
    }

    @Override
    public boolean isShowMonitorInSessionListPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isShowMonitorInSessionListPanel");
        }
        return showMonitorInSessionListPanel;
    }

    @Override
    public void setShowMonitorInSessionListPanel(boolean showMonitorInSessionListPanel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setShowMonitorInSessionListPanel");
        }
        this.showMonitorInSessionListPanel = showMonitorInSessionListPanel;
    }

    @Override
    public boolean isShowEditMonitorInSessionPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isShowEditMonitorInSessionPanel");
        }
        return showEditMonitorInSessionPanel;
    }

    @Override
    public void setShowEditMonitorInSessionPanel(boolean showEditMonitorInSessionPanel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setShowEditMonitorInSessionPanel");
        }
        this.showEditMonitorInSessionPanel = showEditMonitorInSessionPanel;
    }

    @Override
    public boolean isShowAddMonitorInSessionPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isShowAddMonitorInSessionPanel");
        }
        return showAddMonitorInSessionPanel;
    }

    @Override
    public void setShowAddMonitorInSessionPanel(boolean showAddMonitorInSessionPanel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setShowAddMonitorInSessionPanel");
        }
        this.showAddMonitorInSessionPanel = showAddMonitorInSessionPanel;
    }

    @Override
    public MonitorInSession getSelectedMonitorInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedMonitorInSession");
        }
        return selectedMonitorInSession;
    }

    @Override
    public void setSelectedMonitorInSession(MonitorInSession selectedMonitorInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedMonitorInSession");
        }
        this.selectedMonitorInSession = selectedMonitorInSession;
    }

    @Override
    public boolean isShowViewMonitorInSessionPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isShowViewMonitorInSessionPanel");
        }
        return showViewMonitorInSessionPanel;
    }

    @Override
    public void setShowViewMonitorInSessionPanel(boolean showViewMonitorInSessionPanel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setShowViewMonitorInSessionPanel");
        }
        this.showViewMonitorInSessionPanel = showViewMonitorInSessionPanel;
    }

    @Override
    public List<User> getSelectedMonitors() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedMonitors");
        }
        return selectedMonitors;
    }

    @Override
    public void setSelectedMonitors(List<User> selectedMonitors) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedMonitors");
        }
        this.selectedMonitors = selectedMonitors;
    }

    @Override
    public boolean isShowAddTestsPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isShowAddTestsPanel");
        }
        return showAddTestsPanel;
    }

    @Override
    public void setShowAddTestsPanel(boolean showAddTestsPanel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setShowAddTestsPanel");
        }
        this.showAddTestsPanel = showAddTestsPanel;
    }

    @Override
    public List<Test> getTestsWithNoMonitors() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestsWithNoMonitors");
        }
        return testsWithNoMonitors;
    }

    @Override
    public void setTestsWithNoMonitors(List<Test> testsWithNoMonitors) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestsWithNoMonitors");
        }
        this.testsWithNoMonitors = testsWithNoMonitors;
    }

    private void resetSearchValues() {
        selectedCriterion = null;
        selectedActor = null;
        selectedDomain = null;
        selectedIntegrationProfile = null;
        selectedTests = new ArrayList<Test>();
        availableTests = new ArrayList<Test>();
    }

    @Override
    public List<User> getMonitorsList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMonitorsList");
        }
        List<User> monitors = User.getUsersFiltered(this.selectedInstitution, Role.getMONITOR_ROLE());
        List<MonitorInSession> monitorInSessionList = MonitorInSession
                .getAllActivatedMonitorsForATestingSession(TestingSession.getSelectedTestingSession());
        if (monitorInSessionList != null) {
            if (monitors != null) {
                for (MonitorInSession monitorInSession : monitorInSessionList) {
                    monitors.remove(monitorInSession.getUser());
                }
                Collections.sort(monitors);
                return monitors;
            }
        } else {
            Collections.sort(monitors);
            return monitors;
        }
        return null;
    }

    @Override
    public void initMonitorInSessionManagement() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initMonitorInSessionManagement");
        }
        availableTests = new ArrayList<Test>();
        selectedTests = new ArrayList<Test>();
        hideAddMonitorInSessionPanel();
        updateMonitorInSessionListStatus();
    }

    @Override
    public void showEditMonitorInSessionPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showEditMonitorInSessionPanel");
        }
        this.setShowAddMonitorInSessionPanel(false);
        this.setShowEditMonitorInSessionPanel(true);
        this.setShowMonitorInSessionListPanel(false);
        this.setShowViewMonitorInSessionPanel(false);
    }

    @Override
    public void hideEditMonitorInSessionPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("hideEditMonitorInSessionPanel");
        }
        hideAddMonitorInSessionPanel();
    }

    @Override
    public void showAddMonitorInSessionPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showAddMonitorInSessionPanel");
        }
        this.setShowAddMonitorInSessionPanel(true);
        this.setShowEditMonitorInSessionPanel(false);
        this.setShowMonitorInSessionListPanel(true);
        this.setShowViewMonitorInSessionPanel(false);
    }

    @Override
    public void hideAddMonitorInSessionPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("hideAddMonitorInSessionPanel");
        }
        this.setShowAddMonitorInSessionPanel(false);
        this.setShowEditMonitorInSessionPanel(false);
        this.setShowMonitorInSessionListPanel(true);
        this.setShowViewMonitorInSessionPanel(false);
    }

    @Override
    public void showViewMonitorInSessionPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showViewMonitorInSessionPanel");
        }
        this.setShowAddMonitorInSessionPanel(false);
        this.setShowEditMonitorInSessionPanel(false);
        this.setShowMonitorInSessionListPanel(false);
        this.setShowViewMonitorInSessionPanel(true);
    }

    @Override
    public void showAddTestsPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showAddTestsPanel");
        }
        this.setShowAddTestsPanel(true);
        resetSearchValues();
    }

    private void updateMonitorInSessionListStatus() {
        List<MonitorInSession> monitorInSessionList = MonitorInSession
                .getAllActivatedMonitorsForATestingSession(TestingSession.getSelectedTestingSession());
        if (monitorInSessionList != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            Role monitorRole = Role.getMONITOR_ROLE();
            for (MonitorInSession monitorInSession : monitorInSessionList) {
                // if the user is no more monitor, the related monitorInSession must be deactivated
                if (!monitorInSession.getUser().getRoles().contains(monitorRole)) {
                    monitorInSession.setActivated(false);
                    entityManager.merge(monitorInSession);
                }
            }
            entityManager.flush();
        }
    }

    @Override
    public void addMonitorsToActivatedTestingSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addMonitorsToActivatedTestingSession");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        for (User user : selectedMonitors) {
            MonitorInSession monitorInSession;
            List<MonitorInSession> disactivatedMonitorInSessionList = MonitorInSession
                    .getDisactivatedMonitorInSessionForATestingSessionByUser(
                            TestingSession.getSelectedTestingSession(), user);
            if ((disactivatedMonitorInSessionList != null) && (disactivatedMonitorInSessionList.size() == 1)) {
                monitorInSession = disactivatedMonitorInSessionList.get(0);
                monitorInSession.setActivated(true);
            } else {
                monitorInSession = new MonitorInSession(user, true, TestingSession.getSelectedTestingSession());
            }
            entityManager.merge(monitorInSession);
        }
        entityManager.flush();
        selectedMonitors = new ArrayList<User>();
        hideAddMonitorInSessionPanel();
    }

    @Override
    public void editMonitorInSession(MonitorInSession inMonitorInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editMonitorInSession");
        }
        this.selectedMonitorInSession = inMonitorInSession;
        editSelectedMonitorInSession();
    }

    @Override
    public void editSelectedMonitorInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editSelectedMonitorInSession");
        }
        showEditMonitorInSessionPanel();
        selectedCriterion = null;
        selectedDomain = null;
        selectedActor = null;
        selectedIntegrationProfile = null;
        setShowAddTestsPanel((selectedMonitorInSession.getTestList() == null)
                || (selectedMonitorInSession.getTestList().size() == 0));
        selectedTests = new ArrayList<Test>();
        availableTests = new ArrayList<Test>();
    }

    @Override
    public void deleteMonitorInSession(MonitorInSession inMonitorInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteMonitorInSession");
        }

        this.selectedMonitorInSession = inMonitorInSession;
    }

    @Override
    public void deleteSelectedMonitorInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedMonitorInSession");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        selectedMonitorInSession.setActivated(false);
        selectedMonitorInSession = entityManager.merge(selectedMonitorInSession);
        hideEditMonitorInSessionPanel();
    }

    private List<Test> getAvailableTestsForSelectedCriteria() {
        List<Test> result = null;
        List<TestType> testTypes = null;
        if (!selectedMonitorInSession.getTestingSession().isTestingInteroperability()) {
            testTypes = TestType.getTestTypesWithoutMESAandITB();
        } else {
            testTypes = TestType.getTestTypesWithoutMESA();
        }
        if (selectedCriterion != null) {
            if (selectedCriterion.equals(SEARCH_BY_DOMAIN_LABEL_TO_DISPLAY) && (selectedDomain != null)) {
                result = TestRoles.getTestFiltered(testTypes, TestStatus.getSTATUS_READY(),
                        null, null, null, selectedDomain, null, null, null, true, null);

            } else {
                if (selectedCriterion.equals(SEARCH_BY_PROFILE_LABEL_TO_DISPLAY)
                        && (selectedIntegrationProfile != null)) {
                    result = TestRoles.getTestFiltered(testTypes,
                            TestStatus.getSTATUS_READY(), null, null, null, null, selectedIntegrationProfile, null,
                            null, true, null);
                } else {
                    if (selectedCriterion.equals(SEARCH_BY_ACTOR_LABEL_TO_DISPLAY) && (selectedActor != null)) {
                        result = TestRoles.getTestFiltered(testTypes,
                                TestStatus.getSTATUS_READY(), null, null, null, null, null, selectedActor, null, true,
                                null);
                    } else {
                        availableTests = new ArrayList<Test>();
                    }
                }

            }
        }
        if (result != null) {
            Collections.sort(result);
        }
        return result;
    }

    private List<Test> getTestsForSelectedCriteriaForSelectedMonitorInSession() {
        List<Test> result = null;
        List<TestType> testTypes = selectedMonitorInSession.getTestingSession().getTestTypes();
        if (selectedCriterion.equals(SEARCH_BY_DOMAIN_LABEL_TO_DISPLAY) && (selectedDomain != null)) {
            result = MonitorInSession.getTestsByMonitorByDomainByIPByActor(selectedMonitorInSession, testTypes,
                    selectedDomain, null, null, null, true);
        } else {
            if (selectedCriterion.equals(SEARCH_BY_PROFILE_LABEL_TO_DISPLAY) && (selectedIntegrationProfile != null)) {
                result = MonitorInSession.getTestsByMonitorByDomainByIPByActor(selectedMonitorInSession, testTypes,
                        null, selectedIntegrationProfile, null, null, true);
            } else {
                if (selectedCriterion.equals(SEARCH_BY_ACTOR_LABEL_TO_DISPLAY) && (selectedActor != null)) {
                    result = MonitorInSession.getTestsByMonitorByDomainByIPByActor(selectedMonitorInSession, testTypes,
                            null, null, selectedActor, null, true);
                } else {
                    result = new ArrayList<Test>();
                }
            }
        }
        if (result != null) {
            Collections.sort(result);
        }
        return result;
    }

    @Override
    public void findTestsForSelectedCriteria() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findTestsForSelectedCriteria");
        }
        availableTests = getAvailableTestsForSelectedCriteria();
        selectedTests = getTestsForSelectedCriteriaForSelectedMonitorInSession();
        List<Test> affectedTests = getSelectedMonitorInSession().getTestList();
        if (availableTests != null) {
            availableTests.removeAll(affectedTests);
        }
        if (selectedTests == null) {
            selectedTests = new ArrayList<Test>();
        }
        if (availableTests == null) {
            availableTests = new ArrayList<Test>();
        }
    }

    @Override
    public List<Actor> getActorsForIntegrationProfile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorsForIntegrationProfile");
        }
        if (selectedIntegrationProfile != null) {
            return ActorIntegrationProfileOption
                    .getActorsFromAIPOFiltered(null, selectedIntegrationProfile, null, null);

        }
        return new ArrayList<Actor>();
    }

    @Override
    public void addSelectedTestsToMonitor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addSelectedTestsToMonitor");
        }
        if (selectedMonitorInSession != null) {
            if (selectedTests != null) {
                EntityManager entityManager = EntityManagerService.provideEntityManager();
                List<Test> testListToRemove = getTestsForSelectedCriteriaForSelectedMonitorInSession();
                List<Test> monitorTestList = selectedMonitorInSession.getTestList();
                if (monitorTestList != null) {
                    if (testListToRemove != null) {
                        monitorTestList.removeAll(testListToRemove);
                    }

                } else {
                    monitorTestList = new ArrayList<Test>();
                }
                monitorTestList.addAll(selectedTests);
                selectedMonitorInSession.setTestList(monitorTestList);
                selectedMonitorInSession = entityManager.merge(selectedMonitorInSession);
                entityManager.flush();
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "Selected tests are added to current monitor.");
            }
        }
    }

    @Override
    public void viewMonitorInSession(MonitorInSession inMonitorInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewMonitorInSession");
        }
        setSelectedMonitorInSession(inMonitorInSession);
        showViewMonitorInSessionPanel();
    }

    @Override
    public void removeTestFromMonitorList(Test inTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeTestFromMonitorList");
        }
        if ((selectedMonitorInSession != null) && (inTest != null)) {
            List<Test> testList = selectedMonitorInSession.getTestList();
            if (testList != null) {
                EntityManager entityManager = EntityManagerService.provideEntityManager();
                testList.remove(inTest);
                selectedMonitorInSession.setTestList(testList);
                selectedMonitorInSession = entityManager.merge(selectedMonitorInSession);
            }
        }
    }

    @Override
    public void downloadSelectedTestsAsPdf() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("downloadSelectedTestsAsPdf");
        }

        try {
            if (selectedMonitorInSession != null) {
                User monitorUser = selectedMonitorInSession.getUser();
                String monitorName = monitorUser.getFirstname() + " " + monitorUser.getLastname();
                String monitorLogin = monitorUser.getUsername();
                String institution = monitorUser.getInstitution().getKeyword();

                if (selectedMonitorInSession.getTestList().size() != 0) {
                    String selectedTestList = "";
                    List<Integer> selectedTestsId = new ArrayList<Integer>();
                    for (Test test : selectedMonitorInSession.getTestList()) {
                        selectedTestsId.add(test.getId());
                        if (selectedTestList.length() != 0) {
                            selectedTestList = selectedTestList + "," + Integer.toString(test.getId());
                        } else {
                            selectedTestList = Integer.toString(test.getId());
                        }
                    }

                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("selectedIP", "");
                    params.put("selectedActor", "");
                    params.put("selectedInstitution", institution);
                    params.put("selectedObjectName", monitorName);
                    params.put("selectedObjectKeyword", monitorLogin);
                    params.put("testList", selectedTestList);
                    // get the application url to use it as parameter for the report
                    String applicationurl = ApplicationPreferenceManager.instance().getApplicationUrl();
                    params.put("applicationurl", applicationurl);

                    // get the testDescription Language to use it as parameter for the report
                    List<Integer> res = new ArrayList<>();
                    EntityManager em = EntityManagerService.provideEntityManager();
                    for (Integer testId : selectedTestsId) {
                        Test test = em.find(Test.class, testId);
                        List<TestDescription> descriptionList = test.getTestDescription();
                        for (TestDescription testDescription : descriptionList) {
                            res.add(testDescription.getGazelleLanguage().getId());
                        }
                        if (!res.isEmpty()) {
                            //TODO GZL-4659
                            params.put("testDescriptionLanguageId", res.get(0));
                        }
                    }
                    ReportExporterManager.exportToPDF("gazelleMultipleTestReport", monitorLogin.replace(" ", "_")
                            + ".pdf", params);

                } else {
                    LOG.error("no test selected");
                }
            } else {
                LOG.error("no monitor in session selected");
            }
        } catch (JRException e) {
            LOG.error(e.getMessage(), e);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, e.getMessage());
        }

    }

    @Override
    public void downloadSelectedTestsAsPdf(MonitorInSession inMonitorInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("downloadSelectedTestsAsPdf");
        }
        setSelectedMonitorInSession(inMonitorInSession);
        downloadSelectedTestsAsPdf();
    }

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    @Override
    public boolean isConnectedUserMonitorForSelectedSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isConnectedUserMonitorForSelectedSession");
        }
        return MonitorInSession.isConnectedUserMonitorForSelectedSession();
    }

    @Override
    public void modifyQuery(HQLQueryBuilder<MonitorInSession> hqlQueryBuilder, Map<String, Object> map) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyQuery");
        }
        MonitorInSessionQuery query = new MonitorInSessionQuery();
        hqlQueryBuilder.addRestriction(query.testingSession().eqRestriction(TestingSession.getSelectedTestingSession()));
        hqlQueryBuilder.addRestriction(query.isActivated().eqRestriction(true));
    }


    public List<Institution> getInstitutionListForAllMonitorsInSession() {
        List<User> monitors = User.getUsersFiltered(null, Role.getMONITOR_ROLE());
        List<MonitorInSession> monitorsInSessionListUsed = getFoundMonitorInSessionList().getAllItems(FacesContext.getCurrentInstance());

        List<Institution> res = new ArrayList<Institution>();

        for (MonitorInSession monitorUsed : monitorsInSessionListUsed) {
            monitors.remove(monitorUsed.getUser());
        }

        for (User user : monitors) {
            res.add(user.getInstitution());
        }
        res = removeDuplicateInstitution(res);

        Collections.sort(res);
        return res;
    }


    public List<Institution> removeDuplicateInstitution(List<Institution> l) {
        // add elements to al, including duplicates
        Set<Institution> hs = new HashSet<Institution>();
        hs.addAll(l);
        l.clear();
        l.addAll(hs);
        return l;
    }
}
