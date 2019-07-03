package net.ihe.gazelle.tm.gazelletest.ws.trm;

import net.ihe.gazelle.tf.model.Transaction;
import net.ihe.gazelle.tf.model.WSTransactionUsage;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestRoles;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestSteps;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestStepsOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TRMTestStep {

    private static final Logger LOG = LoggerFactory.getLogger(TRMTestStep.class);
    private String description;
    private Integer stepIndex;
    private String transaction;
    private String messageType;
    private Integer testRoleInitiatorId;
    private Integer testRoleResponderId;
    private String testStepOption;
    private String wsTransactionUsage;
    private String wsTransaction;

    public void copyFromTestStep(TestSteps testStep) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("copyFromTestStep");
        }
        this.description = testStep.getDescription();
        this.stepIndex = testStep.getStepIndex();
        Transaction transaction2 = testStep.getTransaction();
        if (transaction2 != null) {
            this.transaction = transaction2.getKeyword();
        }
        this.messageType = testStep.getMessageType();
        TestRoles testRolesInitiator = testStep.getTestRolesInitiator();
        if (testRolesInitiator != null) {
            this.testRoleInitiatorId = testRolesInitiator.getId();
        }
        TestRoles testRolesResponder = testStep.getTestRolesResponder();
        if (testRolesResponder != null) {
            this.testRoleResponderId = testRolesResponder.getId();
        }
        TestStepsOption testStepsOption = testStep.getTestStepsOption();
        if (testStepsOption != null) {
            this.testStepOption = testStepsOption.getKeyword();
        }
        WSTransactionUsage wstransactionUsage2 = testStep.getWstransactionUsage();
        if (wstransactionUsage2 != null) {
            this.wsTransactionUsage = wstransactionUsage2.getUsage();
            this.wsTransaction = wstransactionUsage2.getTransaction().getKeyword();
        } else {
            this.wsTransactionUsage = null;
            this.wsTransaction = null;
        }
    }

    public String getDescription() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDescription");
        }
        return description;
    }

    public void setDescription(String description) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDescription");
        }
        this.description = description;
    }

    public String getMessageType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMessageType");
        }
        return messageType;
    }

    public void setMessageType(String messageType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMessageType");
        }
        this.messageType = messageType;
    }

    public Integer getStepIndex() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getStepIndex");
        }
        return stepIndex;
    }

    public void setStepIndex(Integer stepIndex) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setStepIndex");
        }
        this.stepIndex = stepIndex;
    }

    public Integer getTestRoleInitiatorId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestRoleInitiatorId");
        }
        return testRoleInitiatorId;
    }

    public void setTestRoleInitiatorId(Integer testRoleInitiatorId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestRoleInitiatorId");
        }
        this.testRoleInitiatorId = testRoleInitiatorId;
    }

    public Integer getTestRoleResponderId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestRoleResponderId");
        }
        return testRoleResponderId;
    }

    public void setTestRoleResponderId(Integer testRoleResponderId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestRoleResponderId");
        }
        this.testRoleResponderId = testRoleResponderId;
    }

    public String getTestStepOption() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestStepOption");
        }
        return testStepOption;
    }

    public void setTestStepOption(String testStepOption) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestStepOption");
        }
        this.testStepOption = testStepOption;
    }

    public String getTransaction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTransaction");
        }
        return transaction;
    }

    public void setTransaction(String transaction) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTransaction");
        }
        this.transaction = transaction;
    }

    public String getWsTransaction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getWsTransaction");
        }
        return wsTransaction;
    }

    public void setWsTransaction(String wsTransaction) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setWsTransaction");
        }
        this.wsTransaction = wsTransaction;
    }

    public String getWsTransactionUsage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getWsTransactionUsage");
        }
        return wsTransactionUsage;
    }

    public void setWsTransactionUsage(String wsTransactionUsage) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setWsTransactionUsage");
        }
        this.wsTransactionUsage = wsTransactionUsage;
    }

}
