package net.ihe.gazelle.tm.gazelletest.test;

import net.ihe.gazelle.assertion.AssertionWsClient;
import net.ihe.gazelle.assertion.ws.data.WsAssertion;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.tm.application.action.ApplicationManager;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestQuery;
import org.jboss.seam.annotations.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import java.util.List;

@Name("assertionsManager")
@Stateless
@GenerateInterface(value = "AssertionsManagerLocal")
public class AssertionsManager implements AssertionsManagerLocal {
    private static final Logger LOG = LoggerFactory.getLogger(AssertionsManager.class);

    public List<WsAssertion> getTestAssertions(int testId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestAssertions");
        }
        AssertionWsClient client = new AssertionWsClient(ApplicationManager.instance().getAssertionRestApi());
        return client.getAssertionsForTest(testId);
    }

    public List<WsAssertion> getTestStepAssertions(int testStepId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestStepAssertions");
        }
        AssertionWsClient client = new AssertionWsClient(ApplicationManager.instance().getAssertionRestApi());
        return client.getAssertionsForTestStep(testStepId);
    }

    public List<WsAssertion> getRuleAssertions(int ruleId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRuleAssertions");
        }
        AssertionWsClient client = new AssertionWsClient(ApplicationManager.instance().getAssertionRestApi());
        return client.getAssertionsForRule(ruleId);
    }

    public List<WsAssertion> getActorAssertions(String actorKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorAssertions");
        }
        AssertionWsClient client = new AssertionWsClient(ApplicationManager.instance().getAssertionRestApi());
        return client.getAssertionsForActor(actorKeyword);
    }

    public List<WsAssertion> getProfileAssertions(String profileKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProfileAssertions");
        }
        AssertionWsClient client = new AssertionWsClient(ApplicationManager.instance().getAssertionRestApi());
        return client.getAssertionsForProfile(profileKeyword);
    }

    public List<WsAssertion> getAuditMessageAssertions(int id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAuditMessageAssertions");
        }
        AssertionWsClient client = new AssertionWsClient(ApplicationManager.instance().getAssertionRestApi());
        return client.getAssertionsForAuditMessage(id);
    }

    public List<WsAssertion> getStandardAssertions(int id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getStandardAssertions");
        }
        AssertionWsClient client = new AssertionWsClient(ApplicationManager.instance().getAssertionRestApi());
        return client.getAssertionsForStandard(id);
    }

    public List<WsAssertion> getTransactionAssertions(String transactionKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTransactionAssertions");
        }
        AssertionWsClient client = new AssertionWsClient(ApplicationManager.instance().getAssertionRestApi());
        return client.getAssertionsForTransaction(transactionKeyword);
    }

    public List<WsAssertion> getAipoAssertions(int id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAipoAssertions");
        }
        AssertionWsClient client = new AssertionWsClient(ApplicationManager.instance().getAssertionRestApi());
        return client.getAssertionsForAIPO(id);
    }

    public void getAssertionsFromTestSteps(String testKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAssertionsFromTestSteps");
        }
        TestQuery query = new TestQuery();
        query.keyword().eq(testKeyword);
        Test test = query.getUniqueResult();
        test.getAssertionsFromTestSteps();
        //TODO
    }
}
