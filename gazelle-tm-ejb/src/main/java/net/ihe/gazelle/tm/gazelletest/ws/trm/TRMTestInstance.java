package net.ihe.gazelle.tm.gazelletest.ws.trm;

import net.ihe.gazelle.tm.gazelletest.model.instance.Status;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceParticipants;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestStepsInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TRMTestInstance {
    private static final Logger LOG = LoggerFactory.getLogger(TRMTestInstance.class);

    private List<TRMConfiguration> configurations;
    private Integer id;
    private int testingSessionId;
    private TRMTest test;
    private List<TRMParticipant> participants;
    private String description;
    private String lastStatus;
    private List<TRMTestStepInstance> testSteps;

    public void copyFromTestInstance(TestInstance testInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("copyFromTestInstance");
        }
        this.id = testInstance.getId();

        this.test = new TRMTest();
        this.test.copyFromTest(testInstance.getTest());

        this.participants = new ArrayList<TRMParticipant>();
        List<TestInstanceParticipants> testInstanceParticipants = testInstance.getTestInstanceParticipants();
        for (TestInstanceParticipants testInstanceParticipant : testInstanceParticipants) {
            TRMParticipant participant = new TRMParticipant();
            participant.copyFromTestInstanceParticipant(testInstanceParticipant);
            this.participants.add(participant);
        }

        this.description = testInstance.getDescription();
        this.testingSessionId = testInstance.getTestingSession().getId();
        Status lastStatus2 = testInstance.getLastStatus();
        if (lastStatus2 != null) {
            this.lastStatus = lastStatus2.getKeyword();
        }
        this.testSteps = new ArrayList<TRMTestStepInstance>();

        Map<Integer, TRMConfiguration> configurations = new HashMap<Integer, TRMConfiguration>();

        List<TestStepsInstance> testStepsInstances = testInstance.getTestStepsInstanceList();
        for (TestStepsInstance testStepsInstance : testStepsInstances) {
            TRMTestStepInstance testStepInstance = new TRMTestStepInstance();
            testStepInstance.copyFromTestStepsInstance(testStepsInstance, testInstance, configurations);

            this.testSteps.add(testStepInstance);
        }

        this.configurations = new ArrayList<TRMConfiguration>(configurations.values());

    }

    public List<TRMConfiguration> getConfigurations() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getConfigurations");
        }
        return configurations;
    }

    public void setConfigurations(List<TRMConfiguration> configurations) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setConfigurations");
        }
        this.configurations = configurations;
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

    public Integer getId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getId");
        }
        return id;
    }

    public void setId(Integer id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setId");
        }
        this.id = id;
    }

    public String getLastStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLastStatus");
        }
        return lastStatus;
    }

    public void setLastStatus(String lastStatus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setLastStatus");
        }
        this.lastStatus = lastStatus;
    }

    public List<TRMParticipant> getParticipants() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getParticipants");
        }
        return participants;
    }

    public void setParticipants(List<TRMParticipant> participants) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setParticipants");
        }
        this.participants = participants;
    }

    public TRMTest getTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTest");
        }
        return test;
    }

    public void setTest(TRMTest test) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTest");
        }
        this.test = test;
    }

    public List<TRMTestStepInstance> getTestSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestSteps");
        }
        return testSteps;
    }

    public void setTestSteps(List<TRMTestStepInstance> testSteps) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestSteps");
        }
        this.testSteps = testSteps;
    }

    public int getTestingSessionId() {
        return testingSessionId;
    }

    public void setTestingSessionId(int testingSessionId) {
        this.testingSessionId = testingSessionId;
    }
}
