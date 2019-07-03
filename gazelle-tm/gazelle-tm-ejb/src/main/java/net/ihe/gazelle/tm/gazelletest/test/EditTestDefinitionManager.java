package net.ihe.gazelle.tm.gazelletest.test;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.log.ExceptionLogging;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.tm.filter.TMCriterions;
import net.ihe.gazelle.tm.gazelletest.model.definition.*;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestStepsInstance;
import net.ihe.gazelle.users.model.User;
import org.apache.commons.lang.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.sql.SQLException;
import java.util.*;

@Name("editTestDefinitionManager")
@GenerateInterface(value = "EditTestDefinitionLocal")
@Scope(ScopeType.PAGE)
public class EditTestDefinitionManager implements EditTestDefinitionLocal, QueryModifier<RoleInTest> {

    private static final Logger LOG = LoggerFactory.getLogger(EditTestDefinitionManager.class);
    public static final String HISTORY = "History";
    private static int MAX_PARTICIPANTS = 99;
    private List<TestType> testTypeList;
    private List<TestPeerType> possibleTestPeerTypes;
    private List<TestStatus> testStatusList;
    //TODO remove after ITB refactoring
    private boolean testDescriptionTranslationMode = false;
    private TestDescription editedTestDescription;
    private TestRoles selectedTestRole;
    private String roleInTestKeywordTyped;
    private FilterDataModel<RoleInTest> foundRoleInTests;
    private String backToTests;
    private TestDescription translatedTestDescription = null;
    private Test editedTest = null;
    private TestSteps selectedTestSteps;

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

