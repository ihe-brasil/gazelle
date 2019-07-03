package net.ihe.gazelle.tm.gazelletest.ws;

import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceParticipants;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestStepsInstance;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "TestInstanceInformation")
@XmlAccessorType(XmlAccessType.NONE)
public class TestInstanceInformation {

    private static final Logger LOG = LoggerFactory.getLogger(TestInstanceInformation.class);

    @XmlElement(name = "TestInstanceId")
    private String testInstanceId;

    @XmlElement(name = "TestInstanceStatus")
    private String testInstanceStatus;

    @XmlElement(name = "TestKeyword")
    private String testKeyword;

    @XmlElement(name = "Steps")
    private List<TestStepsInstance> testStepsInstances;

    @XmlElement(name = "Participants")
    private List<TestInstanceParticipants> testInstanceParticipants;

    @XmlElement(name = "testingSession")
    private TestingSession testingSession;

    public TestInstanceInformation() {

    }

    public String getTestInstanceId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestInstanceId");
        }
        return testInstanceId;
    }

    public void setTestInstanceId(String testInstanceId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestInstanceId");
        }
        this.testInstanceId = testInstanceId;
    }

    public String getTestInstanceStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestInstanceStatus");
        }
        return testInstanceStatus;
    }

    public void setTestInstanceStatus(String testInstanceStatus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestInstanceStatus");
        }
        this.testInstanceStatus = testInstanceStatus;
    }

    public String getTestKeyword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestKeyword");
        }
        return testKeyword;
    }

    public void setTestKeyword(String testKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestKeyword");
        }
        this.testKeyword = testKeyword;
    }

    public List<TestStepsInstance> getTestStepsInstances() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestStepsInstances");
        }
        return testStepsInstances;
    }

    public void setTestStepsInstances(List<TestStepsInstance> testStepsInstances) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestStepsInstances");
        }
        this.testStepsInstances = testStepsInstances;
    }

    public List<TestInstanceParticipants> getTestInstanceParticipants() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestInstanceParticipants");
        }
        return testInstanceParticipants;
    }

    public void setTestInstanceParticipants(List<TestInstanceParticipants> testInstanceParticipants) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestInstanceParticipants");
        }
        this.testInstanceParticipants = testInstanceParticipants;
    }

    public TestingSession getTestingSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestingSession");
        }
        return testingSession;
    }

    public void setTestingSession(TestingSession testingSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestingSession");
        }
        this.testingSession = testingSession;
    }

}
