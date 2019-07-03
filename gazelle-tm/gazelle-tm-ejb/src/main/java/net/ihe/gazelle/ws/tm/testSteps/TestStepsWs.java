package net.ihe.gazelle.ws.tm.testSteps;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestSteps;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestStepsQuery;
import net.ihe.gazelle.tm.ws.data.TestStepWrapper;
import org.jboss.seam.annotations.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.xml.bind.JAXBException;

@Stateless
@Name("testStepsWs")
public class TestStepsWs implements TestStepsWsApi {
    private static final Logger LOG = LoggerFactory.getLogger(TestStepsWs.class);

    @Override
    public TestStepWrapper getTestStep(int id) throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestStep");
        }
        TestStepsQuery query = new TestStepsQuery();
        query.id().eq(id);
        TestSteps testStep = query.getUniqueResult();
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

        return testStepWrapper;
    }

}
