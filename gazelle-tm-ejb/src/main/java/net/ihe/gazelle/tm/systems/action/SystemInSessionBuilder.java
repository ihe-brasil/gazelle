package net.ihe.gazelle.tm.systems.action;

import net.ihe.gazelle.tm.application.action.ApplicationManager;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.*;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.Role;
import net.ihe.gazelle.users.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by gthomazon on 05/07/17.
 */
public class SystemInSessionBuilder extends AbstractSystemInSessionBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(SystemInSessionCreator.class);

    public SystemInSessionBuilder() {
    }

    public SystemInSessionBuilder(SystemType sysType, String sysversion, String syskeywordSuffix, String sysname, Institution
            institutionForCreation, User sysOwnerUser) {
        super(sysType, sysversion, syskeywordSuffix, sysname, institutionForCreation, sysOwnerUser);
    }

    public SystemInSessionBuilder(SystemType sysType, String sysversion, String syskeywordSuffix, String sysname, Institution
            institutionForCreation, User sysOwnerUser, SystemInSession systemInSession) {
        super(sysType, sysversion, syskeywordSuffix, sysname, institutionForCreation, sysOwnerUser, systemInSession);
    }

    private Institution getInstitutionOfCurrentUser() {
        return Institution.getLoggedInInstitution();
    }

    private void setDefaultRegistrationStatus() {
        systemInSession.setRegistrationStatus(SystemInSessionRegistrationStatus.IN_PROGRESS);
    }

    /**
     * Add (create/update) a system to the database This operation is allowed for some granted users (check the security.drl)
     */
    public void addSystemForTM() throws SystemActionException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addSystemForTM");
        }
        System newSystem = initSystemWithFormValues();
        createNewSystemInSession(newSystem);
        calculateFinancialSummaryDTO();
    }

    public System initSystemWithFormValues() {
        generateSystemForSystemInSession(systemInSession);
        System newSystem = systemInSession.getSystem();
        newSystem.setName(sysname);
        newSystem.setSystemType(sysType);
        newSystem.setVersion(sysversion);
        newSystem.setKeywordSuffix(syskeywordSuffix);
        newSystem.setOwnerUser(sysOwnerUser);
        return newSystem;
    }

    private void createNewSystemInSession(System newSystem) throws SystemActionException {
        if (institutionForCreation != null) {
            newSystem.setInstitutionForCreation(institutionForCreation);
        }
        if (isSystemKeywordValid()) {
            buildRealKeyword();

            newSystem = entityManager.merge(newSystem);
            InstitutionSystem institutionSystem = new InstitutionSystem(newSystem, institutionForCreation);
            institutionSystem = entityManager.merge(institutionSystem);

            TableSession tableSession = TableSession.getTableSessionByKeyword(TableSession
                    .getDEFAULT_TABLE_SESSION_STRING());
            SystemInSessionStatus systemInSessionStatus = SystemInSessionStatus.getSTATUS_NOT_HERE_YET();
            systemInSession = new SystemInSession(tableSession, newSystem,
                    TestingSession.getSelectedTestingSession(), null, systemInSessionStatus);

            Set<InstitutionSystem> is = new HashSet<InstitutionSystem>();
            is.add(institutionSystem);
            newSystem.setInstitutionSystems(is);

            setDefaultRegistrationStatus();

            systemInSession.setIsCopy(false);

            systemInSession = entityManager.merge(systemInSession);
            entityManager.flush();
        }
    }

    public void generateSystemForSystemInSession(SystemInSession sis) {
        System system = new System();

        User systemOwner = new User();
        if (Role.isLoggedUserVendorAdmin() || Role.isLoggedUserVendorUser()) {
            systemOwner = User.loggedInUser();
        }

        if (ApplicationManager.instance().isProductRegistry()) {
            SystemTypeQuery query = new SystemTypeQuery();
            SystemType systemType = query.getUniqueResult();
            system.setSystemType(systemType);

            system.setIntegrationStatementDate(new Date());
        } else {
            system.setSystemType(null);
        }
        system.setOwnerUser(systemOwner);

        sis.setSystem(system);
    }
}
