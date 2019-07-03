package net.ihe.gazelle.tm.gazelletest.ws.trm;

import net.ihe.gazelle.tm.gazelletest.model.definition.RoleInTest;
import net.ihe.gazelle.tm.gazelletest.model.instance.SystemInSessionUser;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceParticipants;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceParticipantsStatus;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.users.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TRMParticipant {
    private static final Logger LOG = LoggerFactory.getLogger(TRMParticipant.class);

    private TRMaipo aipo;
    private Integer roleInTestId;
    private String status;
    private String systemUsername;
    private String system;

    public void copyFromTestInstanceParticipant(TestInstanceParticipants testInstanceParticipant) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("copyFromTestInstanceParticipant");
        }
        this.aipo = new TRMaipo();
        this.aipo.loadFromAipo(testInstanceParticipant.getActorIntegrationProfileOption());
        RoleInTest roleInTest = testInstanceParticipant.getRoleInTest();
        if (roleInTest != null) {
            this.roleInTestId = roleInTest.getId();
        } else {
            this.roleInTestId = -1;
        }
        TestInstanceParticipantsStatus status2 = testInstanceParticipant.getStatus();
        if (status2 != null) {
            this.status = status2.getKeyword();
        }
        SystemInSessionUser sisu = testInstanceParticipant.getSystemInSessionUser();
        if (sisu != null) {
            User user = sisu.getUser();
            if (user != null) {
                this.systemUsername = user.getUsername();
            }
            SystemInSession systemInSession = sisu.getSystemInSession();
            if (systemInSession != null) {
                this.system = systemInSession.getSystem().getKeyword();
            }
        }
    }

    public TRMaipo getAipo() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAipo");
        }
        return aipo;
    }

    public void setAipo(TRMaipo aipo) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setAipo");
        }
        this.aipo = aipo;
    }

    public Integer getRoleInTestId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRoleInTestId");
        }
        return roleInTestId;
    }

    public void setRoleInTestId(Integer roleInTestId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setRoleInTestId");
        }
        this.roleInTestId = roleInTestId;
    }

    public String getStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getStatus");
        }
        return status;
    }

    public void setStatus(String status) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setStatus");
        }
        this.status = status;
    }

    public String getSystem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystem");
        }
        return system;
    }

    public void setSystem(String system) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSystem");
        }
        this.system = system;
    }

    public String getSystemUsername() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemUsername");
        }
        return systemUsername;
    }

    public void setSystemUsername(String systemUsername) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSystemUsername");
        }
        this.systemUsername = systemUsername;
    }

}
