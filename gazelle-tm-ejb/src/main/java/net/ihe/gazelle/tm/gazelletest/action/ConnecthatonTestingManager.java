package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.log.ExceptionLogging;
import net.ihe.gazelle.common.report.ReportExporterManager;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.*;
import net.ihe.gazelle.tm.gazelletest.bean.*;
import net.ihe.gazelle.tm.gazelletest.model.definition.*;
import net.ihe.gazelle.tm.gazelletest.model.instance.*;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.*;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.Role;
import net.ihe.gazelle.users.model.User;
import net.sf.jasperreports.engine.JRException;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.security.Identity;
import org.richfaces.component.UIPanelMenu;
import org.richfaces.component.UIPanelMenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;


@Name("connecthatonTestingManager")
@Scope(ScopeType.SESSION)
@Synchronized(timeout = 10000)
@GenerateInterface("ConnecthatonTestingManagerLocal")
public class ConnecthatonTestingManager implements ConnecthatonTestingManagerLocal, Serializable {

    public final static String BACK_MESSAGE = "back to test List";
    public final static String BACK_PAGE = "/testing/test/cat.xhtml";//DONE
    private static final long serialVersionUID = 3845076647269285905L;
    private static final Logger LOG = LoggerFactory.getLogger(ConnecthatonTestingManager.class);
    private final String sortByTestsStatus = "sortByTestsStatus";
    private final String sortBySystem = "sortBySystem";
    private final String filterCriticalTestInstancesWithoutMonitor = "criticalTestInstancesWithoutMonitor";
    private final String filterToBeVerifiedTestInstancesWithoutMonitor = "toBeVerifiedTestInstancesWithoutMonitor";
    private final String filterTestsWithoutMonitor = "testsWithoutMonitor";
    protected Test selectedTest;
    protected Institution selectedInstitution;
    protected SystemInSession selectedSystemInSession;
    protected Actor selectedActor;
    protected IntegrationProfile selectedIntegrationProfile;
    protected MonitorInSession selectedMonitor;
    /**
     * Testing Session choosen by a user
     */
    private TestingSession testingSessionChoosen;
    private TestingSession activatedTestingSession;
    private List<TestRoles> testRolesList;
    private TestPeerType selectedTestPeerType;
    private Integer selectedTestPeerTypeId;
    private SystemInSessionStatus selectedSystemInSessionStatus;
    private boolean showCriteriaSelectionPanel = true;
    private boolean showTestListPanel = false;
    private Boolean isConnectedUserAllowedToStartTest;
    private List<MetaTestRolesParticipantsResult> foundMetaTestRolesParticipantsResult;
    private List<TestRolesParticipantsResult> foundTestRolesParticipantsResult;
    private List<MetaTestRolesParticipantsResult> metaTestRolesParticipantsResult;
    private List<TestRolesParticipantsResult> testRolesParticipantsResultListWithoutMetaTest;
    private List<TestRolesParticipantsResult> testRolesParticipantsResultList;
    private List<MetaTest> metaTestListForSelectedSystemInSession;
    private List<TestInstance> toBeVerifiedTestInstanceList = new ArrayList<TestInstance>();
    private List<TestInstance> criticalTestInstanceList = new ArrayList<TestInstance>();
    private List<TestInstance> partiallyVerifiedTestInstanceList = new ArrayList<TestInstance>();
    private List<TestInstance> verifiedTestInstanceList = new ArrayList<TestInstance>();
    private List<TestInstance> failedTestInstanceList = new ArrayList<TestInstance>();
    private List<TestInstance> abortedTestInstanceList = new ArrayList<TestInstance>();
    private List<TestInstance> runningTestInstanceList = new ArrayList<TestInstance>();
    private List<TestInstance> pausedTestInstanceList = new ArrayList<TestInstance>();
    private List<TestInstance> criticalTestInstancesWithoutMonitorList = new ArrayList<TestInstance>();
    private List<TestInstance> toBeVerifiedTestInstancesWithoutMonitorList = new ArrayList<TestInstance>();
    private List<TestInstance> testsWithoutMonitorList = new ArrayList<TestInstance>();
    // all / passed / failed / to be verified / verified / critical
    private String filterSortingValue;
    private Boolean filterPartiallyVerified;
    private Boolean filterFailedTests;
    private Boolean filterToBeVerifiedTests;
    private Boolean filterVerifiedTests;
    private Boolean filterCriticalTests;
    private Boolean filterAbortedTests;
    private Boolean filterRunningTests;
    private Boolean filterPausedTests;
    // critical test instances / to be verified test instances / tests : WITHOUT
    // MONITOR
    private String filterTestsByTestsWithoutMonitor = "testsWithoutMonitor";

