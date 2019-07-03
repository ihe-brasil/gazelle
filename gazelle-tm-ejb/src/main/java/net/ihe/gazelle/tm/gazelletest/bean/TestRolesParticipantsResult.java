package net.ihe.gazelle.tm.gazelletest.bean;

import net.ihe.gazelle.tm.gazelletest.model.definition.TestPeerType;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestRoles;
import net.ihe.gazelle.tm.gazelletest.model.instance.Status;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceParticipants;
import net.ihe.gazelle.tm.systems.model.SystemActorProfiles;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class TestRolesParticipantsResult implements Serializable, Comparable<TestRolesParticipantsResult> {

    private static final Logger LOG = LoggerFactory.getLogger(TestRolesParticipantsResult.class);

    private static final long serialVersionUID = 1L;

    private TestRoles testRoles;

    private SystemActorProfiles systemActorProfiles;

    private TestPeerType testPeerType;

    private boolean isConnectedUserAllowedToStartTest;

    private Map<Status, List<TestInstanceParticipants>> testInstanceParticipantsMap;

    private AIPOSystemPartners partners;

    public TestRolesParticipantsResult(TestingSession testingSession, TestRoles testRoles, TestPeerType testPeerType) {
        this.partners = new AIPOSystemPartners(testingSession);
        this.testRoles = testRoles;
        this.testPeerType = testPeerType;
    }

    public TestRolesParticipantsResult(TestingSession testingSession, TestRoles testRoles, TestPeerType testPeerType,
                                       SystemActorProfiles systemActorProfiles) {
        this(testingSession, testRoles, testPeerType);
        this.systemActorProfiles = systemActorProfiles;
    }

    public TestRoles getTestRoles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestRoles");
        }
        return testRoles;
    }

    public void setTestRoles(TestRoles testRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestRoles");
        }
        this.testRoles = testRoles;
    }

    public SystemActorProfiles getSystemActorProfiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemActorProfiles");
        }
        return systemActorProfiles;
    }

    public void setSystemActorProfiles(SystemActorProfiles systemActorProfiles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSystemActorProfiles");
        }
        this.systemActorProfiles = systemActorProfiles;
    }

    public Map<Status, List<TestInstanceParticipants>> getTestInstanceParticipantsMap() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestInstanceParticipantsMap");
        }
        return testInstanceParticipantsMap;
    }

    public void setTestInstanceParticipantsMap(Map<Status, List<TestInstanceParticipants>> testInstanceParticipantsMap) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestInstanceParticipantsMap");
        }
        this.testInstanceParticipantsMap = testInstanceParticipantsMap;
    }

    public TestPeerType getTestPeerType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestPeerType");
        }
        return testPeerType;
    }

    public void setTestPeerType(TestPeerType testPeerType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestPeerType");
        }
        this.testPeerType = testPeerType;
    }

    public AIPOSystemPartners getPartners() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPartners");
        }
        return partners;
    }

    public void setPartners(AIPOSystemPartners partners) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setPartners");
        }
        this.partners = partners;
    }

    public List<TestInstanceParticipants> getStartedTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getStartedTests");
        }
        return getTestInstanceParticipantsByStatus(Status.getSTATUS_STARTED());
    }

    public List<TestInstanceParticipants> getPausedTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPausedTests");
        }
        return getTestInstanceParticipantsByStatus(Status.getSTATUS_PAUSED());
    }

    public List<TestInstanceParticipants> getPartiallyVerifiedTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPartiallyVerifiedTests");
        }
        return getTestInstanceParticipantsByStatus(Status.getSTATUS_PARTIALLY_VERIFIED());
    }

    public List<TestInstanceParticipants> getCompletedTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCompletedTests");
        }
        return getTestInstanceParticipantsByStatus(Status.getSTATUS_COMPLETED());
    }

    public List<TestInstanceParticipants> getCriticalTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCriticalTests");
        }
        return getTestInstanceParticipantsByStatus(Status.getSTATUS_CRITICAL());
    }

    public List<TestInstanceParticipants> getVerifiedTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getVerifiedTests");
        }
        return getTestInstanceParticipantsByStatus(Status.getSTATUS_VERIFIED());
    }

    public List<TestInstanceParticipants> getFailedTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFailedTests");
        }
        return getTestInstanceParticipantsByStatus(Status.getSTATUS_FAILED());
    }

    public List<TestInstanceParticipants> getTestInstanceParticipantsByStatus(Status inStatus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestInstanceParticipantsByStatus");
        }
        if ((testInstanceParticipantsMap != null) && (inStatus != null)) {
            return this.testInstanceParticipantsMap.get(inStatus);
        }
        return null;
    }

    public boolean getIsConnectedUserAllowedToStartTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIsConnectedUserAllowedToStartTest");
        }
        return isConnectedUserAllowedToStartTest;
    }

    public void setIsConnectedUserAllowedToStartTest(boolean isConnectedUserAllowedToStartTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setIsConnectedUserAllowedToStartTest");
        }
        this.isConnectedUserAllowedToStartTest = isConnectedUserAllowedToStartTest;
    }

    @Override
    public int hashCode() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("hashCode");
        }
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((systemActorProfiles == null) ? 0 : systemActorProfiles.hashCode());
        result = (prime * result)
                + ((testInstanceParticipantsMap == null) ? 0 : testInstanceParticipantsMap.hashCode());
        result = (prime * result) + ((testPeerType == null) ? 0 : testPeerType.hashCode());
        result = (prime * result) + ((testRoles == null) ? 0 : testRoles.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("equals");
        }
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TestRolesParticipantsResult other = (TestRolesParticipantsResult) obj;
        if (testPeerType == null) {
            if (other.testPeerType != null) {
                return false;
            }
        } else if (!testPeerType.equals(other.testPeerType)) {
            return false;
        }
        if (testRoles == null) {
            if (other.testRoles != null) {
                return false;
            }
        } else if (!testRoles.equals(other.testRoles)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(TestRolesParticipantsResult o) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("compareTo");
        }
        return this.testRoles.compareTo(o.testRoles);
    }

}
