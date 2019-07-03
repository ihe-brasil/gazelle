package net.ihe.gazelle.tm.gazelletest.bean;

import net.ihe.gazelle.tm.gazelletest.model.definition.TestRoles;
import net.ihe.gazelle.tm.gazelletest.model.instance.Status;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceParticipants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class TestRolesParticipants implements Serializable, Comparable<TestRolesParticipants> {
    private static final Logger LOG = LoggerFactory.getLogger(TestRolesParticipants.class);
    private static final long serialVersionUID = 1L;

    private TestRoles testRoles;

    private Status status;

    private TestInstanceParticipants testInstanceParticipants;

    public TestRolesParticipants(TestRoles testRoles, Status status) {
        this.testRoles = testRoles;
        this.status = status;
    }

    public TestRolesParticipants(TestRoles testRoles, Status status, TestInstanceParticipants testInstanceParticipants) {
        this.testRoles = testRoles;
        this.status = status;
        this.testInstanceParticipants = testInstanceParticipants;
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

    public TestInstanceParticipants getTestInstanceParticipants() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestInstanceParticipants");
        }
        return testInstanceParticipants;
    }

    public void setTestInstanceParticipants(TestInstanceParticipants testInstanceParticipants) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestInstanceParticipants");
        }
        this.testInstanceParticipants = testInstanceParticipants;
    }

    public Status getStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getStatus");
        }
        return status;
    }

    public void setStatus(Status status) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setStatus");
        }
        this.status = status;
    }

    @Override
    public int hashCode() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("hashCode");
        }
        final int prime = 31;
        int result = 1;
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
        TestRolesParticipants other = (TestRolesParticipants) obj;
        if (testRoles == null) {
            if (other.testRoles != null) {
                return false;
            }
        } else if (!testRoles.equals(other.testRoles)) {
            return false;
        }
        if (status == null) {
            if (other.status != null) {
                return false;
            }
        } else if (!status.equals(other.status)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(TestRolesParticipants o) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("compareTo");
        }
        if (this.testRoles.compareTo(o.testRoles) != 0) {
            return this.testRoles.compareTo(o.testRoles);
        } else {
            return this.status.getKeyword().compareTo(o.status.getKeyword());
        }
    }

}
