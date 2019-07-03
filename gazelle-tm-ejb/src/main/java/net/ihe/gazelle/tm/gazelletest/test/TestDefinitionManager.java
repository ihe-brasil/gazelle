package net.ihe.gazelle.tm.gazelletest.test;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.log.ExceptionLogging;
import net.ihe.gazelle.common.report.ReportExporterManager;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.jira.issue.create.JiraClientCallback;
import net.ihe.gazelle.jira.issue.create.JiraManager;
import net.ihe.gazelle.tm.datamodel.TestDataModel;
import net.ihe.gazelle.tm.gazelletest.model.definition.*;
import net.ihe.gazelle.tm.tee.model.MessageValidationService;
import net.ihe.gazelle.tm.tee.model.TmTestStepMessageProfile;
import net.ihe.gazelle.users.model.Role;
import net.ihe.gazelle.users.model.User;
import net.sf.jasperreports.engine.JRException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.LocaleSelector;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.security.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Name("testDefinitionManager")
@Scope(ScopeType.PAGE)
@Synchronized(timeout = 10000)
@GenerateInterface(value = "TestDefinitionManagerLocal")
public class TestDefinitionManager implements TestDefinitionManagerLocal, JiraClientCallback, Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(TestDefinitionManager.class);
    public static final String HISTORY = "History";
    private static String REPORT_TEST_NAME = "gazelleTestReport";
    private UserComment userComment;
    private Test selectedTest = null;
    private boolean displayJiraPanel = false;
    private JiraManager jiraManager;

    public UserComment getUserComment() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUserComment");
        }
        if (userComment == null) {
            userComment = new UserComment("");
        }
        return userComment;
    }

    public void setUserComment(UserComment userComment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setUserComment");
        }
        this.userComment = userComment;
    }

    public Test getSelectedTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTest");
        }
        if (selectedTest == null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            String testId = fc.getExternalContext().getRequestParameterMap().get("id");

            if (testId != null) {
                EntityManager em = EntityManagerService.provideEntityManager();
                setSelectedTest(em.find(Test.class, Integer.valueOf(testId)));
            }
        }
        return selectedTest;
    }

    public void setSelectedTest(Test selectedTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTest");
        }
        this.selectedTest = selectedTest;
    }

    public List<TestSteps> getTestStepsList() {
        List<TestSteps> res = null;
        if (getSelectedTest() != null && getSelectedTest().getTestStepsList() != null) {
            res = getSelectedTest().getTestStepsList();
            if (res != null) {
                Collections.sort(res);
            }
        }
        return res;
    }

    public void reportJira() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("reportJira");
        }
        jiraManager = new JiraManager(this);
        displayJiraPanel = true;
    }

    @Factory("isLoggedUserAllowedToEditComment")
    public boolean isLoggedUserAllowedToEditComment(UserComment inUserComment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isLoggedUserAllowedToEditComment");
        }
        return !inUserComment.getUser().equals(HISTORY) && (Role.isLoggedUserAdmin() || inUserComment.getUser().equals(User
                .loggedInUser().getUsername()));
    }

    public void addComment(int testId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addComment");
        }

        EntityManager em = EntityManagerService.provideEntityManager();
        Test test = em.find(Test.class, testId);
        if (test != null) {
            if (!("").equals(userComment.getCommentContent())) {
                saveComment(em);
                linkCommentToTest(em, test);
                em.flush();
                userComment = new UserComment("");
            }
        }
        Contexts.getSessionContext().set("selectedTest", em.find(Test.class, testId));
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "Comment added.");
    }

    public void linkCommentToTest(EntityManager em, Test test) {
        List<UserComment> userCommentList = test.getUserCommentList();
        if (userCommentList == null) {
            userCommentList = new ArrayList<UserComment>();
        }
        userCommentList.add(userComment);
        test.setUserCommentList(userCommentList);
        selectedTest = em.merge(test);
    }

    private void saveComment(EntityManager em) {
        userComment.setUser(User.loggedInUser().getUsername());
        userComment.setCreationDate(new Date());
        userComment = em.merge(userComment);
        em.flush();
    }

    public void updateuserComment(int testId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateuserComment");
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        Test test = em.find(Test.class, testId);
        if (test != null) {
            if (userComment != null) {
                em.merge(userComment);
                userComment = new UserComment("");
            }
        }
    }

    public void editUserComment(UserComment inUserComment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editUserComment");
        }
        userComment = inUserComment;
    }

    public void deleteUserComment(UserComment inUserComment, int testId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteUserComment");
        }

        EntityManager em = EntityManagerService.provideEntityManager();
        Test test = em.find(Test.class, testId);
        if (!inUserComment.getUser().equals(HISTORY)) {
            List<UserComment> userCommentList = test.getUserCommentList();
            userCommentList.remove(inUserComment);
            test.setUserCommentList(userCommentList);
            selectedTest = em.merge(test);
            try {
                UserComment.deleteUserCommentWithFind(inUserComment);
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "Comment deleted");
            } catch (Exception e) {
                LOG.error("", e);
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Failed to delete comment");
            }
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "History comment can't be removed !");
        }
    }

    @Deprecated
    /*Use h:outputLink instead of calling this method with an action*/
    public String editTestSummary(int testId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editTestSummary");
        }

        String redirectTo = "/testing/testsDefinition/editTestSummary.xhtml?id=" + testId;
        return redirectTo;
    }

    @Deprecated
    /*Use h:outputLink instead of calling this method with an action*/
    public String editTestDescription(int testId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editTestDescription");
        }

        String redirectTo = "/testing/testsDefinition/editTestDescription.xhtml?id=" + testId;
        return redirectTo;
    }

    @Deprecated
    /*Use h:outputLink instead of calling this method with an action*/
    public String editTestDescription2(Test test) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editTestDescription2");
        }

        String redirectTo = "/testing/testsDefinition/editTestDescription.xhtml?id=" + test.getId() + "&testDescId=" + test
                .getContextualDescription().getId();
        return redirectTo;
    }

    @Deprecated
    /*Use h:outputLink instead of calling this method with an action*/
    public String editTestAssertions(int testId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editTestAssertions");
        }

        String redirectTo = "/testing/testsDefinition/editTestAssertions.xhtml?id=" + testId;
        return redirectTo;
    }

    @Deprecated
    /*Use h:outputLink instead of calling this method with an action*/
    public String editTestRoles(int testId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editTestRoles");
        }

        String redirectTo = "/testing/testsDefinition/editTestRoles.xhtml?id=" + testId;
        return redirectTo;
    }

    @Deprecated
    /*Use h:outputLink instead of calling this method with an action*/
    public String editTestSteps(int testId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editTestSteps");
        }

        String redirectTo = "/testing/testsDefinition/editTestSteps.xhtml?id=" + testId;
        return redirectTo;
    }

    @Deprecated
    /*Use h:outputLink instead of calling this method with an action*/
    public String editNewTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editNewTest");
        }

        String redirectTo = "/testing/testsDefinition/editTestSummary.xhtml?new=1";
        return redirectTo;
    }

    @Deprecated
    /*Use h:outputLink instead of calling this method with an action*/
    public String viewTest(int testId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewTest");
        }
        String redirectTo = "/testing/testsDefinition/viewTestPage.xhtml?id=" + testId;
        return redirectTo;
    }

    public String getTestPermalink(Test inTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestPermalink");
        }
        if (inTest != null) {
            return ApplicationPreferenceManager.instance().getApplicationUrl() + "test.seam?id=" + inTest.getId();
        }
        return null;
    }

    public String getTestPermalinkToGMM(Test inTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestPermalinkToGMM");
        }
        if (inTest != null) {
            String gmmUrl = ApplicationPreferenceManager.getStringValue("gazelle_master_model");
            return gmmUrl + "/test.seam?id=" + inTest.getId();
        }
        return null;
    }

    public boolean tmIsGmm() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("tmIsGmm");
        }
        return ApplicationPreferenceManager.getBooleanValue("is_master_model");
    }

    public int testsCount(TestDataModel tests) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("testsCount");
        }
        @SuppressWarnings("unchecked")
        List<Test> testList = (List<Test>) tests.getAllItems(FacesContext.getCurrentInstance());
        return testList.size();
    }

    public void updateSelectedUserComment() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateSelectedUserComment");
        }
        if (selectedTest != null) {
            if (userComment != null) {
                EntityManager em = EntityManagerService.provideEntityManager();
                em.merge(userComment);
                userComment = new UserComment();
                userComment.setCommentContent("");
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "Comment updated");
            }
        }
    }

    public void downloadTestAsPdf(Integer testId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("downloadTestAsPdf");
        }
        try {
            if (testId != null) {

                EntityManager em = EntityManagerService.provideEntityManager();
                Test inTest = em.find(Test.class, testId);
                if (inTest != null) {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("test_keyword", inTest.getKeyword());

                    // get the application url to use it as parameter for the report
                    String applicationurl = ApplicationPreferenceManager.instance().getApplicationUrl();
                    params.put("applicationurl", applicationurl);

                    // get the testDescription Language to use it as parameter for the report
                    Map<Integer, String> res = new HashMap<>();
                    Test test = em.find(Test.class, testId);
                    List<TestDescription> descriptionList = test.getTestDescription();
                    for (TestDescription testDescription : descriptionList) {
                        res.put(testDescription.getGazelleLanguage().getId(), testDescription.getGazelleLanguage().getDescription().toLowerCase());
                    }
                    if (!res.isEmpty()) {
                        String language = LocaleSelector.instance().getLanguage();
                        LOG.info("Selected language : " + language);
                        for (Map.Entry<Integer, String> integerStringEntry : res.entrySet()) {
                            if (integerStringEntry.getValue().equalsIgnoreCase(language)) {
                                params.put("testDescriptionLanguageId", integerStringEntry.getKey());
                            }
                        }
                    }

                    ReportExporterManager.exportToPDF(REPORT_TEST_NAME, inTest.getKeyword().replace(" ", "_") + ".pdf", params);
                } else {
                    LOG.error("downloadTestAsPdf: Cannot find test id=" + testId);
                }
            } else {
                LOG.error("downloadTestAsPdf: called with null testId");
            }
        } catch (JRException e) {
            ExceptionLogging.logException(e, LOG);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, e.getMessage());
        }
    }

    public void redirectToTestPage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("redirectToTestPage");
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        Test test = null;
        String id = fc.getExternalContext().getRequestParameterMap().get("id");
        Redirect redirect = Redirect.instance();

        if (id != null) {
            Pattern regex = Pattern.compile("^[0-9]*$", Pattern.DOTALL);
            Matcher regexMatcher = regex.matcher(id);
            if (!regexMatcher.find()) {
                test = Test.getTestByKeyword(id);
                if (test != null) {
                    id = test.getId().toString();
                }
            }
            redirect.setParameter("id", id);

            EntityManager em = EntityManagerService.provideEntityManager();
            if (test == null) {
                test = em.find(Test.class, Integer.valueOf(id));
            }
            redirect.setViewId("/" + test.viewFolder() + "/testsDefinition/viewTestPage.xhtml");
        }
        redirect.execute();
    }

    public void copySelectedTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("copySelectedTest");
        }
        if (selectedTest != null) {
            copyTest(selectedTest);
        }
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public void copyTest(Test inTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("copyTest");
        }

        if ((inTest != null) && (inTest.getId() != null)) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            Test test = new Test(inTest);
            Test testResult = Test.getTestByKeyword(test.getKeyword() + "_COPY");
            if (testResult != null) {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "A Test with keyword " + test.getKeyword() + "_COPY already exists in " +
                        "the database");
                return;
            }
            test.setKeyword(test.getKeyword() + "_COPY");
            test.setAuthor(User.loggedInUser().getUsername());
            test.setTestDescription(null);
            test.setTestStepsList(null);
            test.setUserCommentList(null);
            List<TestDescription> testDescriptionList = inTest.getTestDescription();
            if (testDescriptionList != null) {
                List<TestDescription> targetTestDescriptionList = new ArrayList<TestDescription>();
                for (TestDescription testDescription : testDescriptionList) {
                    TestDescription temporaryTestDescription = new TestDescription(testDescription);
                    temporaryTestDescription = entityManager.merge(temporaryTestDescription);
                    targetTestDescriptionList.add(temporaryTestDescription);
                }
                test.setTestDescription(targetTestDescriptionList);
            }
            test = entityManager.merge(test);
            entityManager.flush();
            List<TestRoles> testRolesList = TestRoles.getTestRolesListForATest(inTest);
            if (testRolesList != null) {
                for (TestRoles testRoles : testRolesList) {
                    TestRoles testRolestmp = new TestRoles(testRoles);
                    testRolestmp.setTest(test);
                    testRolestmp.setRoleInTest(testRoles.getRoleInTest());
                    testRolestmp = entityManager.merge(testRolestmp);
                }
            }

            List<TestSteps> testStepsList = inTest.getTestStepsList();
            if (testStepsList != null) {
                List<TestSteps> targetTestStepsList = new ArrayList<TestSteps>();
                testRolesList = TestRoles.getTestRolesListForATest(test);
                for (TestSteps testSteps : testStepsList) {
                    TestSteps testStepstmp = new TestSteps(testSteps);
                    for (TestRoles testRoles : testRolesList) {
                        if (testStepstmp.getTestRolesInitiator().getRoleInTest().getKeyword().equals(testRoles.getRoleInTest().getKeyword())) {
                            testStepstmp.setTestRolesInitiator(testRoles);
                        }
                        if (testStepstmp.getTestRolesResponder().getRoleInTest().getKeyword().equals(testRoles.getRoleInTest().getKeyword())) {
                            testStepstmp.setTestRolesResponder(testRoles);
                        }
                    }

                    for (ContextualInformation ci : testSteps.getInputContextualInformationList()) {
                        ContextualInformation citemp = null;
                        List<TestSteps> lts = TestSteps.getTestStepsFiltered(ci);
                        if (lts == null) {
                            lts = new ArrayList<TestSteps>();
                        }
                        if (lts.size() > 0) {
                            for (TestSteps oldTestSteps : targetTestStepsList) {
                                for (ContextualInformation oldci : oldTestSteps.getInputContextualInformationList()) {
                                    if (oldci.equals(ci)) {
                                        citemp = oldci;
                                    }
                                }
                                for (ContextualInformation oldci : oldTestSteps.getOutputContextualInformationList()) {
                                    if (oldci.equals(ci)) {
                                        citemp = oldci;
                                    }
                                }
                            }
                        } else {
                            citemp = new ContextualInformation(ci);
                            citemp = entityManager.merge(citemp);
                            entityManager.flush();
                        }
                        testStepstmp.addInputContextualInformation(citemp);
                    }

                    for (ContextualInformation ci : testSteps.getOutputContextualInformationList()) {
                        ContextualInformation citemp = new ContextualInformation(ci);
                        citemp = entityManager.merge(citemp);
                        entityManager.flush();
                        testStepstmp.addOutputContextualInformation(citemp);
                    }

                    List<TmTestStepMessageProfile> messageTypesToPersist = null;

                    if (testStepstmp.getTmTestStepMessageProfiles() != null) {
                        messageTypesToPersist = testStepstmp.getTmTestStepMessageProfiles();
                        testStepstmp.setTmTestStepMessageProfiles(new ArrayList<TmTestStepMessageProfile>());
                    }

                    testStepstmp = entityManager.merge(testStepstmp);

                    // as part of ITB, need to copy Message Type profile records as well
                    if (messageTypesToPersist != null) {

                        for (TmTestStepMessageProfile msgType : messageTypesToPersist) {

                            Set<MessageValidationService> msgValSrvsToPersist = msgType.getMessageValidationServices();
                            msgType.setMessageValidationServices(new HashSet<MessageValidationService>());
                            msgType.setTmTestStep(testStepstmp);
                            msgType = entityManager.merge(msgType);
                            entityManager.flush();
                            if ((msgValSrvsToPersist != null) && (msgValSrvsToPersist.size() > 0)) {
                                for (MessageValidationService srv : msgValSrvsToPersist) {
                                    srv.setTmTestStepMessageProfile(msgType);
                                    srv = entityManager.merge(srv);
                                    entityManager.flush();
                                    msgType.getMessageValidationServices().add(srv);
                                }
                            }

                            msgType = entityManager.merge(msgType);
                            entityManager.flush();
                            testStepstmp.getTmTestStepMessageProfiles().add(msgType);
                        }
                    }

                    testStepstmp = entityManager.merge(testStepstmp);
                    targetTestStepsList.add(testStepstmp);
                }

                test.setTestStepsList(targetTestStepsList);
            }

            entityManager.merge(test);
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "A Test with keyword " + inTest.getKeyword() + " was successfully copied");
        }
    }

    public void cancelJira() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelJira");
        }
        setDisplayJiraPanel(false);
    }

    public boolean isDisplayJiraPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isDisplayJiraPanel");
        }
        return displayJiraPanel;
    }

    public void setDisplayJiraPanel(boolean displayJiraPanel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDisplayJiraPanel");
        }
        this.displayJiraPanel = displayJiraPanel;
    }

    @Override
    public void issueCreatedCallback() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("issueCreatedCallback");
        }
        setDisplayJiraPanel(false);
    }

    public JiraManager getJiraManager() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getJiraManager");
        }
        return jiraManager;
    }

    public void setJiraManager(JiraManager jiraManager) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setJiraManager");
        }
        this.jiraManager = jiraManager;
    }

    @Override
    public boolean isPreConnectathonTest(Test test) {
        return test.getTestType().equals(TestType.getTYPE_MESA());
    }

    @Override
    public boolean isPreConnectathonTest(TestDataModel tests) {
        if (tests.getFilter() != null && tests.getFilter().getFilterValues() != null && tests.getFilter().getFilterValues().get("testType") != null) {
            List<Test> testList = (List<Test>) tests.getAllItems(FacesContext.getCurrentInstance());
            if (testList != null && !testList.isEmpty()) {
                return isPreConnectathonTest(testList.get(0));
            } else {
                return false;
            }

        }
        return true;
    }

    public boolean canViewSelectedTestAsNotLoggedIn() {
        return getSelectedTest() != null && (Identity.instance().isLoggedIn() || getSelectedTest().getTestStatus().getLabelToDisplay().equals
                (TestStatus.STATUS_READY_STRING));
    }
}
