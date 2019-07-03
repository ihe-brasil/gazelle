package net.ihe.gazelle.tm.systems.action;

import net.ihe.gazelle.common.log.ExceptionLogging;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.financial.action.FinancialManager;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.*;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gthomazon on 17/07/17.
 */
public abstract class AbstractSystemInSessionBuilder implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSystemInSessionBuilder.class);

    protected SystemInSession systemInSession = new SystemInSession();
    protected EntityManager entityManager = EntityManagerService.provideEntityManager();
    protected Institution institutionForCreation;
    protected SystemType sysType;
    protected String sysversion;
    protected String syskeywordSuffix;
    protected String sysname;
    protected User sysOwnerUser;

    public AbstractSystemInSessionBuilder() {
    }

    public AbstractSystemInSessionBuilder(SystemType sysType, String sysversion, String syskeywordSuffix, String sysname, Institution
            institutionForCreation, User sysOwnerUser) {
        this.institutionForCreation = institutionForCreation;
        this.sysType = sysType;
        this.sysversion = sysversion;
        this.syskeywordSuffix = syskeywordSuffix;
        this.sysname = sysname;
        this.sysOwnerUser = sysOwnerUser;
    }

    public AbstractSystemInSessionBuilder(SystemType sysType, String sysversion, String syskeywordSuffix, String sysname, Institution
            institutionForCreation, User sysOwnerUser, SystemInSession systemInSession) {
        this.systemInSession = systemInSession;
        this.institutionForCreation = institutionForCreation;
        this.sysType = sysType;
        this.sysversion = sysversion;
        this.syskeywordSuffix = syskeywordSuffix;
        this.sysname = sysname;
        this.sysOwnerUser = sysOwnerUser;
    }

    /**
     * Generate the system keyword suffix, using the selected system type and the institution keyword eg. PACS_AGFA_
     */
    public static String generateSystemKeywordSuffix(Institution institutionToUse, SystemType systemType) throws SystemActionException {
        String suggestedKeyword = null;
        Integer increment = -1;

        if (institutionToUse != null) {
            suggestedKeyword = getSystemInSessionKeywordBase(systemType, institutionToUse).toUpperCase();
            List<System> listOfExistingSystemsForCurrentInstitution = new ArrayList<>();

            try {
                listOfExistingSystemsForCurrentInstitution = System.getSystemsForAnInstitution(institutionToUse);
                LOG.debug("generateSystemKeyword : Vendor - listOfExistingSystemsForCurrentInstitution = "
                        + listOfExistingSystemsForCurrentInstitution);
                LOG.debug("generateSystemKeyword : listOfExistingSystemsForCurrentInstitution = "
                        + listOfExistingSystemsForCurrentInstitution.size());

            } catch (Exception e) {
                ExceptionLogging.logException(e, LOG);
                throw new SystemActionException("Problem generating the keyword : " + e.getMessage());
            }

            List<String> existingKeywords = new ArrayList<>();
            for (System system : listOfExistingSystemsForCurrentInstitution) {
                existingKeywords.add(system.getKeyword().toUpperCase());
            }

            String suggestedKeywordBase = suggestedKeyword.toUpperCase();
            while (existingKeywords.contains(suggestedKeyword)) {
                increment++;
                suggestedKeyword = suggestedKeywordBase + "_" + increment;
            }
            if (increment != -1) {
                return increment.toString();
            } else {
                return "";
            }

        } else {
            throw new SystemActionException("Unable to generate system keyword, institution required");
        }
    }

    public static String getSystemInSessionKeywordBase(SystemType systemType, Institution institution) {
        if (systemType == null || institution == null) {
            return "";
        } else {
            return systemType.getSystemTypeKeyword() + "_" + institution.getKeyword();
        }
    }

    public SystemInSession getSystemInSession() {
        return systemInSession;
    }

    public void setSystemInSession(SystemInSession systemInSession) {
        this.systemInSession = systemInSession;
    }

    public Institution getInstitutionForCreation() {
        return institutionForCreation;
    }

    public void setInstitutionForCreation(Institution institutionForCreation) {
        this.institutionForCreation = institutionForCreation;
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

    public User getSysOwnerUser() {
        return sysOwnerUser;
    }

    public void setSysOwnerUser(User sysOwnerUser) {
        this.sysOwnerUser = sysOwnerUser;
    }


    protected boolean isSystemKeywordValid() throws SystemActionException {
        // Before adding the system, we check that validation is good - system
        // keyword does not already exist
        if (!SystemInSessionValidator.validateSystemKeywordStatic(systemInSession.getSystem().getKeywordWithoutSuffix(),
                systemInSession.getSystem().getKeywordSuffix(), systemInSession.getSystem(), false)) {
            return false;
        }
        if (!SystemInSessionValidator.validateSystemNameAndSystemVersionStatic(systemInSession.getSystem().getName(), systemInSession
                .getSystem().getVersion(), systemInSession.getSystem().getId(), entityManager)) {
            return false;
        }
        return true;
    }

    protected void buildRealKeyword() {
        if ((systemInSession.getSystem().getKeywordSuffix() == null) || (systemInSession.getSystem().getKeywordSuffix().equals(""))) {
            systemInSession.getSystem().setKeyword(getSystemInSessionKeywordBase(sysType, institutionForCreation));

        } else {
            systemInSession.getSystem().setKeyword(getSystemInSessionKeywordBase(sysType, institutionForCreation) + "_"
                    + systemInSession.getSystem().getKeywordSuffix());
        }
    }

    protected void calculateFinancialSummaryDTO() {
        TestingSession activatedTestingSession = TestingSession.getSelectedTestingSession();
        FinancialManager.calculateFinancialSummaryDTOByTestingSessionByInstitution(activatedTestingSession, institutionForCreation);
    }

    /**
     * Add (create/update) a system to the database This operation is allowed for some granted users (check the security.drl)
     */
    public void addSystemForPR(System system) {
        LOG.debug("addSystemForPR");
        system.setPrStatus(IntegrationStatementStatus.CREATED);
        system.setPrChecksumValidated(null);
        system.setPrChecksumGenerated(null);
        entityManager.persist(system);
        entityManager.flush();
    }
}
