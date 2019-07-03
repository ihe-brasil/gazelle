package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.log.ExceptionLogging;
import net.ihe.gazelle.common.report.ReportExporterManager;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.tf.model.Hl7MessageProfile;
import net.ihe.gazelle.tf.model.Transaction;
import net.ihe.gazelle.tf.model.WSTransactionUsage;
import net.ihe.gazelle.tm.datamodel.TestDataModel;
import net.ihe.gazelle.tm.filter.TMCriterions;
import net.ihe.gazelle.tm.gazelletest.bean.InfoCookies;
import net.ihe.gazelle.tm.gazelletest.model.definition.*;
import net.ihe.gazelle.tm.gazelletest.model.instance.ContextualInformationInstance;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestStepsInstance;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.tm.tee.dao.MessageValidationServiceDAO;
import net.ihe.gazelle.tm.tee.dao.ValidationServiceDAO;
import net.ihe.gazelle.tm.tee.model.MessageDirection;
import net.ihe.gazelle.tm.tee.model.MessageValidationService;
import net.ihe.gazelle.tm.tee.model.TmTestStepMessageProfile;
import net.ihe.gazelle.tm.tee.model.ValidationService;
import net.ihe.gazelle.users.model.Role;
import net.ihe.gazelle.users.model.User;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.lang.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Name("testsDefinitionsAdministrationManager")
@Scope(ScopeType.PAGE)
public class TestsDefinitionsAdministrationManager implements Serializable, QueryModifier<RoleInTest> {

    // CONSTANTS USED IN THIS CLASS
    public static final String TEST_STEP_ROLE = "testStepRole";
    public static final String CONFIG_FILE_TYPE = "configFileType";

    private static final long serialVersionUID = 4519128455039419256L;

    private static final String MESSAGE_EVENT_CODE_SEPARATOR = "_";
    private static final int DEFAULT_TEST_STEPS_INDEX_INCREMENTATION = 10;
    private static final int MAX_STEP_INDEX_VALUE = 500;
    private static final Logger LOG = LoggerFactory.getLogger(TestsDefinitionsAdministrationManager.class);
    // @Todo - change these constants to final as they can be modified
    // otherwise
    private static String REPORT_TEST_NAME = "gazelleTestReport";
    private static String MULTIPLE_REPORT_TEST_NAME = "gazelleMultipleTestReport";
    private static int MAX_PARTICIPANTS = 99;
    private List<TestPeerType> possibleTestPeerTypes;
    private List<TestStatus> testStatusList;
    private List<TestType> testTypeList;
    /**
     * List of all found tests (this variable is used in datatable)
     */
    private TestDataModel foundTests;
    private Test selectedTest;
    private String searchKeyword;
    private TestDescription selectedTestDescription;
    private TestDescription translatedTestDescription;
    private UserComment selectedUserComment;
    private boolean testDefinitonViewMode = false;

    // test steps
    private boolean testDescriptionEditionMode = true;
    private boolean testDescriptionTranslationMode = false;
    private List<TestRoles> possibleTestRoles;

    private boolean showTestStepsEditionPanel = false;

    private boolean autoResponse = false;

    private TestSteps selectedTestSteps;

    private ContextualInformation selectedContextualInformation;

    private ContextualInformation selectedContextualInformationOld;

    private List<ContextualInformation> listContextualInformationToRemove;

    private String contextualInformationtype;

    private int inputOrOutput = 1;

    @Deprecated
    private String selectedTabForTestDefinistionList;

    private TestRoles selectedTestRoles;
    private String roleInTestKeywordTyped;
    private FilterDataModel<RoleInTest> foundRoleInTests;

    private List<List<Test>> listOfTestLists;
    private Hl7MessageProfile selectedReqMsgType;
    private Hl7MessageProfile selectedRspMsgType;
    private List<TmTestStepMessageProfile> profilesToRemove;
    private List<TmTestStepMessageProfile> profilesToAdd;
    private List<MessageValidationService> validationServicesToRemove;
    private ValidationService selectedReqValidationService;
    private ValidationService selectedRspValidationService;
    private TSMPConfigFile initiatorValidationCtxFile;
    private TSMPConfigFile responderValidationCtxFile;
    private TSMPConfigFile initiatorCodeTableFile;
    private TSMPConfigFile responderCodeTableFile;
    private TSMPConfigFile initiatorExampleMsg;
    private TSMPConfigFile responderExampleMsg;
    private TSMPConfigFile dataSheetFile;
    private TSMPConfigFile initiatorValueSetsFile;
    private TSMPConfigFile responderValueSetsFile;
    private boolean mainTestPage;

