package net.ihe.gazelle.tm.systems.action;

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.tm.application.action.ApplicationManager;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.SystemType;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.User;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.Serializable;

import static org.jboss.seam.ScopeType.PAGE;

/**
 * Created by gthomazon on 05/07/17.
 */
@Name("systemInSessionCreator")
@Scope(PAGE)
@Synchronized(timeout = 10000)
@GenerateInterface("SystemInSessionCreatorLocal")
public class SystemInSessionCreator extends AbstractSystemInSessionEditor implements Serializable, SystemInSessionCreatorLocal {

    private static final Logger LOG = LoggerFactory.getLogger(SystemInSessionCreator.class);
    protected SystemType sysType;
    protected String sysversion;
    protected String syskeywordSuffix;
    protected String sysname;
    protected User sysOwnerUser;
    @In
    private EntityManager entityManager;
    private SystemInSessionBuilder sisBuilder;

    public SystemInSessionCreator() {
    }

    public SystemInSessionCreator(SystemInSessionBuilder sisBuilder) {
        this.sisBuilder = sisBuilder;
    }

    public SystemType getSysType() {
        return sysType;
    }

    public void setSysType(SystemType sysType) {
        this.sysType = sysType;
    }

    public String getSysversion() {
        return sysversion;
    }

    public void setSysversion(String sysversion) {
        this.sysversion = sysversion;
    }

    public String getSyskeywordSuffix() {
        return syskeywordSuffix;
    }

    public void setSyskeywordSuffix(String syskeywordSuffix) {
        this.syskeywordSuffix = syskeywordSuffix;
    }

    public String getSysname() {
        return sysname;
    }

    public void setSysname(String sysname) {
        this.sysname = sysname;
    }

    public Institution getInstitutionForCreation() {
        return institutionForCreation;
    }

    public void setInstitutionForCreation(Institution institutionForCreation) {
        this.institutionForCreation = institutionForCreation;
    }

    public User getSysOwnerUser() {
        return sysOwnerUser;
    }

    public void setSysOwnerUser(User sysOwnerUser) {
        this.sysOwnerUser = sysOwnerUser;
    }

    @Override
    @Create
    public void initCreation() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initCreation");
        }
        sysOwnerUser = User.loggedInUser();
        institutionForCreation = Institution.getLoggedInInstitution();
    }

    /**
     * Add (create) a system to the database This operation is allowed for some granted users (check the security.drl)
     */
    @Override
    @Restrict("#{s:hasPermission('SystemInSessionCreator', 'addSystemForPR', null)}")
    public void addSystemForPR(SystemInSessionBuilder sisBuilder) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addSystemForPR");
        }
        System sys = entityManager.find(System.class, sisBuilder.getSystemInSession().getSystem().getId());
        sisBuilder.addSystemForPR(sys);
    }

    /**
     * Add (create) a system to the database This operation is allowed for some granted users (check the security.drl)
     */
    private void addSystemForTM(SystemInSessionBuilder sisBuilder) throws SystemActionException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addSystemForTM");
        }
        sisBuilder.addSystemForTM();
    }

    /**
     * Add (create) a system to the database This operation is allowed for some granted users (check the security.drl)
     */
    @Override
    @Restrict("#{s:hasPermission('SystemInSessionCreator', 'addSystem', null)}")
    public String addSystem() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addSystem");
        }
        if (TestingSession.getSelectedTestingSession() == null) {
            LOG.error(NO_ACTIVE_SESSION);
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.systems.error.noActivatedTestingSession");
        } else {
            sisBuilder = new SystemInSessionBuilder(sysType, sysversion, syskeywordSuffix, sysname, institutionForCreation, sysOwnerUser);
            try {
                addSystemForTM(sisBuilder);
                if (ApplicationManager.instance().isProductRegistry()) {
                    addSystemForPR(sisBuilder);
                }
                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, "gazelle.systems.system.faces" +
                        ".SystemSuccessfullyCreated", sysname);

                setDefaultTab("editSystemSummaryTab");
                return "/systems/system/editSystemInSession.seam?id=" + sisBuilder.getSystemInSession().getId();
            } catch (SystemActionException e) {
                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, e.getMessage());
            }
        }
        return null;
    }

    /**
     * Generate the system keyword, using the selected system type and the institution keyword eg. PACS_AGFA_
     */
    @Override
    @Restrict("#{s:hasPermission('SystemInSessionCreator', 'generateSystemKeyword', null)}")
    public void generateSystemKeyword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateSystemKeyword");
        }
        try {
            setSyskeywordSuffix(SystemInSessionBuilder.generateSystemKeywordSuffix(institutionForCreation, sysType));
        } catch (SystemActionException e) {
            StatusMessages.instance().add(StatusMessage.Severity.ERROR, e.getMessage());
        }
    }

    @Override
    public String getSystemInSessionKeywordBase() {
        return SystemInSessionBuilder.getSystemInSessionKeywordBase(sysType, institutionForCreation);
    }

}
