package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.tf.model.Actor;
import net.ihe.gazelle.tf.model.IntegrationProfile;
import net.ihe.gazelle.tf.model.Transaction;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.User;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Remove;
import java.io.Serializable;
import java.util.List;

@Name("modalPanelManager")
@Scope(ScopeType.PAGE)
public class ModalPanelManager implements Serializable {

    private static final long serialVersionUID = -2095525476059634481L;

    private static final Logger LOG = LoggerFactory.getLogger(ModalPanelManager.class);

    private User selectedUser;

    private SystemInSession selectedSystemInSession;

    private Test selectedTest;

    private Institution selectedInstitution;

    private IntegrationProfile selectedIntegrationProfile;

    private Actor selectedActor;

    private List<SystemInSession> systemInSessionList;

    private Transaction selectedTransaction;

    private boolean displayConfigurationPanel = false;

    public User getSelectedUser() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedUser");
        }
        return selectedUser;
    }

    public void setSelectedUser(User selectedUser) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedUser");
        }
        this.selectedUser = selectedUser;
    }

    public SystemInSession getSelectedSystemInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedSystemInSession");
        }
        return selectedSystemInSession;
    }

    public void setSelectedSystemInSession(SystemInSession selectedSystemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedSystemInSession");
        }
        this.selectedSystemInSession = selectedSystemInSession;
    }

    public void setSelectedSystemInSessionAndActor(SystemInSession selectedSystemInSession, Actor selectedActor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedSystemInSessionAndActor");
        }
        this.selectedSystemInSession = selectedSystemInSession;
        this.selectedActor = selectedActor;
        setDisplayConfigurationPanel(true);
    }

    public Test getSelectedTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTest");
        }
        return selectedTest;
    }

    public void setSelectedTest(Test selectedTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTest");
        }
        if (selectedTest != null) {
            this.selectedTest = Test.getTestWithAllAttributes(selectedTest);
        }
    }

    public Institution getSelectedInstitution() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedInstitution");
        }
        return selectedInstitution;
    }

    public void setSelectedInstitution(Institution selectedInstitution) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedInstitution");
        }
        this.selectedInstitution = selectedInstitution;
    }

    public IntegrationProfile getSelectedIntegrationProfile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedIntegrationProfile");
        }
        return selectedIntegrationProfile;
    }

    public void setSelectedIntegrationProfile(IntegrationProfile selectedIntegrationProfile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedIntegrationProfile");
        }
        this.selectedIntegrationProfile = selectedIntegrationProfile;
    }

    public Actor getSelectedActor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedActor");
        }
        return selectedActor;
    }

    public void setSelectedActor(Actor selectedActor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedActor");
        }
        this.selectedActor = selectedActor;
    }

    public Transaction getSelectedTransaction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTransaction");
        }
        return selectedTransaction;
    }

    public void setSelectedTransaction(Transaction selectedTransaction) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTransaction");
        }
        this.selectedTransaction = selectedTransaction;
    }

    public List<SystemInSession> getSystemInSessionList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemInSessionList");
        }
        return systemInSessionList;
    }

    public void setSystemInSessionList(List<SystemInSession> systemInSessionList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSystemInSessionList");
        }
        this.systemInSessionList = systemInSessionList;
    }

    public boolean isDisplayConfigurationPanel() {
        return displayConfigurationPanel;
    }

    public void setDisplayConfigurationPanel(boolean displayConfigurationPanel) {
        this.displayConfigurationPanel = displayConfigurationPanel;
    }

    @Remove
    @Destroy
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }
    }
}