    public List<List<Test>> getListOfTestLists() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfTestLists");
        }
        return listOfTestLists;
    }

    public void setListOfTestLists(List<List<Test>> listOfTestLists) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setListOfTestLists");
        }
        this.listOfTestLists = listOfTestLists;
    }

    public String getSearchKeyword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSearchKeyword");
        }
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSearchKeyword");
        }
        if (searchKeyword != null) {
            this.searchKeyword = searchKeyword.trim();
        } else {
            this.searchKeyword = "";
        }
        getFoundTests().setSearchedText(this.searchKeyword);
    }

    public TestDescription getSelectedTestDescription() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestDescription");
        }
        return selectedTestDescription;
    }

    public void setSelectedTestDescription(TestDescription inTestDescription) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestDescription");
        }
        this.selectedTestDescription = inTestDescription;
    }

    public Test getSelectedTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTest");
        }
        return selectedTest;
    }

    public void setSelectedTest(Test selectedTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTest");
        }
        this.selectedTest = selectedTest;
    }

    public List<TestStatus> getPossibleTestStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleTestStatus");
        }
        if (testStatusList == null) {
            testStatusList = TestStatus.getListStatus();
        }
        return testStatusList;
    }

    public boolean isTestDescriptionEditionMode() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isTestDescriptionEditionMode");
        }
        return testDescriptionEditionMode;
    }

    public void setTestDescriptionEditionMode(boolean testDescriptionEditionMode) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestDescriptionEditionMode");
        }
        this.testDescriptionEditionMode = testDescriptionEditionMode;
    }

    public TestDescription getTranslatedTestDescription() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTranslatedTestDescription");
        }
        return translatedTestDescription;
    }

    public void setTranslatedTestDescription(TestDescription translatedTestDescription) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTranslatedTestDescription");
        }
        this.translatedTestDescription = translatedTestDescription;
    }

    public boolean getTestDescriptionTranslationMode() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestDescriptionTranslationMode");
        }
        return testDescriptionTranslationMode;
    }

    public void setTestDescriptionTranslationMode(boolean testDescriptionTranslationMode) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestDescriptionTranslationMode");
        }
        this.testDescriptionTranslationMode = testDescriptionTranslationMode;
    }

    public boolean isTestDefinitonViewMode() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isTestDefinitonViewMode");
        }
        return testDefinitonViewMode;
    }

    public void setTestDefinitonViewMode(boolean testDefinitonViewMode) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestDefinitonViewMode");
        }
        this.testDefinitonViewMode = testDefinitonViewMode;
    }

    public UserComment getSelectedUserComment() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedUserComment");
        }
        return selectedUserComment;
    }

    public void setSelectedUserComment(UserComment selectedUserComment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedUserComment");
        }
        this.selectedUserComment = selectedUserComment;
    }

    public List<TestPeerType> getPossibleTestPeerTypes() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleTestPeerTypes");
        }
        if (possibleTestPeerTypes == null) {
            findTestPeerTypes();
        }
        return possibleTestPeerTypes;
    }

    public TestDataModel getFoundTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFoundTests");
        }
        if (foundTests == null) {
            foundTests = new TestDataModel(searchKeyword);
        }

        return foundTests;
    }

    public void setFoundTests(TestDataModel foundTests) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFoundTests");
        }
        this.foundTests = foundTests;
    }

    public List<TmTestStepMessageProfile> getProfilesToRemove() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProfilesToRemove");
        }
        return profilesToRemove;
    }

    public void setProfilesToRemove(List<TmTestStepMessageProfile> profilesToRemove) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setProfilesToRemove");
        }
        this.profilesToRemove = profilesToRemove;
    }

    public List<TestType> getTestTypeList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestTypeList");
        }
        if (testTypeList == null) {
            testTypeList = TestType.getTypeList();
        }
        return testTypeList;
    }

    /**
     * Variable used to manage all the Tabs in the Test definition
     */
    @Deprecated
    public String getSelectedTabForTestDefinistionList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTabForTestDefinistionList");
        }
        return selectedTabForTestDefinistionList;
    }

    @Deprecated
    public void setSelectedTabForTestDefinistionList(String selectedTabForTestDefinistionList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTabForTestDefinistionList");
        }
        this.selectedTabForTestDefinistionList = selectedTabForTestDefinistionList;
    }

    @Deprecated
    public void switchTab(String tabLabel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("switchTab");
        }
        setSelectedTabForTestDefinistionList(tabLabel);
    }

    public Hl7MessageProfile getSelectedReqMsgType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedReqMsgType");
        }
        return selectedReqMsgType;
    }

    public void setSelectedReqMsgType(Hl7MessageProfile selectedReqMsgType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedReqMsgType");
        }
        this.selectedReqMsgType = selectedReqMsgType;
    }

    public Hl7MessageProfile getSelectedRspMsgType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedRspMsgType");
        }
        return selectedRspMsgType;
    }

    public void setSelectedRspMsgType(Hl7MessageProfile selectedRspMsgType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedRspMsgType");
        }
        this.selectedRspMsgType = selectedRspMsgType;
    }

    public ValidationService getSelectedReqValidationService() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedReqValidationService");
        }
        return selectedReqValidationService;
    }

    public void setSelectedReqValidationService(ValidationService selectedReqValidationService) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedReqValidationService");
        }
        this.selectedReqValidationService = selectedReqValidationService;
    }

    public ValidationService getSelectedRspValidationService() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedRspValidationService");
        }
        return selectedRspValidationService;
    }

    public void setSelectedRspValidationService(ValidationService selectedRspValidationService) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedRspValidationService");
        }
        this.selectedRspValidationService = selectedRspValidationService;
    }

    public TSMPConfigFile getInitiatorValidationCtxFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInitiatorValidationCtxFile");
        }
        return initiatorValidationCtxFile;
    }

    public void setInitiatorValidationCtxFile(TSMPConfigFile initiatorValidationCtxFile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setInitiatorValidationCtxFile");
        }
        this.initiatorValidationCtxFile = initiatorValidationCtxFile;
    }

    public TSMPConfigFile getResponderValidationCtxFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getResponderValidationCtxFile");
        }
        return responderValidationCtxFile;
    }

    public void setResponderValidationCtxFile(TSMPConfigFile responderValidationCtxFile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setResponderValidationCtxFile");
        }
        this.responderValidationCtxFile = responderValidationCtxFile;
    }

    public List<TmTestStepMessageProfile> getProfilesToAdd() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProfilesToAdd");
        }
        return profilesToAdd;
    }

    public void setProfilesToAdd(List<TmTestStepMessageProfile> profilesToAdd) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setProfilesToAdd");
        }
        this.profilesToAdd = profilesToAdd;
    }

    public TSMPConfigFile getInitiatorCodeTableFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInitiatorCodeTableFile");
        }
        return initiatorCodeTableFile;
    }

    public void setInitiatorCodeTableFile(TSMPConfigFile initiatorCodeTableFile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setInitiatorCodeTableFile");
        }
        this.initiatorCodeTableFile = initiatorCodeTableFile;
    }

    public TSMPConfigFile getResponderCodeTableFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getResponderCodeTableFile");
        }
        return responderCodeTableFile;
    }

    public void setResponderCodeTableFile(TSMPConfigFile responderCodeTableFile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setResponderCodeTableFile");
        }
        this.responderCodeTableFile = responderCodeTableFile;
    }

    public TSMPConfigFile getInitiatorExampleMsg() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInitiatorExampleMsg");
        }
        return initiatorExampleMsg;
    }

    public void setInitiatorExampleMsg(TSMPConfigFile initiatorExampleMsg) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setInitiatorExampleMsg");
        }
        this.initiatorExampleMsg = initiatorExampleMsg;
    }

    public TSMPConfigFile getResponderExampleMsg() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getResponderExampleMsg");
        }
        return responderExampleMsg;
    }

    public void setResponderExampleMsg(TSMPConfigFile responderExampleMsg) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setResponderExampleMsg");
        }
        this.responderExampleMsg = responderExampleMsg;
    }

    public TSMPConfigFile getInitiatorValueSetsFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInitiatorValueSetsFile");
        }
        return initiatorValueSetsFile;
    }

    public void setInitiatorValueSetsFile(TSMPConfigFile initiatorValueSetsFile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setInitiatorValueSetsFile");
        }
        this.initiatorValueSetsFile = initiatorValueSetsFile;
    }

    public TSMPConfigFile getResponderValueSetsFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getResponderValueSetsFile");
        }
        return responderValueSetsFile;
    }

    public void setResponderValueSetsFile(TSMPConfigFile responderValueSetsFile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setResponderValueSetsFile");
        }
        this.responderValueSetsFile = responderValueSetsFile;
    }

    public TSMPConfigFile getDataSheetFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDataSheetFile");
        }
        return dataSheetFile;
    }

    public void setDataSheetFile(TSMPConfigFile dataSheetFile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDataSheetFile");
        }
        this.dataSheetFile = dataSheetFile;
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public boolean validateTestKeyword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateTestKeyword");
        }
        if (selectedTest == null) {
            return false;
        }
        if ((selectedTest.getKeyword() == null) || selectedTest.getKeyword().trim().equals("")) {
            return false;
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        if (selectedTest.getId() == null) {
            Query q = entityManager.createQuery("from Test t where t.keyword=:testKeyword");
            q.setParameter("testKeyword", selectedTest.getKeyword());

            if (q.getResultList().size() > 0) {
                FacesMessages.instance().addToControl("keyword",
                        "#{messages['gazelle.tm.testing.testsDefinition.testKeywordNotValid']}");

                return false;
            }
        } else {
            Test tmp = entityManager.find(Test.class, selectedTest.getId());
            if (selectedTest.getKeyword().equals(tmp.getKeyword())) {
                return true;
            } else {
                Query q = entityManager.createQuery("from Test t where t.keyword=:testKeyword");
                q.setParameter("testKeyword", selectedTest.getKeyword());

                if (q.getResultList().size() > 0) {
                    FacesMessages.instance().addToControl("keyword",
                            "#{messages['gazelle.tm.testing.testsDefinition.testKeywordNotValid']}");

                    return false;
                }
            }
        }
        return true;
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public boolean validateTestName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateTestName");
        }
        if (selectedTest == null) {
            return false;
        }
        if ((selectedTest.getName() == null) || selectedTest.getName().trim().equals("")) {
            return false;
        }
        return true;
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public boolean validateTestDescription() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateTestDescription");
        }
        if ((selectedTestDescription.getDescription() == null) || selectedTestDescription.getDescription().equals("")) {
            return false;
        }
        return true;
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public void persistTest(Test inTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistTest");
        }
        setSelectedTest(inTest);
        persistTest();
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public void persistTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistTest");
        }
        if (selectedTest == null) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Test value is null cannot go further");
            return;
        }

        try {
            if (validateTestKeyword() && validateTestName()) {
                String currentUser = User.loggedInUser().getUsername();
                EntityManager entityManager = EntityManagerService.provideEntityManager();
                if (selectedTest.getId() != null) {
                    Test currentTestInDb = entityManager.find(Test.class, selectedTest.getId());
                    if (((currentTestInDb.getValidated() == null) || !currentTestInDb.getValidated())
                            && ((selectedTest.getValidated() != null) && selectedTest.getValidated())) {
                        FacesMessages.instance().add(StatusMessage.Severity.INFO, "Test is validated by " + currentUser);
                        selectedTest.setLastValidatorId(currentUser);
                    } else if (((currentTestInDb.getValidated() != null) && currentTestInDb.getValidated())
                            && ((selectedTest.getValidated() != null) && !selectedTest.getValidated())) {
                        FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Test is no more validated");
                        selectedTest.setLastValidatorId(null);
                    }
                } else if ((selectedTest.getValidated() != null) && selectedTest.getValidated()) {
                    FacesMessages.instance().add(StatusMessage.Severity.INFO, "Test is validated by " + currentUser);
                    selectedTest.setLastValidatorId(currentUser);
                }

                boolean updateMode = (selectedTest.getId() != null);
                if (!updateMode) {
                    selectedTest.setAuthor(currentUser);
                }
                selectedTest = entityManager.merge(selectedTest);
                entityManager.flush();
                if (updateMode) {
                    FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, "gazelle.tm.testing.testsDefinition.testUpdated");
                } else {
                    FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, "net.ihe.gazelle.tm.TestIsCreatedBy" , currentUser);
                }
                getFoundTests().resetCache();
                return;
            } else {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Problem with validation of current test");
                return;
            }

        } catch (Exception e) {
            ExceptionLogging.logException(e, LOG);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Impossible to persist test : " + e.getMessage());
            return;
        }

    }

    public void viewTest(Test inTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewTest");
        }
        Redirect redirect = Redirect.instance();
        if ((inTest != null) && (inTest.getId() != null)) {
            redirect.setParameter("id", inTest.getId());
            redirect.setViewId("/" + inTest.viewFolder() + "/testsDefinition/viewTestPage.xhtml");
            redirect.execute();
        }
    }

    @Deprecated
    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public void editTest(Test inTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editTest");
        }
        Redirect redirect = Redirect.instance();
        if ((inTest != null) && (inTest.getId() != null)) {
            redirect.setParameter("id", inTest.getId());
            redirect.setViewId("/" + inTest.viewFolder() + "/testsDefinition/editTestPage.xhtml");
            redirect.execute();
        }
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public String editNewTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editNewTest");
        }

        String redirectTo = "/testing/testsDefinition/editTestSummary.xhtml?new=1";
        return redirectTo;
    }

    // TODO finish ITB integration
    public void editNewTestITB() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editNewTestITB");
        }
        Redirect redirect = Redirect.instance();
        redirect.setViewId("/" + selectedTest.viewFolder() + "/testsDefinition/editTestPage.xhtml");
        redirect.execute();
    }

    /*Used by ITB only*/
    public void parseURLParameters(boolean edit) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("parseURLParameters");
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        String testId = fc.getExternalContext().getRequestParameterMap().get("id");
        String tabPanel = fc.getExternalContext().getRequestParameterMap().get("tabPanel");

        selectedTest = null;
        getSelectedTabForTestDefinistionList();
        if (testId != null) {
            EntityManager em = EntityManagerService.provideEntityManager();
            selectedTest = em.find(Test.class, Integer.valueOf(testId));
        }

        if ((tabPanel != null) && !tabPanel.isEmpty()) {
            setSelectedTabForTestDefinistionList(tabPanel);
        }

        if (edit) {
            if (selectedTest == null) {
                createAndEditNewTest();
            } else {
                editSelectedTest();
            }
        } else {
            if (selectedTest != null) {
                viewSelectedTest();
            } else {
                throw new EntityNotFoundException("No Test found with the given id.");
            }
        }
    }

    public void viewSelectedTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewSelectedTest");
        }
        testDefinitonViewMode = true;
        selectedTestDescription = null;
        selectedUserComment = new UserComment();
        selectedUserComment.setCommentContent("");
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public void createAndEditNewTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createAndEditNewTest");
        }
        this.selectedTest = new Test();
        this.selectedTest.setTestType(TestType.getTYPE_CONNECTATHON());
        this.selectedTest.setTestStatus(TestStatus.getSTATUS_READY());
        this.selectedTest.setOrchestrable(false);
        this.selectedTestDescription = new TestDescription();
        this.selectedTestDescription.initDefaultValues();
        this.testDescriptionEditionMode = true;
        this.testDescriptionTranslationMode = false;
        this.searchKeyword = null;
        selectedUserComment = new UserComment();
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public void editSelectedTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editSelectedTest");
        }

        if ((selectedTest.getTestDescription() != null) && (selectedTest.getTestDescription().size() > 0)) {
            this.testDescriptionEditionMode = false;
            this.testDescriptionTranslationMode = false;
        } else {
            this.selectedTestDescription = new TestDescription();
            this.selectedTestDescription.initDefaultValues();
            this.testDescriptionEditionMode = true;
            this.testDescriptionTranslationMode = false;
        }
        this.testDefinitonViewMode = false;
        selectedUserComment = new UserComment();
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    private void deleteTest(Test inTest) {
        if (inTest == null) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Test value is null cannot go further");
            return;
        }

        List<TestInstance> testInstanceList = TestInstance.getTestInstancesForATest(inTest);
        if ((testInstanceList != null) && (testInstanceList.size() > 0)) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                    "Impossible to delete test : the selected test is used in many test instances.");
            return;
        }

        try {
            Test.deleteTestWithFind(inTest);
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Test deleted");
            getFoundTests().resetCache();
            return;
        } catch (Exception e) {
            ExceptionLogging.logException(e, LOG);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Impossible to delete test : " + e.getMessage());
            return;
        }
    }

    /*Used by ITB only*/
    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public void deleteSelectedDescription() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedDescription");
        }
        try {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            List<TestDescription> testDescriptionList = selectedTest.getTestDescription();
            if (testDescriptionList != null) {
                testDescriptionList.remove(selectedTestDescription);
                selectedTest.setTestDescription(testDescriptionList);
                selectedTest = entityManager.merge(selectedTest);
            }
            TestDescription.deleteTestDescriptionWithFind(selectedTestDescription);
            selectedTestDescription = new TestDescription();
            selectedTestDescription.initDefaultValues();
        } catch (Exception e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Impossible to delete description : " + e.getMessage());
            return;
        }
        if ((selectedTest.getTestDescription() == null) || (selectedTest.getTestDescription().size() == 0)) {
            this.testDescriptionEditionMode = true;
        }
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public void deleteSelectedTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedTest");
        }
        deleteTest(selectedTest);
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public void editTestDescription(TestDescription inTestDescription) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editTestDescription");
        }
        this.setSelectedTestDescription(inTestDescription);

        if (this.selectedTestDescription == null) {
            this.selectedTestDescription = new TestDescription();
            this.selectedTestDescription.initDefaultValues();
        }
        if (this.selectedTestDescription.getGazelleLanguage() == null) {
            this.selectedTestDescription.setGazelleLanguage(GazelleLanguage.getDefaultLanguage());
        }
        this.testDescriptionEditionMode = true;
        this.testDescriptionTranslationMode = false;
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public void addTestDescription() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addTestDescription");
        }
        if (selectedTestDescription == null) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Test Description value is null cannot go further");
            return;
        }
        List<TestDescription> testDescriptionList = selectedTest.getTestDescription();
        if (testDescriptionList == null) {
            testDescriptionList = new ArrayList<TestDescription>();
        } else {
            int i = 0;
            int j = testDescriptionList.size();
            while (i < j) {
                if (testDescriptionList.get(i) == null) {
                    testDescriptionList.remove(i);
                    j = j - 1;
                }
                i++;
            }
        }
        for (TestDescription testDescription : testDescriptionList) {
            if (testDescription != null) {
                if ((testDescription.getGazelleLanguage() != null) && (testDescription.getId() != null)) {
                    if (testDescription.getGazelleLanguage().equals(selectedTestDescription.getGazelleLanguage())
                            && !testDescription.getId().equals(selectedTestDescription.getId())) {
                        FacesMessages.instance().add(StatusMessage.Severity.ERROR, "There is already a description with the same language");
                        return;
                    }
                }
            }
        }

        try {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            boolean updateMode = (selectedTestDescription.getId() != null);
            selectedTestDescription = entityManager.merge(selectedTestDescription);
            entityManager.flush();
            selectedTest.setLastChanged(selectedTestDescription.getLastChanged());
            selectedTest.setLastModifierId(selectedTestDescription.getLastModifierId());

            if (updateMode) {
                selectedTest = entityManager.merge(selectedTest);
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "Description updated");
            } else {
                testDescriptionList.add(selectedTestDescription);
                this.selectedTest.setTestDescription(testDescriptionList);
                selectedTest = entityManager.merge(selectedTest);
                entityManager.flush();
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "Description added");
            }
            this.testDescriptionEditionMode = false;
            this.testDescriptionTranslationMode = false;
            selectedTestDescription = new TestDescription();
            selectedTestDescription.initDefaultValues();
            return;
        } catch (Exception e) {
            ExceptionLogging.logException(e, LOG);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Impossible to add description : " + e.getMessage());
            return;
        }
    }

    public void cancelTestView() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelTestView");
        }
        testDefinitonViewMode = false;
    }

    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    public void addComment() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addComment");
        }
        if (selectedTest != null) {
            List<UserComment> userCommentList = selectedTest.getUserCommentList();
            if (userCommentList == null) {
                userCommentList = new ArrayList<UserComment>();
            }
            selectedUserComment.setUser(User.loggedInUser().getUsername());
            selectedUserComment.setCreationDate(new Date());
            EntityManager em = EntityManagerService.provideEntityManager();
            selectedUserComment = em.merge(selectedUserComment);
            userCommentList.add(selectedUserComment);
            selectedTest.setUserCommentList(userCommentList);
            selectedTest = em.merge(selectedTest);
            selectedUserComment = new UserComment();
            selectedUserComment.setCommentContent("");
            em.flush();
        }
    }

    public void updateSelectedUserComment() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateSelectedUserComment");
        }
        if (selectedTest != null) {
            if (selectedUserComment != null) {
                EntityManager em = EntityManagerService.provideEntityManager();
                em.merge(selectedUserComment);
                selectedUserComment = new UserComment();
                selectedUserComment.setCommentContent("");
            }
        }
    }

    public void editUserComment(UserComment inUserComment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editUserComment");
        }
        selectedUserComment = inUserComment;
    }

    public void deleteUserComment(UserComment inUserComment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteUserComment");
        }
        List<UserComment> userCommentList = selectedTest.getUserCommentList();
        userCommentList.remove(inUserComment);
        selectedTest.setUserCommentList(userCommentList);
        EntityManager em = EntityManagerService.provideEntityManager();
        selectedTest = em.merge(selectedTest);
        try {
            UserComment.deleteUserCommentWithFind(inUserComment);
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Comment selected was deleted.");
        } catch (Exception e) {
            LOG.error("", e);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, e.getMessage());
        }
    }

    public boolean isLoggedUserAllowedToEditComment(UserComment inUserComment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isLoggedUserAllowedToEditComment");
        }
        return Role.isLoggedUserAdmin() || inUserComment.getUser().equals(User.loggedInUser().getUsername());
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
            EntityManagerService.provideEntityManager();
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

    public Integer getSystemInSessionListSizeForActiveTestingSessionByTest(Test inTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemInSessionListSizeForActiveTestingSessionByTest");
        }
        if (inTest != null) {
            EntityManager em = EntityManagerService.provideEntityManager();
            Query query = em
                    .createQuery("SELECT count(DISTINCT sIs) "
                            + "FROM SystemInSession as sIs,TestRoles tr join tr.roleInTest.testParticipantsList participants, SystemActorProfiles " +
                            "sap "
                            + "WHERE sap.actorIntegrationProfileOption = participants.actorIntegrationProfileOption "
                            + "AND sIs.system=sap.system " + "AND tr.test=:inTest " + "AND participants.tested='true' "
                            + "AND tr.testOption=:inTestOption " + "AND sIs.testingSession=:inTestingSession");
            query.setParameter("inTestingSession", TestingSession.getSelectedTestingSession());
            query.setParameter("inTest", inTest);
            query.setParameter("inTestOption", TestOption.getTEST_OPTION_REQUIRED());
            return ((Long) query.getSingleResult()).intValue();
        }
        return 0;
    }

    public void downloadTestAsPdf(Test inTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("downloadTestAsPdf");
        }
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("test_keyword", inTest.getKeyword());

            // get the application url to use it as parameter for the report
            String applicationurl = ApplicationPreferenceManager.instance().getApplicationUrl();
            params.put("applicationurl", applicationurl);

            // get the testDescription Language to use it as parameter for the report
            List<Integer> res = new ArrayList<>();
            EntityManager em = EntityManagerService.provideEntityManager();
            Test test = em.find(Test.class, inTest.getId());
            List<TestDescription> descriptionList = test.getTestDescription();
            for (TestDescription testDescription : descriptionList) {
                res.add(testDescription.getGazelleLanguage().getId());
            }
            if (!res.isEmpty()) {
                //TODO GZL-4659
                params.put("testDescriptionLanguageId", res.get(0));
            }

            ReportExporterManager.exportToPDF(REPORT_TEST_NAME, inTest.getKeyword().replace(" ", "_") + ".pdf", params);

        } catch (JRException e) {
            ExceptionLogging.logException(e, LOG);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, e.getMessage());
        }
    }

    public void downloadSelectedTestsAsPdf(List<Test> tests, int index) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("downloadSelectedTestsAsPdf");
        }

        try {
            String monitorName = "All monitors";
            String fileName = "All_tests_";
            if (index > 0) {
                monitorName += " part_" + index;
                fileName += "part_" + index + "_";
            }
            String monitorLogin = "All monitors";
            String institution = "All institutions";

            if (tests.size() != 0) {
                String selectedTestList = "";

                for (Test test : tests) {
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

                ReportExporterManager.exportToPDF(MULTIPLE_REPORT_TEST_NAME, fileName + new Date() + ".pdf", params);

            } else {
                LOG.error("no test selected");
            }
        } catch (JRException e) {
            ExceptionLogging.logException(e, LOG);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, e.getMessage());
        }

    }

    public void downloadSelectedTestsAsPdf(List<Test> tests) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("downloadSelectedTestsAsPdf");
        }
        int i = 0;
        downloadSelectedTestsAsPdf(tests, i);
    }

    public void downloadAllTestsAsPdf(TestDataModel tests) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("downloadAllTestsAsPdf");
        }

        @SuppressWarnings("unchecked")
        List<Test> testList = (List<Test>) tests.getAllItems(FacesContext.getCurrentInstance());
        downloadSelectedTestsAsPdf(testList);
    }

    public int testsCount(TestDataModel tests) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("testsCount");
        }
        @SuppressWarnings("unchecked")
        List<Test> testList = (List<Test>) tests.getAllItems(FacesContext.getCurrentInstance());
        return testList.size();
    }

    public void splitTestList(TestDataModel tests) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("splitTestList");
        }

        @SuppressWarnings("unchecked")
        List<Test> testList = (List<Test>) tests.getAllItems(FacesContext.getCurrentInstance());
        listOfTestLists = new ArrayList<List<Test>>();

        while (testList.size() > 100) {
            List<Test> testList1 = null;
            testList1 = testList.subList(0, 100);
            listOfTestLists.add(testList1);
            testList = testList.subList(100, testList.size());
        }
        listOfTestLists.add(testList);

    }

    public List<GazelleLanguage> getLanguageList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLanguageList");
        }
        Set<GazelleLanguage> gazelleLanguageList = new TreeSet<GazelleLanguage>(GazelleLanguage.getLanguageList());
        if (selectedTest != null) {
            List<TestDescription> testDescriptionList = selectedTest.getTestDescription();
            if (testDescriptionList != null) {
                for (TestDescription testDescription : testDescriptionList) {
                    gazelleLanguageList.remove(testDescription.getGazelleLanguage());
                }
            }
            if (testDescriptionEditionMode) {
                if ((selectedTestDescription != null) && (selectedTestDescription.getGazelleLanguage() != null)) {
                    gazelleLanguageList.add(selectedTestDescription.getGazelleLanguage());
                }
            }
        }
        return new ArrayList<GazelleLanguage>(gazelleLanguageList);
    }

    public void translateTestDescription(TestDescription inTestDescription) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("translateTestDescription");
        }
        translatedTestDescription = inTestDescription;
        this.selectedTestDescription = new TestDescription();
        this.selectedTestDescription.initDefaultValues();
        this.testDescriptionTranslationMode = true;
    }

    /**
     * Find the list of possible Peer Types for tests
     */
    public void findTestPeerTypes() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findTestPeerTypes");
        }
        possibleTestPeerTypes = TestPeerType.getPeerTypeList();
    }

    public String cancelTestCreationWithReturn() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelTestCreationWithReturn");
        }
        return "/testing/testsDefinition/testsList.xhtml";
    }

    @SuppressWarnings("unchecked")
    public List<Test> getListOfAllTestsFromTestDataModel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfAllTestsFromTestDataModel");
        }
        return (List<Test>) getFoundTests().getAllItems(FacesContext.getCurrentInstance());
    }

    // getter and setter //////////////////////////////////////////////////////////

    public void resetFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetFilter");
        }
        getFoundTests().getFilter().clear();
        setSearchKeyword(null);
    }

    public String getContextualInformationtype() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getContextualInformationtype");
        }
        return contextualInformationtype;
    }

    public void setContextualInformationtype(String contextualInformationtype) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setContextualInformationtype");
        }
        this.contextualInformationtype = contextualInformationtype;
    }

    public List<ContextualInformation> getListContextualInformationToRemove() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListContextualInformationToRemove");
        }
        return listContextualInformationToRemove;
    }

    public void setListContextualInformationToRemove(List<ContextualInformation> listContextualInformationToRemove) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setListContextualInformationToRemove");
        }
        this.listContextualInformationToRemove = listContextualInformationToRemove;
    }

    public int getInputOrOutput() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInputOrOutput");
        }
        return inputOrOutput;
    }

    public void setInputOrOutput(int inputOrOutput) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setInputOrOutput");
        }
        this.inputOrOutput = inputOrOutput;
    }

    public ContextualInformation getSelectedContextualInformation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedContextualInformation");
        }
        if (selectedContextualInformation == null) {
            selectedContextualInformation = new ContextualInformation();
        }
        return selectedContextualInformation;
    }

    public void setSelectedContextualInformation(ContextualInformation selectedContextualInformation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedContextualInformation");
        }
        this.selectedContextualInformation = selectedContextualInformation;
    }

    public TestSteps getSelectedTestSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestSteps");
        }
        return selectedTestSteps;
    }

    public void setSelectedTestSteps(TestSteps selectedTestSteps) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestSteps");
        }
        this.selectedTestSteps = selectedTestSteps;
    }

    public boolean isShowTestStepsEditionPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isShowTestStepsEditionPanel");
        }
        return showTestStepsEditionPanel;
    }

    public void setShowTestStepsEditionPanel(boolean showTestStepsEditionPanel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setShowTestStepsEditionPanel");
        }
        this.showTestStepsEditionPanel = showTestStepsEditionPanel;
    }

    public boolean isAutoResponse() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isAutoResponse");
        }
        return autoResponse;
    }

    public void setAutoResponse(boolean autoResponse) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setAutoResponse");
        }
        this.autoResponse = autoResponse;
    }

    public int getDEFAULT_TEST_STEPS_INDEX_INCREMENTATION() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDEFAULT_TEST_STEPS_INDEX_INCREMENTATION");
        }
        return DEFAULT_TEST_STEPS_INDEX_INCREMENTATION;
    }

    // methods //////////////////////////////////////////////////////////

    public int getMAX_STEP_INDEX_VALUE() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMAX_STEP_INDEX_VALUE");
        }
        return MAX_STEP_INDEX_VALUE;
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public void cancelTestStepsCreation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelTestStepsCreation");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        showTestStepsEditionPanel = false;
        profilesToRemove = null;
        setSelectedReqMsgType(null);
        setSelectedRspMsgType(null);
        setSelectedReqValidationService(null);
        setSelectedRspValidationService(null);
        autoResponse = false;
        selectedTest = entityManager.merge(selectedTest);
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public void editTestSteps(TestSteps inTestSteps) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editTestSteps");
        }
        selectedTestSteps = inTestSteps;
        editTestSteps();
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public void editTestSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editTestSteps");
        }
        setAutoResponse(selectedTestSteps.getTestRolesInitiator().equals(selectedTestSteps.getTestRolesResponder()));
        setSelectedReqMsgType((null != selectedTestSteps.getRequestMessageProfile()) ? selectedTestSteps
                .getRequestMessageProfile().getTfHl7MessageProfile() : null);
        setSelectedRspMsgType((null != selectedTestSteps.getResponseMessageProfile()) ? selectedTestSteps
                .getResponseMessageProfile().getTfHl7MessageProfile() : null);
        setSelectedReqValidationService(getValidationService(selectedTestSteps, MessageDirection.REQUEST));
        setSelectedRspValidationService(getValidationService(selectedTestSteps, MessageDirection.RESPONSE));

        initializeConfigurationFiles(selectedTestSteps);

        showTestStepsEditionPanel();
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public void addNewTestSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNewTestSteps");
        }
        selectedTestSteps = new TestSteps();
        if (selectedTest != null) {
            selectedTestSteps.setStepIndex(getHighestStepIndex(selectedTest.getTestStepsList())
                    + DEFAULT_TEST_STEPS_INDEX_INCREMENTATION);
        }

        profilesToRemove = null;

        setSelectedReqMsgType(null);
        setSelectedRspMsgType(null);
        setSelectedReqValidationService(null);
        setSelectedRspValidationService(null);
        autoResponse = false;
        showTestStepsEditionPanel();
    }

    @SuppressWarnings("unchecked")
    public List<TestStepsOption> getPossibleTestStepsOptions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleTestStepsOptions");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();

        Query q = entityManager.createQuery("from TestStepsOption");
        return q.getResultList();

    }

    public void showTestStepsEditionPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showTestStepsEditionPanel");
        }
        showTestStepsEditionPanel = true;
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public void persistTestSteps(TestSteps inTestSteps) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistTestSteps");
        }
        this.selectedTestSteps = inTestSteps;
        persistSelectedTestSteps();
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public void persistSelectedTestSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistSelectedTestSteps");
        }

        EntityManager entityManager = EntityManagerService.provideEntityManager();
        if (selectedTestSteps == null) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "TestSteps value is null cannot go further");
            return;
        }
        if (validateStepIndex() && validateTestStepsDescription()) {
            try {

                if (autoResponse) {
                    selectedTestSteps.setTestRolesResponder(selectedTestSteps.getTestRolesInitiator());
                    selectedTestSteps.setTransaction(null);
                    selectedTestSteps.setWstransactionUsage(null);
                    autoResponse = false;
                } else {
                    if ((selectedTestSteps.getTransaction() == null)
                            && (!selectedTestSteps.getTestRolesInitiator().equals(
                            selectedTestSteps.getTestRolesResponder()))) {
                        FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.WARN,
                                "gazelle.tm.testing.testsDefinition.transactionNullWarning");
                        return;
                    }

                    if (selectedTestSteps.getTestRolesResponder() == null) {
                        FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.WARN,
                                "gazelle.tm.testing.testsDefinition.testRolesResponderNullWarning");
                        return;
                    }

                }

                if ((selectedTestSteps.getDescription() == null) || selectedTestSteps.getDescription().isEmpty()) {
                    selectedTestSteps.setDescription(" ");
                }

                if (selectedReqMsgType != null) {
                    addMessageProfileType(selectedReqMsgType, MessageDirection.REQUEST);
                }

                if (selectedRspMsgType != null) {
                    addMessageProfileType(selectedRspMsgType, MessageDirection.RESPONSE);
                }

                if (selectedReqValidationService != null) {
                    ValidationService service = getValidationService(selectedTestSteps, MessageDirection.REQUEST);
                    if (service != null) {
                        if (!(service.getKey().equals(selectedReqValidationService.getKey()))) {
                            updateValidationService(selectedTestSteps, MessageDirection.REQUEST,
                                    selectedReqValidationService);
                        }
                    } else { // first time defining a validation service
                        updateValidationService(selectedTestSteps, MessageDirection.REQUEST,
                                selectedReqValidationService);
                    }
                }

                if (selectedRspValidationService != null) {
                    ValidationService service = getValidationService(selectedTestSteps, MessageDirection.RESPONSE);
                    if (service != null) {
                        if (!(service.getKey().equals(selectedRspValidationService.getKey()))) {
                            updateValidationService(selectedTestSteps, MessageDirection.RESPONSE,
                                    selectedRspValidationService);
                        }
                    } else {
                        updateValidationService(selectedTestSteps, MessageDirection.RESPONSE,
                                selectedRspValidationService);
                    }
                }

                selectedTestSteps = TestSteps.mergeTestSteps(selectedTestSteps, entityManager);

                if (selectedTest.getTestStepsList() == null) {
                    selectedTest.setTestStepsList(new ArrayList<TestSteps>());
                }

                selectedTest = entityManager.find(Test.class, selectedTest.getId());
                entityManager.refresh(selectedTest);
                List<TestSteps> lts = selectedTest.getTestStepsList();

                boolean updateMode = false;
                if (!lts.contains(selectedTestSteps)) {
                    lts.add(selectedTestSteps);
                } else {
                    updateMode = true;
                }
                selectedTest.setTestStepsList(lts);

                selectedTest = entityManager.merge(selectedTest);

                entityManager.flush();

                if (updateMode) {
                    FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO,
                            "gazelle.tm.testing.testsDefinition.testStepsUpdated");
                } else {
                    FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO,
                            "gazelle.tm.testing.testsDefinition.testStepsCreated");
                }

                selectedTestSteps = new TestSteps();
                showTestStepsEditionPanel = false;

                return;

            } catch (Exception e) {
                LOG.error("Failed to persist test steps", e);
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Impossible to persist testSteps : " + e.getMessage());
                return;
            }
        } else {
            selectedTest = entityManager.merge(selectedTest);
        }
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public List<TestRoles> getPossibleTestRoles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleTestRoles");
        }

        this.possibleTestRoles = TestRoles.getTestRolesListForATest(selectedTest);
        return possibleTestRoles;

    }

    public void setPossibleTestRoles(List<TestRoles> possibleTestRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setPossibleTestRoles");
        }
        this.possibleTestRoles = possibleTestRoles;
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public boolean validateStepIndex() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateStepIndex");
        }
        List<TestSteps> testStepsList = selectedTest.getTestStepsList();
        if (selectedTestSteps.getStepIndex() != null) {
            if (testStepsList != null) {
                for (TestSteps testSteps : testStepsList) {
                    if ((testSteps.getStepIndex().equals(selectedTestSteps.getStepIndex()))
                            && (!testSteps.getId().equals(selectedTestSteps.getId()))) {
                        FacesMessages.instance().addToControl("stepIndex",
                                "#{messages['gazelle.tm.testing.testsDefinition.stepIndexNotValid']}");

                        return false;
                    }
                }
            }
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "The step index entered was null");
            return false;
        }
        return true;
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public boolean validateTestStepsDescription() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateTestStepsDescription");
        }
        if (selectedTestSteps.getDescription() != null) {
            if (selectedTestSteps.getDescription().length() > TestSteps.MAX_DESCRIPTION_LENGTH) {
                FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.validator.length.max",
                        TestSteps.MAX_DESCRIPTION_LENGTH);
                return false;
            }
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "The description entered was null");
            return false;
        }
        return true;

    }

    /*Used By Itb*/
    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public void copySelectedTestStep(TestSteps selectedTestSteps) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("copySelectedTestStep");
        }
        this.selectedTestSteps = new TestSteps(selectedTestSteps);
        int testStepsIndex = getHighestStepIndex(selectedTest.getTestStepsList())
                + DEFAULT_TEST_STEPS_INDEX_INCREMENTATION;
        this.selectedTestSteps.setStepIndex(testStepsIndex);
        this.selectedTestSteps.setDescription("Copy of Step " + selectedTestSteps.getStepIndex() + " :"
                + this.selectedTestSteps.getDescription());
        persistSelectedTestSteps();
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public int getHighestStepIndex(List<TestSteps> testStepsList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getHighestStepIndex");
        }
        int result = 0;
        if (testStepsList != null) {
            for (TestSteps testSteps : testStepsList) {
                if (testSteps.getStepIndex() > result) {
                    result = testSteps.getStepIndex();
                }
            }
        }
        return result;
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public void deleteTestSteps(TestSteps inTestSteps) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteTestSteps");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        if (selectedTestSteps == null) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "TestSteps value is null cannot go further");
            return;
        }

        List<TestStepsInstance> testStepsInstanceList = TestStepsInstance.getTestStepsInstanceByTestSteps(inTestSteps);

        if ((testStepsInstanceList != null) && (testStepsInstanceList.size() > 0)) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                    "Impossible to delete TestSteps : selected Test Steps is used in many testSteps instances.");
            return;
        }

        // this is a safety check, we don't want to delete this record if message types captured for this Test Steps
        // already have test instance message(s) captured.
        if ((selectedTestSteps.getTmTestStepMessageProfiles() != null)
                && (selectedTestSteps.getTmTestStepMessageProfiles().size() > 0)) {
            for (TmTestStepMessageProfile prof : selectedTestSteps.getTmTestStepMessageProfiles()) {
                if ((prof.getTmStepInstanceMessages() != null) && (prof.getTmStepInstanceMessages().size() > 0)) {
                    FacesMessages
                            .instance()
                            .add(StatusMessage.Severity.ERROR, "Impossible to delete TestSteps : selected Test Steps is used in many testSteps " +
                                    "instance messages.");
                    return;
                }
            }
        }

        try {
            List<TestSteps> testStepsList = selectedTest.getTestStepsList();

            if (testStepsList != null) {
                testStepsList.remove(inTestSteps);
                selectedTest.setTestStepsList(testStepsList);
                selectedTest = entityManager.merge(selectedTest);
                entityManager.flush();
            }

            TestSteps.deleteTestStepsWithFind(inTestSteps);
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "TestSteps deleted");
            return;
        } catch (Exception e) {
            LOG.error("Failed to delete teststeps", e);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Impossible to delete TestSteps : " + e.getMessage());
            return;
        }
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public void deleteSelectedTestSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedTestSteps");
        }
        deleteTestSteps(selectedTestSteps);
    }

    public void resetTransactionValue() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetTransactionValue");
        }
        selectedTestSteps.setTransaction(null);
        selectedTestSteps.setWstransactionUsage(null);
        resetTestRolesResponderValue();
    }

    public void resetTestRolesResponderValue() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetTestRolesResponderValue");
        }
        selectedTestSteps.setTestRolesResponder(null);
        selectedTestSteps.setMessageType(null);
        selectedTestSteps.setWstransactionUsage(null);

        // added to support nulling ITB related attributes
        if (selectedTest.isTypeInteroperability()) {
            selectedTestSteps.setResponderMessageType(null);
            selectedTestSteps.setTmTestStepMessageProfiles(null);
        }
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public List<TestRoles> getPossibleInitiatorTestRoles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleInitiatorTestRoles");
        }
        if (autoResponse) {
            return getPossibleTestRoles();
        } else {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            Query q = entityManager
                    .createQuery("SELECT distinct tr "
                            + "FROM TestRoles tr, TransactionLink tl, ProfileLink pl "
                            + "JOIN tr.roleInTest.testParticipantsList testParticipantsList "
                            + "WHERE pl.actorIntegrationProfile = testParticipantsList.actorIntegrationProfileOption.actorIntegrationProfile "
                            + "AND tl.fromActor = testParticipantsList.actorIntegrationProfileOption.actorIntegrationProfile.actor "
                            + "AND tl.transaction = pl.transaction " + "AND tr.test = :test");
            q.setParameter("test", selectedTest);
            @SuppressWarnings("unchecked")
            List<TestRoles> result = q.getResultList();
            return result;
        }
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public List<Transaction> getPossibleTransactions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleTransactions");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        if (selectedTestSteps != null) {
            if (selectedTestSteps.getTestRolesInitiator() != null) {

                Query q = entityManager
                        .createQuery("SELECT distinct pl.transaction "
                                + "FROM ProfileLink pl, TestRoles tr "
                                + "JOIN tr.roleInTest.testParticipantsList testParticipantsList "
                                + "WHERE pl.actorIntegrationProfile = testParticipantsList.actorIntegrationProfileOption.actorIntegrationProfile "
                                + "AND tr = :testRoles");
                q.setParameter("testRoles", selectedTestSteps.getTestRolesInitiator());
                @SuppressWarnings("unchecked")
                List<Transaction> result = q.getResultList();
                return result;
            }
        }
        return null;
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public List<TestRoles> getPossibleTestRolesResponder() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleTestRolesResponder");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        if (selectedTestSteps.getTransaction() != null) {
            Query q = entityManager
                    .createQuery("SELECT distinct tr "
                            + "FROM ProfileLink pl, TestRoles tr,TestRoles tr2, TransactionLink tl "
                            + "JOIN tr.roleInTest.testParticipantsList testParticipantsList "
                            + "JOIN tr2.roleInTest.testParticipantsList testParticipantsList2 "
                            + "WHERE pl.actorIntegrationProfile = testParticipantsList.actorIntegrationProfileOption.actorIntegrationProfile "
                            + "AND tl.toActor = testParticipantsList.actorIntegrationProfileOption.actorIntegrationProfile.actor "
                            + "AND tl.fromActor = testParticipantsList2.actorIntegrationProfileOption.actorIntegrationProfile.actor "
                            + "AND tl.transaction = pl.transaction " + "AND pl.transaction = :transaction "
                            + "AND tr2=:testRolesInitiator " + "AND tr.test = :test");
            q.setParameter("testRolesInitiator", selectedTestSteps.getTestRolesInitiator());
            q.setParameter("transaction", selectedTestSteps.getTransaction());
            q.setParameter("test", selectedTest);
            @SuppressWarnings("unchecked")
            List<TestRoles> result = q.getResultList();
            if (result != null) {
                result.remove(selectedTestSteps.getTestRolesInitiator());
                return result;
            }
        }
        return null;

    }

    // -----------------------------------------------

    public List<String> getPossibleMessageType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleMessageType");
        }
        if (selectedTestSteps.getTransaction() != null) {
            if (selectedTestSteps.getTestRolesInitiator() != null) {
                EntityManager em = EntityManagerService.provideEntityManager();
                Query query = em
                        .createQuery("SELECT DISTINCT hmp.messageType "
                                + "FROM Hl7MessageProfile hmp,TransactionLink tl,TestRoles tr JOIN tr.roleInTest.testParticipantsList " +
                                "testParticipants "
                                + "WHERE hmp.transaction=tl.transaction "
                                + "AND testParticipants.actorIntegrationProfileOption.actorIntegrationProfile.actor=hmp.actor "
                                + "AND tl.fromActor=hmp.actor " + "AND tl.transaction=:inTransaction "
                                + "AND tr=:inTestRolesInitiator");
                query.setParameter("inTransaction", selectedTestSteps.getTransaction());
                query.setParameter("inTestRolesInitiator", selectedTestSteps.getTestRolesInitiator());
                @SuppressWarnings("unchecked")
                List<String> list = query.getResultList();
                if (list.size() > 0) {
                    Collections.sort(list);
                    return list;
                }
            }
        }
        return null;
    }

    public void initializeContextualInformation(int i) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeContextualInformation");
        }
        this.selectedContextualInformation = new ContextualInformation();
        this.inputOrOutput = i;
    }

    public void addSelectedCIToTestSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addSelectedCIToTestSteps");
        }
        if (this.selectedTestSteps != null) {
            if (this.selectedContextualInformation != null) {
                if (this.inputOrOutput == 1) {
                    if (this.selectedTestSteps.getInputContextualInformationList() == null) {
                        this.selectedTestSteps
                                .setInputContextualInformationList(new ArrayList<ContextualInformation>());
                    }
                    if (this.selectedContextualInformationOld != null) {
                        this.selectedTestSteps.getInputContextualInformationList().remove(
                                this.selectedContextualInformationOld);
                        this.selectedContextualInformationOld = null;
                    }
                    this.selectedTestSteps.getInputContextualInformationList().add(this.selectedContextualInformation);
                } else {
                    if (this.selectedTestSteps.getOutputContextualInformationList() == null) {
                        this.selectedTestSteps
                                .setOutputContextualInformationList(new ArrayList<ContextualInformation>());
                    }
                    if (this.selectedContextualInformationOld != null) {
                        this.selectedTestSteps.getOutputContextualInformationList().remove(
                                this.selectedContextualInformationOld);
                        this.selectedContextualInformationOld = null;
                    }
                    this.selectedTestSteps.getOutputContextualInformationList().add(this.selectedContextualInformation);
                }
            }
        }
    }

    public boolean viewTableOfCIInput() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewTableOfCIInput");
        }
        if (this.selectedTestSteps.getInputContextualInformationList() != null) {
            if (this.selectedTestSteps.getInputContextualInformationList().size() > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean viewTableOfCIOutput() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewTableOfCIOutput");
        }
        if (this.selectedTestSteps.getOutputContextualInformationList() != null) {
            if (this.selectedTestSteps.getOutputContextualInformationList().size() > 0) {
                return true;
            }
        }
        return false;
    }

    public void updateSelectedContextualInformationForInput(ContextualInformation inContextualInformation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateSelectedContextualInformationForInput");
        }
        this.selectedContextualInformation = inContextualInformation;
        this.contextualInformationtype = "input";
    }

    public void updateSelectedContextualInformationForOutput(ContextualInformation inContextualInformation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateSelectedContextualInformationForOutput");
        }
        this.selectedContextualInformation = inContextualInformation;
        this.contextualInformationtype = "output";
    }

    public void deleteSelectedContextualInformation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedContextualInformation");
        }
        if (contextualInformationtype.equals("input")) {
            this.deleteSelectedContextualInformationFromInput();
        } else {
            this.deleteSelectedContextualInformationFromOutput();
        }
    }

    public void deleteSelectedContextualInformationFromInput() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedContextualInformationFromInput");
        }
        if (selectedContextualInformation != null) {
            this.deleteSelectedContextualInformationFromInput(selectedContextualInformation);
        }
    }

    private void deleteSelectedContextualInformationFromInput(ContextualInformation inContextualInformation) {
        if (inContextualInformation != null) {
            List<ContextualInformationInstance> contextualInformationInstanceList = ContextualInformationInstance
                    .getContextualInformationInstanceListForContextualInformation(inContextualInformation);
            if (contextualInformationInstanceList != null) {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "The Selected Contextual Information is used");
                return;
            }
            selectedTestSteps.getInputContextualInformationList().remove(inContextualInformation);
            if (listContextualInformationToRemove == null) {
                listContextualInformationToRemove = new ArrayList<ContextualInformation>();
            }
            listContextualInformationToRemove.add(inContextualInformation);
        }
    }

    public void deleteSelectedContextualInformationFromOutput() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedContextualInformationFromOutput");
        }
        if (selectedContextualInformation != null) {
            this.deleteSelectedContextualInformationFromOutput(selectedContextualInformation);
        }
    }

    private void deleteSelectedContextualInformationFromOutput(ContextualInformation inContextualInformation) {
        if (inContextualInformation != null) {
            List<ContextualInformationInstance> contextualInformationInstanceList = ContextualInformationInstance
                    .getContextualInformationInstanceListForContextualInformation(inContextualInformation);
            if (contextualInformationInstanceList != null) {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "The Selected Contextual Information is used");
                return;
            }
            List<TestSteps> testStepsList = selectedTest.getTestStepsList();
            if (testStepsList != null) {
                for (TestSteps testSteps : testStepsList) {
                    if (testSteps.getInputContextualInformationList() != null) {
                        if (testSteps.getInputContextualInformationList().contains(inContextualInformation)) {
                            FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                                    "The Selected Contextual Information is used as input in other steps.");
                            return;
                        }
                    }
                }
            }
            selectedTestSteps.getOutputContextualInformationList().remove(inContextualInformation);
            listContextualInformationToRemove.add(inContextualInformation);
        }
    }

    public void updateSelectedContextualInformation(ContextualInformation inContextualInformation, int i) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateSelectedContextualInformation");
        }
        this.inputOrOutput = i;
        this.selectedContextualInformation = inContextualInformation;
        this.selectedContextualInformationOld = this.selectedContextualInformation;

    }

    public List<ContextualInformation> getPossibleContextualInformations() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleContextualInformations");
        }
        if (this.selectedTest != null) {
            if (this.selectedTestSteps != null) {
                List<TestSteps> lts = new ArrayList<TestSteps>();
                try {
                    lts = this.selectedTest.getTestStepsListAntecedent(this.selectedTestSteps);
                } catch (Exception e) {
                    LOG.error("", e);
                }
                List<ContextualInformation> res = new ArrayList<ContextualInformation>();
                for (TestSteps ts : lts) {
                    if (ts.getOutputContextualInformationList() != null) {
                        if (ts.getOutputContextualInformationList().size() > 0) {
                            res.addAll(ts.getOutputContextualInformationList());
                        }
                    }
                }
                return res;
            }
        }
        return null;
    }

    public void persistSelectedTestStepsAndDeleteNonUsedCI() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistSelectedTestStepsAndDeleteNonUsedCI");
        }
        this.persistSelectedTestSteps();
        this.deleteNonUsedTmTestStepMessageType();
        this.deleteNonUsedContextualInformation();
    }

    public void deleteNonUsedContextualInformation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteNonUsedContextualInformation");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        for (ContextualInformation ci : listContextualInformationToRemove) {
            if (ci != null) {
                if (ci.getId() != null) {
                    ci = entityManager.find(ContextualInformation.class, ci.getId());
                    List<TestSteps> lts = TestSteps.getTestStepsFiltered(ci);
                    if (lts != null) {
                        if (lts.size() > 0) {
                            return;
                        }
                    }
                    entityManager.remove(ci);
                    entityManager.flush();
                }
            }
        }

        listContextualInformationToRemove = new ArrayList<ContextualInformation>();
    }

    public void editTestStepsAndInitializeVar(TestSteps testSteps) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editTestStepsAndInitializeVar");
        }
        this.editTestSteps(testSteps);
        this.listContextualInformationToRemove = new ArrayList<ContextualInformation>();
        this.profilesToRemove = new ArrayList<TmTestStepMessageProfile>();
    }

    public boolean canEditCIAsInput(ContextualInformation currentCI) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canEditCIAsInput");
        }
        if (this.selectedTestSteps != null) {
            List<ContextualInformation> loutput = this.getPossibleContextualInformations();
            if (loutput.contains(currentCI)) {
                return false;
            }
            return true;
        }
        return false;
    }

    public List<WSTransactionUsage> getPossibleWSTransactionUsages() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleWSTransactionUsages");
        }
        if (this.selectedTestSteps != null) {
            if (this.selectedTestSteps.getTransaction() != null) {
                return WSTransactionUsage.getWSTransactionUsageFiltered(this.selectedTestSteps.getTransaction(), null,
                        null, null);
            }
        }
        return null;
    }

    public boolean canEditUsage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canEditUsage");
        }
        if (this.selectedTestSteps != null) {
            if (this.selectedTestSteps.getTransaction() != null) {
                List<WSTransactionUsage> lwstr = WSTransactionUsage.getWSTransactionUsageFiltered(
                        this.selectedTestSteps.getTransaction(), null, null, null);
                if (lwstr != null) {
                    if (lwstr.size() > 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public List<TestRoles> getListOfTestRoles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfTestRoles");
        }
        TestRolesQuery query = new TestRolesQuery();
        query.test().id().eq(selectedTest.getId());
        return query.getList();
    }

    public List<TestSteps> getListOfTestSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfTestSteps");
        }
        TestStepsQuery query = new TestStepsQuery();
        query.testParent().id().eq(selectedTest.getId());
        return query.getList();
    }

    public TestRoles getSelectedTestRoles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestRoles");
        }
        return selectedTestRoles;
    }

    public void setSelectedTestRoles(TestRoles selectedTestRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestRoles");
        }
        this.selectedTestRoles = selectedTestRoles;
    }

    public String getRoleInTestKeywordTyped() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRoleInTestKeywordTyped");
        }
        return roleInTestKeywordTyped;
    }

    public void setRoleInTestKeywordTyped(String roleInTestKeywordTyped) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setRoleInTestKeywordTyped");
        }
        this.roleInTestKeywordTyped = roleInTestKeywordTyped;
        getFoundRoleInTests().getFilter().modified();
    }

    public FilterDataModel<RoleInTest> getFoundRoleInTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFoundRoleInTests");
        }
        if (foundRoleInTests == null) {
            RoleInTestQuery query = new RoleInTestQuery();
            HQLCriterionsForFilter<RoleInTest> hqlCriterions = query.getHQLCriterionsForFilter();
            TMCriterions.addAIPOCriterions(hqlCriterions, query.testParticipantsList().actorIntegrationProfileOption());
            hqlCriterions.addQueryModifier(this);
            Filter<RoleInTest> filter = new Filter<RoleInTest>(hqlCriterions);
            foundRoleInTests = new FilterDataModel<RoleInTest>(filter) {
                @Override
                protected Object getId(RoleInTest t) {
                    // TODO Auto-generated method stub
                    return t.getId();
                }
            };
        }
        return foundRoleInTests;
    }

    @Override
    public void modifyQuery(HQLQueryBuilder<RoleInTest> queryBuilder, Map<String, Object> filterValuesApplied) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyQuery");
        }
        String searchField;
        searchField = StringUtils.trimToNull(roleInTestKeywordTyped);
        if (searchField != null) {
            queryBuilder.addRestriction(HQLRestrictions.like("keyword", searchField));
        }
        queryBuilder.addOrder("keyword", true);
    }

    public void updateTestRoles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateTestRoles");
        }
        boolean doAdd = false;
        if (selectedTestRoles.getId() != null) {
            doAdd = true;
        } else {
            List<TestRoles> listOfTestRoles = TestRoles.getTestsFiltered(null, null, null, null, selectedTest, null,
                    null, null, null, null);
            if (!TestRoles.testRolesContainsKeyword(selectedTestRoles.getRoleInTest().getKeyword(), listOfTestRoles)) {
                doAdd = true;
            }
        }
        if (doAdd) {
            if (selectedTestRoles.getCardMin() == null) {
                selectedTestRoles.setCardMin(1);
            }
            if (selectedTestRoles.getCardMax() == null) {
                selectedTestRoles.setCardMax(1);
            }

            if (selectedTestRoles.getCardMax() < selectedTestRoles.getCardMin()) {
                int tmp = selectedTestRoles.getCardMax();
                selectedTestRoles.setCardMax(selectedTestRoles.getCardMin());
                selectedTestRoles.setCardMin(tmp);
            }

            if (selectedTestRoles.getCardMax() > MAX_PARTICIPANTS) {
                selectedTestRoles.setCardMax(MAX_PARTICIPANTS);
            }
            selectedTestRoles.setTest(selectedTest);

            try {
                EntityManager entityManager = EntityManagerService.provideEntityManager();
                selectedTestRoles = entityManager.merge(selectedTestRoles);
                entityManager.flush();
            } catch (Exception e) {
                ExceptionLogging.logException(e, LOG);
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Errors : " + e.getMessage());
            }
            cancelEditingTestRoles();
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "This test already contains the role " + roleInTestKeywordTyped);
        }

    }

    public void cancelEditingTestRoles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelEditingTestRoles");
        }
        selectedTestRoles = null;
    }

    public void addTestRoles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addTestRoles");
        }
        selectedTestRoles = new TestRoles();
        selectedTestRoles.setNumberOfTestsToRealize(1);
        selectedTestRoles.setCardMin(0);
        selectedTestRoles.setCardMax(1);
        selectedTestRoles.setTestOption(TestOption.getTEST_OPTION_REQUIRED());
        roleInTestKeywordTyped = "";
    }

    public void resetFilterRoleInTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetFilterRoleInTests");
        }
        getFoundRoleInTests().getFilter().clear();
        setRoleInTestKeywordTyped(null);
    }

    public void deleteTestRole() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteTestRole");
        }
        if (selectedTestRoles == null) {
            return;
        }

        // GZL-2972 Remove linked test steps before test role
        List<TestSteps> tsl = selectedTestRoles.getTestStepsList();
        if (tsl != null) {
            for (TestSteps ts : tsl) {
                setSelectedTestSteps(ts);
                deleteTestSteps(ts);
            }
        }

        try {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            selectedTestRoles = entityManager.find(TestRoles.class, selectedTestRoles.getId());
            entityManager.remove(selectedTestRoles);
            entityManager.flush();
            cancelEditingTestRoles();
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Test role deleted");
        } catch (PersistenceException pex) {
            Throwable throwable = pex.getCause();
            if (throwable instanceof ConstraintViolationException) {
                ConstraintViolationException cve = (ConstraintViolationException) throwable;
                SQLException sqlException = cve.getSQLException().getNextException();
                if (sqlException != null) {
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                            "Problem deleting role, there is a constraint exception : " + sqlException.getMessage());
                }

            }
        } catch (Exception e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Problem deleting role " + e.getMessage());
        }

    }

    public void selectRoleInTestAndUpdateKeyword(RoleInTest inRoleInTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("selectRoleInTestAndUpdateKeyword");
        }

        this.selectedTestRoles.setRoleInTest(inRoleInTest);
        this.roleInTestKeywordTyped = this.selectedTestRoles.getRoleInTest().getKeyword();
    }

    // Presets
    public boolean containCookie() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("containCookie");
        }
        return CookiesPreset.instance().containTestCookie("testsListCookie", getFoundTests().getFilter());
    }

    public boolean shortcutsIsFull() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("shortcutsIsFull");
        }
        return CookiesPreset.instance().shortcutsIsFull("testsListCookie");
    }

    public String getPresetName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPresetName");
        }
        return CookiesPreset.instance().getPresetNameTest("testsListCookie", getFoundTests().getFilter());
    }

    public String getShortcutName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getShortcutName");
        }
        return CookiesPreset.instance().getShortcutName();
    }

    public void setShortcutName(String shortcutName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setShortcutName");
        }
        CookiesPreset.instance().setShortcutName(shortcutName);
    }

    public String createCookie() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createCookie");
        }
        CookiesPreset.instance().createCookieTest("testsListCookie", getFoundTests().getFilter());
        return "/testing/testsDefinition/testsList.seam" + "?" + getFoundTests().getFilter().getUrlParameters();
    }

    public String getSelectedPreset() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedPreset");
        }
        return CookiesPreset.instance().getSelectedTestPreset();
    }

    public void setSelectedPreset(String selectedPreset) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedPreset");
        }
        CookiesPreset.instance().setSelectedTestPreset(selectedPreset);
    }

    public String deleteSelectedCookie(String presetName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedCookie");
        }
        CookiesPreset.instance().deleteSelectedTestCookie(presetName);
        return "/testing/testsDefinition/testsList.seam";
    }

    public void saveNewMainPage(ValueChangeEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveNewMainPage");
        }
        CookiesPreset.instance().deleteCookieToLoadTest("cookieToLoadTest");
        if (getSelectedPreset() != null) {
            CookiesPreset.instance().setMainPageCookie(true);
            createCookieToLoadIt((String) e.getNewValue());
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Main page is changed !");
        }
    }

    public void createCookieToLoadIt(String encodedUri) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createCookieToLoadIt");
        }
        if (encodedUri == null) {
            encodedUri = "None";
        }
        CookiesPreset.instance().createSimpleTestCookie("cookieToLoadTest", encodedUri);
    }

    public List<InfoCookies> getAllPresets() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllPresets");
        }
        return CookiesPreset.instance().getAllPresets("testsListCookie");
    }

    public List<SelectItem> getListOfPresets() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfPresets");
        }
        return CookiesPreset.instance().getListOfPresets(getAllPresets());
    }

    public String getCookieToLoadUri() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCookieToLoadUri");
        }
        return CookiesPreset.instance().getCookieUri("cookieToLoadTest");
    }

    public boolean isMainTestPage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isMainTestPage");
        }
        return mainTestPage;
    }

    public void setMainTestPage(boolean mainTestPage) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMainTestPage");
        }
        this.mainTestPage = mainTestPage;
        loadNewMainPage();
    }

    @Create
    public void create() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("create");
        }
        CookiesPreset.instance().setMainPageCookie(true);
    }

    public void loadNewMainPage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("loadNewMainPage");
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext externalContext = fc.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        String cookieUri = getCookieToLoadUri();
        String currentUri = CookiesPreset.instance().getUriWithParamTest(request, getFoundTests().getFilter());
        String baseUri = "/testing/testsDefinition/testsList.seam?testStatus=1";

        if (currentUri != null && currentUri.contains("testType")) {
            String testTypePattern = "testType=(.+)(&)";
            Pattern regex = Pattern.compile(testTypePattern, Pattern.DOTALL);
            Matcher regexMatcher = regex.matcher(currentUri);
            if (regexMatcher.find()) {
                String testTypeVal = regexMatcher.group(1);
                baseUri = "/testing/testsDefinition/testsList.seam?testType=" + testTypeVal + "&testStatus=1";
            }
        }

        int uriLength = baseUri.length();

        if ((cookieUri != null) && !cookieUri.isEmpty() && !cookieUri.equals("None") && !cookieUri.equals(currentUri)
                && (currentUri.length() == uriLength) && CookiesPreset.instance().isMainPageCookie()) {
            String viewId = cookieUri;
            CookiesPreset.instance().setMainPageCookie(false);
            try {
                externalContext.redirect(externalContext.getRequestContextPath() + viewId);
            } catch (IOException e) {
                LOG.error("" + e);
            }

        }
    }

    /*==================================================================*
     *                                                                  *
     *                          ITB Additions                           *
     *                                                                  *
     *=================================================================*/

    public List<String> retrievePossibleResponseMessageTypes() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("retrievePossibleResponseMessageTypes");
        }
        if (selectedTestSteps.getTransaction() != null) {
            if ((selectedTestSteps.getTestRolesInitiator() != null)
                    && (selectedTestSteps.getTestRolesResponder() != null)
                    && !(StringUtils.isEmpty(selectedTestSteps.getMessageType()))) {
                EntityManager em = EntityManagerService.provideEntityManager();
                Query query = em
                        .createQuery("SELECT DISTINCT hmp.messageType "
                                + "FROM Hl7MessageProfile hmp,TransactionLink tl,TestRoles tr JOIN tr.roleInTest.testParticipantsList " +
                                "testParticipants "
                                + "WHERE hmp.transaction=tl.transaction "
                                + "AND testParticipants.actorIntegrationProfileOption.actorIntegrationProfile.actor=hmp.actor "
                                + "AND tl.toActor=hmp.actor " + "AND tl.transaction=:inTransaction "
                                + "AND tr=:inTestRolesResponder");
                query.setParameter("inTransaction", selectedTestSteps.getTransaction());
                query.setParameter("inTestRolesResponder", selectedTestSteps.getTestRolesResponder());
                @SuppressWarnings("unchecked")
                List<String> list = query.getResultList();
                if (list.size() > 0) {
                    list = filterMessageTypesByEventCode(
                            list,
                            selectedTestSteps.getMessageType().substring(
                                    selectedTestSteps.getMessageType().indexOf(MESSAGE_EVENT_CODE_SEPARATOR) + 1,
                                    selectedTestSteps.getMessageType().length()));
                    Collections.sort(list);
                    return list;
                }
            }
        }
        return null;
    }

    public List<String> filterMessageTypesByEventCode(List<String> messageTypes, String eventCode) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("filterMessageTypesByEventCode");
        }

        List<String> removedMessageTypes = new ArrayList<String>();
        if ((messageTypes != null) && (messageTypes.size() > 0)) {
            for (Iterator<String> itr = messageTypes.iterator(); itr.hasNext(); ) {
                String str = itr.next();
                if (!(str.substring(str.indexOf(MESSAGE_EVENT_CODE_SEPARATOR) + 1, str.length())
                        .equalsIgnoreCase(eventCode))) {
                    removedMessageTypes.add(str);
                    itr.remove();
                }
            }
        }

        // added this bit in to avoid returning no matching response
        // message types if event codes don't match up
        if (messageTypes != null && messageTypes.size() == 0) {
            messageTypes = removedMessageTypes;
        }

        return messageTypes;
    }

    public List<Hl7MessageProfile> findValidationProfile(String messageType, String messageDirection) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findValidationProfile");
        }

        if (selectedTestSteps.getTransaction() != null) {
            if (!(StringUtils.isEmpty(messageType))
                    && !(StringUtils.isEmpty(selectedTestSteps.getTransaction().getKeyword()))) {
                List<Hl7MessageProfile> profiles = Hl7MessageProfile.getHL7MessageProfilesFiltered(null, null,
                        selectedTestSteps.getTransaction().getKeyword(), null, messageType, null);
                return profiles;
            }
        }

        return new ArrayList<Hl7MessageProfile>();
    }

    public void addMessageProfileType(Hl7MessageProfile messageProfile, MessageDirection messageDirection) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addMessageProfileType");
        }

        TmTestStepMessageProfile profile = selectedTestSteps.getMessageProfile(messageDirection);
        if ((profile != null)
                && ((profile.getTfHl7MessageProfile().getProfileOid().equalsIgnoreCase(messageProfile.getProfileOid())))) {
            manageConfigFilesForStepMsgProfile(profile);
            return; // since we already have this profile we do not want to add it again.
        }

        TmTestStepMessageProfile messageTypeToAdd = new TmTestStepMessageProfile();
        messageTypeToAdd.setTfHl7MessageProfile(messageProfile);

        // 1 = boolean TRUE
        // 0 = boolean FALSE
        // by default we want the message auto validated
        messageTypeToAdd.setAutoValidate(Integer.valueOf(1).byteValue());
        messageTypeToAdd.setDirection(messageDirection);
        manageConfigFilesForStepMsgProfile(messageTypeToAdd);

        if (MessageDirection.REQUEST.equals(messageDirection)) {

            messageTypeToAdd.getMessageValidationServices().add(
                    newMessageValidationService(selectedReqValidationService));

        }

        if (MessageDirection.RESPONSE.equals(messageDirection)) {

            messageTypeToAdd.getMessageValidationServices().add(
                    newMessageValidationService(selectedRspValidationService));

        }

        if (selectedTestSteps.getTmTestStepMessageProfiles() == null) {
            selectedTestSteps.setTmTestStepMessageProfiles(new ArrayList<TmTestStepMessageProfile>());
        }

        for (TmTestStepMessageProfile currProfile : selectedTestSteps.getTmTestStepMessageProfiles()) {
            if (messageDirection.equals(currProfile.getDirection())) {
                if (!currProfile.getTfHl7MessageProfile().getProfileOid()
                        .equalsIgnoreCase(messageProfile.getProfileOid())) {
                    profilesToRemove.add(currProfile);
                }
            }
        }

        if (profilesToRemove == null) {
            profilesToRemove = new ArrayList<TmTestStepMessageProfile>();
        }

        for (TmTestStepMessageProfile profToRemove : profilesToRemove) {
            selectedTestSteps.removeTmTestStepMessageProfile(profToRemove);
        }

        selectedTestSteps.addTmTestStepMessageProfile(messageTypeToAdd);

    }

    public List<ValidationService> getValidationServices() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValidationServices");
        }
        return (new ValidationServiceDAO(EntityManagerService.provideEntityManager()).findAll());
    }

    public void resetMessageTypes() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetMessageTypes");
        }
        if (selectedTestSteps != null) {
            selectedTestSteps.setTmTestStepMessageProfiles(null);
            selectedTestSteps.setResponderMessageType(null);
            selectedReqMsgType = null;
            selectedRspMsgType = null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getHL7Versions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getHL7Versions");
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        Query query = em.createQuery("SELECT DISTINCT hl7Profile.hl7Version from Hl7MessageProfile hl7profile");
        List<String> hl7Versions = query.getResultList();
        if ((hl7Versions != null) && (hl7Versions.size() > 0)) {
            Collections.sort(hl7Versions);
        }

        return hl7Versions;
    }

    public void deleteNonUsedTmTestStepMessageType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteNonUsedTmTestStepMessageType");
        }

        EntityManager entityManager = EntityManagerService.provideEntityManager();

        if (profilesToRemove != null) {
            if (profilesToRemove.size() > 0) {
                for (TmTestStepMessageProfile msgType : profilesToRemove) {
                    if (msgType != null) {
                        if (!isMsgTypeStillAttachedToTestStep(selectedTestSteps, msgType)) {
                            TmTestStepMessageProfile.deleteTmTestStepMessageProfile(entityManager, msgType);
                        }
                    }
                }
            }
        }

        profilesToRemove = new ArrayList<TmTestStepMessageProfile>();
    }

    public boolean isMsgTypeStillAttachedToTestStep(TestSteps inTestStep, TmTestStepMessageProfile msgType)
            throws IllegalArgumentException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isMsgTypeStillAttachedToTestStep");
        }
        boolean result = false;

        if (inTestStep == null) {
            throw new IllegalArgumentException("TestSteps cannot be null.");
        }
        if (msgType == null) {
            throw new IllegalArgumentException("TmTestStepMessageProfile cannot be null.");
        }
        if (inTestStep.getTmTestStepMessageProfiles() == null) {
            return false; // if the TestSteps doesn't have any TmTestStepMessageProfile objects, impossible for this msgType to be attached.
        }
        if (inTestStep.getTmTestStepMessageProfiles().size() < 1) {
            return false;
        }

        for (TmTestStepMessageProfile profile : inTestStep.getTmTestStepMessageProfiles()) {
            if (profile.getId() == msgType.getId()) {
                result = true;
                break;
            }
        }

        return result;
    }

    public void deletedNonUsedMessageValidationService() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deletedNonUsedMessageValidationService");
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        MessageValidationServiceDAO mvsDAO = new MessageValidationServiceDAO(em);
        if (validationServicesToRemove != null) {
            if (validationServicesToRemove.size() > 0) {
                for (MessageValidationService srv : validationServicesToRemove) {
                    if (srv != null) {
                        mvsDAO.deleteMessageValidationService(srv);
                    }
                }
            }
        }

        validationServicesToRemove = new ArrayList<MessageValidationService>();
    }

    public MessageValidationService newMessageValidationService(ValidationService service) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("newMessageValidationService");
        }
        MessageValidationService mvs = new MessageValidationService();
        mvs.setValidationService(service);
        return mvs;
    }

    public ValidationService getValidationService(TestSteps inTestStep, MessageDirection direction) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getValidationService");
        }
        ValidationService service = null;
        TmTestStepMessageProfile profile = inTestStep.getMessageProfile(direction);
        if (profile != null) {
            if (profile.getMessageValidationServices() != null) {
                if (profile.getMessageValidationServices().size() > 0) {
                    switch (direction) {
                        case REQUEST:
                            service = profile.getMessageValidationServices().toArray(
                                    new MessageValidationService[profile.getMessageValidationServices().size()])[0]
                                    .getValidationService();
                            break;
                        case RESPONSE:
                            service = profile.getMessageValidationServices().toArray(
                                    new MessageValidationService[profile.getMessageValidationServices().size()])[0]
                                    .getValidationService();
                            break;
                    }
                }
            }
        }

        return service;
    }

    public void updateValidationService(TestSteps inTestStep, MessageDirection direction, ValidationService srv) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateValidationService");
        }

        TmTestStepMessageProfile profile = inTestStep.getMessageProfile(direction);

        if (profile != null) {
            if (profile.getMessageValidationServices() != null) {

                MessageValidationService msgValSrv = new MessageValidationService();
                msgValSrv.setValidationService(srv);
                profile.getMessageValidationServices().add(msgValSrv);

            }
        }
    }

    public void fileUploadListener(FileUploadEvent event) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("fileUploadListener");
        }

        UploadedFile fileToUpload = event.getUploadedFile();

        RoleInTestEnum role = RoleInTestEnum.getRoleInTest((String) event.getComponent().getAttributes()
                .get(TEST_STEP_ROLE));
        TestStepConfigurationFileType configurationFileType = TestStepConfigurationFileType
                .getConfigurationFileType((String) event.getComponent().getAttributes().get(CONFIG_FILE_TYPE));

        processFileUpload(fileToUpload, role, configurationFileType);

    }

    private void processFileUpload(UploadedFile item, RoleInTestEnum role, TestStepConfigurationFileType configFile) {

        if (item != null) {

            TSMPConfigFile file = new TSMPConfigFile();

            file.setData(item.getData());
            file.setName(item.getName());

            if (role != null) {

                if (configFile != null) {

                    if (configFile.equals(TestStepConfigurationFileType.DATA_SHEET)) {
                        dataSheetFile = file;
                    } else {
                        switch (role) {

                            case INITIATOR:

                                switch (configFile) {

                                    case VALIDATION_CONTEXT:
                                        initiatorValidationCtxFile = file;
                                        break;
                                    case CODE_TABLE:
                                        initiatorCodeTableFile = file;
                                        break;
                                    case VALUE_SETS:
                                        initiatorValueSetsFile = file;
                                        break;
                                    case DATA_SHEET:
                                        // handled outside of a particular role
                                        break;
                                    case BASE_VALIDATION_PROFILE:
                                        break;
                                    case EXAMPLE_MESSAGE:
                                        initiatorExampleMsg = file;
                                        break;
                                }

                                break;

                            case RESPONDER:

                                switch (configFile) {

                                    case VALIDATION_CONTEXT:
                                        responderValidationCtxFile = file;
                                        break;
                                    case CODE_TABLE:
                                        responderCodeTableFile = file;
                                        break;
                                    case VALUE_SETS:
                                        responderValueSetsFile = file;
                                        break;
                                    case DATA_SHEET:
                                        // handled outside of a particular role
                                        break;
                                    case BASE_VALIDATION_PROFILE:
                                        break;
                                    case EXAMPLE_MESSAGE:
                                        responderExampleMsg = file;
                                        break;
                                }

                                break;
                        }
                    }
                }
            }
        }
    }

    /**
     * This method null's the current value for the configuration file that the user wishes to change.
     *
     * @param event
     * @throws Exception
     */
    public void changeFile(ActionEvent event) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("changeFile");
        }

        RoleInTestEnum role = RoleInTestEnum.getRoleInTest((String) event.getComponent().getAttributes()
                .get(TEST_STEP_ROLE));
        TestStepConfigurationFileType configurationFileType = TestStepConfigurationFileType
                .getConfigurationFileType((String) event.getComponent().getAttributes().get(CONFIG_FILE_TYPE));

        if (role != null) {

            if (configurationFileType != null) {
                switch (role) {

                    case INITIATOR:

                        switch (configurationFileType) {

                            case VALIDATION_CONTEXT:
                                initiatorValidationCtxFile = null;
                                break;
                            case CODE_TABLE:
                                initiatorCodeTableFile = null;
                                break;
                            case VALUE_SETS:
                                initiatorValueSetsFile = null;
                                break;
                            case DATA_SHEET:
                                dataSheetFile = null;
                                break;
                            case BASE_VALIDATION_PROFILE:
                                break;
                            case EXAMPLE_MESSAGE:
                                initiatorExampleMsg = null;
                                break;
                        }

                        break;

                    case RESPONDER:

                        switch (configurationFileType) {

                            case VALIDATION_CONTEXT:
                                responderValidationCtxFile = null;
                                break;
                            case CODE_TABLE:
                                responderCodeTableFile = null;
                                break;
                            case VALUE_SETS:
                                responderValueSetsFile = null;
                                break;
                            case DATA_SHEET:
                                dataSheetFile = null;
                                break;
                            case BASE_VALIDATION_PROFILE:
                                break;
                            case EXAMPLE_MESSAGE:
                                responderExampleMsg = null;
                                break;
                        }

                        break;
                }
            }
        }
    }

    public void cancelFileUpload(ActionEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelFileUpload");
        }
        initializeConfigurationFiles(selectedTestSteps);
    }

    public void initializeConfigurationFiles(TestSteps ts) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeConfigurationFiles");
        }

        if (ts != null) {

            // TODO: add logic to assign value for data sheet file once implemented
            dataSheetFile = null;

            TmTestStepMessageProfile itsmp = ts.getRequestMessageProfile();
            TmTestStepMessageProfile rtsmp = ts.getResponseMessageProfile();

            if (itsmp != null) {
                initiatorValidationCtxFile = (itsmp.getValidationContextContent() != null) ? generateConfigFile(
                        itsmp.getValidationContextContent(), itsmp.getValidationContextFileName()) : null;
                initiatorExampleMsg = (itsmp.getExampleMsgContent() != null) ? generateConfigFile(
                        itsmp.getExampleMsgContent(), itsmp.getExampleMsgFileName()) : null;
                // TODO: add logic to assign value for initiator code table and valueset file initialization once implemented

                initiatorCodeTableFile = null;
                initiatorValueSetsFile = null;
            }

            if (rtsmp != null) {
                responderValidationCtxFile = (rtsmp.getValidationContextContent() != null) ? generateConfigFile(
                        rtsmp.getValidationContextContent(), rtsmp.getValidationContextFileName()) : null;
                responderExampleMsg = (rtsmp.getExampleMsgContent() != null) ? generateConfigFile(
                        rtsmp.getExampleMsgContent(), rtsmp.getExampleMsgFileName()) : null;
                // TODO: add logic to assign value for responder code table and valueset file initialization once implemented
                responderCodeTableFile = null;
                responderValueSetsFile = null;
            }

        }
    }

    /**
     * Converts the supplied parameters into a TSCMPConfigFile object
     *
     * @param content
     * @param fileName
     */
    public TSMPConfigFile generateConfigFile(String content, String fileName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateConfigFile");
        }

        TSMPConfigFile file = null;

        if (!StringUtils.isEmpty(content) && !StringUtils.isEmpty(fileName)) {
            file = new TSMPConfigFile();
            file.setData(content.getBytes(Charset.forName(StandardCharsets.UTF_8.name())));
            file.setName(fileName);
            return file;
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Configuration file content or name cannot be empty.");
            file = null;
            return file;
        }

    }

    public void deleteConfigFile(ActionEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteConfigFile");
        }

    }

    /**
     * Manages Updating the TmTestStepMessageProfile object with configuration files.
     *
     * @param profile
     */
    public void manageConfigFilesForStepMsgProfile(TmTestStepMessageProfile profile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("manageConfigFilesForStepMsgProfile");
        }

        TSMPConfigFile validationCtx = null;
        TSMPConfigFile exampleMsg = null;
        TSMPConfigFile codeTable = null;
        TSMPConfigFile valueSets = null;

        switch (profile.getDirection()) {

            case REQUEST:

                validationCtx = initiatorValidationCtxFile;
                exampleMsg = initiatorExampleMsg;
                codeTable = initiatorCodeTableFile;
                valueSets = initiatorValueSetsFile;

                break;

            case RESPONSE:

                validationCtx = responderValidationCtxFile;
                exampleMsg = responderExampleMsg;
                codeTable = responderCodeTableFile;
                valueSets = responderValueSetsFile;

                break;
        }

        if (validationCtx != null) {
            profile.setValidationContextContent(validationCtx.getDataAsString());
            profile.setValidationContextFileName(validationCtx.getName());
        }

        if (exampleMsg != null) {
            profile.setExampleMsgContent(exampleMsg.getDataAsString());
            profile.setExampleMsgFileName(exampleMsg.getName());
        }

        // TODO: implement logic to handle code tables once implemented
        if (codeTable != null) {

        }

        // TODO: implement logic to handle valuesets once implemented
        if (valueSets != null) {

        }
    }
}
