package net.ihe.gazelle.tm.gazelletest.bean;

import net.ihe.gazelle.tm.gazelletest.model.definition.RoleInTest;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class RoleInTestSystemInSession implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(RoleInTestSystemInSession.class);
    private static final long serialVersionUID = -3968712038630847330L;

    private RoleInTest roleInTest;

    private SystemInSession systemInSession;

    public RoleInTestSystemInSession(RoleInTest roleInTest, SystemInSession systemInSession) {
        this.roleInTest = roleInTest;
        this.systemInSession = systemInSession;
    }

    public RoleInTest getRoleInTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRoleInTest");
        }
        return roleInTest;
    }

    public void setRoleInTest(RoleInTest roleInTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setRoleInTest");
        }
        this.roleInTest = roleInTest;
    }

    public SystemInSession getSystemInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemInSession");
        }
        return systemInSession;
    }

    public void setSystemInSession(SystemInSession systemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSystemInSession");
        }
        this.systemInSession = systemInSession;
    }

    @Override
    public int hashCode() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("hashCode");
        }
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((roleInTest == null) ? 0 : roleInTest.hashCode());
        result = (prime * result) + ((systemInSession == null) ? 0 : systemInSession.hashCode());
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
        RoleInTestSystemInSession other = (RoleInTestSystemInSession) obj;
        if (roleInTest == null) {
            if (other.roleInTest != null) {
                return false;
            }
        } else if (!roleInTest.equals(other.roleInTest)) {
            return false;
        }
        if (systemInSession == null) {
            if (other.systemInSession != null) {
                return false;
            }
        } else if (!systemInSession.equals(other.systemInSession)) {
            return false;
        }
        return true;
    }

}
