package net.ihe.gazelle.tm.gazelletest.test;

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.Transaction;
import net.ihe.gazelle.tf.model.WSTransactionUsage;
import net.ihe.gazelle.tm.gazelletest.bean.DiagramGenerator;
import net.ihe.gazelle.tm.gazelletest.model.definition.*;
import net.ihe.gazelle.tm.gazelletest.model.instance.ContextualInformationInstance;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestStepsInstance;
import net.ihe.gazelle.users.model.User;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Name("editTestStepsManager")
@GenerateInterface(value = "EditTestStepsManagerLocal")
@Scope(ScopeType.PAGE)
public class EditTestStepsManager implements Serializable, EditTestStepsManagerLocal {

    private static final long serialVersionUID = 645439846131L;

    private static final int DEFAULT_TEST_STEPS_INDEX_INCREMENTATION = 10;

    private static final Logger LOG = LoggerFactory.getLogger(EditTestStepsManager.class);

    private Test editedTestForTestSteps;
    private boolean showTestStepsEditionPanel = false;
    private TestSteps selectedTestStep;
    private List<TestRoles> possibleTestRoles;
    private ContextualInformation selectedContextualInformation;
    private int inputOrOutput = 1;
    private ContextualInformation selectedContextualInformationOld;
    private List<ContextualInformation> listContextualInformationToRemove;
    private String contextualInformationtype;
    private boolean autoResponse = false;

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
        return selectedTestStep;
    }

    public void setSelectedTestSteps(TestSteps selectedTestSteps) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestSteps");
        }
        this.selectedTestStep = selectedTestSteps;
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

    @Factory(value = "editedTestForTestSteps", scope = ScopeType.PAGE)
    public Test getEditedTestForTestSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEditedTestForTestSteps");
        }
        if (editedTestForTestSteps == null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            String testId = fc.getExternalContext().getRequestParameterMap().get("id");
            setEditedTestForTestSteps(Integer.valueOf(testId));
        }

        return editedTestForTestSteps;
    }

    private void setEditedTestForTestSteps(int testId) {
        EntityManager em = EntityManagerService.provideEntityManager();
        setEditedTestForTestSteps(em.find(Test.class, testId));
    }

    // Test step edition

    public void setEditedTestForTestSteps(Test editedTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setEditedTestForTestSteps");
        }
        this.editedTestForTestSteps = editedTest;
    }

    public List<TestSteps> getListOfTestSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfTestSteps");
        }
        TestStepsQuery query = new TestStepsQuery();
        query.testParent().id().eq(getEditedTestForTestSteps().getId());
        List<TestSteps> res = query.getList();
        Collections.sort(res);
        return res;
    }

    public List<TestStepsOption> getPossibleTestStepsOptions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleTestStepsOptions");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();

        Query q = entityManager.createQuery("from TestStepsOption");
        return q.getResultList();

    }

    public void addNewTestSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNewTestSteps");
        }
        selectedTestStep = new TestSteps();
        if (editedTestForTestSteps != null) {
            selectedTestStep.setStepIndex(getHighestStepIndex(editedTestForTestSteps.getTestStepsList())
                    + DEFAULT_TEST_STEPS_INDEX_INCREMENTATION);
            selectedTestStep.setTestParent(editedTestForTestSteps);
            selectedTestStep.setLastChanged(new Date());
            selectedTestStep.setLastModifierId(User.loggedInUser().getUsername());
        }
        autoResponse = false;
        updateTest(selectedTestStep);
        showTestStepsEditionPanel();
    }

    public void updateTest(TestSteps selectedTestStep) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateTest");
        }
        Test test = selectedTestStep.getTestParent();
        test.setLastChanged(selectedTestStep.getLastChanged());
        test.setLastModifierId(selectedTestStep.getLastModifierId());

        EntityManager entityManager = EntityManagerService.provideEntityManager();
        entityManager.merge(test);
        entityManager.flush();
    }

    public void showTestStepsEditionPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showTestStepsEditionPanel");
        }
        showTestStepsEditionPanel = true;
    }

    private int getHighestStepIndex(List<TestSteps> testStepsList) {
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
            q.setParameter("test", getEditedTestForTestSteps());
            List<TestRoles> result = q.getResultList();
            return result;
        }
    }

    private List<TestRoles> getPossibleTestRoles() {

        this.possibleTestRoles = TestRoles.getTestRolesListForATest(getEditedTestForTestSteps());
        return possibleTestRoles;

    }

    public List<Transaction> getPossibleTransactions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleTransactions");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        if (selectedTestStep != null) {
            if (selectedTestStep.getTestRolesInitiator() != null) {

                Query q = entityManager
                        .createQuery("SELECT distinct pl.transaction "
                                + "FROM ProfileLink pl, TestRoles tr "
                                + "JOIN tr.roleInTest.testParticipantsList testParticipantsList "
                                + "WHERE pl.actorIntegrationProfile = testParticipantsList.actorIntegrationProfileOption.actorIntegrationProfile "
                                + "AND tr = :testRoles");
                q.setParameter("testRoles", selectedTestStep.getTestRolesInitiator());
                List<Transaction> result = q.getResultList();
                return result;
            }
        }
        return null;
    }

    public List<WSTransactionUsage> getPossibleWSTransactionUsages() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleWSTransactionUsages");
        }
        if (this.selectedTestStep != null) {
            if (this.selectedTestStep.getTransaction() != null) {
                return WSTransactionUsage.getWSTransactionUsageFiltered(this.selectedTestStep.getTransaction(), null,
                        null, null);
            }
        }
        return null;
    }

    public List<TestRoles> getPossibleTestRolesResponder() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleTestRolesResponder");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        if (selectedTestStep.getTransaction() != null) {
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
            q.setParameter("testRolesInitiator", selectedTestStep.getTestRolesInitiator());
            q.setParameter("transaction", selectedTestStep.getTransaction());
            q.setParameter("test", editedTestForTestSteps);
            List<TestRoles> result = q.getResultList();
            if (result != null) {
                result.remove(selectedTestStep.getTestRolesInitiator());
                return result;
            }
        }
        return null;

    }

    public List<String> getPossibleMessageType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleMessageType");
        }
        if (selectedTestStep.getTransaction() != null) {
            if (selectedTestStep.getTestRolesInitiator() != null) {
                EntityManager em = EntityManagerService.provideEntityManager();
                Query query = em
                        .createQuery("SELECT DISTINCT hmp.messageType "
                                + "FROM Hl7MessageProfile hmp,TransactionLink tl,TestRoles tr JOIN tr.roleInTest.testParticipantsList " +
                                "testParticipants "
                                + "WHERE hmp.transaction=tl.transaction "
                                + "AND testParticipants.actorIntegrationProfileOption.actorIntegrationProfile.actor=hmp.actor "
                                + "AND tl.fromActor=hmp.actor " + "AND tl.transaction=:inTransaction "
                                + "AND tr=:inTestRolesInitiator");
                query.setParameter("inTransaction", selectedTestStep.getTransaction());
                query.setParameter("inTestRolesInitiator", selectedTestStep.getTestRolesInitiator());
                List<String> list = query.getResultList();
                if (list.size() > 0) {
                    Collections.sort(list);
                    return list;
                }
            }
        }
        return null;
    }

    public boolean canEditUsage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canEditUsage");
        }
        if (this.selectedTestStep != null) {
            if (this.selectedTestStep.getTransaction() != null) {
                List<WSTransactionUsage> lwstr = WSTransactionUsage.getWSTransactionUsageFiltered(
                        this.selectedTestStep.getTransaction(), null, null, null);
                if (lwstr != null) {
                    if (lwstr.size() > 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean viewTableOfCIInput() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewTableOfCIInput");
        }
        if (this.selectedTestStep.getInputContextualInformationList() != null) {
            if (this.selectedTestStep.getInputContextualInformationList().size() > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean viewTableOfCIOutput() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewTableOfCIOutput");
        }
        if (this.selectedTestStep.getOutputContextualInformationList() != null) {
            if (this.selectedTestStep.getOutputContextualInformationList().size() > 0) {
                return true;
            }
        }
        return false;
    }

    public void resetTransactionValue() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetTransactionValue");
        }
        selectedTestStep.setTransaction(null);
        selectedTestStep.setWstransactionUsage(null);
        resetTestRolesResponderValue();
    }

    public void resetTestRolesResponderValue() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetTestRolesResponderValue");
        }
        selectedTestStep.setTestRolesResponder(null);
        selectedTestStep.setMessageType(null);
        selectedTestStep.setWstransactionUsage(null);
    }

    public void persistSelectedTestSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistSelectedTestSteps");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        if (selectedTestStep == null) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "TestSteps value is null cannot go further");
            return;
        }
        if (selectedTestStep.getTestParent() == null) {
            selectedTestStep.setTestParent(editedTestForTestSteps);
        }
        if (validateStepIndex() && validateTestStepsDescription()) {
            try {

                if (autoResponse) {
                    selectedTestStep.setTestRolesResponder(selectedTestStep.getTestRolesInitiator());
                    selectedTestStep.setTransaction(null);
                    selectedTestStep.setWstransactionUsage(null);
                    autoResponse = false;
                } else {
                    if ((selectedTestStep.getTransaction() == null)
                            && (!selectedTestStep.getTestRolesInitiator().equals(
                            selectedTestStep.getTestRolesResponder()))) {
                        FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR,
                                "gazelle.tm.testing.testsDefinition.transactionNullWarning");
                        return;
                    }

                    if (selectedTestStep.getTestRolesResponder() == null) {
                        FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR,
                                "gazelle.tm.testing.testsDefinition.testRolesResponderNullWarning");
                        return;
                    }

                }

                if ((selectedTestStep.getDescription() == null) || selectedTestStep.getDescription().isEmpty()) {
                    selectedTestStep.setDescription(" ");
                }

                selectedTestStep = TestSteps.mergeTestSteps(selectedTestStep, entityManager);

                if (editedTestForTestSteps.getTestStepsList() == null) {
                    editedTestForTestSteps.setTestStepsList(new ArrayList<TestSteps>());
                }

                editedTestForTestSteps = entityManager.find(Test.class, editedTestForTestSteps.getId());
                entityManager.refresh(editedTestForTestSteps);
                List<TestSteps> lts = editedTestForTestSteps.getTestStepsList();

                boolean updateMode = false;
                if (!lts.contains(selectedTestStep)) {
                    lts.add(selectedTestStep);
                } else {
                    updateMode = true;
                }
                editedTestForTestSteps.setTestStepsList(lts);

                editedTestForTestSteps = entityManager.merge(editedTestForTestSteps);

                entityManager.flush();

                if (updateMode) {
                    FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO,
                            "gazelle.tm.testing.testsDefinition.testStepsUpdated");
                } else {
                    FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO,
                            "gazelle.tm.testing.testsDefinition.testStepsCreated");
                }

                updateTest(selectedTestStep);

                updateDiagram(editedTestForTestSteps);

                selectedTestStep = new TestSteps();
                showTestStepsEditionPanel = false;

                return;

            } catch (Exception e) {
                LOG.error("Failed to persist test steps", e);
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Impossible to persist testSteps : " + e.getMessage());
                return;
            }
        } else {
            editedTestForTestSteps = entityManager.merge(editedTestForTestSteps);
            updateTest(selectedTestStep);
        }
    }

    public String updateDiagram(Test test) {
        try {
            DiagramGenerator.updateSequenceDiagram(test);
        } catch (Exception e) {
            LOG.error("", e);
        }
        return test.viewFolder() + "/testsDefinition/editTestSteps.seam?id=" + test.getId();
    }

    public boolean validateStepIndex() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateStepIndex");
        }
        List<TestSteps> testStepsList = editedTestForTestSteps.getTestStepsList();
        if (selectedTestStep.getStepIndex() != null) {
            if (testStepsList != null) {
                for (TestSteps testSteps : testStepsList) {
                    if ((testSteps.getStepIndex().equals(selectedTestStep.getStepIndex()))
                            && (!testSteps.getId().equals(selectedTestStep.getId()))) {
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

    public boolean validateTestStepsDescription() {
        if (selectedTestStep.getDescription() != null) {
            if (selectedTestStep.getDescription().length() > TestSteps.MAX_DESCRIPTION_LENGTH) {
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
        if (this.selectedTestStep != null) {
            if (this.selectedContextualInformation != null) {
                if (this.inputOrOutput == 1) {
                    if (this.selectedTestStep.getInputContextualInformationList() == null) {
                        this.selectedTestStep.setInputContextualInformationList(new ArrayList<ContextualInformation>());
                    }
                    if (this.selectedContextualInformationOld != null) {
                        this.selectedTestStep.getInputContextualInformationList().remove(
                                this.selectedContextualInformationOld);
                        this.selectedContextualInformationOld = null;
                    }
                    this.selectedTestStep.getInputContextualInformationList().add(this.selectedContextualInformation);
                } else {
                    if (this.selectedTestStep.getOutputContextualInformationList() == null) {
                        this.selectedTestStep
                                .setOutputContextualInformationList(new ArrayList<ContextualInformation>());
                    }
                    if (this.selectedContextualInformationOld != null) {
                        this.selectedTestStep.getOutputContextualInformationList().remove(
                                this.selectedContextualInformationOld);
                        this.selectedContextualInformationOld = null;
                    }
                    this.selectedTestStep.getOutputContextualInformationList().add(this.selectedContextualInformation);
                }
            }
        }
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
            selectedTestStep.getInputContextualInformationList().remove(inContextualInformation);
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
            List<TestSteps> testStepsList = editedTestForTestSteps.getTestStepsList();
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
            selectedTestStep.getOutputContextualInformationList().remove(inContextualInformation);
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
        if (this.editedTestForTestSteps != null) {
            if (this.selectedTestStep != null) {
                List<TestSteps> lts = new ArrayList<TestSteps>();
                try {
                    lts = this.editedTestForTestSteps.getTestStepsListAntecedent(this.selectedTestStep);
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
    }

    public void editTestSteps(TestSteps inTestSteps) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editTestSteps");
        }
        selectedTestStep = inTestSteps;
        editTestSteps();
    }

    public void editTestSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editTestSteps");
        }
        setAutoResponse(selectedTestStep.getTestRolesInitiator().equals(selectedTestStep.getTestRolesResponder()));
        showTestStepsEditionPanel();
    }

    public void cancelTestStepsCreation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelTestStepsCreation");
        }
        showTestStepsEditionPanel = false;
        autoResponse = false;
        selectedTestStep = null;
    }

    public boolean canEditCIAsInput(ContextualInformation currentCI) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canEditCIAsInput");
        }
        if (this.selectedTestStep != null) {
            List<ContextualInformation> loutput = this.getPossibleContextualInformations();
            if (loutput.contains(currentCI)) {
                return false;
            }
            return true;
        }
        return false;
    }

    public void copySelectedTestStep(TestSteps selectedTestSteps) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("copySelectedTestStep");
        }
        this.selectedTestStep = new TestSteps(selectedTestSteps);
        int testStepsIndex = getHighestStepIndex(editedTestForTestSteps.getTestStepsList())
                + DEFAULT_TEST_STEPS_INDEX_INCREMENTATION;
        this.selectedTestStep.setStepIndex(testStepsIndex);
        this.selectedTestStep.setDescription("Copy of Step " + selectedTestSteps.getStepIndex() + " :"
                + this.selectedTestStep.getDescription());
        persistSelectedTestSteps();
    }

    public void deleteSelectedTestSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSelectedTestSteps");
        }

        EntityManager entityManager = EntityManagerService.provideEntityManager();
        if (selectedTestStep == null) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "TestSteps value is null cannot go further");
            return;
        }

        List<TestStepsInstance> testStepsInstanceList = TestStepsInstance
                .getTestStepsInstanceByTestSteps(selectedTestStep);

        if ((testStepsInstanceList != null) && (testStepsInstanceList.size() > 0)) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                    "Impossible to delete TestSteps : selected Test Steps is used in many testSteps instances.");
            return;
        }

        try {
            List<TestSteps> testStepsList = editedTestForTestSteps.getTestStepsList();
            if (testStepsList != null) {
                testStepsList.remove(selectedTestStep);
                editedTestForTestSteps.setTestStepsList(testStepsList);
                editedTestForTestSteps = entityManager.merge(editedTestForTestSteps);
                entityManager.flush();
            }
            TestSteps.deleteTestStepsWithFind(selectedTestStep);
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "TestSteps deleted");
            return;
        } catch (Exception e) {
            LOG.error("Failed to delete teststeps", e);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Impossible to delete TestSteps : " + e.getMessage());
            return;
        }
    }

    public void persistTestSteps(TestSteps inTestSteps) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistTestSteps");
        }
        this.selectedTestStep = inTestSteps;
        persistSelectedTestSteps();
    }
}
