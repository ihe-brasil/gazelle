package net.ihe.gazelle.ws.tm.tests;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.tm.gazelletest.model.definition.*;
import net.ihe.gazelle.tm.ws.data.*;
import org.jboss.seam.annotations.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;

@Stateless
@Name("tests")
public class TestsWs implements TestsWsApi {

    private static final Logger LOG = LoggerFactory.getLogger(TestsWs.class);

    @Override
    public TestsWrapper getTests(String integrationProfileKeyword, String domain, String testType, String actor,
                                 String transaction) throws JAXBException {
        TestQuery query = new TestQuery();
        query.testStatus().keyword().eq(TestStatus.STATUS_READY_STRING);
        if ((domain != null) && !"".equals(domain)) {
            query.testRoles().roleInTest().testParticipantsList().actorIntegrationProfileOption()
                    .actorIntegrationProfile().integrationProfile().domainsForDP().keyword().eq(domain);
        }
        if ((integrationProfileKeyword != null) && !"".equals(integrationProfileKeyword)) {
            query.testRoles().roleInTest().testParticipantsList().actorIntegrationProfileOption()
                    .actorIntegrationProfile().integrationProfile().keyword().eq(integrationProfileKeyword);
        }

        if ((actor != null) && !"".equals(actor)) {
            query.testRoles().roleInTest().testParticipantsList().actorIntegrationProfileOption()
                    .actorIntegrationProfile().actor().keyword().eq(actor);
            query.testRoles().roleInTest().testParticipantsList().tested().eq(true);
            query.testRoles().roleInTest().isRolePlayedByATool().eq(false);
        }
        if ((transaction != null) && !"".equals(transaction)) {
            query.testStepsList().transaction().keyword().eq(transaction);
        }

        if ((testType != null) && !"".equals(testType)) {
            query.testType().keyword().eq(testType);
        }

        List<Test> tests = query.getListDistinct();

        List<TestWrapper> testWrapperList = new ArrayList<TestWrapper>();

        for (Test test : tests) {
            testWrapperList.add(new TestWrapper(test.getKeyword(), test.getId()));
        }

        return new TestsWrapper(testWrapperList);
    }

    @Override
    public TestWrapper getTest(int testId) throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTest");
        }
        TestQuery query = new TestQuery();
        query.id().eq(testId);
        Test test = query.getUniqueResult();
        List<TestDescriptionWrapper> descriptionList = new ArrayList<TestDescriptionWrapper>();
        for (TestDescription description : test.getTestDescription()) {
            descriptionList.add(new TestDescriptionWrapper(description.getDescription()));
        }
        TestWrapper testWrapper = new TestWrapper(test.getKeyword(), test.getId(), descriptionList);
        testWrapper.setBaseUrl(ApplicationPreferenceManager.instance().getApplicationUrl());

        return testWrapper;
    }

    @Override
    public TestDescriptionWrapper getTestDescription(int testId, String languageKeyword) throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestDescription");
        }
        TestDescriptionWrapper result;
        TestQuery query = new TestQuery();
        query.id().eq(testId);
        query.testDescription().gazelleLanguage().keyword().eq(languageKeyword);
        List<String> testDescriptions = query.testDescription().description().getListDistinct();
        if (testDescriptions.size() == 1) {
            result = new TestDescriptionWrapper(testDescriptions.get(0));
        } else {
            result = new TestDescriptionWrapper();
        }
        return result;
    }

    @Override
    public TestStepsWrapper getTestSteps(int testId) throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestSteps");
        }
        TestQuery query = new TestQuery();
        query.id().eq(testId);
        Test test = query.getUniqueResult();

        List<TestStepWrapper> testStepsList = new ArrayList<TestStepWrapper>();
        List<TestSteps> testSteps = test.getTestStepsList();

        for (TestSteps testStep : testSteps) {

            Integer id = testStep.getId();
            String initiator = testStep.getTestRolesInitiator().getRoleInTest().getKeyword();
            String responder = testStep.getTestRolesResponder().getRoleInTest().getKeyword();
            String description = testStep.getDescription();
            String transaction = "";
            if (testStep.getTransaction() != null) {
                transaction = testStep.getTransaction().getKeyword();
            }
            TestStepWrapper testStepWrapper = new TestStepWrapper(id, testStep.getStepIndex(), initiator, responder,
                    transaction, description, testStep.getTestParent().getKeyword(), testStep.getTestParent().getId());

            testStepWrapper.setBaseUrl(ApplicationPreferenceManager.instance().getApplicationUrl());
            testStepsList.add(testStepWrapper);
        }

        return new TestStepsWrapper(testStepsList);
    }

    @Override
    public TestTypesWrapper getTestsTypes(String domain, String integrationProfileKeyword) throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestsTypes");
        }

        TestQuery query = new TestQuery();
        if ((integrationProfileKeyword != null) && !"".equals(integrationProfileKeyword)) {
            query.testRoles().roleInTest().testParticipantsList().actorIntegrationProfileOption()
                    .actorIntegrationProfile().integrationProfile().keyword().eq(integrationProfileKeyword);
        }
        query.testStatus().keyword().eq(TestStatus.STATUS_READY_STRING);
        if ((domain != null) && !"".equals(domain)) {
            query.testRoles().roleInTest().testParticipantsList().actorIntegrationProfileOption()
                    .actorIntegrationProfile().integrationProfile().domainsForDP().keyword().eq(domain);
        }
        List<String> list = query.testType().keyword().getListDistinct();
        return new TestTypesWrapper(list);
    }
}
