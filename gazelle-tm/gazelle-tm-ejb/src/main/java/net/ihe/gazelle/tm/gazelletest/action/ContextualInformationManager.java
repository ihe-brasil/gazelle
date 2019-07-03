package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.gazelletest.model.definition.ContextualInformation;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestSteps;
import net.ihe.gazelle.tm.gazelletest.model.instance.ContextualInformationInstance;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Name("contextualInformationManager")
@Scope(ScopeType.SESSION)
@GenerateInterface("ContextualInformationManagerLocal")
public class ContextualInformationManager implements ContextualInformationManagerLocal, Serializable {

    private static final long serialVersionUID = 1L;

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ContextualInformationManager.class);

    @In(required = false)
    @Out(required = false)
    private Test selectedTest;

    private ContextualInformation selectedContextualInformation;

    private TestSteps selectedTestStepsAsInput;

    private TestSteps selectedTestStepsAsOutput;

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
    public ContextualInformation getSelectedContextualInformation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedContextualInformation");
        }
        return selectedContextualInformation;
    }

    @Override
    public void setSelectedContextualInformation(ContextualInformation selectedContextualInformation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedContextualInformation");
        }
        this.selectedContextualInformation = selectedContextualInformation;
    }

    @Override
    public TestSteps getSelectedTestStepsAsInput() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestStepsAsInput");
        }
        return selectedTestStepsAsInput;
    }

    @Override
    public void setSelectedTestStepsAsInput(TestSteps selectedTestStepsAsInput) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestStepsAsInput");
        }
        this.selectedTestStepsAsInput = selectedTestStepsAsInput;
    }

    @Override
    public TestSteps getSelectedTestStepsAsOutput() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestStepsAsOutput");
        }
        return selectedTestStepsAsOutput;
    }

    @Override
    public void setSelectedTestStepsAsOutput(TestSteps selectedTestStepsAsOutput) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestStepsAsOutput");
        }
        this.selectedTestStepsAsOutput = selectedTestStepsAsOutput;
    }

    @Override
    public void initContextualInformationManagement() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initContextualInformationManagement");
        }
        selectedContextualInformation = new ContextualInformation();
    }

    @Override
    public List<ContextualInformation> getContextualInformationList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getContextualInformationList");
        }
        List<TestSteps> testStepsList = selectedTest.getTestStepsList();
        if (testStepsList != null) {
            ArrayList<ContextualInformation> contextualInformations = new ArrayList<ContextualInformation>();
            for (TestSteps testSteps : testStepsList) {
                if (testSteps.getInputContextualInformationList() != null) {
                    contextualInformations.addAll(testSteps.getInputContextualInformationList());
                }
                if (testSteps.getOutputContextualInformationList() != null) {
                    for (ContextualInformation contextualInformation : testSteps.getOutputContextualInformationList()) {
                        if (!contextualInformations.contains(contextualInformation)) {
                            contextualInformations.add(contextualInformation);
                        }
                    }
                }
            }
            return contextualInformations;
        }
        return null;
    }

    @Override
    public ContextualInformation persistContextualInformation(ContextualInformation inContextualInformation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistContextualInformation");
        }
        if (inContextualInformation != null) {
            EntityManager em = EntityManagerService.provideEntityManager();
            inContextualInformation = em.merge(inContextualInformation);
            return inContextualInformation;
        }
        return null;
    }

    @Override
    public TestSteps getInputTestStepsForContextualInformation(ContextualInformation inContextualInformation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInputTestStepsForContextualInformation");
        }
        List<TestSteps> testStepsList = selectedTest.getTestStepsList();
        if (testStepsList != null) {
            for (TestSteps testSteps : testStepsList) {
                if (testSteps.getInputContextualInformationList() != null) {
                    if (testSteps.getInputContextualInformationList().contains(inContextualInformation)) {
                        return testSteps;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public TestSteps getOutputTestStepsForContextualInformation(ContextualInformation inContextualInformation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOutputTestStepsForContextualInformation");
        }
        List<TestSteps> testStepsList = selectedTest.getTestStepsList();
        if (testStepsList != null) {
            for (TestSteps testSteps : testStepsList) {
                if (testSteps.getOutputContextualInformationList() != null) {
                    if (testSteps.getOutputContextualInformationList().contains(inContextualInformation)) {
                        return testSteps;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void persistSelectedContextualInformation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistSelectedContextualInformation");
        }
        if (selectedContextualInformation == null) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Contextual Information value is null cannot go further");
            return;
        }

        List<ContextualInformation> contextualInformations = getContextualInformationList();
        if (contextualInformations != null) {
            for (ContextualInformation contextualInformation : contextualInformations) {
                if (contextualInformation.getLabel().equals(selectedContextualInformation.getLabel())) {
                    FacesMessages.instance()
                            .add(StatusMessage.Severity.ERROR, "A Contexual Information with same label already exists for this test");
                    return;
                }
            }
        }

        if ((selectedTestStepsAsInput == null) && (selectedTestStepsAsOutput == null)) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Input and Output Test Steps for Contextual Information can not be null both");
            return;
        }

        EntityManager entityManager = EntityManagerService.provideEntityManager();
        selectedContextualInformation = entityManager.merge(selectedContextualInformation);
        if (selectedTestStepsAsInput != null) {
            selectedTestStepsAsInput.addInputContextualInformation(selectedContextualInformation);
            selectedTestStepsAsInput = entityManager.merge(selectedTestStepsAsInput);
        }
        if (selectedTestStepsAsOutput != null) {
            selectedTestStepsAsOutput.addOutputContextualInformation(selectedContextualInformation);
            selectedTestStepsAsOutput = entityManager.merge(selectedTestStepsAsOutput);
        }
        entityManager.flush();
        selectedTest = Test.getTestWithAllAttributes(selectedTest);
        selectedContextualInformation = new ContextualInformation();
        selectedTestStepsAsInput = null;
        selectedTestStepsAsOutput = null;
    }

    @Override
    public void deleteContextualInformation(ContextualInformation inContextualInformation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteContextualInformation");
        }
        if (inContextualInformation != null) {
            List<ContextualInformationInstance> contextualInformationInstanceList = ContextualInformationInstance
                    .getContextualInformationInstanceListForContextualInformation(inContextualInformation);
            if (contextualInformationInstanceList != null) {
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "The Selected Contextual Information is used");
            }
            List<TestSteps> testStepsList = selectedTest.getTestStepsList();
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            if (testStepsList != null) {
                for (TestSteps testSteps : testStepsList) {
                    if (testSteps.getInputContextualInformationList() != null) {
                        testSteps.getInputContextualInformationList().remove(inContextualInformation);
                    }
                    if (testSteps.getOutputContextualInformationList() != null) {
                        testSteps.getOutputContextualInformationList().remove(inContextualInformation);
                    }
                    testSteps = entityManager.merge(testSteps);
                }
            }
            entityManager.remove(inContextualInformation);
        }
    }

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

}
