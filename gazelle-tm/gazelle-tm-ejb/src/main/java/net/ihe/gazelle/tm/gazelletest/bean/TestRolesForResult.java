package net.ihe.gazelle.tm.gazelletest.bean;

import net.ihe.gazelle.tm.gazelletest.model.definition.TestPeerType;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestRoles;
import net.ihe.gazelle.tm.systems.model.SystemActorProfiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class TestRolesForResult implements Serializable, Comparable<TestRolesForResult> {

    private static final Logger LOG = LoggerFactory.getLogger(TestRolesForResult.class);
    private static final long serialVersionUID = -7083669822338478013L;

    private TestRoles testRoles;

    private SystemActorProfiles systemActorProfiles;

    private TestPeerType testPeerType;

    public TestRolesForResult(TestRoles testRoles, TestPeerType testPeerType, SystemActorProfiles systemActorProfiles) {
        this.testRoles = testRoles;
        this.testPeerType = testPeerType;
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

    @Override
    public int compareTo(TestRolesForResult o) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("compareTo");
        }
        return this.testRoles.compareTo(o.getTestRoles());
    }

    @Override
    public int hashCode() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("hashCode");
        }
        final int prime = 31;
        int result = 1;
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
        TestRolesForResult other = (TestRolesForResult) obj;
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
        if (systemActorProfiles == null) {
            if (other.systemActorProfiles != null) {
                return false;
            }
        } else {
            if (systemActorProfiles.getActorIntegrationProfileOption() == null) {
                if ((other.getSystemActorProfiles() == null)
                        || (other.getSystemActorProfiles().getActorIntegrationProfileOption() != null)) {
                    return false;
                }
            } else {
                if (systemActorProfiles.getActorIntegrationProfileOption().getActorIntegrationProfile() == null) {
                    if ((other.getSystemActorProfiles() == null)
                            || (other.getSystemActorProfiles().getActorIntegrationProfileOption() == null)
                            || (other.getSystemActorProfiles().getActorIntegrationProfileOption()
                            .getActorIntegrationProfile() != null)) {
                        return false;
                    }
                } else {
                    if ((other.getSystemActorProfiles() == null)
                            || (other.getSystemActorProfiles().getActorIntegrationProfileOption() == null)
                            || (other.getSystemActorProfiles().getActorIntegrationProfileOption()
                            .getActorIntegrationProfile() == null)) {
                        return false;
                    } else {
                        return systemActorProfiles
                                .getActorIntegrationProfileOption()
                                .getActorIntegrationProfile()
                                .equals(other.systemActorProfiles.getActorIntegrationProfileOption()
                                        .getActorIntegrationProfile());
                    }
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("toString");
        }
        return this.testRoles.getRoleInTest().getKeyword()
                + "---"
                + this.getSystemActorProfiles().getActorIntegrationProfileOption().getActorIntegrationProfile()
                .getIntegrationProfile().getKeyword()
                + "---"
                + this.getSystemActorProfiles().getActorIntegrationProfileOption().getActorIntegrationProfile()
                .getActor().getKeyword()
                + "---"
                + this.getSystemActorProfiles().getActorIntegrationProfileOption().getIntegrationProfileOption()
                .getKeyword();
    }
}
