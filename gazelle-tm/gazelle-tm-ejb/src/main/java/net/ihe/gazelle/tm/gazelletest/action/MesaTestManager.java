package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.action.CacheRequest;
import net.ihe.gazelle.common.action.CacheUpdater;
import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.fineuploader.FineuploaderListener;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.log.ExceptionLogging;
import net.ihe.gazelle.common.util.DocumentFileUploadLocal;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.HQLReloader;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.*;
import net.ihe.gazelle.tm.application.action.ApplicationManager;
import net.ihe.gazelle.tm.filter.TMCriterions;
import net.ihe.gazelle.tm.filter.valueprovider.InstitutionFixer;
import net.ihe.gazelle.tm.gazelletest.bean.TestStatusStatus;
import net.ihe.gazelle.tm.gazelletest.model.definition.*;
import net.ihe.gazelle.tm.gazelletest.model.instance.*;
import net.ihe.gazelle.tm.gazelletest.model.reversed.AIPO;
import net.ihe.gazelle.tm.gazelletest.model.reversed.AIPOQuery;
import net.ihe.gazelle.tm.messages.MessageManager;
import net.ihe.gazelle.tm.messages.TestInstanceParticipantCommentMessageSource;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.*;
import net.ihe.gazelle.tm.users.action.UserManagerExtra;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.Role;
import net.ihe.gazelle.users.model.User;
import net.ihe.gazelle.util.Pair;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.*;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.security.Identity;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.jboss.seam.ScopeType.SESSION;

/**
 * @author abderrazek boufahja
 */
@Name("mesaTestManager")
@Scope(SESSION)
@Synchronized(timeout = 10000)
@GenerateInterface("MesaTestManagerLocal")
public class MesaTestManager extends ValidationForTIP implements MesaTestManagerLocal, QueryModifier<SystemInSession>, FineuploaderListener {

    private static final String MINUS = " - ";
    private static final String MESA_AUTOMATIC_VALIDATION = "mesa_test_automatic_validation";
    private static final String MESA_SEND_MAIL = "send_mail_mesa_notification";
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(MesaTestManager.class);
    private static String MESA_PAGE_VALIDATE_MESA_TEST = "/testing/test/mesa/validateMesaTest.seam";
    private static String MESA_PAGE_LIST_OF_PRECONNECTATHON_TEST = "/testing/test/mesa/listOfPreconnectathonTests.seam";
    private static String MESA_PAGE_LOG_RETURN = "/testing/test/mesa/logReturnForMesaTest.seam";
    private static String MESA_PAGE_LIST_ALL_SYSTEMS = "/testing/test/mesa/listAllSystemsForMesaTesting.seam";
    private static int MAX_UPLOADS = 50;
    private String previousPage;
    private TestInstanceParticipants currentTestInstanceParticipants;
    private Status selectedStatusForEdition;
    private List<TestInstancePathToLogFile> files;
    private IntegrationProfile selectedIntegrationProfile;
    private System selectedSystem;
    private Actor selectedActor;
    private TestOption selectedTestOption;
    private Institution selectedInstitution;
    /**
     * Used to process an additional filter in listOfPreconnectathon test / filter test by LOG returned or not returned possible values all /
     * testsReturned / testsToDo
     */
    private TestStatusStatus filterByInstanceStatus;
    private List<TestRoles> listTestRoles;
    private SystemInSession selectedSystemInSession;
    private FilterDataModel<SystemInSession> systemsInSession;
    private String oldComments;
    private boolean allowedToAddLogs = true;

    // getter and setter //////////////////////////////////////////

    private static HQLCriterionsForFilter<SystemInSession> getHQLCriterions() {
        SystemInSessionQuery query = new SystemInSessionQuery();
        HQLCriterionsForFilter<SystemInSession> result = query.getHQLCriterionsForFilter();

        TMCriterions.addTestingSession(result, "testing_session", query.testingSession());
        result.addPath("institution", query.system().institutionSystems().institution(), null, InstitutionFixer.INSTANCE);

        return result;
    }

