package net.ihe.gazelle.tm.systems.action;

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.log.ExceptionLogging;
import net.ihe.gazelle.tm.financial.action.FinancialManager;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.*;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.User;
import net.ihe.gazelle.util.Pair;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;

import static org.jboss.seam.ScopeType.PAGE;

/**
 * Created by gthomazon on 05/07/17.
 */
@Name("systemInSessionCopier")
@Scope(PAGE)
@Synchronized(timeout = 10000)
@GenerateInterface("SystemInSessionCopierLocal")
public class SystemInSessionCopier extends AbstractSystemInSessionEditor implements Serializable, SystemInSessionCopierLocal {

    private static final Logger LOG = LoggerFactory.getLogger(SystemInSessionCopier.class);

    private static final String _COPY = "_copy_";
    private static final String COPY = "COPY_";
    private static final String COPY_ = "_COPY_";
    private static final String NEW_SUFFIX = ", newSuffix = ";
    private static final String NEW_KEY = ", newKey=";
    private static final String KEY_STRING = " + keyString = ";
    private static final String WITH_ID = "   with id=";

    @In
    EntityManager entityManager;
    private List<Pair<Boolean, SystemInSession>> previousSystemsInSession;
    private List<Pair<Boolean, System>> previousSystems;
    private Institution choosenInstitutionForAdmin;

    @Override
    public String getSystemInSessionKeywordBase() {
        return null;
    }

    @Override
    public List<Pair<Boolean, SystemInSession>> getPreviousSystemsInSession() {
        LOG.debug("getPreviousSystemsInSession");
        return previousSystemsInSession;
    }

    @Override
    public void setPreviousSystemsInSession(List<Pair<Boolean, SystemInSession>> previousSystemsInSession) {
        LOG.debug("setPreviousSystemsInSession");
        this.previousSystemsInSession = previousSystemsInSession;
    }

    @Override
    public List<Pair<Boolean, System>> getPreviousSystems() {
        LOG.debug("getPreviousSystems");
        return previousSystems;
    }

    @Override
    public void setPreviousSystems(List<Pair<Boolean, System>> previousSystems) {
        LOG.debug("setPreviousSystems");
        this.previousSystems = previousSystems;
    }

    @Override
    public Institution getChoosenInstitutionForAdmin() {
        return choosenInstitutionForAdmin;
    }

    @Override
    public void setChoosenInstitutionForAdmin(Institution choosenInstitutionForAdmin) {
        this.choosenInstitutionForAdmin = choosenInstitutionForAdmin;
    }

    @Override
    public void getSystemsListToCopyFromSelectedSession(boolean isTestingSessionChoosen) {
        if (isTestingSessionChoosen) {
            ((TestingSessionManagerLocal) Component.getInstance("testingSessionManager")).removenoTestingSessionChoosen();
        } else {
            ((TestingSessionManagerLocal) Component.getInstance("testingSessionManager")).removeTestingSessionChoosenForCopy();
        }
        getSystemsListToCopyFromSelectedSession();
    }

