package net.ihe.gazelle.tm.systems.action;

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.tm.configurations.model.Host;
import net.ihe.gazelle.tm.systems.model.InstitutionSystem;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.Role;
import net.ihe.gazelle.users.model.User;
import org.jboss.seam.annotations.*;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.jboss.seam.ScopeType.PAGE;

@Name("systemManager")
@Scope(PAGE)
@Synchronized(timeout = 10000)
@GenerateInterface("SystemManagerLocal")
public class SystemManager implements Serializable, SystemManagerLocal {
    /**
     * Serial ID version of this object
     */
    private static final long serialVersionUID = -450911331542283760L;

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SystemManager.class);

    /**
     * EntityManager is the interface used to interact with the persistence context
     */
    @In(required = false)
    private EntityManager entityManager;

    /**
     * SelectedSystemInSession object managed my this manager bean and injected in JSF
     */
    private SystemInSession selectedSystemInSession;

    // ~ Methods
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public SystemManager() throws Exception {
        super();
    }

    public static List<System> removeDuplicateSystems(List<System> systemList) {
        Set<System> systems = new HashSet<System>();
        systems.addAll(systemList);
        systemList.clear();
        systemList.addAll(systems);
        return systemList;
    }

    @Override
    public String displayInstitutionKeywordForHost(Host host) {
        return host.getInstitution().getKeyword();
    }

    @Override
    public String displayInstitutionNameForHost(Host host) {
        return host.getInstitution().getName();
    }

    @Override
    public String displayInstitutionsForSystemsInSession(List<SystemInSession> inSystemsInSession) {
        List<System> systemList = getSystems(inSystemsInSession);
        return displayInstitutionForSystem(systemList.get(0));
    }

    private List<System> getSystems(List<SystemInSession> inSystemsInSession) {
        List<System> systemList = new ArrayList<>();
        for (SystemInSession systemInSession : inSystemsInSession) {
            systemList.add(systemInSession.getSystem());
        }
        removeDuplicateSystems(systemList);
        return systemList;
    }

    @Override
    public String displayInstitutionNameForSystemsInSession(List<SystemInSession> inSystemsInSession) {
        List<System> systemList = getSystems(inSystemsInSession);
        return displayInstitutionNameForSystem(systemList.get(0));
    }

    public static String displayInstitutionsForSystemInSession(SystemInSession inSystemSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("String displayInstitutionsForSystemInSession");
        }
        List<Institution> listInst = System.getInstitutionsForASystem(inSystemSession.getSystem());
        StringBuffer s = new StringBuffer();
        int size = listInst.size();
        for (int i = 0; i < size; i++) {
            s.append(listInst.get(i).getKeyword().replace("\"", ""));
            if (i < (size - 1)) {
                s.append("/");
            }
        }
        return s.toString();
    }

    @Override
    public String displayInstitutionForSystem(System inSystem) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("displayInstitutionForSystem");
        }
        List<Institution> listInst = System.getInstitutionsForASystem(inSystem);
        StringBuffer s = new StringBuffer();
        if (listInst != null) {
            int size = listInst.size();
            for (int i = 0; i < size; i++) {
                if ((size > 1) && (i == (size - 1))) {
                    s.append(" / ");
                }
                if (listInst.get(i) != null) {
                    s.append(listInst.get(i).getKeyword());
                }
            }
            return s.toString();
        } else {
            return "";
        }
    }

    @Override
    public String displayInstitutionForSystemInSession(SystemInSession inSystemSession) {
        LOG.debug("displayInstitutionForSystemInSession");
        return displayInstitutionsForSystemInSession(inSystemSession);
    }

    @Override
    public String displayInstitutionNameForSystemInSession(SystemInSession inSystemSession) {
        LOG.debug("displayInstitutionNameForSystemInSession");
        return displayInstitutionNameForSystem(inSystemSession.getSystem());
    }

    @Override
    public String displayInstitutionNameForSystem(System inSystem) {
        LOG.debug("displayInstitutionNameForSystemInSession");
        List<Institution> listInst = System.getInstitutionsForASystem(inSystem);
        StringBuffer s = new StringBuffer();
        int size = listInst.size();
        for (int i = 0; i < size; i++) {
            s.append(listInst.get(i).getName().replace("\"", ""));
            if ((size > 1) && (i == (size - 1))) {
                s.append("/");
            }
        }
        return s.toString();
    }

    @Override
    public SystemInSession getSelectedSystemInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedSystemInSession");
        }
        return selectedSystemInSession;
    }

    @Override
    public void setSelectedSystemInSession(SystemInSession selectedSystemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedSystemInSession");
        }
        this.selectedSystemInSession = selectedSystemInSession;
    }

    @Override
    public boolean canViewSystemInSessionAsVendor(SystemInSession sis) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("canViewSystemInSessionAsVendor");
        }
        if (sis != null) {
            if (Role.isLoggedUserAdmin() || Role.isLoggedUserMonitor() || Role.isLoggedUserProjectManager()) {
                return true;
            }
            System s = entityManager.find(System.class, sis.getSystem().getId());
            Set<InstitutionSystem> institutionSystems = s.getInstitutionSystems();
            for (InstitutionSystem institutionSystem : institutionSystems) {
                if (institutionSystem.getInstitution().getId().equals(User.loggedInUser().getInstitution().getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Destroy the Manager bean when the session is over.
     */
    @Override
    @Destroy
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }
    }
}
