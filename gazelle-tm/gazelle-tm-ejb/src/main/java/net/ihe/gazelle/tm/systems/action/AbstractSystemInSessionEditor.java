package net.ihe.gazelle.tm.systems.action;

import net.ihe.gazelle.common.model.AuditedObject;
import net.ihe.gazelle.common.util.File;
import net.ihe.gazelle.dao.SystemInSessionDAO;
import net.ihe.gazelle.hql.HQLReloader;
import net.ihe.gazelle.hql.HQLRestriction;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.menu.Authorizations;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOption;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.*;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.Role;
import net.ihe.gazelle.users.model.User;
import org.jboss.seam.security.Identity;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gthomazon on 12/07/17.
 */
public abstract class AbstractSystemInSessionEditor extends SystemInSessionNavigator {

    protected static final String NO_ACTIVE_SESSION = "No Active Session !";

    protected Institution institutionForCreation;
    protected String defaultTab;
    protected SystemInSession systemInSession = new SystemInSession();

    // List of all HL7 Conformance Statements (Documents) for a selected system
    protected ArrayList<File> hl7DocumentsFilesForSelectedSystem = new ArrayList<File>();
    // List of all DICOM Conformance Statements (Documents) for a selected system
    protected ArrayList<File> dicomDocumentsFilesForSelectedSystem = new ArrayList<File>();

    public static List<SystemType> getPossibleSystemTypes() {
        SystemTypeQuery query = new SystemTypeQuery();
        HQLRestriction visibleTrue = query.visible().eqRestriction(true);
        HQLRestriction visibleNull = query.visible().isNullRestriction();
        query.addRestriction(HQLRestrictions.or(visibleTrue, visibleNull));
        query.systemTypeKeyword().order(true);
        return query.getList();
    }

    public void reloadSystemInSession() {
        systemInSession = HQLReloader.reloadDetached(systemInSession);
    }

    public SystemInSession getSystemInSession() {
        return systemInSession;
    }

    public void setSystemInSession(SystemInSession systemInSession) {
        this.systemInSession = systemInSession;
    }

    public ArrayList<File> getHl7DocumentsFilesForSelectedSystem() {
        return hl7DocumentsFilesForSelectedSystem;
    }

    public ArrayList<File> getDicomDocumentsFilesForSelectedSystem() {
        return dicomDocumentsFilesForSelectedSystem;
    }

    public abstract String getSystemInSessionKeywordBase();

    public String getDefaultTab() {
        if (defaultTab == null || defaultTab.isEmpty()) {
            setDefaultTab("editSystemSummaryTab");
        }
        return defaultTab;
    }

    public void setDefaultTab(String defaultTab) {
        this.defaultTab = defaultTab;
    }

    public String goToSystemList() {
        return "/systems/system/listSystemsInSession.xhtml";
    }

    public boolean userIsCreator(SystemInSession currentSystemInSession) {
        String ownerUserName = currentSystemInSession.getSystem().getOwnerUser().getUsername();
        String userName = Identity.instance().getCredentials().getUsername();
        if (ownerUserName.equals(userName)) {
            return true;
        }
        return false;
    }

    /**
     * Reinitialize the list of Dicom Documents listed in the Dicom Conformance statement modal panel (method used
     * when user clicks on 'Cancel' or for initialization)
     */
    public void initializeDicomDocuments() {
        this.dicomDocumentsFilesForSelectedSystem = SystemInSessionDAO.instance()
                .retrieveDicomConformanceStatementForSelectedSystemToViewOrEdit(systemInSession);
    }

    /**
     * Reinitialize the list of HL7 Documents listed in the HL7 Conformance statement modal panel (method used when
     * user clicks on 'Cancel' or for initialization)
     */
    public void initializeHL7Documents() {
        this.hl7DocumentsFilesForSelectedSystem = SystemInSessionDAO.instance()
                .retrieveHL7ConformanceStatementForSelectedSystemToViewOrEdit(systemInSession);
    }

    /**
     * Show a Hl7 conformance statements document (clicking on a link) It calls a static method which performing job
     * for all kind of docs (HL7, DICOM...) This method is in
     * net.ihe.gazelle.common.util.DocumentFileUpload
     *
     * @param file name : File name of the conformance statements (without path)
     */
    public void showHl7ConformanceStatementFile(String file) {
        showConformanceStatementFile(file, SystemInSessionDAO.instance().getHl7ConformanceStatementsPath());
    }

    /**
     * Show a DICOM conformance statements document (clicking on a link) It calls a static method which performing
     * job for all kind of docs (HL7, DICOM...) This method is in
     * net.ihe.gazelle.common.util.DocumentFileUpload
     *
     * @param file name : File name of the conformance statements (without path)
     */
    public void showDicomConformanceStatementFile(String file) {
        showConformanceStatementFile(file, SystemInSessionDAO.instance().getDicomConformanceStatementsPath());
    }

    private void showConformanceStatementFile(String file, String conformanceStatementsPath) {
        String fullPath = conformanceStatementsPath + java.io.File.separatorChar + systemInSession.getSystem().getId() + java.io.File.separatorChar
                + file;
        net.ihe.gazelle.common.util.DocumentFileUpload.showFile(fullPath);
    }

    public boolean canEditSystemInSession() {
        User usr = User.loggedInUser();
        if (Role.isUserAdmin(usr) || Authorizations.TESTING_SESSION_ADMIN_OF_CURRENT_TESTING_SESSION.isGranted()) {
            return true;
        }

        if (systemInSession != null) {
            if (Identity.instance() != null) {
                if (Identity.instance().isLoggedIn()) {
                    List<SystemInSession> lsis = SystemInSession.getSystemInSessionRelatedToUser(usr,
                            TestingSession.getSelectedTestingSession());
                    if ((lsis != null) && (lsis.contains(systemInSession))) {
                        if (userIsCreator(systemInSession) || Role.isUserVendorAdmin(usr)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Copy apio from <b>systemFrom</b> to <b>newSystem</b>
     *
     * @param systemFrom system containing AIPO
     * @param newSystem  system to copy AIPO
     */
    protected void linkAIPO(System systemFrom, System newSystem) {
        List<SystemActorProfiles> listSAP = SystemActorProfiles.getListOfActorIntegrationProfileOptionsWithTestingType(
                systemFrom, TestingSession.getSelectedTestingSession());

        EntityManager entityManager = EntityManagerService.provideEntityManager();

        for (int elementAIPO = 0; elementAIPO < listSAP.size(); elementAIPO++) {
            SystemActorProfiles newSAP = new SystemActorProfiles();
            newSAP.setSystem(newSystem);
            newSAP.setActorIntegrationProfileOption(entityManager.find(ActorIntegrationProfileOption.class, listSAP
                    .get(elementAIPO).getActorIntegrationProfileOption().getId()));
            newSAP.setTestingType(AuditedObject.getObjectByKeyword(TestingType.class, "T"));
            entityManager.merge(newSAP);
        }
        entityManager.flush();
    }
}