    @Override
    public void getSystemsListToCopyFromSelectedSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemsListToCopyFromSelectedSession");
        }
        TestingSession testingSessionChoosenForCopy = ((TestingSessionManagerLocal) Component
                .getInstance("testingSessionManager")).getTestingSessionChoosenForCopy();
        boolean noTestingSession = ((TestingSessionManagerLocal) Component.getInstance("testingSessionManager"))
                .isNoTestingSessionChoosen();

        if (testingSessionChoosenForCopy != null) {
            LOG.debug("getSystemsListToCopyFromSelectedSession - testingSessionChoosenForCopy = "
                    + testingSessionChoosenForCopy);
            previousSystemsInSession = SystemInSession.getPairBoolSystemsInSessionForCompanyForSession(Institution
                    .getLoggedInInstitution().getName(), testingSessionChoosenForCopy);
            previousSystems = null;
        } else if (noTestingSession) {
            previousSystems = SystemInSession.getPairBoolSystemsForCompanyForSession(Institution
                    .getLoggedInInstitution());
            previousSystemsInSession = null;
        } else {
            previousSystems = null;
            previousSystemsInSession = null;
        }
    }

    /**
     * Copy a system with associated objects (sap and testing type option) from an existing testing session to the current active testing session.
     */
    @Override
    public String copySystemFromPreviousSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("copySystemFromPreviousSession");
        }
        Boolean isNewSystem = false;

        for (int i = 0; (previousSystemsInSession != null) && (i < previousSystemsInSession.size()); i++) {
            if (previousSystemsInSession.get(i).getObject1()) {
                isNewSystem = true;
                copySystem(previousSystemsInSession.get(i).getObject2().getSystem());
            }
        }
        for (int i = 0; (previousSystems != null) && (i < previousSystems.size()); i++) {
            if (previousSystems.get(i).getObject1()) {
                isNewSystem = true;
                copySystem(previousSystems.get(i).getObject2());
            }
        }

        if (!isNewSystem) {
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.WARN,
                    "gazelle.systems.system.faces.PleaseSelectOneOrSeveralSystemToCopyInTheActivatedTestingSession");
            LOG.warn("Please select the system to copy");
            return "";
        }
        this.calculateFinancialSummaryDTO();

        ((TestingSessionManagerLocal) Component.getInstance("testingSessionManager")).setNoTestingSessionChoosen(false);
        return goToSystemList();
    }

    private void calculateFinancialSummaryDTO() {
        TestingSession activatedTestingSession = TestingSession.getSelectedTestingSession();
        this.calculateFinancialSummaryDTOByTestingSession(activatedTestingSession);
    }

    private void calculateFinancialSummaryDTOByTestingSession(TestingSession ts) {
        if (ts == null) {
            return;
        }
        if (choosenInstitutionForAdmin == null) {
            choosenInstitutionForAdmin = Institution.getLoggedInInstitution();
        }
        FinancialManager.calculateFinancialSummaryDTOByTestingSessionByInstitution(ts, choosenInstitutionForAdmin);
    }

    private void copySystem(System system) {
        try {
            // We don't copy configurations associated to this system
            // We don't copy certificates associated to this system
            System newSystem = createSystemCopy(system);
            createSystemInSessionCopy(newSystem);
            createSystemInstitution(newSystem);
            linkAIPO(system, newSystem);
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "System " + system.getKeyword() + " is successfully copied : " + newSystem
                    .getKeyword());
        } catch (SystemActionException e) {
            ExceptionLogging.logException(e, LOG);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                    "Error persisting the new system, maybe a system keyword " + system.getKeyword() + "_Version_"
                            + TestingSession.getSelectedTestingSession().getYear() + " already exists");
        }
    }

    private System createSystemCopy(System system) throws SystemActionException {
        System newSystem = createCopyNewSystem(system);
        newSystem.setOwnerUser(User.loggedInUser());
        newSystem = entityManager.merge(newSystem);
        return newSystem;
    }

    private System createCopyNewSystem(System inSystem) throws SystemActionException {
        System newSystem = new System(inSystem);

        String keyString = inSystem.getKeyword();
        String name = inSystem.getName();
        if (!keyString.contains(_COPY)) {
            keyString += COPY_;
        }
        if (!name.contains(_COPY)) {
            name += COPY_;
        }

        int n = 0;
        String newKey = keyString + n;
        String newName = name + n;
        name += n;

        String newSuffix = COPY + n;
        if (LOG.isInfoEnabled()) {
            LOG.info("Validate :  " + newSystem.getKeyword() + NEW_SUFFIX + newSuffix + NEW_KEY + newKey + KEY_STRING
                    + keyString + WITH_ID + newSystem.getId());
        }
        boolean sysKeywordIsNotUnique = true;
        while (sysKeywordIsNotUnique) {
            try {
                sysKeywordIsNotUnique = !SystemInSessionValidator.validateSystemKeywordStatic(newSystem.getKeyword(), newSuffix, newSystem, true);
            } catch (SystemActionException e) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Validate -  " + newSystem.getKeyword() + NEW_SUFFIX + newSuffix + NEW_KEY + newKey + KEY_STRING
                            + keyString + WITH_ID + newSystem.getId());
                }
                n++;
                newKey = keyString + n;
                newName = name + n;

                newSuffix = COPY + n;
                FacesMessages.instance().clear();
            }
        }

        boolean sysNameAndVersionIsNotUnique = true;
        while (sysNameAndVersionIsNotUnique) {
            try {
                sysNameAndVersionIsNotUnique = !SystemInSessionValidator.validateSystemNameAndSystemVersionStatic(newName, newSystem.getVersion(),
                        newSystem.getId(),
                        entityManager);
            } catch (SystemActionException e) {
                n++;
                newName = name + n;
            }
        }

        newSystem.setName(newName);
        newSystem.setKeywordSuffix(newSuffix);
        newSystem.setKeyword(newKey);
        return newSystem;
    }

    private void createSystemInstitution(System newSystem) {
        InstitutionSystem newInstitutionSystem = new InstitutionSystem(newSystem, Institution.getLoggedInInstitution());
        entityManager.merge(newInstitutionSystem);
    }

    private void createSystemInSessionCopy(System newSystem) {
        TableSession tableSession = TableSession.getTableSessionByKeyword(TableSession.getDEFAULT_TABLE_SESSION_STRING());
        SystemInSessionStatus systemInSessionStatus = SystemInSessionStatus.getSTATUS_NOT_HERE_YET();
        SystemInSession newSysInSess = new SystemInSession(tableSession, newSystem,
                TestingSession.getSelectedTestingSession(), null, systemInSessionStatus);
        // The copied system status need to be set to IN_PROGRESS
        newSysInSess.setRegistrationStatus(SystemInSessionRegistrationStatus.IN_PROGRESS);
        // The copied system is set to true
        newSysInSess.setIsCopy(true);
        // The copied system shall not be accepted to the connectathon yet.
        newSysInSess.setAcceptedToSession(false);
        entityManager.merge(newSysInSess);
        entityManager.flush();
    }

    @Override
    public String goToSystemList() {
        ((TestingSessionManagerLocal) Component.getInstance("testingSessionManager")).setNoTestingSessionChoosen(false);
        return "/systems/system/listSystemsInSession.xhtml";
    }

}
