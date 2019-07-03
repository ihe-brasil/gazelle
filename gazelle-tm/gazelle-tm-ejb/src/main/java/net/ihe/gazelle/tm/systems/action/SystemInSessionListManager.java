package net.ihe.gazelle.tm.systems.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.list.GazelleListDataModel;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.log.ExceptionLogging;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.menu.Authorizations;
import net.ihe.gazelle.objects.model.ObjectInstance;
import net.ihe.gazelle.objects.model.ObjectInstanceFile;
import net.ihe.gazelle.tm.application.action.ApplicationManager;
import net.ihe.gazelle.tm.gazelletest.action.SystemInSessionOverview;
import net.ihe.gazelle.tm.gazelletest.model.instance.SystemInSessionUser;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestStepsInstance;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.*;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.InstitutionQuery;
import net.ihe.gazelle.users.model.Role;
import net.ihe.gazelle.util.Pair;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.security.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.jboss.seam.ScopeType.PAGE;

@Name("systemInSessionListManager")
@Scope(PAGE)
@GenerateInterface("SystemInSessionListManagerLocal")
public class SystemInSessionListManager extends AbstractSystemInSessionEditor implements Serializable, SystemInSessionListManagerLocal {

    private static final Logger LOG = LoggerFactory.getLogger(SystemInSessionListManager.class);

    @In
    private EntityManager entityManager;

    private Institution selectedInstitution;
    private SystemInSessionRegistrationStatus selectedRegistrationStatus;
    private List<SystemInSession> systemsInSession;
    private SystemInSession selectedSystemInSession;
    private SystemInSessionModifier sisModifier;
    private System systemForInstitutions;
    private Filter<Institution> filterInstitution;
    private boolean canDeleteSIS = false;
    private String messageProblemOnDelete;

    public Institution getSelectedInstitution() {
        return selectedInstitution;
    }

    public void setSelectedInstitution(Institution selectedInstitution) {
        this.selectedInstitution = selectedInstitution;
    }

    public SystemInSessionRegistrationStatus getSelectedRegistrationStatus() {
        return selectedRegistrationStatus;
    }

    public void setSelectedRegistrationStatus(SystemInSessionRegistrationStatus selectedRegistrationStatus) {
        this.selectedRegistrationStatus = selectedRegistrationStatus;
    }

    public SystemInSession getSelectedSystemInSession() {
        return selectedSystemInSession;
    }

    public void setSelectedSystemInSession(SystemInSession selectedSystemInSession) {
        this.selectedSystemInSession = selectedSystemInSession;
    }

    @Override
    public System getSystemForInstitutions() {
        LOG.debug("getSystemForInstitutions");
        return systemForInstitutions;
    }

    @Override
    public void setSystemForInstitutions(System systemForInstitutions) {
        LOG.debug("setSystemForInstitutions");
        this.systemForInstitutions = systemForInstitutions;
        filterInstitution = null;
    }