    @Override
    public String getOldComments() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOldComments");
        }
        return oldComments;
    }

    @Override
    public void setOldComments(String oldComments) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setOldComments");
        }
        this.oldComments = oldComments;
    }

    @Override
    public FilterDataModel<SystemInSession> getSystemsInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemsInSession");
        }
        if (systemsInSession == null) {
            systemsInSession = new FilterDataModel<SystemInSession>(new Filter<SystemInSession>(getHQLCriterions())) {
                @Override
                protected Object getId(SystemInSession t) {
                    return t.getId();
                }
            };
        }
        return systemsInSession;
    }

    @Override
    public void modifyQuery(HQLQueryBuilder queryBuilder, Map filterValuesApplied) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyQuery");
        }
        SystemInSessionQuery query = new SystemInSessionQuery();
        UserManagerExtra umeb = new UserManagerExtra();

        if (!User.loggedInUser().hasRole(Role.getADMINISTRATOR_ROLE())) {
            queryBuilder.addRestriction(query.testingSession().eqRestriction(umeb.getSelectedTestingSession()));
        }
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
    public System getSelectedSystem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedSystem");
        }
        return selectedSystem;
    }

    @Override
    public void setSelectedSystem(System selectedSystem) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedSystem");
        }
        this.selectedSystem = selectedSystem;
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
    public TestOption getSelectedTestOption() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestOption");
        }
        return selectedTestOption;
    }

    @Override
    public void setSelectedTestOption(TestOption selectedTestOption) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestOption");
        }
        this.selectedTestOption = selectedTestOption;
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
    public TestStatusStatus getFilterByInstanceStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilterByInstanceStatus");
        }
        return filterByInstanceStatus;
    }

    @Override
    public void setFilterByInstanceStatus(TestStatusStatus filterByInstanceStatus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFilterByInstanceStatus");
        }
        this.filterByInstanceStatus = filterByInstanceStatus;
    }

    @Override
    public List<TestRoles> getListTestRoles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListTestRoles");
        }
        if (listTestRoles != null) {
            Collections.sort(listTestRoles, TestRoles.Comparators.KEYWORD);
        }
        return listTestRoles;
    }

    @Override
    public void setListTestRoles(List<TestRoles> listTestRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setListTestRoles");
        }
        this.listTestRoles = listTestRoles;
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
    public List<TestInstancePathToLogFile> getFiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFiles");
        }
        if (files == null) {
            files = new ArrayList<TestInstancePathToLogFile>();
        }
        return files;
    }

    @Override
    public void setFiles(List<TestInstancePathToLogFile> files) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFiles");
        }
        this.files = files;
    }

    @Override
    public String getPreviousPage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPreviousPage");
        }
        return previousPage;
    }

    @Override
    public void setPreviousPage(String previousPage) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setPreviousPage");
        }
        this.previousPage = previousPage;
    }

    @Override
    public TestInstanceParticipants getCurrentTestInstanceParticipants() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCurrentTestInstanceParticipants");
        }
        return currentTestInstanceParticipants;
    }

    @Override
    public void setCurrentTestInstanceParticipants(TestInstanceParticipants currentTestInstanceParticipants) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCurrentTestInstanceParticipants");
        }
        this.currentTestInstanceParticipants = currentTestInstanceParticipants;
    }

    @Override
    public Status getSelectedStatusForEdition() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedStatusForEdition");
        }
        return selectedStatusForEdition;
    }

    @Override
    public void setSelectedStatusForEdition(Status selectedStatusForEdition) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedStatusForEdition");
        }
        this.selectedStatusForEdition = selectedStatusForEdition;
    }

    @Override
    public boolean isAllowedToAddLogs() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isAllowedToAddLogs");
        }
        if (!User.loggedInUser().hasRole(Role.getADMINISTRATOR_ROLE()) && User.loggedInUser().hasRole(Role.getMONITOR_ROLE())) {
            setAllowedToAddLogs(false);
        }
        return allowedToAddLogs;
    }

    @Override
    public void setAllowedToAddLogs(boolean allowedToAddLogs) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setAllowedToAddLogs");
        }
        this.allowedToAddLogs = allowedToAddLogs;
    }

    // methods ////////////////////////////////////////////////////

    @Override
    public String createInstanceBackToPreviousScreen() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createInstanceBackToPreviousScreen");
        }
        return previousPage;
    }

    @Override
    public String editTestInstanceParticipantFromListInstances(TestInstanceParticipants inTestInstanceParticipants) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editTestInstanceParticipantFromListInstances");
        }
        previousPage = MESA_PAGE_VALIDATE_MESA_TEST;
        return editTestInstanceParticipantForMesaTests(inTestInstanceParticipants);
    }

    @Override
    public String editTestInstanceParticipantForMesaTests(TestInstanceParticipants inTestInstanceParticipants) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editTestInstanceParticipantForMesaTests");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        currentTestInstanceParticipants = inTestInstanceParticipants;
        TestInstance currentTestInstance = entityManager.find(TestInstance.class, currentTestInstanceParticipants.getTestInstance().getId());
        setFiles(new ArrayList<TestInstancePathToLogFile>(currentTestInstance.getFileLogPathReturn()));
        if (currentTestInstance.getLastStatus() == null) {
            selectedStatusForEdition = Status.getSTATUS_STARTED();
        } else {
            selectedStatusForEdition = currentTestInstance.getLastStatus();
        }

        return MESA_PAGE_LOG_RETURN;
    }

    @Override
    public String setTestToVerified() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestToVerified");
        }
        if (currentTestInstanceParticipants == null) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Impossible to set the test to verified : test instance used is null");
            return previousPage;
        }
        if (currentTestInstanceParticipants.getId() == null) {
            this.persistFiles(null);
        }
        this.setTestToVerified(currentTestInstanceParticipants);
        return previousPage;
    }

    @Override
    public List<IntegrationProfile> getPossibleIntegrationProfileForCurrentSystem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleIntegrationProfileForCurrentSystem");
        }
        List<IntegrationProfile> ipList = SystemActorProfiles.getListOfIntegrationProfilesImplementedByASystem(selectedSystem);
        if (ipList != null) {
            Collections.sort(ipList);
            return ipList;
        }
        return new ArrayList<>();
    }

    @Override
    public List<Actor> getPossibleActorsForCurrentSystem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleActorsForCurrentSystem");
        }
        if (this.selectedIntegrationProfile == null) {
            List<Actor> actorsList = SystemActorProfiles.getListOfActorsImplementedByASystem(selectedSystem);
            Collections.sort(actorsList);
            return actorsList;
        } else {
            List<Actor> actorsList = SystemActorProfiles.getListOfActorsImplementedByASystemForAnIntegrationProfile(selectedSystem,
                    selectedIntegrationProfile);
            Collections.sort(actorsList);
            return actorsList;
        }
    }

    @Override
    public List<TestOption> getTestOptions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestOptions");
        }
        return TestOption.listTestOptions();
    }

    @Override
    public String displayListOfTests(SystemInSession sIs, TestStatusStatus testStatusStatus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayListOfTests");
        }
        selectedSystemInSession = sIs;
        selectedSystem = sIs.getSystem();

        this.filterByInstanceStatus = testStatusStatus;

        // TESTTYPE
        this.listOfTests(selectedSystem, null, null, null, null, TestType.getTYPE_MESA(), null, null, TestStatus.getSTATUS_READY(), true);

        return MESA_PAGE_LIST_OF_PRECONNECTATHON_TEST;
    }

    @Override
    public void displayListOfTestsFiltered() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayListOfTestsFiltered");
        }
        this.listOfTests(selectedSystem, selectedIntegrationProfile, null, selectedActor, null, TestType.getTYPE_MESA(), null, selectedTestOption,
                TestStatus.getSTATUS_READY(), true);
    }

    @Override
    public void displayListOfTestsUnfiltered() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayListOfTestsUnfiltered");
        }
        setSelectedIntegrationProfile(null);
        setSelectedActor(null);
        setSelectedTestOption(null);
        displayListOfTestsFiltered();
    }

    private void listOfTests(System inSystem, IntegrationProfile inIntegrationProfile, IntegrationProfileOption inIntegrationProfileOption, Actor
            inActor, Test inTest, TestType inTestType,
                             TestPeerType inTestPeerType, TestOption inTestOption, TestStatus inTestStatus, boolean inTested) {

        List<TestType> inTestTypes = Collections.singletonList(inTestType);
        listTestRoles = TestRoles.getTestsFiltered(inSystem, inIntegrationProfile, inIntegrationProfileOption, inActor, inTest, inTestTypes,
                inTestPeerType, inTestOption, inTestStatus, inTested);
        List<TestRoles> tmpList = new ArrayList<TestRoles>(listTestRoles);

        if (filterByInstanceStatus != null) {
            for (TestRoles testToFilter : listTestRoles) {
                List<TestInstanceParticipants> participants = TestInstanceParticipants.getTestInstancesParticipantsForATest(testToFilter,
                        selectedSystemInSession);
                if (((participants == null) || (participants.size() == 0)) && (filterByInstanceStatus != TestStatusStatus.TO_COMPLETE_BY_VENDOR)) {
                    tmpList.remove(testToFilter);
                } else if ((participants != null) && (participants.size() > 0)) {
                    for (TestInstanceParticipants testInstanceParticipants : participants) {
                        Status lastStatus = testInstanceParticipants.getTestInstance().getLastStatus();
                        if (!ArrayUtils.contains(filterByInstanceStatus.getStatuses(), lastStatus)) {
                            tmpList.remove(testToFilter);
                            break;
                        }
                    }
                }
            }
        }

        listTestRoles = tmpList;
    }

    @Override
    public List<Pair<TestRoles, ActorIntegrationProfileOption>> getListTestRolesWithAIPO() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListTestRolesWithAIPO");
        }
        List<Pair<TestRoles, ActorIntegrationProfileOption>> result = new ArrayList<Pair<TestRoles, ActorIntegrationProfileOption>>();

        for (TestRoles testRoles : listTestRoles) {
            List<ActorIntegrationProfileOption> aipos = getAiposForTestRole(testRoles);
            for (ActorIntegrationProfileOption actorIntegrationProfileOption : aipos) {
                result.add(new Pair<TestRoles, ActorIntegrationProfileOption>(testRoles, actorIntegrationProfileOption));
            }
        }

        return result;
    }

    @Override
    public TestInstanceParticipants getSingleInstanceOfTestParticipants(TestRoles inTestRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSingleInstanceOfTestParticipants");
        }
        List<TestInstanceParticipants> testInstanceParticipants = TestInstanceParticipants.getTestInstancesParticipantsForATest(inTestRoles,
                selectedSystemInSession);
        TestInstanceParticipants tip = null;
        if ((testInstanceParticipants != null) && (testInstanceParticipants.size() > 0)) {
            tip = testInstanceParticipants.get(0);
        }
        return tip;
    }

    private String generateAMesaTestInstance_(TestRoles inCurrentTestRole) {
        TestInstance currentTestInstance = new TestInstance();
        currentTestInstance.setTest(inCurrentTestRole.getTest());
        currentTestInstance.setTestingSession(TestingSession.getSelectedTestingSession());
        currentTestInstanceParticipants = new TestInstanceParticipants();
        currentTestInstanceParticipants.setTestInstance(currentTestInstance);
        currentTestInstanceParticipants.setRoleInTest(inCurrentTestRole.getRoleInTest());
        EntityManager entityManager = EntityManagerService.provideEntityManager();

        setSystemInSessionUser(entityManager);

        selectedStatusForEdition = Status.getSTATUS_STARTED();
        ActorIntegrationProfileOption aipoTIP = this.getActorIntegrationProfileOptionByRoleInTestBySystemInSession(selectedSystemInSession,
                inCurrentTestRole.getRoleInTest());

        currentTestInstanceParticipants.setActorIntegrationProfileOption(aipoTIP);
        currentTestInstanceParticipants.getTestInstance().setLastStatus(selectedStatusForEdition);
        if ((files != null) && (files.size() > 0)) {
            files.clear();
        }
        persistStatus(currentTestInstance);
        return MESA_PAGE_LOG_RETURN;
    }

    @Override
    public String generateAMesaTestInstance(TestRoles inCurrentTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateAMesaTestInstance");
        }
        previousPage = MESA_PAGE_LIST_OF_PRECONNECTATHON_TEST;
        return generateAMesaTestInstance_(inCurrentTest);
    }

    @Override
    public boolean instanceParticipantExists(TestRoles inTestRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("instanceParticipantExists");
        }
        List<TestInstanceParticipants> list = TestInstanceParticipants.getTestInstancesParticipantsForATest(inTestRoles, selectedSystemInSession);
        if (list == null) {
            return false;
        } else {
            return list.size() > 0;
        }
    }

    @Override
    public void displaySingleInstanceOfTestParticipants(TestRoles inTestRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displaySingleInstanceOfTestParticipants");
        }
        List<TestInstanceParticipants> testInstanceParticipants = TestInstanceParticipants.getTestInstancesParticipantsForATest(inTestRoles,
                selectedSystemInSession);
        if (testInstanceParticipants.size() > 0) {
            currentTestInstanceParticipants = testInstanceParticipants.get(0);
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "No instances for this test ");
        }
    }

    @Override
    public String preConnectathonTestingClickFromMenu() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("preConnectathonTestingClickFromMenu");
        }
        return MESA_PAGE_LIST_ALL_SYSTEMS;
    }

    @Override
    public String editMesaTestInstanceParticipant(TestInstanceParticipants inTestInstanceParticipants) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editMesaTestInstanceParticipant");
        }
        previousPage = MESA_PAGE_LIST_OF_PRECONNECTATHON_TEST;
        return editTestInstanceParticipantForMesaTests(inTestInstanceParticipants);
    }

    @Override
    public long getNumberOfTestToDo(final SystemInSession sis) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNumberOfTestToDo");
        }
        if (sis == null) {
            return 0;
        }
        CacheRequest cacheRequest = (CacheRequest) Component.getInstance("cacheRequest");
        Integer result = (Integer) cacheRequest.getValueUpdater(sis.getId() + "_getNumberOfTestToDo", new CacheUpdater() {

            @Override
            public Object getValue(String key, Object parameter) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getValue");
                }
                TestRolesQuery testRolesQuery = new TestRolesQuery();
                testRolesQuery.roleInTest().testParticipantsList().aipo().systemActorProfiles().system().eq(sis.getSystem());
                testRolesQuery.test().testType().eq(TestType.getTYPE_MESA());
                testRolesQuery.test().testStatus().eq(TestStatus.getSTATUS_READY());
                testRolesQuery.roleInTest().testParticipantsList().tested().eq(Boolean.TRUE);
                return testRolesQuery.getCount();
            }
        }, null);
        return result;
    }

    @Override
    public long getNumberOfTestToComplete(SystemInSession sis) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNumberOfTestToComplete");
        }
        return Math.max(0, getNumberOfTestToDo(sis) - getNumberOfTestCompleted(sis) - getNumberOfTestVerified(sis));
    }

    @Override
    public long getNumberOfTestCompleted(final SystemInSession sis) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNumberOfTestCompleted");
        }
        return getNumberOfTestsForSystemAndStatuses(sis, "getNumberOfTestCompleted", Arrays.asList(TestStatusStatus.TO_VERIFY_BY_ADMIN.getStatuses
                ()));
    }

    @Override
    public long getNumberOfTestVerified(final SystemInSession sis) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNumberOfTestVerified");
        }
        return getNumberOfTestsForSystemAndStatuses(sis, "getNumberOfTestVerified", Arrays.asList(TestStatusStatus.VERIFIED.getStatuses()));
    }

    private long getNumberOfTestsForSystemAndStatuses(final SystemInSession sis, String keyword, final List<Status> statuses) {
        if (sis == null) {
            return 0;
        }
        CacheRequest cacheRequest = (CacheRequest) Component.getInstance("cacheRequest");
        Integer result = (Integer) cacheRequest.getValueUpdater(sis.getId() + "_" + keyword, new CacheUpdater() {

            @Override
            public Object getValue(String key, Object parameter) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getValue");
                }
                TestInstanceParticipantsQuery query = new TestInstanceParticipantsQuery();
                query.systemInSessionUser().systemInSession().eq(sis);
                query.testInstance().lastStatus().in(statuses);
                query.testInstance().test().testType().eq(TestType.getTYPE_MESA());
                query.testInstance().testingSession().eq(sis.getTestingSession());
                return query.getCount();
            }
        }, null);
        return result;
    }

    // destroy ////////////////////////////////////////////////////

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }
    }

    @Override
    public void savecurrentTestInstanceParticipant() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("savecurrentTestInstanceParticipant");
        }
    }

    @Override
    public void persistTest(TestInstance ti) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistTest");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        User loginUser = User.loggedInUser();
        TestInstance currentTestInstance = ti;

        Status llastStatus = null;
        if (currentTestInstance != null) {
            llastStatus = currentTestInstance.getLastStatus();
        }

        String gazelleLogReturnPath = ApplicationManager.instance().getGazelleLogReturnPath();

        if (currentTestInstance != null && currentTestInstance.getId() == null) {
            currentTestInstance = entityManager.merge(currentTestInstance);
        }

        String directoryPathForThisInstance = null;
        java.io.File newDirectoryForInstance = null;
        if (currentTestInstance != null) {
            directoryPathForThisInstance = gazelleLogReturnPath + java.io.File.separatorChar + currentTestInstance.getId();
            newDirectoryForInstance = new java.io.File(directoryPathForThisInstance);
        }


        if (newDirectoryForInstance != null && newDirectoryForInstance.mkdirs()) {
            // Creating a file to LOG some information
            try {
                String fileInfoPath = directoryPathForThisInstance + java.io.File.separatorChar + "instance.info";
                java.io.File fileInfo = new java.io.File(fileInfoPath);

                // Crash when admin !!!!!!!!!!!!! To verify
                FileOutputStream fost = new FileOutputStream(fileInfo);

                StringBuilder sb = new StringBuilder();
                sb.append("Instance id : ").append(currentTestInstance.getId()).append("<br />");
                if (this.currentTestInstanceParticipants != null) {
                    if (this.currentTestInstanceParticipants.getTestInstance() != null) {
                        if (this.currentTestInstanceParticipants.getTestInstance().getTest() != null) {
                            sb.append("For test : ").append(this.currentTestInstanceParticipants.getTestInstance().getTest().getKeyword()).append
                                    (MINUS).append(this.currentTestInstanceParticipants.getTestInstance().getTest().getName()).append("<br />");
                        }
                    }
                }
                if (selectedSystem != null) {
                    sb.append("For System : ").append(selectedSystem.getKeyword()).append(MINUS).append(selectedSystem.getName()).append("<br />");
                    sb.append("For company : ").append(System.getInstitutionsForASystem(selectedSystem).get(0).getName());
                }

                fost.write(sb.toString().getBytes(StandardCharsets.UTF_8.name()));
                fost.close();
            } catch (FileNotFoundException e) {
                ExceptionLogging.logException(e, LOG);
            } catch (IOException e) {
                ExceptionLogging.logException(e, LOG);
            }
        }

        if (currentTestInstance != null && currentTestInstance.getFileLogPathReturn() == null) {
            ArrayList<TestInstancePathToLogFile> arrayList = new ArrayList<TestInstancePathToLogFile>();
            currentTestInstance.setFileLogPathReturn(arrayList);
            currentTestInstance.setFileLogPathReturn(getFiles());
            currentTestInstance = entityManager.merge(currentTestInstance);
        }


        if (selectedStatusForEdition != null) {
            Status lastStatus = null;
            if (currentTestInstance != null) {
                if (currentTestInstance.getLastStatus() == null) {
                    lastStatus = Status.getSTATUS_STARTED();
                    currentTestInstance.setPreCatLastStatus(lastStatus, currentTestInstanceParticipants);
                } else {
                    lastStatus = currentTestInstance.getLastStatus();
                }
            }

            if (lastStatus != null && !lastStatus.getKeyword().equals(selectedStatusForEdition.getKeyword())) {
                currentTestInstance.setPreCatLastStatus(selectedStatusForEdition, currentTestInstanceParticipants);
            }
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Status is null");
        }

        setSystemInSessionUser(entityManager);

        // Adding now a validation only if non admin

        if ((!Role.isLoggedUserAdmin() && (Role.isLoggedUserVendorAdmin() || Role.isLoggedUserUser())) && (currentTestInstance.getLastStatus() !=
                null)
                && !currentTestInstance.getLastStatus().equals(Status.getSTATUS_STARTED()) && (files != null) && (files.size() > 0)) {

            String currentTestKeyword = currentTestInstanceParticipants.getTestInstance().getTest().getKeyword();

            currentTestInstanceParticipants.setTestInstance(currentTestInstance);

            try {
                Integer currentTestNumber = null;
                try {
                    currentTestNumber = Integer.valueOf(currentTestKeyword);
                } catch (Exception e) {
                }

                Status statusToUse;
                if (currentTestNumber != null && (((ValidationNistReturn.XDS_RANGE_LOW1 <= currentTestNumber) && (currentTestNumber <=
                        ValidationNistReturn.XDS_RANGE_HIGH1))
                        || ((ValidationNistReturn.XDS_RANGE_LOW2 <= currentTestNumber) && (currentTestNumber <= ValidationNistReturn
                        .XDS_RANGE_HIGH2)))) {
                    // USE WS NIST XDS VALIDATOR
                    statusToUse = ValidationNistReturn.validateNistTestReturns(null, currentTestInstanceParticipants, loginUser.getInstitution());
                } else {
                    statusToUse = null;
                }

                if (statusToUse != null) {
                    Status lastStatus = statusToUse;
                    currentTestInstance.setPreCatLastStatus(lastStatus, currentTestInstanceParticipants);
                }
            } catch (NumberFormatException e) {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Your uploaded file is not valid, invalid test number.");
            }
        }

        currentTestInstance = entityManager.merge(currentTestInstance);
        currentTestInstanceParticipants.setTestInstance(currentTestInstance);

        this.mergeCurrentTestInstanceParticipants();
        if (currentTestInstanceParticipants.getId() != null) {
            this.sendMailToNotifyFailing(this.selectedStatusForEdition, llastStatus, 2);
        }
        currentTestInstanceParticipants = entityManager.merge(currentTestInstanceParticipants);
        entityManager.flush();
        this.addMessageCommentIndicatorToPreCATTestCreatorIfNeeded();
    }

    private void setSystemInSessionUser(EntityManager entityManager) {
        if (currentTestInstanceParticipants.getSystemInSessionUser() == null) {

            SystemInSessionUser systemInSessionUser = SystemInSessionUser.getSystemInSessionUserBySystemInSessionByUser(selectedSystemInSession,
                    selectedSystemInSession.getSystem().getOwnerUser());

            if (systemInSessionUser == null) {
                systemInSessionUser = new SystemInSessionUser();
                systemInSessionUser.setSystemInSession(selectedSystemInSession);
                systemInSessionUser.setUser(selectedSystemInSession.getSystem().getOwnerUser());

                systemInSessionUser = entityManager.merge(systemInSessionUser);
            }
            currentTestInstanceParticipants.setSystemInSessionUser(systemInSessionUser);
        }
    }

    @Override
    public void persistStatus(TestInstance ti) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistStatus");
        }

        TestInstance currentTestInstance = currentTestInstanceParticipants.getTestInstance();

        if (selectedStatusForEdition != null) {
            Status lastStatus = null;
            if (ti.getLastStatus() == null) {
                lastStatus = Status.getSTATUS_STARTED();
                ti.setPreCatLastStatus(lastStatus, currentTestInstanceParticipants);
            } else {
                lastStatus = ti.getLastStatus();
            }

            if (!lastStatus.getKeyword().equals(selectedStatusForEdition.getKeyword())) {
                ti.setPreCatLastStatus(selectedStatusForEdition, currentTestInstanceParticipants);
                EntityManager entityManager = EntityManagerService.provideEntityManager();
                entityManager.merge(ti);

                entityManager.flush();
            }
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Status is null");
        }

        EntityManager entityManager = EntityManagerService.provideEntityManager();
        currentTestInstance = entityManager.merge(currentTestInstance);
        currentTestInstanceParticipants.setTestInstance(currentTestInstance);
        this.mergeCurrentTestInstanceParticipants();
        currentTestInstanceParticipants = entityManager.merge(currentTestInstanceParticipants);
        entityManager.flush();

        this.addMessageCommentIndicatorToPreCATTestCreatorIfNeeded();
        this.initEditCurrentTestInstanceParticipant();
    }

    @Override
    public String persistFiles(Status s) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistFiles");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        User loginUser = User.loggedInUser();
        TestInstance currentTestInstance = null;
        if (this.currentTestInstanceParticipants != null && this.currentTestInstanceParticipants.getTestInstance() != null) {
            currentTestInstance = currentTestInstanceParticipants.getTestInstance();

        }
        Status llastStatus = null;
        if (currentTestInstance != null) {
            llastStatus = currentTestInstance.getLastStatus();
        }

        String gazelleLogReturnPath = ApplicationManager.instance().getGazelleLogReturnPath();

        if (currentTestInstance != null && currentTestInstance.getId() == null) {
            currentTestInstance = entityManager.merge(currentTestInstance);
        }

        String directoryPathForThisInstance = null;
        java.io.File newDirectoryForInstance = null;
        if (currentTestInstance != null) {
            directoryPathForThisInstance = gazelleLogReturnPath + java.io.File.separatorChar + currentTestInstance.getId();
            newDirectoryForInstance = new java.io.File(directoryPathForThisInstance);
        }


        if (newDirectoryForInstance != null && newDirectoryForInstance.mkdirs()) {
            // Creating a file to LOG some information
            try {
                String fileInfoPath = directoryPathForThisInstance + java.io.File.separatorChar + "instance.info";
                java.io.File fileInfo = new java.io.File(fileInfoPath);

                // Crash when admin !!!!!!!!!!!!! To verify
                FileOutputStream fost = new FileOutputStream(fileInfo);

                StringBuilder sb = new StringBuilder();
                sb.append("Instance id : ").append(currentTestInstance.getId()).append("<br />");
                if (this.currentTestInstanceParticipants != null) {
                    if (this.currentTestInstanceParticipants.getTestInstance() != null) {
                        if (this.currentTestInstanceParticipants.getTestInstance().getTest() != null) {
                            sb.append("For test : ").append(this.currentTestInstanceParticipants.getTestInstance().getTest().getKeyword()).append
                                    (MINUS).append(this.currentTestInstanceParticipants.getTestInstance().getTest().getName()).append("<br />");
                        }
                    }
                }
                if (selectedSystem != null) {
                    sb.append("For System : ").append(selectedSystem.getKeyword()).append(MINUS).append(selectedSystem.getName()).append("<br />");
                    sb.append("For company : ").append(System.getInstitutionsForASystem(selectedSystem).get(0).getName());
                }

                fost.write(sb.toString().getBytes(StandardCharsets.UTF_8.name()));
                fost.close();
            } catch (FileNotFoundException e) {
                ExceptionLogging.logException(e, LOG);
            } catch (IOException e) {
                ExceptionLogging.logException(e, LOG);
            }
        }

        if (currentTestInstance != null && currentTestInstance.getFileLogPathReturn() == null) {
            currentTestInstance.setFileLogPathReturn(getFiles());
            currentTestInstance = entityManager.merge(currentTestInstance);
        }

        if (selectedStatusForEdition != null) {
            Status lastStatus = null;
            if (currentTestInstance.getLastStatus() == null) {
                lastStatus = Status.getSTATUS_STARTED();
                currentTestInstance.setPreCatLastStatus(lastStatus, currentTestInstanceParticipants);
            } else {
                lastStatus = currentTestInstance.getLastStatus();
            }

            if (!lastStatus.getKeyword().equals(selectedStatusForEdition.getKeyword())) {
                currentTestInstance.setPreCatLastStatus(selectedStatusForEdition, currentTestInstanceParticipants);
            }
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Status is null");
            return MESA_PAGE_LOG_RETURN;
        }

        setSystemInSessionUser(entityManager);

        // Adding now a validation only if non admin

        if ((!Role.isLoggedUserAdmin() && (Role.isLoggedUserVendorAdmin() || Role.isLoggedUserUser())) && (currentTestInstance.getLastStatus() !=
                null)
                && !currentTestInstance.getLastStatus().equals(Status.getSTATUS_STARTED()) && (files != null) && (files.size() > 0)) {

            String currentTestKeyword = currentTestInstanceParticipants.getTestInstance().getTest().getKeyword();

            currentTestInstanceParticipants.setTestInstance(currentTestInstance);

            try {
                Integer currentTestNumber = null;
                try {
                    currentTestNumber = Integer.valueOf(currentTestKeyword);
                } catch (Exception e) {
                }

                Status statusToUse;
                if (currentTestNumber != null && (((ValidationNistReturn.XDS_RANGE_LOW1 <= currentTestNumber) && (currentTestNumber <=
                        ValidationNistReturn.XDS_RANGE_HIGH1))
                        || ((ValidationNistReturn.XDS_RANGE_LOW2 <= currentTestNumber) && (currentTestNumber <= ValidationNistReturn
                        .XDS_RANGE_HIGH2)))) {
                    // USE WS NIST XDS VALIDATOR
                    statusToUse = ValidationNistReturn.validateNistTestReturns(null, currentTestInstanceParticipants, loginUser.getInstitution());
                } else {
                    statusToUse = null;
                }

                if (statusToUse != null) {
                    Status lastStatus = statusToUse;
                    currentTestInstance.setPreCatLastStatus(lastStatus, currentTestInstanceParticipants);
                }
            } catch (NumberFormatException e) {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Your uploaded file is not valid, invalid test number.");
            }
        }

        currentTestInstance = entityManager.merge(currentTestInstance);
        currentTestInstanceParticipants.setTestInstance(currentTestInstance);

        this.mergeCurrentTestInstanceParticipants();
        if (currentTestInstanceParticipants.getId() != null) {
            this.sendMailToNotifyFailing(this.selectedStatusForEdition, llastStatus, 2);
        }
        currentTestInstanceParticipants = entityManager.merge(currentTestInstanceParticipants);
        entityManager.flush();
        this.addMessageCommentIndicatorToPreCATTestCreatorIfNeeded();
        return previousPage;
    }

    @Override
    public int getUploadsAvailable() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUploadsAvailable");
        }
        return MAX_UPLOADS - this.getSize();
    }

    @Override
    public int getSize() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSize");
        }
        return getFiles().size();
    }

    @Override
    public String clearUploadData() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("clearUploadData");
        }

        TestInstanceManager tim = new TestInstanceManager();

        EntityManager entityManager = EntityManagerService.provideEntityManager();
        getFiles().clear();
        TestInstance currentTestInstance = currentTestInstanceParticipants.getTestInstance();
        if (currentTestInstance.getFileLogPathReturn() != null) {
            int size = currentTestInstance.getFileLogPathReturn().size();
            while (currentTestInstance.getFileLogPathReturn().size() > 0) {
                TestInstancePathToLogFile tiptlf = currentTestInstance.getFileLogPathReturn().get(0);
                currentTestInstance.getFileLogPathReturn().remove(tiptlf);
                tiptlf = entityManager.find(TestInstancePathToLogFile.class, tiptlf.getId());
                entityManager.remove(tiptlf);
                String fullAbsolutePath = ApplicationManager.instance().getGazelleLogReturnPath() + java.io.File.separatorChar + tiptlf.getPath();
                java.io.File f = new java.io.File(fullAbsolutePath);
                if (f.delete()) {
                    LOG.info("f deleted");
                } else {
                    LOG.error("Failed to delete f");
                }
                if (size == 1) {
                    // Add a message to say to user that a file has been uploaded
                    tim.removeFile(getFiles(), currentTestInstanceParticipants, selectedSystem, selectedSystemInSession, selectedStatusForEdition,
                            tiptlf.getFilename() + " has been deleted");
                } else {
                    tim.removeFile(getFiles(), currentTestInstanceParticipants, selectedSystem, selectedSystemInSession, selectedStatusForEdition,
                            "All files have been deleted");
                }
            }
        }
        currentTestInstance = entityManager.merge(currentTestInstance);
        currentTestInstanceParticipants.setTestInstance(currentTestInstance);
        this.mergeCurrentTestInstanceParticipants();

        return null;
    }

    @Override
    public void deleteFile(TestInstancePathToLogFile f) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteFile");
        }
        if (f.getFile().delete()) {
            LOG.info("f deleted");
        } else {
            LOG.error("Failed to delete f");
        }
        getFiles().remove(f);

        TestInstance currentTestInstance = currentTestInstanceParticipants.getTestInstance();

        EntityManager entityManager = EntityManagerService.provideEntityManager();
        List<TestInstancePathToLogFile> fileLogPathReturn = currentTestInstance.getFileLogPathReturn();
        if (fileLogPathReturn != null) {
            fileLogPathReturn.remove(f);
        }

        // Add a message to say to user that a file has been uploaded
        TestInstanceManager tim = new TestInstanceManager();
        tim.removeFile(getFiles(), currentTestInstanceParticipants, selectedSystem, selectedSystemInSession, selectedStatusForEdition, f
                .getFilename() + " has been deleted");

        currentTestInstance = entityManager.merge(currentTestInstance);
        currentTestInstanceParticipants.setTestInstance(currentTestInstance);
        this.mergeCurrentTestInstanceParticipants();
    }

    @Override
    public boolean isFileExist(TestInstanceParticipants tip, TestInstancePathToLogFile path) {
        TestInstance currentTestInstance = tip.getTestInstance();
        File newDirectoryForInstance = new File(ApplicationManager.instance().getGazelleLogReturnPath() + File.separatorChar + currentTestInstance
                .getId());

        StringBuilder filePath = new StringBuilder(newDirectoryForInstance.getAbsolutePath());
        filePath.append(File.separatorChar);
        filePath.append(path.getId());

        File f = new File(filePath.toString());
        if (f.exists() && !f.isDirectory()) {
            return true;
        }
        return false;
    }

    @Transactional
    public void uploadedFile(java.io.File tmpFile, String filename, String id, String param) throws IOException {
        if (getFiles().size() >= MAX_UPLOADS) {
            throw new IOException("Too many files uploaded");
        }

        // create directory
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        TestInstance currentTestInstance = currentTestInstanceParticipants.getTestInstance();
        if (currentTestInstance.getId() == null) {
            currentTestInstance = entityManager.merge(currentTestInstance);
        }
        java.io.File newDirectoryForInstance = new java.io.File(ApplicationManager.instance().getGazelleLogReturnPath() + java.io.File
                .separatorChar + currentTestInstance.getId());
        if (newDirectoryForInstance.mkdirs()) {
            LOG.info("newDirectoryForInstance directory created");
        } else {
            LOG.error("Failed to create newDirectoryForInstance");
        }

        // create path
        TestInstancePathToLogFile testInstancePathToLogFile = new TestInstancePathToLogFile();
        testInstancePathToLogFile.setWithoutFileName(true);
        testInstancePathToLogFile.setFilename(filename);
        testInstancePathToLogFile.setType(testInstancePathToLogFile.getType());
        testInstancePathToLogFile.setPath(UUID.randomUUID().toString());

        // First save instance
        testInstancePathToLogFile = entityManager.merge(testInstancePathToLogFile);

        // set properties using new id
        String pathOfFile = currentTestInstance.getId().toString() + java.io.File.separatorChar + testInstancePathToLogFile.getId();
        testInstancePathToLogFile.setPath(pathOfFile);

        // copy file
        java.io.File completeFile = testInstancePathToLogFile.getFile();
        FileUtils.copyFile(tmpFile, completeFile);

        testInstancePathToLogFile = entityManager.merge(testInstancePathToLogFile);

        getFiles().add(testInstancePathToLogFile);

        if (tmpFile.delete()) {
            LOG.info("tmpFile deleted");
        } else {
            LOG.error("Failed to delete tmpFile");
        }

        // Add a message to say to user that a file has been uploaded
        TestInstanceManager tim = new TestInstanceManager();
        tim.addFile(getFiles(), currentTestInstanceParticipants, selectedSystem, selectedSystemInSession, selectedStatusForEdition, filename + " " +
                "has been uploaded");
    }

    @Override
    public void initializeSelectedInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeSelectedInstitution");
        }
        if (this.selectedInstitution == null) {

            User curr = User.loggedInUser();
            if (!(curr.getRoles().contains(Role.getADMINISTRATOR_ROLE()) || curr.getRoles().contains(Role.getMONITOR_ROLE()))) {
                this.selectedInstitution = Institution.getLoggedInInstitution();
            }
        }
    }

    @Override
    public ActorIntegrationProfileOption getActorIntegrationProfileOptionByRoleInTestBySystemInSession(SystemInSession inSystemInSession,
                                                                                                       RoleInTest inRoleInTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorIntegrationProfileOptionByRoleInTestBySystemInSession");
        }
        if ((inSystemInSession != null) && (inRoleInTest != null)) {
            List<ActorIntegrationProfileOption> actorIntegrationProfileOptions = TestRoles
                    .getActorIntegrationProfileOptionByRoleInTestBySystemInSession(inSystemInSession, inRoleInTest);
            if (actorIntegrationProfileOptions == null) {
                actorIntegrationProfileOptions = TestRoles.getActorIntegrationProfileOptionByRoleInTestBySystemInSessionForAllTestParticipants
                        (inSystemInSession, inRoleInTest);
            }
            if (actorIntegrationProfileOptions != null) {
                if (actorIntegrationProfileOptions.size() == 1) {
                    return actorIntegrationProfileOptions.get(0);
                }
                for (ActorIntegrationProfileOption actorIntegrationProfileOption : actorIntegrationProfileOptions) {
                    if (actorIntegrationProfileOption.getIntegrationProfileOption().equals(IntegrationProfileOption.getNoneOption())) {
                        return actorIntegrationProfileOption;
                    }
                }
                return actorIntegrationProfileOptions.get(0);
            }
        }
        return null;
    }

    @Override
    public void updateComment() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateComment");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        entityManager.merge(this.currentTestInstanceParticipants.getTestInstance());
        entityManager.flush();
    }

    @Override
    public String getTestInstanceDescription() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestInstanceDescription");
        }
        String res = "";
        if (this.currentTestInstanceParticipants != null) {
            if (this.currentTestInstanceParticipants.getTestInstance() != null) {
                res = this.currentTestInstanceParticipants.getTestInstance().getDescription();
                res = StringEscapeUtils.escapeHtml(res);
                res = res.replaceAll("\n", "<br />");
            }
        }
        return res;
    }

    @Override
    public void merge(TestInstance testInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("merge");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        entityManager.merge(testInstance);
        entityManager.flush();
    }

    @Override
    public void mergeStatus(TestInstance testInstance, Status currentStatus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("mergeStatus");
        }

        EntityManager entityManager = EntityManagerService.provideEntityManager();
        entityManager.merge(testInstance);
        entityManager.flush();

        // if status is passed to failed, send email to notfy
        this.sendMailToNotifyFailing(currentStatus, Status.STARTED, 2);
    }

    @Override
    public void mergeCurrentTestInstanceParticipants() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("mergeCurrentTestInstanceParticipants");
        }
        if (this.currentTestInstanceParticipants != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            currentTestInstanceParticipants = entityManager.merge(currentTestInstanceParticipants);
            entityManager.flush();
        }
    }

    @Override
    public void initialize() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initialize");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String pageid = params.get("id");
        this.currentTestInstanceParticipants = null;
        if (pageid != null) {
            if (!pageid.isEmpty()) {
                try {
                    int id = Integer.valueOf(pageid);
                    this.currentTestInstanceParticipants = entityManager.find(TestInstanceParticipants.class, id);
                    previousPage = MESA_PAGE_VALIDATE_MESA_TEST;
                    editTestInstanceParticipantForMesaTests(this.currentTestInstanceParticipants);
                } catch (NumberFormatException e) {

                    FacesMessages.instance().add(StatusMessage.Severity.ERROR, "#{messages['gazelle.testmanagement.object.ProblemIdFormat']}");
                }
            }
        }
    }

    @Override
    public void actionOnTypeRejected() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("actionOnTypeRejected");
        }
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "#{messages['gazelle.tm.mesatest.filetoupload']}");
    }

    @Override
    public String getListIPOption(TestRoles currentTestRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListIPOption");
        }
        SortedSet<String> options = new TreeSet<String>();
        if (currentTestRoles != null) {
            if (currentTestRoles.getRoleInTest() != null) {
                if (currentTestRoles.getRoleInTest().getTestParticipantsList() != null) {
                    for (TestParticipants tp : currentTestRoles.getRoleInTest().getTestParticipantsList()) {
                        if (tp.getActorIntegrationProfileOption() != null) {
                            options.add(tp.getActorIntegrationProfileOption().getIntegrationProfileOption().getKeyword());
                        }
                    }
                }
            }
        }
        StringBuilder result = new StringBuilder("");
        for (String option : options) {
            if (result.length() > 0) {
                result.append(" / ");
            }
            result.append(option);
        }
        return result.toString();
    }

    @Override
    public boolean isEnableSendMail() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isEnableSendMail");
        }
        return ApplicationPreferenceManager.getBooleanValue(MESA_SEND_MAIL);
    }

    @Override
    public boolean isEnableAutomaticValidation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isEnableAutomaticValidation");
        }
        return ApplicationPreferenceManager.getBooleanValue(MESA_AUTOMATIC_VALIDATION);
    }

    @Override
    public TestParticipants getBestBestParticipant(List<TestParticipants> tpl) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getBestBestParticipant");
        }
        for (TestParticipants tp : tpl) {
            if (tp.getTested() != null) {
                if (tp.getTested().booleanValue() == true) {
                    return tp;
                }
            }
        }
        return tpl.get(0);
    }

    @Override
    public String getPermanentLink() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPermanentLink");
        }
        if ((this.currentTestInstanceParticipants != null) && (this.currentTestInstanceParticipants.getId() != null)) {
            String res = ApplicationPreferenceManager.instance().getApplicationUrl() + "mesaTestInstance.seam?id=" + this
                    .currentTestInstanceParticipants.getId();
            return res;
        }
        return null;
    }

    @Create
    @Override
    public void initEditCurrentTestInstanceParticipant() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initEditCurrentTestInstanceParticipant");
        }
        this.oldComments = null;
    }

    @Deprecated
    private void addMessageCommentIndicatorToPreCATTestCreatorIfNeeded() {
        if (this.oldComments == null) {
            return;
        }
        if ((this.currentTestInstanceParticipants != null) && (this.currentTestInstanceParticipants.getTestInstance() != null)) {
            if (!this.oldComments.equals(this.currentTestInstanceParticipants.getTestInstance().getDescription())) {
                MessageManager.addMessage(TestInstanceParticipantCommentMessageSource.INSTANCE, this.currentTestInstanceParticipants, this
                        .currentTestInstanceParticipants.getId().toString());
            }
        }
        this.oldComments = null;
    }

    @Override
    public List<ActorIntegrationProfileOption> getAiposForTestRole(TestRoles testRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAiposForTestRole");
        }
        AIPOQuery query = new AIPOQuery();
        query.testParticipants().roleInTest().testRoles().id().eq(testRoles.getId());
        if (selectedSystem != null) {
            query.systemActorProfiles().system().eq(selectedSystem);
        }
        query.testParticipants().tested().eq(true);
        List<AIPO> list = query.getList();
        if (list.size() > 0) {
            List<Integer> ids = new ArrayList<Integer>();
            for (AIPO aipo : list) {
                ids.add(aipo.getId());
            }
            ActorIntegrationProfileOptionQuery query2 = new ActorIntegrationProfileOptionQuery();
            query2.id().in(ids);
            return query2.getList();
        } else {
            return null;
        }
    }

    @Override
    public String getTestRoleStatus(TestRoles testRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestRoleStatus");
        }
        if (instanceParticipantExists(testRoles)) {
            TestInstanceParticipants tip = getSingleInstanceOfTestParticipants(testRoles);
            return tip.getTestInstance().getLastStatus().getLabelToDisplay();
        } else {
            return "gazelle.tm.NoLogReturnedYet";
        }
    }

    @Override
    public List<Status> getPossibleStatusesWithStatus(Status status) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleStatusesWithStatus");
        }
        List<Status> result = getPossibleStatuses();
        if (status != null) {
            if (!result.contains(status)) {
                result.add(status);
            }
        }
        return result;
    }

    @Override
    public List<Status> getPossibleStatuses() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleStatuses");
        }
        List<Status> statuses = new ArrayList<Status>();
        statuses.add(Status.STARTED);
        statuses.add(Status.SELF_VERIFIED);
        statuses.add(Status.COMPLETED_ERRORS);
        statuses.add(Status.SUPPORTIVE);
        if (Identity.instance().hasRole("admin_role") || Identity.instance().hasRole("monitor_role")) {
            statuses.add(Status.COMPLETED);
            statuses.add(Status.VERIFIED);
            statuses.add(Status.PARTIALLY_VERIFIED);
            statuses.add(Status.FAILED);
        }
        return statuses;
    }

    @Override
    public String saveStatus(Status status) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveStatus");
        }
        TestInstance testInstance = currentTestInstanceParticipants.getTestInstance();
        if (testInstance != null) {
            selectedStatusForEdition = status;
            testInstance.setPreCatLastStatus(status, currentTestInstanceParticipants);
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            entityManager.merge(testInstance);
            entityManager.flush();
            return MESA_PAGE_LOG_RETURN;
        }
        return null;
    }

    @Override
    public List<TestStatusStatus> getInstanceStatuses() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstanceStatuses");
        }
        List<TestStatusStatus> result = new ArrayList<TestStatusStatus>();
        result.add(TestStatusStatus.TO_COMPLETE_BY_VENDOR);
        result.add(TestStatusStatus.COMPLETED);
        result.add(TestStatusStatus.SUPPORTIVE);
        result.add(TestStatusStatus.COMPLETED_ERRORS);
        result.add(TestStatusStatus.VERIFIED);
        return result;
    }

    @Override
    public void showFile(TestInstancePathToLogFile testInstancePathToLogFile, boolean download) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showFile");
        }
        DocumentFileUploadLocal documentFileUpload = (DocumentFileUploadLocal) Component.getInstance("documentFileUpload");
        documentFileUpload.showFile(testInstancePathToLogFile);
    }

    @Override
    public String getURLValue(TestInstancePathToLogFile testInstancePathToLogFile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getURLValue");
        }
        try {
            return URIUtil.encodePath(testInstancePathToLogFile.getName());
        } catch (URIException e) {
            return "file";
        }
    }

    @Override
    public List<String> getDocumentationByTest(Test test) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDocumentationByTest");
        }
        if (test != null) {
            test = HQLReloader.reloadDetached(test);
            List<TestRoles> testRoles = test.getTestRoles();
            Set<String> urls = new TreeSet<String>();
            if (testRoles != null) {
                for (TestRoles testRole : testRoles) {
                    String urlDocumentation = testRole.getUrlDocumentation();
                    if (StringUtils.trimToNull(urlDocumentation) != null) {
                        urls.add(urlDocumentation);
                    }
                }
            }
            return new ArrayList<String>(urls);
        }
        return null;
    }

    @Override
    public List<String> getDocumentation(TestInstanceParticipants tip) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDocumentation");
        }
        if (tip != null) {
            tip = HQLReloader.reloadDetached(tip);
            TestInstance testInstance = tip.getTestInstance();
            if (testInstance != null) {
                Test test = testInstance.getTest();
                if (test != null) {
                    return getDocumentationByTest(test);
                }
            }
        }
        return null;
    }
}