    @Override
    public String getBACK_MESSAGE() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getBACK_MESSAGE");
        }
        return BACK_MESSAGE;
    }

    @Override
    public String getBACK_PAGE() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getBACK_PAGE");
        }
        return BACK_PAGE;
    }

    @Override
    public List<MetaTestRolesParticipantsResult> getFoundMetaTestRolesParticipantsResult() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFoundMetaTestRolesParticipantsResult");
        }
        return foundMetaTestRolesParticipantsResult;
    }

    @Override
    public void setFoundMetaTestRolesParticipantsResult(
            List<MetaTestRolesParticipantsResult> foundMetaTestRolesParticipantsResult) {
        this.foundMetaTestRolesParticipantsResult = foundMetaTestRolesParticipantsResult;
    }

    @Override
    public List<TestRolesParticipantsResult> getFoundTestRolesParticipantsResult() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFoundTestRolesParticipantsResult");
        }
        return foundTestRolesParticipantsResult;
    }

    @Override
    public void setFoundTestRolesParticipantsResult(List<TestRolesParticipantsResult> foundTestRolesParticipantsResult) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFoundTestRolesParticipantsResult");
        }
        this.foundTestRolesParticipantsResult = foundTestRolesParticipantsResult;
    }

    @Override
    public TestingSession getTestingSessionChoosen() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestingSessionChoosen");
        }
        testingSessionChoosen = (TestingSession) Component.getInstance("testingSessionChoosen");
        return testingSessionChoosen;
    }

    @Override
    public void setTestingSessionChoosen(TestingSession testingSessionChoosen) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestingSessionChoosen");
        }

        Contexts.getSessionContext().set("testingSessionChoosen", testingSessionChoosen);
        this.testingSessionChoosen = testingSessionChoosen;

    }

    @Override
    public boolean isShowCriteriaSelectionPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isShowCriteriaSelectionPanel");
        }
        return showCriteriaSelectionPanel;
    }

    @Override
    public void setShowCriteriaSelectionPanel(boolean showCriteriaSelectionPanel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setShowCriteriaSelectionPanel");
        }
        this.showCriteriaSelectionPanel = showCriteriaSelectionPanel;
    }

    @Override
    public boolean isShowTestListPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isShowTestListPanel");
        }
        return showTestListPanel;
    }

    @Override
    public void setShowTestListPanel(boolean showTestListPanel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setShowTestListPanel");
        }
        this.showTestListPanel = showTestListPanel;
    }

    @Override
    public List<TestRoles> getTestRolesList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestRolesList");
        }
        return testRolesList;
    }

    @Override
    public void setTestRolesList(List<TestRoles> testRolesList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestRolesList");
        }
        this.testRolesList = testRolesList;
    }

    @Override
    public void setTestingSession(TestingSession activatedTestingSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestingSession");
        }
        this.activatedTestingSession = activatedTestingSession;
    }

    @Override
    public TestPeerType getSelectedTestPeerType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestPeerType");
        }
        return selectedTestPeerType;
    }

    @Override
    public void setSelectedTestPeerType(TestPeerType selectedTestPeerType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestPeerType");
        }
        this.selectedTestPeerType = selectedTestPeerType;
    }

    @Override
    public SystemInSessionStatus getSelectedSystemInSessionStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedSystemInSessionStatus");
        }
        return selectedSystemInSessionStatus;
    }

    @Override
    public void setSelectedSystemInSessionStatus(SystemInSessionStatus selectedSystemInSessionStatus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedSystemInSessionStatus");
        }
        this.selectedSystemInSessionStatus = selectedSystemInSessionStatus;
    }

    @Override
    public Boolean getFilterPartiallyVerified() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilterPartiallyVerified");
        }
        return filterPartiallyVerified;
    }

    @Override
    public void setFilterPartiallyVerified(Boolean filterPartiallyVerified) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFilterPartiallyVerified");
        }
        this.filterPartiallyVerified = filterPartiallyVerified;
    }

    @Override
    public Boolean getFilterFailedTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilterFailedTests");
        }
        return filterFailedTests;
    }

    @Override
    public void setFilterFailedTests(Boolean filterFailedTests) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFilterFailedTests");
        }
        this.filterFailedTests = filterFailedTests;
    }

    @Override
    public Boolean getFilterToBeVerifiedTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilterToBeVerifiedTests");
        }
        return filterToBeVerifiedTests;
    }

    @Override
    public void setFilterToBeVerifiedTests(Boolean filterToBeVerifiedTests) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFilterToBeVerifiedTests");
        }
        this.filterToBeVerifiedTests = filterToBeVerifiedTests;
    }

    @Override
    public Boolean getFilterVerifiedTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilterVerifiedTests");
        }
        return filterVerifiedTests;
    }

    @Override
    public void setFilterVerifiedTests(Boolean filterVerifiedTests) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFilterVerifiedTests");
        }
        this.filterVerifiedTests = filterVerifiedTests;
    }

    @Override
    public Boolean getFilterCriticalTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilterCriticalTests");
        }
        return filterCriticalTests;
    }

    @Override
    public void setFilterCriticalTests(Boolean filterCriticalTests) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFilterCriticalTests");
        }
        this.filterCriticalTests = filterCriticalTests;
    }

    @Override
    public Boolean getFilterAbortedTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilterAbortedTests");
        }
        return filterAbortedTests;
    }

    @Override
    public void setFilterAbortedTests(Boolean filterAbortedTests) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFilterAbortedTests");
        }
        this.filterAbortedTests = filterAbortedTests;
    }

    @Override
    public Boolean getFilterRunningTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilterRunningTests");
        }
        return filterRunningTests;
    }

    @Override
    public void setFilterRunningTests(Boolean filterRunningTests) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFilterRunningTests");
        }
        this.filterRunningTests = filterRunningTests;
    }

    @Override
    public Boolean getFilterPausedTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilterPausedTests");
        }
        return filterPausedTests;
    }

    @Override
    public void setFilterPausedTests(Boolean filterPausedTests) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFilterPausedTests");
        }
        this.filterPausedTests = filterPausedTests;
    }

    @Override
    public Test getSelectedTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTest");
        }
        return selectedTest;
    }

    @Override
    public void setSelectedTest(Test selectedTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTest");
        }
        this.selectedTest = selectedTest;
    }

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
    public SystemInSession getSelectedSystemInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedSystemInSession");
        }
        return selectedSystemInSession;
    }

    @Override
    public void setSelectedSystemInSession(SystemInSession selectedSystemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedSystemInSession");
        }
        this.selectedSystemInSession = selectedSystemInSession;
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
    public MonitorInSession getSelectedMonitor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedMonitor");
        }
        return selectedMonitor;
    }

    @Override
    public void setSelectedMonitor(MonitorInSession selectedMonitor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedMonitor");
        }
        this.selectedMonitor = selectedMonitor;
    }

    @Override
    public Integer getSelectedTestPeerTypeId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestPeerTypeId");
        }
        if (selectedTestPeerType != null) {
            selectedTestPeerTypeId = selectedTestPeerType.getId();
        } else {
            selectedTestPeerTypeId = -1;
        }
        return selectedTestPeerTypeId;
    }

    @Override
    public void setSelectedTestPeerTypeId(Integer selectedTestPeerTypeId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestPeerTypeId");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        if ((selectedTestPeerTypeId != null) && (selectedTestPeerTypeId != -1)) {
            selectedTestPeerType = entityManager.find(TestPeerType.class, selectedTestPeerTypeId);
        } else {
            selectedTestPeerType = null;
        }
        this.selectedTestPeerTypeId = selectedTestPeerTypeId;
    }

    @Override
    public void search() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("search");
        }
        if (selectedSystemInSession != null) {
            searchTestRoles();
            metaTestListForSelectedSystemInSession = getMetaTestForSelectedSystemInSession();
        }
    }

    @Override
    public void updateTestInstanceParticipantsList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateTestInstanceParticipantsList");
        }
        searchTestRolesParicipants();
        searchMetaTestRolesParticipantsResult();
        searchTestRolesParticipantsResultByTestPeerType();
    }

    @Override
    public void updateTestInstanceParticipantsList2() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateTestInstanceParticipantsList2");
        }
        searchTestRolesParicipants2();
        searchMetaTestRolesParticipantsResult();
        searchTestRolesParticipantsResultByTestPeerType();
    }

    private void computeTestPartners() {
        if (foundTestRolesParticipantsResult != null) {
            for (TestRolesParticipantsResult trpr : foundTestRolesParticipantsResult) {
                computeTestPartnersForTRPR(trpr);
            }
        }

        if (foundMetaTestRolesParticipantsResult != null) {
            for (MetaTestRolesParticipantsResult metaTestRolesParticipantsResultElement : foundMetaTestRolesParticipantsResult) {
                List<TestRolesParticipantsResult> testRolesParticipantsList = metaTestRolesParticipantsResultElement
                        .getTestRolesParticipantsResultList();
                if (testRolesParticipantsList != null) {
                    for (TestRolesParticipantsResult trpr : testRolesParticipantsList) {
                        computeTestPartnersForTRPR(trpr);
                    }
                }
            }
        }
    }

    private void computeTestPartnersForTRPR(TestRolesParticipantsResult testRolesParticipantsResult) {
        AIPOSystemPartners partners = testRolesParticipantsResult.getPartners();
        SystemActorProfiles systemActorProfile = testRolesParticipantsResult.getSystemActorProfiles();
        TestRoles testRoles = testRolesParticipantsResult.getTestRoles();
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        PartnersStatisticsBuilder.get(TestingSession.getSelectedTestingSession()).addPartners(entityManager, partners,
                systemActorProfile, Collections.singletonList(testRoles));
    }

    @Override
    public TestingSession getActivatedTestingSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActivatedTestingSession");
        }
        activatedTestingSession = TestingSession.getSelectedTestingSession();
        return activatedTestingSession;
    }

    @Override
    public void initCriteriaList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initCriteriaList");
        }

        if (Identity.instance().isLoggedIn()) {
            filterPartiallyVerified = true;
            filterFailedTests = true;
            filterToBeVerifiedTests = true;
            filterVerifiedTests = true;
            filterCriticalTests = true;
            filterAbortedTests = true;
            filterRunningTests = true;
            filterPausedTests = true;
            filterSortingValue = getSortByTestsStatus();

            showTestListPanel();
            if (selectedInstitution == null) {
                if (!Role.isLoggedUserAdmin()) {
                    User loggedUser = User.loggedInUser();
                    selectedInstitution = loggedUser.getInstitution();
                    List<SystemInSession> systemInSessionList = SystemInSession
                            .getSystemInSessionByUserForActivatedTestingSession(loggedUser);
                    if (systemInSessionList != null) {
                        getSystemListForSelectedInstitution();
                    }
                } else {
                    selectedSystemInSession = null;
                    selectedIntegrationProfile = null;
                    selectedActor = null;
                    testRolesList = null;
                    showCriteriaSelectionPanel();
                }
            }
        } else {
            Redirect redirect = Redirect.instance();
            redirect.setViewId("/home.xhtml");
            redirect.execute();
        }
    }

    @Override
    public void showCriteriaSelectionPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showCriteriaSelectionPanel");
        }
        setShowCriteriaSelectionPanel(true);
    }

    @Override
    public void showTestListPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showTestListPanel");
        }
        setShowCriteriaSelectionPanel(false);
        setShowTestListPanel(true);
    }

    @Override
    public void resetActorValue() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetActorValue");
        }
        selectedActor = null;
    }

    @Override
    public void resetIntegrationProfileValue() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetIntegrationProfileValue");
        }
        selectedIntegrationProfile = null;
        resetActorValue();
    }

    @Override
    public void resetSystemInSessionValue() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetSystemInSessionValue");
        }
        selectedSystemInSession = null;
        resetIntegrationProfileValue();
    }

    @Override
    public String initializeTestInstanceParticipantsCreation(SystemInSession inSystemInSession, TestRoles inTestRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeTestInstanceParticipantsCreation");
        }
        Contexts.getSessionContext().set("startTestInstanceSISid", inSystemInSession.getId());
        Contexts.getSessionContext().set("startTestInstanceTRid", inTestRoles.getId());

        return "/testing/test/test/StartTestInstance.seam";//DONE
    }

    @Override
    public void displayTestInstancesByTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayTestInstancesByTest");
        }
        showTestListPanel();
        search();
        filterPartiallyVerified = true;
        filterFailedTests = true;
        filterToBeVerifiedTests = true;
        filterVerifiedTests = true;
        filterCriticalTests = true;
        filterAbortedTests = true;
        filterRunningTests = true;
        filterPausedTests = true;
    }

    @Override
    public List<SystemInSession> getListOfSystemToDisplay() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfSystemToDisplay");
        }
        if (selectedInstitution != null) {
            if (selectedSystemInSession != null) {
                List<SystemInSession> list = new ArrayList<SystemInSession>();
                list.add(selectedSystemInSession);
                return list;
            } else {
                return getSystemListForSelectedInstitution();
            }
        }
        return null;
    }

    @Override
    public List<Institution> getInstitutionListForActivatedTestingSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstitutionListForActivatedTestingSession");
        }
        List<Institution> list = TestingSession.getListOfInstitutionsParticipatingInSession(TestingSession
                .getSelectedTestingSession());
        Collections.sort(list);
        return list;
    }

    @Override
    public List<SystemInSession> getSystemListByInstitution(Institution inInstitution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemListByInstitution");
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        activatedTestingSession = getActivatedTestingSession();
        if ((inInstitution != null) && (activatedTestingSession != null)) {
            List<SystemInSession> systemInSessionList = SystemInSession.getSystemsInSessionForCompanyForSession(em,
                    inInstitution, activatedTestingSession);
            Collections.sort(systemInSessionList);
            if (systemInSessionList.size() > 0) {
                return systemInSessionList;
            }
        }
        return null;
    }

    @Override
    public List<SystemInSession> getSystemListForSelectedInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemListForSelectedInstitution");
        }
        return getSystemListByInstitution(selectedInstitution);
    }

    @Override
    public List<IntegrationProfile> getIntegrationProfilesForSelectedSystem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntegrationProfilesForSelectedSystem");
        }
        return getIntegrationProfilesForASystemInSession(selectedSystemInSession);
    }

    @Override
    public List<IntegrationProfile> getIntegrationProfilesForASystemInSession(SystemInSession inSystemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntegrationProfilesForASystemInSession");
        }
        if (inSystemInSession != null) {
            List<IntegrationProfile> list = SystemActorProfiles
                    .getListOfIntegrationProfilesImplementedByASystem(inSystemInSession.getSystem());
            if (list != null) {
                HashSet<IntegrationProfile> setOfIntegrationProfile = new HashSet<IntegrationProfile>();
                for (IntegrationProfile ip : list) {
                    setOfIntegrationProfile.add(ip);
                }
                list = new ArrayList<IntegrationProfile>(setOfIntegrationProfile);
                Collections.sort(list);
                return list;
            }
        }
        return null;
    }

    @Override
    public List<Actor> getActorsForSelectedIPForSelectedSystem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorsForSelectedIPForSelectedSystem");
        }
        return getActorsForAnIPForASystemInSession(selectedSystemInSession, selectedIntegrationProfile);
    }

    @Override
    public List<Actor> getActorsForAnIPForASystemInSession(SystemInSession inSystemInSession,
                                                           IntegrationProfile inIntegrationProfile) {
        if (inSystemInSession != null) {
            List<Actor> list;
            if (inIntegrationProfile == null) {
                list = SystemActorProfiles.getListOfActorsImplementedByASystem(inSystemInSession.getSystem());
            } else {
                list = SystemActorProfiles.getListOfActorsImplementedByASystemForAnIntegrationProfile(
                        inSystemInSession.getSystem(), inIntegrationProfile);
            }
            if (list != null) {
                HashSet<Actor> setOfActor = new HashSet<Actor>();
                for (Actor actor : list) {
                    setOfActor.add(actor);
                }
                list = new ArrayList<Actor>(setOfActor);
                Collections.sort(list);
                return list;
            }
        }
        return null;
    }

    @Override
    public String getSortByTestsStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSortByTestsStatus");
        }
        return sortByTestsStatus;
    }

    @Override
    public String getSortBySystem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSortBySystem");
        }
        return sortBySystem;
    }

    @Override
    public String getFilterSortingValue() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilterSortingValue");
        }
        return filterSortingValue;
    }

    @Override
    public void setFilterSortingValue(String filterSortingValue) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFilterSortingValue");
        }
        this.filterSortingValue = filterSortingValue;
    }

    @Override
    public List<MetaTest> getMetaTestForSelectedSystemInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMetaTestForSelectedSystemInSession");
        }
        return getMetaTestBySystemInSession(selectedSystemInSession);
    }

    @Override
    public List<TestRoles> getTestRolesByMetaTesForSelectedSystemInSession(MetaTest inMetaTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestRolesByMetaTesForSelectedSystemInSession");
        }
        return getTestRolesBySystemInSessionByMetaTest(selectedSystemInSession, inMetaTest);
    }

    @Override
    public List<TestRoles> getTestRolesBySystemInSessionByMetaTest(SystemInSession inSystemInSession,
                                                                   MetaTest inMetaTest) {
        if ((inSystemInSession != null) && (inMetaTest != null)) {
            TestingSession testingSession = inSystemInSession.getTestingSession();
            List<TestRoles> testRolesList2 = MetaTest.getTestRolesFiltered(inMetaTest, inSystemInSession.getSystem(),
                    null, selectedIntegrationProfile, null, selectedActor, null, testingSession.getTestTypes(),
                    selectedTestPeerType, null, TestStatus.getSTATUS_READY(), true);
            if (testRolesList2 != null) {
                Collections.sort(testRolesList2);
            }
            return testRolesList2;
        }
        return null;
    }

    @Override
    public List<MetaTest> getMetaTestBySystemInSession(SystemInSession inSystemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMetaTestBySystemInSession");
        }
        if (inSystemInSession != null) {
            TestingSession testingSession = inSystemInSession.getTestingSession();
            List<MetaTest> metaTestList = MetaTest.getMetaTestFiltered(inSystemInSession.getSystem(), null,
                    selectedIntegrationProfile, null, selectedActor, null, testingSession.getTestTypes(),
                    selectedTestPeerType, null, TestStatus.getSTATUS_READY(), true);
            return metaTestList;
        }
        return null;
    }

    @Override
    public IntegrationProfileOption getIntegrationProfileOptionByRoleInTestBySystemInSession(
            SystemInSession selectedSystemInSession, RoleInTest inRoleInTest) {
        ActorIntegrationProfileOption actorIntegrationProfileOption = getActorIntegrationProfileOptionByRoleInTestBySystemInSession(
                selectedSystemInSession, inRoleInTest);
        if (actorIntegrationProfileOption != null) {
            return actorIntegrationProfileOption.getIntegrationProfileOption();
        }
        return null;
    }

    @Override
    public ActorIntegrationProfileOption getActorIntegrationProfileOptionByRoleInTestBySystemInSession(
            SystemInSession inSystemInSession, RoleInTest inRoleInTest) {
        if ((inSystemInSession != null) && (inRoleInTest != null)) {
            List<ActorIntegrationProfileOption> actorIntegrationProfileOptions = TestRoles
                    .getActorIntegrationProfileOptionByRoleInTestBySystemInSession(inSystemInSession, inRoleInTest);
            if (actorIntegrationProfileOptions == null) {
                actorIntegrationProfileOptions = TestRoles
                        .getActorIntegrationProfileOptionByRoleInTestBySystemInSessionForAllTestParticipants(
                                inSystemInSession, inRoleInTest);
            }
            if (actorIntegrationProfileOptions != null) {
                if (actorIntegrationProfileOptions.size() == 1) {
                    return actorIntegrationProfileOptions.get(0);
                }
                for (ActorIntegrationProfileOption actorIntegrationProfileOption : actorIntegrationProfileOptions) {
                    if (actorIntegrationProfileOption.getIntegrationProfileOption().equals(
                            IntegrationProfileOption.getNoneOption())) {
                        return actorIntegrationProfileOption;
                    }
                }
                return actorIntegrationProfileOptions.get(0);
            }
        }
        return null;
    }

    @Override
    public Actor getActorByTestRolesBySystemInSession(SystemInSession inSystemInSession, TestRoles currenTestRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorByTestRolesBySystemInSession");
        }
        ActorIntegrationProfileOption actorIntegrationProfileOption = getActorIntegrationProfileOptionByTestRolesBySystemInSession(
                inSystemInSession, currenTestRoles);
        if (actorIntegrationProfileOption != null) {
            return actorIntegrationProfileOption.getActorIntegrationProfile().getActor();
        }
        return null;
    }

    @Override
    public IntegrationProfile getIntegrationProfileByTestRolesBySystemInSession(SystemInSession inSystemInSession,
                                                                                TestRoles currenTestRoles) {
        ActorIntegrationProfileOption actorIntegrationProfileOption = getActorIntegrationProfileOptionByTestRolesBySystemInSession(
                inSystemInSession, currenTestRoles);
        if (actorIntegrationProfileOption != null) {
            return actorIntegrationProfileOption.getActorIntegrationProfile().getIntegrationProfile();
        }
        return null;
    }

    @Override
    public IntegrationProfileOption getIntegrationProfileOptionByTestRolesBySystemInSession(
            SystemInSession inSystemInSession, TestRoles currenTestRoles) {
        ActorIntegrationProfileOption actorIntegrationProfileOption = getActorIntegrationProfileOptionByTestRolesBySystemInSession(
                inSystemInSession, currenTestRoles);
        if (actorIntegrationProfileOption != null) {
            return actorIntegrationProfileOption.getIntegrationProfileOption();
        }
        return null;
    }

    @Override
    public TestingType getTestingTypeByTestRolesBySystemInSession(SystemInSession inSystemInSession,
                                                                  TestRoles inTestRoles) {
        if ((inSystemInSession != null) && (inTestRoles != null)) {
            List<SystemActorProfiles> systemActorProfiles = TestRoles
                    .getSystemActorProfilesByTestRolesBySystemInSession(inSystemInSession, inTestRoles);
            if (systemActorProfiles != null) {
                if (systemActorProfiles.size() == 1) {
                    return systemActorProfiles.get(0).getTestingType();
                }
                for (SystemActorProfiles systemActorProfile : systemActorProfiles) {
                    if (systemActorProfile.getActorIntegrationProfileOption().getIntegrationProfileOption()
                            .equals(IntegrationProfileOption.getNoneOption())) {
                        return systemActorProfile.getTestingType();
                    }
                }
                return systemActorProfiles.get(0).getTestingType();
            }
        }
        return null;
    }

    @Override
    public ActorIntegrationProfileOption getActorIntegrationProfileOptionByTestRolesBySystemInSession(
            SystemInSession inSystemInSession, TestRoles inTestRoles) {
        if ((inSystemInSession != null) && (inTestRoles != null)) {
            List<ActorIntegrationProfileOption> actorIntegrationProfileOptions = TestRoles
                    .getActorIntegrationProfileOptionByTestRolesBySystemInSession(inSystemInSession, inTestRoles);
            if (actorIntegrationProfileOptions == null) {
                actorIntegrationProfileOptions = TestRoles
                        .getActorIntegrationProfileOptionByTestRolesBySystemInSessionForAllTestParticipants(
                                inSystemInSession, inTestRoles);
            }
            if (actorIntegrationProfileOptions != null) {
                if (actorIntegrationProfileOptions.size() == 1) {
                    return actorIntegrationProfileOptions.get(0);
                }
                for (ActorIntegrationProfileOption actorIntegrationProfileOption : actorIntegrationProfileOptions) {
                    if (actorIntegrationProfileOption.getIntegrationProfileOption().equals(
                            IntegrationProfileOption.getNoneOption())) {
                        return actorIntegrationProfileOption;
                    }
                }
                return actorIntegrationProfileOptions.get(0);
            }
        }
        return null;
    }

    // *******************************************************//
    // ************ TestInstanceParticipants Management*******//
    // *******************************************************//

    @Override
    public List<TestInstanceParticipants> getTestInstanceParticipantsByStatus(Status testInstanceStatus,
                                                                              Test currentTest) {
        return getTestInstanceParticipantsByStatusByIPByActorBySIS(selectedSystemInSession, testInstanceStatus,
                currentTest, selectedIntegrationProfile, selectedActor);
    }

    @Override
    public List<TestInstanceParticipants> getTestInstanceParticipantsByStatusBySystemInSessionByTestRoles(
            SystemInSession inSystemInSession, Status testInstanceStatus, TestRoles currentTestRoles) {
        ActorIntegrationProfileOption actorIntegrationProfileOption = getActorIntegrationProfileOptionByTestRolesBySystemInSession(
                inSystemInSession, currentTestRoles);
        if (actorIntegrationProfileOption != null) {
            return getTestInstanceParticipantsByStatusByIPByActorBySIS(inSystemInSession, testInstanceStatus,
                    currentTestRoles.getTest(), actorIntegrationProfileOption.getActorIntegrationProfile()
                            .getIntegrationProfile(), actorIntegrationProfileOption.getActorIntegrationProfile()
                            .getActor());
        }
        return getTestInstanceParticipantsByStatusByIPByActorBySIS(inSystemInSession, testInstanceStatus,
                currentTestRoles.getTest(), selectedIntegrationProfile, selectedActor);
    }

    @Override
    public List<TestInstanceParticipants> getTestInstanceParticipantsByStatusBySystemInSessionByTest(
            SystemInSession inSystemInSession, Status testInstanceStatus, Test currentTest) {
        return getTestInstanceParticipantsByStatusByIPByActorBySIS(inSystemInSession, testInstanceStatus, currentTest,
                selectedIntegrationProfile, selectedActor);
    }

    @Override
    public List<TestInstanceParticipants> getTestInstanceParticipantsByStatusByIPByActorBySIS(
            SystemInSession inSystemInSession, Status testInstanceStatus, Test currentTest,
            IntegrationProfile inIntegrationProfile, Actor inActor) {
        if (inSystemInSession != null) {
            TestingSession testingSession = inSystemInSession.getTestingSession();
            List<TestInstanceParticipants> testInstanceParticipants = TestInstanceParticipants
                    .getTestInstancesParticipantsFiltered(activatedTestingSession, selectedInstitution,
                            inSystemInSession.getSystem(), currentTest, testingSession.getTestTypes(),
                            inIntegrationProfile, null, inActor, testInstanceStatus);
            if (testInstanceParticipants.size() > 0) {
                return testInstanceParticipants;
            }
        }
        return null;
    }

    @Override
    public void updateSystemInSessionStatus(SystemInSession inSystemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateSystemInSessionStatus");
        }
        if (inSystemInSession != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            entityManager.merge(inSystemInSession);
        }
    }

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    @Override
    public void deleteAllConnectathonTestInstanceForActivatedTestingSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteAllConnectathonTestInstanceForActivatedTestingSession");
        }
        deleteAllConnectathonTestInstanceByTestingSession(TestingSession.getSelectedTestingSession());
    }

    @Override
    public void deleteAllConnectathonTestInstanceByTestingSession(TestingSession inTestingSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteAllConnectathonTestInstanceByTestingSession");
        }
        if (inTestingSession != null) {
            try {
                TestInstance.deleteAllConnecthatonTestInstanceForATestingSession(inTestingSession);
                FacesMessages.instance().add(StatusMessage.Severity.INFO,
                        "Test Instances of the selected testing session were deleted successfully ");
            } catch (Exception e) {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                        "A problem occured when deleting test instance for the selected testing session");
                LOG.error("", e);
            }
        }
    }

    // NEW IMPLEMENTATION

    @Override
    public List<SystemInSession> getSystemsForSelectedInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemsForSelectedInstitution");
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        if (selectedInstitution != null) {
            List<SystemInSession> lsis = SystemInSession.getSystemsInSessionForCompanyForSession(em,
                    selectedInstitution);
            Collections.sort(lsis);
            return lsis;
        }
        return null;
    }

    @Override
    public void editFilterForSelectedSystemInSession(SystemInSession selectedSystemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editFilterForSelectedSystemInSession");
        }
        setSelectedSystemInSession(selectedSystemInSession);
        showCriteriaSelectionPanel();
    }

    @Override
    public List<TestPeerType> getPeerTypeListForSelectedSystem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPeerTypeListForSelectedSystem");
        }
        return TestPeerType.getPeerTypeList();
    }

    @Override
    public List<SelectItem> getPeerTypeListForSelectedSystemAsSelectItems() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPeerTypeListForSelectedSystemAsSelectItems");
        }
        List<TestPeerType> list = getPeerTypeListForSelectedSystem();
        List<SelectItem> result = new ArrayList<SelectItem>();
        String label = org.jboss.seam.core.ResourceBundle.instance().getString("gazelle.tests.testInstance.allOptions");
        result.add(new SelectItem(-1, label));
        for (TestPeerType testPeerType : list) {
            label = org.jboss.seam.core.ResourceBundle.instance().getString(testPeerType.getLabelToDisplay());
            result.add(new SelectItem(testPeerType.getId(), label));
        }
        return result;
    }

    @Override
    public UIPanelMenu getPanelMenu() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPanelMenu");
        }
        UIPanelMenu panelMenu = new UIPanelMenu();

        for (Institution institution : getInstitutionListForActivatedTestingSession()) {
            UIPanelMenuItem panelItem = new UIPanelMenuItem();
            panelItem.setLabel(institution.getKeyword());
            panelMenu.getChildren().add(panelItem);
        }

        return panelMenu;
    }

    @Override
    public void updateSystemInSessionStatusByTestingSession(TestingSession inTestingSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateSystemInSessionStatusByTestingSession");
        }
        updateSystemInSessionStatusByTestingSession(inTestingSession, selectedSystemInSessionStatus);
        selectedSystemInSessionStatus = null;
    }

    @Override
    public void updateSystemInSessionStatusByTestingSession(TestingSession inTestingSession,
                                                            SystemInSessionStatus inSystemInSessionStatus) {
        if ((inTestingSession != null) && (inSystemInSessionStatus != null)) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            Query query = entityManager
                    .createQuery("UPDATE SystemInSession sIs SET sIs.systemInSessionStatus = :inSystemInSessionStatus where sIs.testingSession = " +
                            ":inTestingSession");
            query.setParameter("inSystemInSessionStatus", inSystemInSessionStatus);
            query.setParameter("inTestingSession", inTestingSession);
            int result = query.executeUpdate();
            if (result > 0) {
                FacesMessages.instance().add(StatusMessage.Severity.INFO,
                        "The status of " + result
                                + " systems belonging to the selected testing session was changed to "
                                + inSystemInSessionStatus.getKeyword());
            }
        }
    }

    /**
     * Generate pdf report containing all selected Tests
     */
    @Override
    public void downloadSelectedTestsAsPdf() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("downloadSelectedTestsAsPdf");
        }
        List<Integer> selectedTests = new ArrayList<Integer>();
        if ((foundTestRolesParticipantsResult != null) && (foundTestRolesParticipantsResult.size() > 0)) {
            for (TestRolesParticipantsResult testRolesParticipantsResult : foundTestRolesParticipantsResult) {
                Integer id = testRolesParticipantsResult.getTestRoles().getTest().getId();
                if (!selectedTests.contains(id)) {
                    selectedTests.add(id);
                }
            }
        }

        if ((foundMetaTestRolesParticipantsResult != null) && (foundMetaTestRolesParticipantsResult.size() > 0)) {
            for (MetaTestRolesParticipantsResult metaTestRolesParticipantsResult : foundMetaTestRolesParticipantsResult) {
                for (TestRolesParticipantsResult testRolesParticipantsResult : metaTestRolesParticipantsResult
                        .getTestRolesParticipantsResultList()) {
                    Integer id = testRolesParticipantsResult.getTestRoles().getTest().getId();
                    if (!selectedTests.contains(id)) {
                        selectedTests.add(id);
                    }
                }
            }
        }
        try {
            if (selectedSystemInSession != null) {
                if (selectedTests != null) {
                    // create string containing all ids separated by coma
                    String testList = "";
                    for (Integer id : selectedTests) {
                        if (testList.length() != 0) {
                            testList = testList + "," + Integer.toString(id);
                        } else {
                            testList = Integer.toString(id);
                        }
                    }

                    System selectedSystem = selectedSystemInSession.getSystem();
                    String selectedSytemKeyword = selectedSystem.getKeyword();
                    String selectedSystemName = selectedSystem.getName();
                    String reportName = selectedSytemKeyword;
                    String ipkeyword = "";
                    String actorkeyword = "";
                    if (selectedIntegrationProfile != null) {
                        ipkeyword = selectedIntegrationProfile.getKeyword();
                        reportName = reportName + "_" + ipkeyword;
                    }
                    if (selectedActor != null) {
                        actorkeyword = selectedActor.getKeyword();
                        reportName = reportName + "_" + actorkeyword;
                    }
                    reportName = reportName + ".pdf";
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("selectedIP", ipkeyword);
                    params.put("selectedActor", actorkeyword);
                    params.put("selectedInstitution", selectedInstitution.getName());
                    params.put("selectedObjectKeyword", selectedSytemKeyword);
                    params.put("selectedObjectName", selectedSystemName);
                    params.put("testList", testList);
                    // get the application url to use it as parameter for the report
                    String applicationurl = ApplicationPreferenceManager.instance().getApplicationUrl();
                    params.put("applicationurl", applicationurl);

                    // get the testDescription Language to use it as parameter for the report
                    List<Integer> res = new ArrayList<>();
                    EntityManager em = EntityManagerService.provideEntityManager();
                    for (Integer testId : selectedTests) {
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

                    ReportExporterManager.exportToPDF("gazelleMultipleTestReport", reportName, params);
                }
            }

        } catch (JRException e) {
            ExceptionLogging.logException(e, LOG);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, e.getMessage());
        }
    }

    private void searchTestRoles() {
        if (selectedSystemInSession != null) {
            boolean isUserAdminOrMonitorForSession = Role.isLoggedUserAdmin()
                    || MonitorInSession.isConnectedUserMonitorForSelectedSession();
            if (isConnectedUserAllowedToStartTest == null) {
                User user = User.loggedInUser();
                if (user != null) {
                    if (selectedInstitution.equals(user.getInstitution())) {
                        isConnectedUserAllowedToStartTest = true;
                    } else {
                        isConnectedUserAllowedToStartTest = false;
                    }
                } else {
                    isConnectedUserAllowedToStartTest = false;
                }
            }
            EntityManager em = EntityManagerService.provideEntityManager();

            String queryString = "SELECT DISTINCT new net.ihe.gazelle.tm.gazelletest.bean.TestRolesForResult(tr as testRoles,tr.test.testPeerType " +
                    "as testPeerType,sap as systemActorProfiles) "
                    + "FROM TestRoles tr join tr.roleInTest.testParticipantsList participants, SystemInSession sIs, SystemActorProfiles sap "
                    + "WHERE sap.actorIntegrationProfileOption = participants.actorIntegrationProfileOption "
                    + "AND sIs.system=sap.system "
                    + "AND participants.tested='true' "
                    + "AND tr.test.testType in (:inTestTypes) " +
                    // "AND tr.testOption=:inTestOption " +
                    "AND tr.test.testStatus=:inTestStatus " + "AND sIs=:inSystemInSession ";
            if (selectedActor != null) {
                queryString += "AND sap.actorIntegrationProfileOption.actorIntegrationProfile.actor=:inActor ";
            }
            if (selectedIntegrationProfile != null) {
                queryString += "AND sap.actorIntegrationProfileOption.actorIntegrationProfile.integrationProfile=:inIntegrationProfile ";
            }
            Query query = em.createQuery(queryString);
            query.setParameter("inSystemInSession", selectedSystemInSession);
            query.setParameter("inTestTypes", selectedSystemInSession.getTestingSession().getTestTypes());

            query.setParameter("inTestStatus", TestStatus.getSTATUS_READY());
            if (selectedActor != null) {
                query.setParameter("inActor", selectedActor);
            }
            if (selectedIntegrationProfile != null) {
                query.setParameter("inIntegrationProfile", selectedIntegrationProfile);
            }
            Comparator<TestRolesParticipantsResult> comparator = new Comparator<TestRolesParticipantsResult>() {
                @Override
                public int compare(TestRolesParticipantsResult o1, TestRolesParticipantsResult o2) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("compare");
                    }
                    int result = 0;
                    ActorIntegrationProfile actorIntegrationProfile1 = o1.getSystemActorProfiles()
                            .getActorIntegrationProfileOption().getActorIntegrationProfile();
                    ActorIntegrationProfile actorIntegrationProfile2 = o2.getSystemActorProfiles()
                            .getActorIntegrationProfileOption().getActorIntegrationProfile();
                    if (selectedIntegrationProfile == null) {
                        result = actorIntegrationProfile1.getIntegrationProfile().getKeyword()
                                .compareToIgnoreCase(actorIntegrationProfile2.getIntegrationProfile().getKeyword());
                    }
                    if (result == 0) {
                        if (selectedActor == null) {
                            result = actorIntegrationProfile1.getActor().getKeyword()
                                    .compareToIgnoreCase(actorIntegrationProfile2.getActor().getKeyword());
                        }
                        if (result == 0) {
                            result = o1.getTestRoles().getTestOption().getKeyword()
                                    .compareToIgnoreCase(o2.getTestRoles().getTestOption().getKeyword());
                        }
                        if (result == 0) {
                            result = o1.getTestRoles().getTest().getKeyword()
                                    .compareToIgnoreCase(o2.getTestRoles().getTest().getKeyword());
                        }
                    }
                    return result;
                }
            };

            List<TestRolesForResult> foundtestRolesForResultList = query.getResultList();
            testRolesParticipantsResultList = new ArrayList<TestRolesParticipantsResult>();
            if ((foundtestRolesForResultList != null) && (foundtestRolesForResultList.size() > 0)) {
                Collections.sort(foundtestRolesForResultList);
                List<TestRolesForResult> testRolesForResultList = new ArrayList<TestRolesForResult>();
                testRolesForResultList.add(foundtestRolesForResultList.get(0));
                for (int i = 1; i < foundtestRolesForResultList.size(); i++) {
                    if (foundtestRolesForResultList.get(i - 1).equals(foundtestRolesForResultList.get(i))) {
                        testRolesForResultList.add(foundtestRolesForResultList.get(i));
                    } else {
                        SystemActorProfiles systemActorProfiles = testRolesForResultList.get(0)
                                .getSystemActorProfiles();
                        for (TestRolesForResult testRolesForResult : testRolesForResultList) {
                            if (testRolesForResult.getSystemActorProfiles().getActorIntegrationProfileOption()
                                    .getIntegrationProfileOption().equals(IntegrationProfileOption.getNoneOption())) {
                                systemActorProfiles = testRolesForResult.getSystemActorProfiles();
                            }
                        }
                        TestRolesParticipantsResult tmp = new TestRolesParticipantsResult(
                                selectedSystemInSession.getTestingSession(), testRolesForResultList.get(0)
                                .getTestRoles(), testRolesForResultList.get(0).getTestPeerType(),
                                systemActorProfiles);
                        testRolesParticipantsResultList.add(tmp);
                        if (isUserAdminOrMonitorForSession) {
                            tmp.setIsConnectedUserAllowedToStartTest(true);
                        } else {
                            if (tmp.getTestRoles().getTest().getTestPeerType().equals(TestPeerType.getWORKFLOW_TEST())) {
                                tmp.setIsConnectedUserAllowedToStartTest(false);
                            } else {
                                tmp.setIsConnectedUserAllowedToStartTest(isConnectedUserAllowedToStartTest);
                            }
                        }
                        testRolesForResultList = new ArrayList<TestRolesForResult>();
                        testRolesForResultList.add(foundtestRolesForResultList.get(i));
                    }
                }
                TestRolesParticipantsResult tmp = new TestRolesParticipantsResult(
                        selectedSystemInSession.getTestingSession(), testRolesForResultList.get(
                        testRolesForResultList.size() - 1).getTestRoles(), testRolesForResultList.get(
                        testRolesForResultList.size() - 1).getTestPeerType(), testRolesForResultList.get(
                        testRolesForResultList.size() - 1).getSystemActorProfiles());
                if (isUserAdminOrMonitorForSession) {
                    tmp.setIsConnectedUserAllowedToStartTest(true);
                } else {
                    if (tmp.getTestRoles().getTest().getTestPeerType().equals(TestPeerType.getWORKFLOW_TEST())) {
                        tmp.setIsConnectedUserAllowedToStartTest(false);
                    } else {
                        tmp.setIsConnectedUserAllowedToStartTest(isConnectedUserAllowedToStartTest);
                    }
                }
                testRolesParticipantsResultList.add(tmp);
                if (comparator != null) {
                    Collections.sort(testRolesParticipantsResultList, comparator);
                }
            }
        }

    }

    @Override
    public void searchTestRolesParticipantsResultByTestPeerType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("searchTestRolesParticipantsResultByTestPeerType");
        }
        if (selectedTestPeerType != null) {
            foundTestRolesParticipantsResult = new ArrayList<TestRolesParticipantsResult>();
            for (TestRolesParticipantsResult testRolesParticipantsResult : testRolesParticipantsResultListWithoutMetaTest) {
                if (testRolesParticipantsResult.getTestPeerType().equals(selectedTestPeerType)) {
                    foundTestRolesParticipantsResult.add(testRolesParticipantsResult);
                }
            }
            foundMetaTestRolesParticipantsResult = new ArrayList<MetaTestRolesParticipantsResult>();
            for (MetaTestRolesParticipantsResult metaTestRolesParticipantsResulttmp : metaTestRolesParticipantsResult) {
                if (metaTestRolesParticipantsResulttmp.getMetaTest().getTestRolesList().get(0).getTest()
                        .getTestPeerType().equals(selectedTestPeerType)) {
                    foundMetaTestRolesParticipantsResult.add(metaTestRolesParticipantsResulttmp);
                }
            }
        } else {
            foundTestRolesParticipantsResult = testRolesParticipantsResultListWithoutMetaTest;
            foundMetaTestRolesParticipantsResult = metaTestRolesParticipantsResult;
        }
        computeTestPartners();
    }

    private void searchMetaTestRolesParticipantsResult() {
        if (metaTestListForSelectedSystemInSession != null) {
            metaTestRolesParticipantsResult = new ArrayList<MetaTestRolesParticipantsResult>();
            testRolesParticipantsResultListWithoutMetaTest = new ArrayList<TestRolesParticipantsResult>(
                    testRolesParticipantsResultList.size());
            testRolesParticipantsResultListWithoutMetaTest.addAll(testRolesParticipantsResultList);
            for (MetaTest metaTest : metaTestListForSelectedSystemInSession) {
                MetaTestRolesParticipantsResult metaTestRolesParticipantsResulttmp = new MetaTestRolesParticipantsResult();
                metaTestRolesParticipantsResulttmp.setMetaTest(metaTest);
                for (TestRoles testRoles : metaTest.getTestRolesList()) {
                    TestRolesParticipantsResult testRolesParticipantsResult = new TestRolesParticipantsResult(
                            selectedSystemInSession.getTestingSession(), testRoles, testRoles.getTest()
                            .getTestPeerType());
                    int index = testRolesParticipantsResultListWithoutMetaTest.indexOf(testRolesParticipantsResult);
                    if (index != -1) {
                        metaTestRolesParticipantsResulttmp
                                .addTestRolesParticipantsResult(testRolesParticipantsResultListWithoutMetaTest
                                        .get(index));
                        testRolesParticipantsResultListWithoutMetaTest.remove(index);
                    }
                }
                if (metaTestRolesParticipantsResulttmp.getTestRolesParticipantsResultList() != null) {
                    metaTestRolesParticipantsResulttmp.setNumberOfTestsToRealize(metaTestRolesParticipantsResulttmp
                            .getTestRolesParticipantsResultList().get(0).getTestRoles().getNumberOfTestsToRealize());
                }
                metaTestRolesParticipantsResult.add(metaTestRolesParticipantsResulttmp);
            }
        } else {
            metaTestRolesParticipantsResult = null;
        }
    }

    private void searchTestRolesParicipants() {
        if ((selectedSystemInSession != null) && (testRolesParticipantsResultList != null)) {

            TestRolesQuery trQuery = new TestRolesQuery();

            trQuery.roleInTest().testParticipantsList().tested().eq(Boolean.TRUE);
            trQuery.test().testType().in(selectedSystemInSession.getTestingSession().getTestTypes());
            trQuery.test().testStatus().keyword().eq("ready");

            trQuery.roleInTest().addFetch();
            trQuery.roleInTest().testParticipantsList().addFetch();
            trQuery.roleInTest().testParticipantsList().actorIntegrationProfileOption().addFetch();
            trQuery.roleInTest().testParticipantsList().aipo().addFetch();

            trQuery.roleInTest().testParticipantsList().aipo().systemActorProfiles().system()
                    .eq(selectedSystemInSession.getSystem());

            trQuery.roleInTest().testParticipantsList().actorIntegrationProfileOption().actorIntegrationProfile()
                    .actor().eqIfValueNotNull(selectedActor);
            trQuery.roleInTest().testParticipantsList().actorIntegrationProfileOption().actorIntegrationProfile()
                    .integrationProfile().eqIfValueNotNull(selectedIntegrationProfile);

            List<TestRoles> roles = trQuery.getList();

            Set<Integer> roleInTestIds = new HashSet<Integer>();
            for (TestRoles testRoles : roles) {
                roleInTestIds.add(testRoles.getRoleInTest().getId());
            }

            TestInstanceParticipantsQuery tipQuery = new TestInstanceParticipantsQuery();
            tipQuery.roleInTest().testParticipantsList().actorIntegrationProfileOption().actorIntegrationProfile()
                    .actor().eqIfValueNotNull(selectedActor);
            tipQuery.roleInTest().testParticipantsList().actorIntegrationProfileOption().actorIntegrationProfile()
                    .integrationProfile().eqIfValueNotNull(selectedIntegrationProfile);
            tipQuery.systemInSessionUser().systemInSession().system().id()
                    .eq(selectedSystemInSession.getSystem().getId());
            tipQuery.systemInSessionUser().systemInSession().testingSession()
                    .eq(selectedSystemInSession.getTestingSession());
            tipQuery.roleInTest().id().in(roleInTestIds);
            List<TestInstanceParticipants> testInstanceParticipantsList = tipQuery.getList();

            List<Status> statusList = Status.getListStatus();
            for (TestRolesParticipantsResult testRolesParticipantsResult : testRolesParticipantsResultList) {
                HashMap<Status, List<TestInstanceParticipants>> hashMap = new HashMap<Status, List<TestInstanceParticipants>>();
                for (Status status : statusList) {
                    List<TestInstanceParticipants> tmpList = new ArrayList<TestInstanceParticipants>();
                    for (TestInstanceParticipants testInstanceParticipants : testInstanceParticipantsList) {
                        if (testInstanceParticipants.getRoleInTest().getId()
                                .equals(testRolesParticipantsResult.getTestRoles().getRoleInTest().getId())
                                && testInstanceParticipants.getTestInstance().getTest().getKeyword()
                                .equals(testRolesParticipantsResult.getTestRoles().getTest().getKeyword())
                                && testInstanceParticipants.getTestInstance().getLastStatus().equals(status)) {
                            tmpList.add(testInstanceParticipants);
                        }
                    }
                    hashMap.put(status, tmpList);
                }
                testRolesParticipantsResult.setTestInstanceParticipantsMap(hashMap);
            }

        }
    }

    private void searchTestRolesParicipants2() {
        if ((selectedSystemInSession != null) && (testRolesParticipantsResultList != null)) {
            List<Status> statusList = Status.getListStatus();
            for (int i = 0; i < testRolesParticipantsResultList.size(); i++) {
                TestRolesParticipantsResult testRolesParticipantsResult = testRolesParticipantsResultList.get(i);
                for (Status status : statusList) {
                    new TestRolesParticipants(testRolesParticipantsResult.getTestRoles(), status);
                    TestInstanceParticipants.getTestInstancesParticipantsForATestWithStatus(
                            testRolesParticipantsResult.getTestRoles(), selectedSystemInSession, status);
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private boolean isResponderReachable(String ip, int port) {
        try {
            Inet4Address address = (Inet4Address) InetAddress.getByName(ip);
            try {
                Socket s = new Socket(address, port);
                s.close();
                return true;
            } catch (Exception e) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /*
     * Random Test Instance generation
     */
    @Override
    public void generateTestInstanceForAllSystemsInTestingSession(TestingSession inTestingSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateTestInstanceForAllSystemsInTestingSession");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        TestInstanceGenerator.generateTestInstanceForAllSystemsInTestingSession(inTestingSession, entityManager);
    }

    @Override
    public void generateRandomlyTestInstanceForSelectedInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateRandomlyTestInstanceForSelectedInstitution");
        }
        if (selectedInstitution != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            List<SystemInSession> systemInSessionList = getSystemListByInstitution(selectedInstitution);
            Collections.sort(systemInSessionList);
            TestInstanceGenerator.generateTestInstanceForAListOfSystems(systemInSessionList, entityManager);
        }
    }

    // //////////

    @Override
    public void updateWorkListFromDb() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateWorkListFromDb");
        }
        toBeVerifiedTestInstanceList = getTestInstanceListForSelectedSessionByTestInstanceStatus(Status
                .getSTATUS_COMPLETED());
        criticalTestInstanceList = getTestInstanceListForSelectedSessionByTestInstanceStatus(Status
                .getSTATUS_CRITICAL());
        partiallyVerifiedTestInstanceList = getTestInstanceListForSelectedSessionByTestInstanceStatus(Status
                .getSTATUS_PARTIALLY_VERIFIED());
        verifiedTestInstanceList = getTestInstanceListForSelectedSessionByTestInstanceStatus(Status
                .getSTATUS_VERIFIED());
        failedTestInstanceList = getTestInstanceListForSelectedSessionByTestInstanceStatus(Status.getSTATUS_FAILED());
        abortedTestInstanceList = getTestInstanceListForSelectedSessionByTestInstanceStatus(Status.getSTATUS_ABORTED());
        pausedTestInstanceList = getTestInstanceListForSelectedSessionByTestInstanceStatus(Status.getSTATUS_PAUSED());
        runningTestInstanceList = getTestInstanceListForSelectedSessionByTestInstanceStatus(Status.getSTATUS_STARTED());
    }

    @Override
    public List<TestInstance> getTestInstanceListForSelectedSessionByTestInstanceStatus(Status inStatus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestInstanceListForSelectedSessionByTestInstanceStatus");
        }
        List<TestInstance> testInstanceList = null;
        if (inStatus != null) {

            List<TestType> testTypes = TestingSession.getSelectedTestingSession().getTestTypes();
            if ((selectedInstitution == null) && (selectedSystemInSession == null)
                    && (selectedIntegrationProfile == null) && (selectedActor == null) && (selectedMonitor == null)
                    && (selectedTest == null)) {
                testInstanceList = TestInstance.getTestInstancesFiltered(null, inStatus, null, null, null, null, null,
                        null, testTypes, TestingSession.getSelectedTestingSession(), null);
            } else {
                testInstanceList = TestInstance.getTestInstancesFiltered(selectedMonitor, inStatus,
                        selectedInstitution, selectedSystemInSession, null, selectedIntegrationProfile, selectedActor,
                        null, testTypes, TestingSession.getSelectedTestingSession(), selectedTest);
            }
        }

        if (testInstanceList != null) {
            return testInstanceList;
        }
        return new ArrayList<TestInstance>();
    }

    @Override
    public void filterTestsLists() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("filterTestsLists");
        }

        toBeVerifiedTestInstanceList = null;
        criticalTestInstanceList = null;
        partiallyVerifiedTestInstanceList = null;
        verifiedTestInstanceList = null;
        failedTestInstanceList = null;
        abortedTestInstanceList = null;
        runningTestInstanceList = null;
        pausedTestInstanceList = null;

        if ((filterPartiallyVerified != null) && filterPartiallyVerified) {

            partiallyVerifiedTestInstanceList = getTestInstanceListForSelectedSessionByTestInstanceStatus(Status
                    .getSTATUS_PARTIALLY_VERIFIED());
        }
        if ((filterFailedTests != null) && filterFailedTests) {

            failedTestInstanceList = getTestInstanceListForSelectedSessionByTestInstanceStatus(Status
                    .getSTATUS_FAILED());
        }
        if ((filterToBeVerifiedTests != null) && filterToBeVerifiedTests) {

            toBeVerifiedTestInstanceList = getTestInstanceListForSelectedSessionByTestInstanceStatus(Status
                    .getSTATUS_COMPLETED());
        }
        if ((filterVerifiedTests != null) && filterVerifiedTests) {

            verifiedTestInstanceList = getTestInstanceListForSelectedSessionByTestInstanceStatus(Status
                    .getSTATUS_VERIFIED());
        }
        if ((filterCriticalTests != null) && filterCriticalTests) {

            criticalTestInstanceList = getTestInstanceListForSelectedSessionByTestInstanceStatus(Status
                    .getSTATUS_CRITICAL());
        }
        if ((filterAbortedTests != null) && filterAbortedTests) {

            abortedTestInstanceList = getTestInstanceListForSelectedSessionByTestInstanceStatus(Status
                    .getSTATUS_ABORTED());
        }
        if ((filterRunningTests != null) && filterRunningTests) {

            runningTestInstanceList = getTestInstanceListForSelectedSessionByTestInstanceStatus(Status
                    .getSTATUS_STARTED());
        }
        if ((filterPausedTests != null) && filterPausedTests) {

            pausedTestInstanceList = getTestInstanceListForSelectedSessionByTestInstanceStatus(Status
                    .getSTATUS_PAUSED());
        }
    }

    @Override
    public List<TestInstance> getToBeVerifiedTestInstanceList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getToBeVerifiedTestInstanceList");
        }
        return toBeVerifiedTestInstanceList;
    }

    @Override
    public void setToBeVerifiedTestInstanceList(List<TestInstance> toBeVerifiedTestInstanceList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setToBeVerifiedTestInstanceList");
        }
        this.toBeVerifiedTestInstanceList = toBeVerifiedTestInstanceList;
    }

    @Override
    public List<TestInstance> getCriticalTestInstanceList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCriticalTestInstanceList");
        }
        return criticalTestInstanceList;
    }

    @Override
    public void setCriticalTestInstanceList(List<TestInstance> criticalTestInstanceList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCriticalTestInstanceList");
        }
        this.criticalTestInstanceList = criticalTestInstanceList;
    }

    @Override
    public List<TestInstance> getPartiallyVerifiedTestInstanceList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPartiallyVerifiedTestInstanceList");
        }
        return partiallyVerifiedTestInstanceList;
    }

    @Override
    public void setPartiallyVerifiedTestInstanceList(List<TestInstance> partiallyVerifiedTestInstanceList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setPartiallyVerifiedTestInstanceList");
        }
        this.partiallyVerifiedTestInstanceList = partiallyVerifiedTestInstanceList;
    }

    @Override
    public List<TestInstance> getVerifiedTestInstanceList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getVerifiedTestInstanceList");
        }
        return verifiedTestInstanceList;
    }

    @Override
    public void setVerifiedTestInstanceList(List<TestInstance> verifiedTestInstanceList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setVerifiedTestInstanceList");
        }
        this.verifiedTestInstanceList = verifiedTestInstanceList;
    }

    @Override
    public List<TestInstance> getFailedTestInstanceList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFailedTestInstanceList");
        }
        return failedTestInstanceList;
    }

    @Override
    public void setFailedTestInstanceList(List<TestInstance> failedTestInstanceList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFailedTestInstanceList");
        }
        this.failedTestInstanceList = failedTestInstanceList;
    }

    @Override
    public List<TestInstance> getAbortedTestInstanceList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAbortedTestInstanceList");
        }
        return abortedTestInstanceList;
    }

    @Override
    public void setAbortedTestInstanceList(List<TestInstance> abortedTestInstanceList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setAbortedTestInstanceList");
        }
        this.abortedTestInstanceList = abortedTestInstanceList;
    }

    @Override
    public List<TestInstance> getRunningTestInstanceList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRunningTestInstanceList");
        }
        return runningTestInstanceList;
    }

    @Override
    public void setRunningTestInstanceList(List<TestInstance> runningTestInstanceList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setRunningTestInstanceList");
        }
        this.runningTestInstanceList = runningTestInstanceList;
    }

    @Override
    public List<TestInstance> getPausedTestInstanceList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPausedTestInstanceList");
        }
        return pausedTestInstanceList;
    }

    @Override
    public void setPausedTestInstanceList(List<TestInstance> pausedTestInstanceList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setPausedTestInstanceList");
        }
        this.pausedTestInstanceList = pausedTestInstanceList;
    }

    @Override
    public String getFilterTestsByTestsWithoutMonitor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilterTestsByTestsWithoutMonitor");
        }
        return filterTestsByTestsWithoutMonitor;
    }

    @Override
    public void setFilterTestsByTestsWithoutMonitor(String filterTestsByTestsWithoutMonitor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFilterTestsByTestsWithoutMonitor");
        }
        this.filterTestsByTestsWithoutMonitor = filterTestsByTestsWithoutMonitor;
    }

    @Override
    public String getFilterCriticalTestInstancesWithoutMonitor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilterCriticalTestInstancesWithoutMonitor");
        }
        return filterCriticalTestInstancesWithoutMonitor;
    }

    @Override
    public String getFilterToBeVerifiedTestInstancesWithoutMonitor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilterToBeVerifiedTestInstancesWithoutMonitor");
        }
        return filterToBeVerifiedTestInstancesWithoutMonitor;
    }

    @Override
    public String getFilterTestsWithoutMonitor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilterTestsWithoutMonitor");
        }
        return filterTestsWithoutMonitor;
    }

    @Override
    public String viewSystemInMainConnectathonPage(SystemInSession sis) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewSystemInMainConnectathonPage");
        }

        EntityManager entityManager = EntityManagerService.provideEntityManager();
        selectedSystemInSession = entityManager.find(SystemInSession.class, sis.getId());
        selectedInstitution = System.getInstitutionsForASystem(selectedSystemInSession.getSystem()).get(0);

        displayTestInstancesByTest();

        return "/testing/test/test/mainConnectathonPage.seam";//DONE
    }

    @Override
    public void displayTestInstancesByTestForTestsOverviewPage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayTestInstancesByTestForTestsOverviewPage");
        }
        showTestListPanel();

        filterSortingValue = this.getSortByTestsStatus();

        filterPartiallyVerified = true;
        filterFailedTests = true;
        filterToBeVerifiedTests = true;
        filterVerifiedTests = true;
        filterCriticalTests = true;
        filterAbortedTests = true;
        filterRunningTests = true;
        filterPausedTests = true;

        filterTestsLists();
    }

    @Override
    @Deprecated
    public void initializeTestsWithoutMonitorPage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeTestsWithoutMonitorPage");
        }

        if (testingSessionChoosen.getIsCriticalStatusEnabled()) {
            filterTestsByTestsWithoutMonitor = filterCriticalTestInstancesWithoutMonitor;
            getListOfCriticalTestInstancesWithoutMonitorList();
            getListOfToBeVerifiedTestInstancesWithoutMonitorList();
        } else {
            filterTestsByTestsWithoutMonitor = filterTestsWithoutMonitor;
            getListOfToBeVerifiedTestInstancesWithoutMonitorList();
        }
        filterTestsLists();
    }

    @Override
    public void getListOfCriticalTestInstancesWithoutMonitorList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfCriticalTestInstancesWithoutMonitorList");
        }

        List<TestType> testTypes = testingSessionChoosen.getTestTypes();
        List<TestInstance> listOfTestInstance = TestInstance
                .getTestInstancesFiltered(null, Status.getSTATUS_CRITICAL(), null, null, null, null, null, null,
                        testTypes, testingSessionChoosen, null);
        List<TestInstance> listOfAllTestsInstancesAssignedToMonitors = new ArrayList<TestInstance>();
        List<TestInstance> listOfAllTestsInstancesAssignedForOneMonitor = null;
        criticalTestInstancesWithoutMonitorList = new ArrayList<TestInstance>();

        List<MonitorInSession> listOfAllMonitorsInSession = MonitorInSession
                .getAllActivatedMonitorsForATestingSession(testingSessionChoosen);

        for (MonitorInSession monitor : listOfAllMonitorsInSession) {
            listOfAllTestsInstancesAssignedForOneMonitor = MonitorInSession.getTestInstancesFilteredForAMonitor(null,
                    monitor, Status.getSTATUS_CRITICAL(), null, null, null, null, null, null,
                    testingSessionChoosen.getTestTypes(), testingSessionChoosen);

            for (TestInstance oneTestInstance : listOfAllTestsInstancesAssignedForOneMonitor) {
                if (!listOfAllTestsInstancesAssignedToMonitors.contains(oneTestInstance)) {
                    listOfAllTestsInstancesAssignedToMonitors.add(oneTestInstance);
                }
            }
        }

        for (TestInstance aTestInstance : listOfTestInstance) {
            if (!listOfAllTestsInstancesAssignedToMonitors.contains(aTestInstance)) {
                criticalTestInstancesWithoutMonitorList.add(aTestInstance);
            }
        }
    }

    @Override
    public void getListOfToBeVerifiedTestInstancesWithoutMonitorList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfToBeVerifiedTestInstancesWithoutMonitorList");
        }

        List<TestInstance> listOfTestInstance = TestInstance.getTestInstancesFiltered(null,
                Status.getSTATUS_COMPLETED(), null, null, null, null, null, null, testingSessionChoosen.getTestTypes(),
                testingSessionChoosen, null);
        List<TestInstance> listOfAllTestsInstancesAssignedToMonitors = new ArrayList<TestInstance>();
        List<TestInstance> listOfAllTestsInstancesAssignedForOneMonitor = null;
        toBeVerifiedTestInstancesWithoutMonitorList = new ArrayList<TestInstance>();

        List<MonitorInSession> listOfAllMonitorsInSession = MonitorInSession
                .getAllActivatedMonitorsForATestingSession(testingSessionChoosen);

        if (listOfAllMonitorsInSession == null) {

            return;
        }

        for (MonitorInSession monitor : listOfAllMonitorsInSession) {
            listOfAllTestsInstancesAssignedForOneMonitor = MonitorInSession.getTestInstancesFilteredForAMonitor(null,
                    monitor, Status.getSTATUS_COMPLETED(), null, null, null, null, null, null,
                    testingSessionChoosen.getTestTypes(), testingSessionChoosen);

            for (TestInstance oneTestInstance : listOfAllTestsInstancesAssignedForOneMonitor) {
                if (!listOfAllTestsInstancesAssignedToMonitors.contains(oneTestInstance)) {
                    listOfAllTestsInstancesAssignedToMonitors.add(oneTestInstance);
                }
            }
        }

        for (TestInstance aTestInstance : listOfTestInstance) {
            if (!listOfAllTestsInstancesAssignedToMonitors.contains(aTestInstance)) {
                criticalTestInstancesWithoutMonitorList.add(aTestInstance);
            }
        }
    }

    @Override
    public void getListOfTestsWithoutMonitorList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfTestsWithoutMonitorList");
        }

        testsWithoutMonitorList = null;
    }

    @Override
    public List<TestInstance> getCriticalTestInstancesWithoutMonitorList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCriticalTestInstancesWithoutMonitorList");
        }
        return criticalTestInstancesWithoutMonitorList;
    }

    @Override
    public void setCriticalTestInstancesWithoutMonitorList(List<TestInstance> criticalTestInstancesWithoutMonitorList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCriticalTestInstancesWithoutMonitorList");
        }
        this.criticalTestInstancesWithoutMonitorList = criticalTestInstancesWithoutMonitorList;
    }

    @Override
    public List<TestInstance> getToBeVerifiedTestInstancesWithoutMonitorList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getToBeVerifiedTestInstancesWithoutMonitorList");
        }
        return toBeVerifiedTestInstancesWithoutMonitorList;
    }

    @Override
    public void setToBeVerifiedTestInstancesWithoutMonitorList(
            List<TestInstance> toBeVerifiedTestInstancesWithoutMonitorList) {
        this.toBeVerifiedTestInstancesWithoutMonitorList = toBeVerifiedTestInstancesWithoutMonitorList;
    }

    @Override
    public List<TestInstance> getTestsWithoutMonitorList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestsWithoutMonitorList");
        }
        return testsWithoutMonitorList;
    }

    @Override
    public void setTestsWithoutMonitorList(List<TestInstance> testsWithoutMonitorList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestsWithoutMonitorList");
        }
        this.testsWithoutMonitorList = testsWithoutMonitorList;
    }

    @Override
    public void selectSystemInSessionAndViewAllTests(SystemInSession selectedSystemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("selectSystemInSessionAndViewAllTests");
        }
        this.setSelectedSystemInSession(selectedSystemInSession);
        this.displayTestInstancesByTest();
    }

    @Override
    public void showListSystemInSessionsForCurrentInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showListSystemInSessionsForCurrentInstitution");
        }
        this.setSelectedSystemInSession(null);
        this.setSelectedActor(null);
        this.setSelectedIntegrationProfile(null);
        this.displayTestInstancesByTest();
    }

}