    @Override
    public Filter<Institution> getFilterInstitution() {
        LOG.debug("getFilterInstitution");
        if (filterInstitution == null) {
            InstitutionQuery query = new InstitutionQuery();
            HQLCriterionsForFilter<Institution> hqlCriterionsForFilter = query.getHQLCriterionsForFilter();
            hqlCriterionsForFilter.addPath("institution", query);
            filterInstitution = new Filter<Institution>(hqlCriterionsForFilter) {
                private static final long serialVersionUID = -8300279869803615591L;

                @Override
                public boolean isCountEnabled() {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("isCountEnabled");
                    }
                    return false;
                }
            };
        }
        return filterInstitution;
    }

    @Override
    public List<SystemInSession> getSystemsInSession() {
        LOG.debug("getSystemsInSession");
        List<SystemInSession> ssIs = new ArrayList<SystemInSession>();

        if (selectedInstitution != null) {
            for (SystemInSession sis : systemsInSession) {
                String institutionKeyword = displayInstitutionKeywordForSystem(sis.getSystem());
                if (institutionKeyword.equals(selectedInstitution.getKeyword())) {
                    if (selectedRegistrationStatus != null) {
                        if (sis.getRegistrationStatus().equals(selectedRegistrationStatus)) {
                            ssIs.add(sis);
                        }
                    } else {
                        ssIs.add(sis);
                    }
                }
            }
            return ssIs;
        }
        if (selectedRegistrationStatus != null) {
            for (SystemInSession sis : systemsInSession) {
                if (sis.getRegistrationStatus().equals(selectedRegistrationStatus)) {
                    ssIs.add(sis);
                }
            }
            return ssIs;
        }
        return systemsInSession;
    }

    @Override
    public void setSystemsInSession(List<SystemInSession> systemsInSession) {
        LOG.debug("setSystemsInSession");
        this.systemsInSession = systemsInSession;
    }

    @Override
    public boolean isCanDeleteSIS() {
        LOG.debug("isCanDeleteSIS");
        return canDeleteSIS;
    }

    @Override
    public void setCanDeleteSIS(boolean canDeleteSIS) {
        LOG.debug("setCanDeleteSIS");
        this.canDeleteSIS = canDeleteSIS;
    }

    @Override
    public String getMessageProblemOnDelete() {
        LOG.debug("getMessageProblemOnDelete");
        return messageProblemOnDelete;
    }

    @Override
    public void setMessageProblemOnDelete(String messageProblemOnDelete) {
        LOG.debug("setMessageProblemOnDelete");
        this.messageProblemOnDelete = messageProblemOnDelete;
    }

    @Override
    public String getSystemInSessionKeywordBase() {
        return null;
    }

    @Override
    @Restrict("#{s:hasPermission('SystemInSessionListManager', 'getSystemsInSessionListDependingInstitution', null)}")
    public void getSystemsInSessionListDependingInstitution() {
        LOG.debug("getSystemsInSessionListDependingInstitution");
        EntityManager em = EntityManagerService.provideEntityManager();
        if (Identity.instance().hasRole(net.ihe.gazelle.users.model.Role.ADMINISTRATOR_ROLE_STRING)
                || Identity.instance().hasRole(net.ihe.gazelle.users.model.Role.VENDOR_ADMIN_ROLE_STRING)
                || Identity.instance().hasRole(net.ihe.gazelle.users.model.Role.TESTING_SESSION_ADMIN_STRING)
                || Identity.instance().hasRole(net.ihe.gazelle.users.model.Role.VENDOR_ROLE_STRING)) {

            TestingSession testingSession = TestingSession.getSelectedTestingSession();
            if (testingSession == null) {
                LOG.error(NO_ACTIVE_SESSION);
                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.systems.error.noActivatedTestingSession");
            }

            Institution institution;
            if (Identity.instance().hasRole(net.ihe.gazelle.users.model.Role.ADMINISTRATOR_ROLE_STRING)
                    || Identity.instance().hasRole(net.ihe.gazelle.users.model.Role.TESTING_SESSION_ADMIN_STRING)) {
                institution = null;
            } else {
                institution = Institution.getLoggedInInstitution();
            }
            systemsInSession = SystemInSession.getSystemsInSessionForCompanyForSession(em, institution, testingSession);
        }
    }

    public List<SystemInSessionRegistrationStatus> getListOfRegistrationStatus(TestingSession ts) {
        LOG.debug("getListOfRegistrationStatus");
        SystemInSessionQuery q = new SystemInSessionQuery();
        q.testingSession().id().eq(ts.getId());
        return q.registrationStatus().getListDistinct();
    }

    public GazelleListDataModel<SystemInSession> getAllSystemsInSession() {
        List<SystemInSession> res = getSystemsInSession();
        GazelleListDataModel<SystemInSession> dm = new GazelleListDataModel<SystemInSession>(res);
        return dm;
    }

    @Override
    public String displayInstitutionKeywordForSystem(System inSystem) {
        LOG.debug("displayInstitutionKeywordForSystem");
        List<Institution> listInst = System.getInstitutionsForASystem(inSystem);
        StringBuffer s = new StringBuffer();
        int size = listInst.size();
        for (int i = 0; i < size; i++) {
            s.append(listInst.get(i).getKeyword());
            if ((size > 1) && (i == (size - 1))) {
                s.append("/");
            }
        }
        return s.toString();
    }

    public List<SystemInSessionRegistrationStatus> getPossibleRegistrationStatus(SystemInSession sys) {
        LOG.debug("getPossibleRegistrationStatus");
        return sys.getPossibleRegistrationStatus();
    }


    public void updateSystemInSession(SystemInSession currentSystemInSession) {
        LOG.debug("updateSystemInSession");
        entityManager.merge(currentSystemInSession);
        entityManager.flush();
    }

    /**
     * This method indicates for a system if the logged in user may delete this system. It returns a flag, if true, then 'delete' button is
     * displayed. Delete button is displayed for admins and
     * system's owner.
     *
     * @param currentSystem : system (containing Integration Statement informations) used to determinate status
     * @return Boolean : true if we display the delete button
     */
    @Override
    public boolean isDeleteSystemButtonRendered(System currentSystem) {
        LOG.debug("isDeleteSystemButtonRendered");
        Boolean returnedFlag = false;
        if (Identity.instance().hasRole(Role.ADMINISTRATOR_ROLE_STRING) || Authorizations.TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION.isGranted()
                || Identity.instance().hasRole(Role.VENDOR_ADMIN_ROLE_STRING) ||
                Identity.instance().getCredentials().getUsername().equals(currentSystem.getOwnerUser().getUsername())) {
            returnedFlag = true;
        }
        return returnedFlag;
    }

    @Override
    @Restrict("#{s:hasPermission('SystemInSessionListManager', 'deleteSystemInSession', null)}")
    public void deleteSystemInSession() {
        LOG.debug("deleteSystemInSession");
        try {
            List<SystemInSession> listSiS = SystemInSession.findSystemInSessionsWithSystem(selectedSystemInSession
                    .getSystem());
            if (listSiS.size() >= 1) {
                SystemInSession.deleteSelectedSystemInSessionWithDependencies(selectedSystemInSession);
                FacesMessages.instance().add(StatusMessage.Severity.INFO,
                        "The selected system (" + selectedSystemInSession.getSystem().getKeyword()
                                + ") was deleted from the testing session :"
                                + selectedSystemInSession.getTestingSession().getDescription());
            }

            // It's just for PR
            if (ApplicationManager.instance().isProductRegistry()) {
                DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
                Date date = new Date();
                System sys = entityManager.find(System.class, selectedSystemInSession.getSystem().getId());
                String version = sys.getVersion() + "_Deleted_" + dateFormat.format(date);
                sys.setVersion(version);
                entityManager.merge(sys);
                entityManager.flush();
            }

            selectedSystemInSession = null;

            // Calculate new amounts for invoice - Fees and VAT changes when a system is deleted
            sisModifier = new SystemInSessionModifier(selectedInstitution, selectedSystemInSession);
            sisModifier.calculateFinancialSummaryDTO();
        } catch (Exception ex) {
            net.ihe.gazelle.common.log.ExceptionLogging.logException(ex, LOG);
            StatusMessages.instance().addFromResourceBundle(
                    "gazelle.systems.error.SystemCanNotBeDeleted");
        }
    }

    @Override
    @Restrict("#{s:hasPermission('SystemInSessionListManager', 'deleteSystemInAllSession', null)}")
    public void deleteSystemInAllSession() {
        LOG.debug("deleteSystemInAllSession");
        try {
            SystemInSession sis = selectedSystemInSession;
            deleteSystemInSession();
            FacesMessages.instance().add(StatusMessage.Severity.INFO, sis.getSystem().getKeyword() + "-" + sis.getSystem().getName()
                    + " has been removed from all testing session");
            SystemInSessionOverview sso = new SystemInSessionOverview();
            sso.refreshFilter();
            selectedSystemInSession = null;
        } catch (Exception e) {
            ExceptionLogging.logException(e, LOG);
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.systems.error.SystemCanNotBeDeleted");
        }
    }

    @Override
    @Restrict("#{s:hasPermission('SystemInSessionListManager', 'createSystemSessionForSystem', null)}")
    public void createSystemSessionForSystem(System s) {
        LOG.debug("createSystemSessionForSystem");
        TableSession tableSession = TableSession.getTableSessionByKeyword(TableSession.getDEFAULT_TABLE_SESSION_STRING());
        SystemInSessionStatus systemInSessionStatus = SystemInSessionStatus.getSTATUS_NOT_HERE_YET();
        SystemInSession newSysInSess = new SystemInSession(tableSession, s, TestingSession.getSelectedTestingSession(), null, systemInSessionStatus);
        // The system status need to be set to IN_PROGRESS
        newSysInSess.setRegistrationStatus(SystemInSessionRegistrationStatus.IN_PROGRESS);
        // The copied system is set to false
        newSysInSess.setIsCopy(false);
        // The system shall not be accepted to the connectathon yet.
        newSysInSess.setAcceptedToSession(false);
        newSysInSess = entityManager.merge(newSysInSess);
        entityManager.flush();

        FacesMessages.instance().add(StatusMessage.Severity.INFO, "New system in session : " + newSysInSess.getLabel() + " has been added in "
                + TestingSession.getSelectedTestingSession().getDescription());
    }

    @Override
    @Restrict("#{s:hasPermission('SystemInSessionListManager', 'updateSelectedSystemInSession', null)}")
    public void updateSelectedSystemInSession(SystemInSession currentSIS) {
        LOG.debug("updateSelectedSystemInSession");
        this.selectedSystemInSession = currentSIS;
        Pair<Boolean, String> pbs = this.canDeleteSystemInSession(selectedSystemInSession);
        this.canDeleteSIS = pbs.getObject1().booleanValue();
        this.messageProblemOnDelete = pbs.getObject2();
    }

    @Override
    public Pair<Boolean, String> canDeleteSystemInSession(SystemInSession inSystemInSession) {
        LOG.debug("canDeleteSystemInSession");
        boolean res = true;
        if (inSystemInSession != null) {
            if (inSystemInSession.getId() != null) {
                int lsisucount = SystemInSessionUser.getCountSystemInSessionUserFiltered(inSystemInSession, null);
                if (lsisucount > 0) {
                    return new Pair<Boolean, String>(false, "The system is used on some tests.");
                }
                int noi = ObjectInstance.getNumberObjectInstanceFiltered(null, inSystemInSession, null, null);
                if (noi > 0) {
                    return new Pair<Boolean, String>(false,
                            "The system has upload some samples on the testing session.");
                }
                int noif = ObjectInstanceFile.getCountObjectInstanceFileFiltered(null, null, inSystemInSession, null,
                        null);
                if (noif > 0) {
                    return new Pair<Boolean, String>(false, "The system has upload some sampleson the testing session.");
                }
                int ntsi = TestStepsInstance.getCountTestStepsInstanceBySystemInSession(inSystemInSession);
                if (ntsi > 0) {
                    return new Pair<Boolean, String>(false, "The system is used on some tests.");
                }
            }
        }
        return new Pair<Boolean, String>(res, null);
    }

    @Override
    @Restrict("#{s:hasPermission('SystemInSessionListManager', 'addInstitutionToSystem', null)}")
    public void addInstitutionToSystem() {
        LOG.debug("addInstitutionToSystem");
        if (systemForInstitutions != null) {
            Institution institution = (Institution) getFilterInstitution().getRealFilterValue("institution");
            if (institution != null) {
                InstitutionSystem institutionSystem = new InstitutionSystem();
                institutionSystem.setInstitution(institution);
                institutionSystem.setSystem(systemForInstitutions);
                entityManager.persist(institutionSystem);
                entityManager.flush();
            }
        }
    }
}