    public TestRoles getSelectedTestRole() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestRole");
        }
        return selectedTestRole;
    }

    public void setSelectedTestRole(TestRoles selectedTestRole) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestRole");
        }
        this.selectedTestRole = selectedTestRole;
    }

    public TestDescription getEditedTestDescription() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEditedTestDescription");
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        String newDescription = fc.getExternalContext().getRequestParameterMap().get("newDescription");
        if ("true".equals(newDescription)) {
            editedTestDescription = new TestDescription();
            editedTestDescription.initDefaultValues();
        }

        return editedTestDescription;
    }

    public void setEditedTestDescription(TestDescription inTestDescription) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setEditedTestDescription");
        }
        this.editedTestDescription = inTestDescription;
    }

    public void setEditedTestDescriptionId(int inTestDescriptionId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setEditedTestDescription");
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        this.editedTestDescription = em.find(TestDescription.class, inTestDescriptionId);
    }

    public TestDescription getTranslatedTestDescription() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTranslatedTestDescription");
        }
        if (translatedTestDescription == null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            String testId = fc.getExternalContext().getRequestParameterMap().get("id");
            if (testId != null) {
                setEditedTest(Integer.valueOf(testId));
            }

            String translatedTestDescriptionId = fc.getExternalContext().getRequestParameterMap().get("translatedTestDescriptionId");
            if (translatedTestDescriptionId != null) {
                EntityManager em = EntityManagerService.provideEntityManager();
                translatedTestDescription = em.find(TestDescription.class, Integer.valueOf(translatedTestDescriptionId));
                this.editedTestDescription = new TestDescription();
                this.editedTestDescription.initDefaultValues();
                this.testDescriptionTranslationMode = true;
            }
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

    public Test getEditedTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEditedTest");
        }
        if (editedTest == null) {

            FacesContext fc = FacesContext.getCurrentInstance();
            String testId = fc.getExternalContext().getRequestParameterMap().get("id");
            String testDescId = fc.getExternalContext().getRequestParameterMap().get("testDescId");
            backToTests = fc.getExternalContext().getRequestParameterMap().get("backToTests");

            if (testId != null) {
                setEditedTest(Integer.valueOf(testId));
            } else {
                String newTest = fc.getExternalContext().getRequestParameterMap().get("new");
                if ("1".equals(newTest)) {
                    createAndEditNewTest();
                }
            }
            if (testDescId != null) {
                // Request to find TestDescription with id
                int i = Integer.parseInt(testDescId);
                EntityManager em = EntityManagerService.provideEntityManager();
                TestDescription inTestDescription = em.find(TestDescription.class, i);
                editTestDescription(inTestDescription);
            }
        } else {

        }
        return editedTest;
    }

    public void setEditedTest(Test editedTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setEditedTest");
        }
        this.editedTest = editedTest;
    }

    private void setEditedTest(int testId) {
        EntityManager em = EntityManagerService.provideEntityManager();
        setEditedTest(em.find(Test.class, testId));
    }

    private void createAndEditNewTest() {
        this.editedTest = new Test();
        this.editedTest.setTestType(TestType.getTYPE_CONNECTATHON());
        this.editedTest.setTestStatus(TestStatus.getSTATUS_READY());
        this.editedTest.setOrchestrable(false);
        this.editedTestDescription = new TestDescription();
        this.editedTestDescription.initDefaultValues();
        editedTestDescription = EntityManagerService.provideEntityManager().merge(editedTestDescription);
        EntityManagerService.provideEntityManager().flush();
        ArrayList<TestDescription> testDescriptions = new ArrayList<TestDescription>();
        testDescriptions.add(this.editedTestDescription);
        this.editedTest.setTestDescription(testDescriptions);

        this.testDescriptionTranslationMode = false;
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

    public List<TestStatus> getPossibleTestStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleTestStatus");
        }
        if (testStatusList == null) {
            testStatusList = TestStatus.getListStatus();
        }
        return testStatusList;
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

    public String backTo(int testId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("backTo");
        }
        String redirectTo;
        if (testId == 0) {
            redirectTo = "/testing/testsDefinition/testsList.xhtml";
        } else {
            redirectTo = "/testing/testsDefinition/viewTestPage.xhtml?id=" + testId;
        }
        return redirectTo;
    }

    public String getTestPermalink(int testId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestPermalink");
        }
        return ApplicationPreferenceManager.instance().getApplicationUrl() + "test.seam?id=" + testId;
    }

    /**
     * Find the list of possible Peer Types for tests
     */
    private void findTestPeerTypes() {
        possibleTestPeerTypes = TestPeerType.getPeerTypeList();
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public boolean validateTestKeyword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateTestKeyword");
        }
        if (editedTest == null) {
            return false;
        }
        if ((editedTest.getKeyword() == null) || editedTest.getKeyword().trim().equals("")) {
            return false;
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        if (editedTest.getId() == null) {
            Query q = entityManager.createQuery("from Test t where t.keyword=:testKeyword");
            q.setParameter("testKeyword", editedTest.getKeyword());

            if (q.getResultList().size() > 0) {
                FacesMessages.instance().addToControl("keyword",
                        "#{messages['gazelle.tm.testing.testsDefinition.testKeywordNotValid']}");

                return false;
            }
        } else {
            Test tmp = entityManager.find(Test.class, editedTest.getId());
            if (editedTest.getKeyword().equals(tmp.getKeyword())) {
                return true;
            } else {
                Query q = entityManager.createQuery("from Test t where t.keyword=:testKeyword");
                q.setParameter("testKeyword", editedTest.getKeyword());

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
        if (editedTest == null) {
            return false;
        }
        if ((editedTest.getName() == null) || editedTest.getName().trim().equals("")) {
            return false;
        }
        return true;
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public void persistTest(Test inTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistTest");
        }
        setEditedTest(inTest);
        persistTest();
    }

    public void traceTestValidated() {
        User user = User.loggedInUser();
        String comment = "Test Validated by " + user.getUsername();
        traceHistory(user, comment);
    }

    public void traceTestUnValidated() {
        User user = User.loggedInUser();
        String comment = "Test UnValidated by " + user.getUsername();
        traceHistory(user, comment);
    }

    public void traceTestUpdated() {
        User user = User.loggedInUser();
        String comment = "Test Updated by " + user.getUsername();
        traceHistory(user, comment);
    }

    protected void traceHistory(User user, String comment) {
        EntityManager em = EntityManagerService.provideEntityManager();
        TestDefinitionManager tdm = new TestDefinitionManager();
        UserComment comToAdd = new UserComment();

        // Create history comment
        comToAdd.setCommentContent(comment);
        comToAdd.setUser(HISTORY);
        comToAdd.setCreationDate(new Date());
        comToAdd = em.merge(comToAdd);

        // Add history comment to test
        tdm.setSelectedTest(editedTest);
        tdm.setUserComment(comToAdd);
        tdm.linkCommentToTest(em, editedTest);

        editedTest = em.find(Test.class, tdm.getSelectedTest().getId());

        em.flush();
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public String persistTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistTest");
        }
        if (editedTest == null) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Test value is null cannot go further");
            return "";
        }

        try {
            if (validateTestKeyword() && validateTestName()) {
                String currentUser = User.loggedInUser().getUsername();
                EntityManager entityManager = EntityManagerService.provideEntityManager();
                boolean updateMode = (editedTest.getId() != null);
                if (!updateMode) {
                    editedTest.setAuthor(currentUser);
                }

                if (updateMode) {
                    Test currentTestInDb = entityManager.find(Test.class, editedTest.getId());
                    if (((currentTestInDb.getValidated() == null) || !currentTestInDb.getValidated())
                            && ((editedTest.getValidated() != null) && editedTest.getValidated())) {
                        FacesMessages.instance().add(StatusMessage.Severity.INFO, "Test is validated by " + currentUser);
                        editedTest.setLastValidatorId(currentUser);
                        traceTestValidated();
                    } else if (((currentTestInDb.getValidated() != null) && currentTestInDb.getValidated())
                            && ((editedTest.getValidated() != null) && !editedTest.getValidated())) {
                        FacesMessages.instance().add(StatusMessage.Severity.INFO, "Test is no more validated");
                        editedTest.setLastValidatorId(null);
                        traceTestUnValidated();
                    }
                } else if ((editedTest.getValidated() != null) && editedTest.getValidated()) {
                    FacesMessages.instance().add(StatusMessage.Severity.INFO, "Test is validated by " + currentUser);
                    editedTest.setLastValidatorId(currentUser);
                    traceTestValidated();
                }

                editedTest = entityManager.merge(editedTest);
                entityManager.flush();
                if (updateMode) {
                    FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, "gazelle.tm.testing.testsDefinition.testUpdated");
                } else {
                    FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, "net.ihe.gazelle.tm.TestIsCreatedBy", currentUser);
                }
                return backTo(editedTest.getId());
            } else {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Problem with validation of current test");
                return "";
            }
        } catch (Exception e) {
            ExceptionLogging.logException(e, LOG);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Impossible to persist test : " + e.getMessage());
            return "";
        }
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public void editTestDescription(TestDescription inTestDescription) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editTestDescription");
        }
        this.setEditedTestDescription(inTestDescription);

        if (this.editedTestDescription == null) {
            this.editedTestDescription = new TestDescription();
            this.editedTestDescription.initDefaultValues();
        }
        if (this.editedTestDescription.getGazelleLanguage() == null) {
            this.editedTestDescription.setGazelleLanguage(GazelleLanguage.getDefaultLanguage());
        }
        this.testDescriptionTranslationMode = false;
    }

    public List<GazelleLanguage> getLanguageList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLanguageList");
        }
        Set<GazelleLanguage> gazelleLanguageList = new TreeSet<GazelleLanguage>(GazelleLanguage.getLanguageList());
        if (editedTest != null) {
            List<TestDescription> testDescriptionList = editedTest.getTestDescription();
            if (testDescriptionList != null) {
                for (TestDescription testDescription : testDescriptionList) {
                    gazelleLanguageList.remove(testDescription.getGazelleLanguage());
                }
            }
            if ((editedTestDescription != null) && (editedTestDescription.getGazelleLanguage() != null)) {
                gazelleLanguageList.add(editedTestDescription.getGazelleLanguage());
            }
        }
        return new ArrayList<GazelleLanguage>(gazelleLanguageList);
    }

    @Restrict("#{s:hasPermission('TestsDefinitionsAdministrationManager', 'EditTest', null)}")
    public String addTestDescription() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addTestDescription");
        }
        if (editedTestDescription == null) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Test Description value is null cannot go further");
            return "/testing/testsDefinition/showTestDescriptions.xhtml?id=" + getEditedTest().getId();
        }

        List<TestDescription> testDescriptionList = editedTest.getTestDescription();
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
                    if (testDescription.getGazelleLanguage().equals(editedTestDescription.getGazelleLanguage())
                            && !testDescription.getId().equals(editedTestDescription.getId())) {
                        FacesMessages.instance().add(StatusMessage.Severity.ERROR, "There is already a description with the same language");
                        return "/testing/testsDefinition/showTestDescriptions.xhtml?id=" + getEditedTest().getId();
                    }
                }
            }
        }

        try {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            boolean updateMode = (editedTestDescription.getId() != null);
            editedTestDescription = entityManager.merge(editedTestDescription);
            entityManager.flush();
            editedTest = entityManager.find(Test.class, editedTest.getId());
            editedTest.setLastChanged(editedTestDescription.getLastChanged());
            editedTest.setLastModifierId(editedTestDescription.getLastModifierId());
            editedTest.setChildNodeUpdate(editedTestDescription.getLastChanged());
            if (updateMode) {
                editedTest = entityManager.merge(editedTest);
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "Description updated");
            } else {
                testDescriptionList.add(editedTestDescription);
                this.editedTest.setTestDescription(testDescriptionList);
                editedTest = entityManager.merge(editedTest);
                entityManager.flush();
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "Description added");
            }
            editedTestDescription = new TestDescription();
            editedTestDescription.initDefaultValues();
            return "/testing/testsDefinition/showTestDescriptions.xhtml?id=" + getEditedTest().getId();
        } catch (Exception e) {
            ExceptionLogging.logException(e, LOG);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Impossible to add description : " + e.getMessage());
            return "/testing/testsDefinition/showTestDescriptions.xhtml?id=" + getEditedTest().getId();
        }
    }

    @Deprecated
    //TODO refactor ITB tests
    public String translateTestDescription(int inTestDescriptionId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("translateTestDescription");
        }
        return "/testing/testsDefinition/translateDescription.xhtml?id=" + getEditedTest().getId() + "&translatedTestDescriptionId=" +
                inTestDescriptionId;
    }

    public String cancel(int testId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancel");
        }
        String redirectTo;
        if ("true".equals(backToTests)) {
            redirectTo = "/testing/testsDefinition/viewTestPage.xhtml?id=" + testId;
        } else {

            redirectTo = "/testing/testsDefinition/showTestDescriptions.xhtml?id=" + testId;
        }
        return redirectTo;
    }

    public List<TestRoles> getListOfTestRoles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfTestRoles");
        }
        List<TestRoles> testRolesList = TestRoles.getTestRolesListForATest(editedTest);
        if (testRolesList != null && !testRolesList.isEmpty()){
            for (int i = 0; i<testRolesList.size() ; i++){
                if (testRolesList.get(i).getRoleRank() == null || testRolesList.get(i).getRoleRank() != i){
                    testRolesList.get(i).setRoleRank(i);
                    EntityManager entityManager = EntityManagerService.provideEntityManager();
                    try {
                        testRolesList.set(i, entityManager.merge(testRolesList.get(i)));
                        entityManager.flush();
                    } catch (Exception e) {
                        ExceptionLogging.logException(e, LOG);
                        FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Errors : " + e.getMessage());
                    }
                }
            }
            return testRolesList;
        }
        return new ArrayList<TestRoles>();
    }

    public boolean canBeMovedUp(TestRoles testRoles){
       return testRoles.getTestRolesAtPreviousRank() != null;
    }

    public boolean canBeMovedDown(TestRoles testRoles){
        return testRoles.getTestRolesAtNextRank() != null;
    }

    public void moveRoleUp(TestRoles testRolesAfter){
        TestRoles testRolesBefore = testRolesAfter.getTestRolesAtPreviousRank();
        if (testRolesBefore != null){
            testRolesBefore.setRoleRank(testRolesAfter.getRoleRank());
            testRolesAfter.setRoleRank(testRolesAfter.getRoleRank()-1);
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            try {
                entityManager.merge(testRolesBefore);
                entityManager.merge(testRolesAfter);
                entityManager.flush();
            } catch (Exception e) {
                ExceptionLogging.logException(e, LOG);
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Errors : " + e.getMessage());
            }
        }
    }

    public void moveRoleDown(TestRoles testRolesBefore){
        TestRoles testRolesAfter = testRolesBefore.getTestRolesAtNextRank();
        if (testRolesAfter != null){
            testRolesAfter.setRoleRank(testRolesBefore.getRoleRank());
            testRolesBefore.setRoleRank(testRolesBefore.getRoleRank()+1);
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            try {
                entityManager.merge(testRolesBefore);
                entityManager.merge(testRolesAfter);
                entityManager.flush();
            } catch (Exception e) {
                ExceptionLogging.logException(e, LOG);
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Errors : " + e.getMessage());
            }
        }
    }

    public void addTestRoles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addTestRoles");
        }
        selectedTestRole = new TestRoles();
        selectedTestRole.setNumberOfTestsToRealize(1);
        selectedTestRole.setCardMin(0);
        selectedTestRole.setCardMax(1);
        selectedTestRole.setTestOption(TestOption.getTEST_OPTION_REQUIRED());
        roleInTestKeywordTyped = "";
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

    public void selectRoleInTestAndUpdateKeyword(RoleInTest inRoleInTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("selectRoleInTestAndUpdateKeyword");
        }

        this.selectedTestRole.setRoleInTest(inRoleInTest);
        this.roleInTestKeywordTyped = this.selectedTestRole.getRoleInTest().getKeyword();
    }

    public void updateTestRoles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateTestRoles");
        }
        boolean doAdd = false;
        if (selectedTestRole.getId() != null) {
            doAdd = true;
        } else {
            List<TestRoles> listOfTestRoles = TestRoles.getTestsFiltered(null, null, null, null, editedTest, null,
                    null, null, null, null);
            if (!TestRoles.testRolesContainsKeyword(selectedTestRole.getRoleInTest().getKeyword(), listOfTestRoles)) {
                doAdd = true;
            }
        }
        if (doAdd) {
            if (selectedTestRole.getCardMin() == null) {
                selectedTestRole.setCardMin(1);
            }
            if (selectedTestRole.getCardMax() == null) {
                selectedTestRole.setCardMax(1);
            }

            if (selectedTestRole.getCardMax() < selectedTestRole.getCardMin()) {
                int tmp = selectedTestRole.getCardMax();
                selectedTestRole.setCardMax(selectedTestRole.getCardMin());
                selectedTestRole.setCardMin(tmp);
            }

            if (selectedTestRole.getCardMax() > MAX_PARTICIPANTS) {
                selectedTestRole.setCardMax(MAX_PARTICIPANTS);
            }
            selectedTestRole.setTest(editedTest);
            selectedTestRole.setRoleRank(TestRoles.maxRankForTest(editedTest)+1);

            try {
                EntityManager entityManager = EntityManagerService.provideEntityManager();
                selectedTestRole = entityManager.merge(selectedTestRole);
                entityManager.flush();
                updateTest(selectedTestRole);
            } catch (Exception e) {
                ExceptionLogging.logException(e, LOG);
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Errors : " + e.getMessage());
            }
            cancelEditingTestRoles();
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "This test already contains the role " + roleInTestKeywordTyped);
        }
    }

    public void updateTest(TestRoles selectedTestRole) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateTest");
        }
        Test test = selectedTestRole.getTest();
        test.setLastChanged(selectedTestRole.getLastChanged());
        test.setLastModifierId(selectedTestRole.getLastModifierId());

        EntityManager entityManager = EntityManagerService.provideEntityManager();
        entityManager.merge(test);
        entityManager.flush();
    }

    public void cancelEditingTestRoles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelEditingTestRoles");
        }
        selectedTestRole = null;
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

        try {
            List<TestSteps> testStepsList = editedTest.getTestStepsList();
            if (testStepsList != null) {
                testStepsList.remove(inTestSteps);
                editedTest.setTestStepsList(testStepsList);
                editedTest = entityManager.merge(editedTest);
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

    public void setSelectedTestSteps(TestSteps selectedTestSteps) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestSteps");
        }
        this.selectedTestSteps = selectedTestSteps;
    }

    public boolean isRoleLinkedToStep() {
        return selectedTestRole != null && selectedTestRole.getTestStepsList() != null && !selectedTestRole.getTestStepsList().isEmpty();
    }

    public int countStepsLinkedToRole() {
        if (selectedTestRole != null && selectedTestRole.getTestStepsList() != null) {
            return selectedTestRole.getTestStepsList().size();
        }
        return 0;
    }

    public void deleteTestRole() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteTestRole");
        }
        if (selectedTestRole == null) {
            return;
        }

        List<TestSteps> tsl = selectedTestRole.getTestStepsList();
        if (tsl != null) {
            // GZL-2972 Remove linked test steps before test role
            for (TestSteps ts : tsl) {
                setSelectedTestSteps(ts);
                deleteTestSteps(ts);
            }
        }

        try {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            selectedTestRole = entityManager.find(TestRoles.class, selectedTestRole.getId());
            entityManager.remove(selectedTestRole);
            setSelectedTestRole(null);
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

    public String deleteSelectedDescription() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedDescription");
        }

        try {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            List<TestDescription> testDescriptionList = editedTest.getTestDescription();
            if (testDescriptionList != null) {
                testDescriptionList.remove(editedTestDescription);
                editedTest.setTestDescription(testDescriptionList);
                editedTest = entityManager.merge(editedTest);
            }
            TestDescription.deleteTestDescriptionWithFind(editedTestDescription);
            editedTestDescription = new TestDescription();
            editedTestDescription.initDefaultValues();
            if (editedTest.getTestDescription().isEmpty()) {
                List<TestDescription> testDescriptions = new ArrayList<TestDescription>();
                testDescriptions.add(editedTestDescription);
                editedTest.setTestDescription(testDescriptions);
                editedTest = entityManager.merge(editedTest);
            }
            entityManager.flush();
        } catch (Exception e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Impossible to delete description : " + e.getMessage());
            return "/testing/testsDefinition/showTestDescriptions.xhtml?id=" + getEditedTest().getId();
        }
        if ((editedTest.getTestDescription() == null) || (editedTest.getTestDescription().size() == 0)) {
            return "/testing/testsDefinition/editTestDescription.xhtml?id=" + getEditedTest().getId();
        }
        return "/testing/testsDefinition/showTestDescriptions.xhtml?id=" + getEditedTest().getId();
    }

    public boolean noTestDescriptionExists() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("noTestDescriptionExists");
        }
        return getEditedTest().getTestDescription().isEmpty();
    }

    public String newTestDescription() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("newTestDescription");
        }
        return "/testing/testsDefinition/editTestDescription.xhtml?id=" + getEditedTest().getId() + "&backToTests=true&newDescription=true";
    }

    public boolean displayTranslateButton() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayTranslateButton");
        }
        if (editedTest.getTestDescription().size() == GazelleLanguage.getLanguageList().size()) {
            return false;
        }
        return true;
    }

    public void resetFilterRoleInTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetFilterRoleInTests");
        }
        getFoundRoleInTests().getFilter().clear();
        setRoleInTestKeywordTyped(null);
    }
}
