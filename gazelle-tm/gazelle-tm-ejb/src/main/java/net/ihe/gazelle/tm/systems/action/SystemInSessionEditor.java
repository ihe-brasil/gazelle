package net.ihe.gazelle.tm.systems.action;

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.model.PathLinkingADocument;
import net.ihe.gazelle.dao.SystemInSessionDAO;
import net.ihe.gazelle.tm.application.action.ApplicationManager;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.*;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.User;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.*;

import static org.jboss.seam.ScopeType.PAGE;

/**
 * Created by gthomazon on 12/07/17.
 */
@Name("systemInSessionEditor")
@Scope(PAGE)
@Synchronized(timeout = 10000)
@GenerateInterface("SystemInSessionEditorLocal")
public class SystemInSessionEditor extends AbstractSystemInSessionEditor implements Serializable,
        SystemInSessionEditorLocal {

    private static final Logger LOG = LoggerFactory.getLogger(SystemInSessionEditor.class);
    // Number Max of downloads allowed
    private static final int MAX_UPLOADS = 5;

    @In
    private EntityManager entityManager;
    private SystemInSessionModifier sisModifier;
    private SystemSubtypesPerSystemType systemSubtypesPerSystemType;
    @Pattern(regexp = "(^$)|(?i)(^http://.+$|^https://.+$|^ftp://.+$)", message = "{gazelle.validator.http}")
    @Size(max = 255)
    private String newUrlForHL7;
    @Pattern(regexp = "(^$)|(?i)(^http://.+$|^https://.+$|^ftp://.+$)", message = "{gazelle.validator.http}")
    @Size(max = 255)
    private String newUrlForDicom;
    /**
     * This corresponds to a flag indicating if inputText box HL7 URL is enabled (true : input is enabled, false :
     * upload documents box is enabled). This variable is used between business and
     * presentation layer to render labels.
     */
    private Boolean hl7URLRadioButtonSelected = true;
    /**
     * This corresponds to a flag indicating if inputText box DICOM URL is enabled (true : input is enabled, false :
     * upload documents box is enabled). This variable is used between business and
     * presentation layer to render labels.
     */
    private Boolean dicomURLRadioButtonSelected = true;

    public SystemInSessionEditor() {
    }

    public SystemInSessionEditor(SystemInSessionModifier sisModifier) {
        this.sisModifier = sisModifier;
    }

    public static int getMaxUploads() {
        return MAX_UPLOADS;
    }

    public String getNewUrlForDicom() {
        LOG.debug("getNewUrlForDicom");
        return newUrlForDicom;
    }

    public void setNewUrlForDicom(String newUrlForDicom) {
        LOG.debug("setNewUrlForDicom");
        this.newUrlForDicom = newUrlForDicom;
    }

    public String getNewUrlForHL7() {
        LOG.debug("getNewUrlForHL7");
        return newUrlForHL7;
    }

    public void setNewUrlForHL7(String newUrlForHL7) {
        LOG.debug("setNewUrlForHL7");
        this.newUrlForHL7 = newUrlForHL7;
    }

    public System getSystem() {
        return systemInSession.getSystem();
    }

    public void setSystem(System system) {
        systemInSession.setSystem(system);
    }

    public SystemInSessionRegistrationStatus getSystemRegistrationStatus() {
        return systemInSession.getRegistrationStatus();
    }

    public void setSystemRegistrationStatus(SystemInSessionRegistrationStatus systemInSessionRegistrationStatus) {
        systemInSession.setRegistrationStatus(systemInSessionRegistrationStatus);
    }

    public String getSystemTestingSessionDescription() {
        return systemInSession.getTestingSession().getDescription();
    }

    public void setSystemTestingSessionDescription(String description) {
        systemInSession.getTestingSession().setDescription(description);
    }

    public boolean isSystemRegistrationOpened() {
        return systemInSession.isRegistrationOpened();
    }

    public String getSystemName() {
        return getSystem().getName();
    }

    public void setSystemName(String systemName) {
        getSystem().setName(systemName);
    }

    public String getSystemKeyword() {
        return getSystem().getKeyword();
    }

    public void setSystemKeyword(String systemKeyword) {
        getSystem().setKeyword(systemKeyword);
    }

    public SystemType getSystemType() {
        return getSystem().getSystemType();
    }

    public void setSystemType(SystemType systemType) {
        getSystem().setSystemType(systemType);
    }

    public String getSystemTypeKeyword() {
        return getSystem().getSystemType().getSystemTypeKeyword();
    }

    public void setSystemTypeKeyword(String systemTypeKeyword) {
        getSystem().getSystemType().setSystemTypeKeyword(systemTypeKeyword);
    }

    public String getSystemVersion() {
        return getSystem().getVersion();
    }

    public void setSystemVersion(String systemVersion) {
        getSystem().setVersion(systemVersion);
    }

    public String getSystemKeywordSuffix() {
        return getSystem().getKeywordSuffix();
    }

    public void setSystemKeywordSuffix(String systemKeywordSuffix) {
        getSystem().setKeywordSuffix(systemKeywordSuffix);
    }

    public User getSystemOwnerUser() {
        return getSystem().getOwnerUser();
    }

    public void setSystemOwnerUser(User systemOwner) {
        getSystem().setOwnerUser(systemOwner);
    }

    public String getSystemHL7ConformanceStatementUrl() {
        return getSystem().getHL7ConformanceStatementUrl();
    }

    public void setSystemHL7ConformanceStatementUrl(String hL7ConformanceStatementUrl) {
        getSystem().setHL7ConformanceStatementUrl(hL7ConformanceStatementUrl);
    }

    public String getSystemDicomConformanceStatementUrl() {
        return getSystem().getDicomConformanceStatementUrl();
    }

    public void setSystemDicomConformanceStatementUrl(String dicomConformanceStatementUrl) {
        getSystem().setDicomConformanceStatementUrl(dicomConformanceStatementUrl);
    }

    public List<PathLinkingADocument> getSystemPathsToHL7Documents() {
        return getSystem().getPathsToHL7Documents();
    }

    public void setSystemPathsToHL7Documents(List<PathLinkingADocument> pathsToHL7Documents) {
        getSystem().setPathsToHL7Documents(pathsToHL7Documents);
    }

    public List<PathLinkingADocument> getSystemPathsToDicomDocuments() {
        return getSystem().getPathsToDicomDocuments();
    }

    public void setSystemPathsToDicomDocuments(List<PathLinkingADocument> pathsToDicomDocuments) {
        getSystem().setPathsToDicomDocuments(pathsToDicomDocuments);
    }

    public String getSystemIntegrationStatementUrl() {
        return getSystem().getIntegrationStatementUrl();
    }

    public void setSystemIntegrationStatementUrl(String dicomConformanceStatementUrl) {
        getSystem().setIntegrationStatementUrl(dicomConformanceStatementUrl);
    }

    public Date getSystemIntegrationStatementDate() {
        return getSystem().getIntegrationStatementDate();
    }

    public void setSystemIntegrationStatementDate(Date integrationStatementDate) {
        getSystem().setIntegrationStatementDate(integrationStatementDate);
    }

    public List<SystemSubtypesPerSystemType> getSystemSystemSubtypesPerSystemType() {
        return getSystem().getSystemSubtypesPerSystemType();
    }

    public void setSystemSystemSubtypesPerSystemType(List<SystemSubtypesPerSystemType> systemSubtypesPerSystemType) {
        getSystem().setSystemSubtypesPerSystemType(systemSubtypesPerSystemType);
    }

    @Override
    public String getSystemInSessionKeywordBase() {
        return SystemInSessionModifier.getSystemInSessionKeywordBase(getSystemType(), institutionForCreation);
    }

    @Override
    @Create
    public void init() {
        final String systemInSessionId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        String selectedTab = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selectedTab");
        try {
            institutionForCreation = Institution.getLoggedInInstitution();
            Integer id = Integer.parseInt(systemInSessionId);
            systemInSession = entityManager.find(SystemInSession.class, id);
            dicomConformanceTypeIsURL();
            hl7ConformanceTypeIsURL();
            initializeDicomDocuments();
            initializeHL7Documents();

            if (selectedTab == null || selectedTab.equals("showSystemSummaryTab")) {
                selectedTab = "editSystemSummaryTab";
            }
            setDefaultTab(selectedTab);
            institutionForCreation = verifyValidInstitution(institutionForCreation, systemInSession);
        } catch (NumberFormatException e) {
            LOG.error("failed to find system in session with id = " + systemInSessionId);
        }
    }

    //GZL-4638 : if user is admin
    private Institution verifyValidInstitution(Institution institution, SystemInSession systemInSession) {
        if (systemInSession != null) {
            List<Institution> institutionList = System.getInstitutionsForASystem(systemInSession.getSystem());
            if (institutionList != null && !institutionList.contains(institution)) {
                return institutionList.get(0);
            }
        }
        return institution;
    }


    @Override
    public String canAccessThisPage() {
        if (!canEditSystemInSession()) {
            return "KO";
        }
        return "OK";
    }

    @Override
    public List<SystemInSessionRegistrationStatus> getRegistrationStatus() {
        LOG.debug("getRegistrationStatus");
        return systemInSession.getPossibleRegistrationStatus();
    }

    @Override
    public List<SystemSubtypesPerSystemType> getSortedTypes() {
        LOG.debug("getSortedTypes");
        List<SystemSubtypesPerSystemType> result = new ArrayList<SystemSubtypesPerSystemType>();
        if (getSystemSystemSubtypesPerSystemType() != null) {
            result.addAll(getSystemSystemSubtypesPerSystemType());
        }
        Collections.sort(result);
        return result;
    }

    @Override
    public void removeSubType(SystemSubtypesPerSystemType subType) {
        LOG.debug("removeSubType");
        List<SystemSubtypesPerSystemType> subTypes = getSystemSystemSubtypesPerSystemType();
        SystemSubtypesPerSystemType toRemove = null;
        for (SystemSubtypesPerSystemType systemSubtypesPerSystemType : subTypes) {
            if (systemSubtypesPerSystemType.equals(subType)) {
                toRemove = systemSubtypesPerSystemType;
            }
        }
        if (toRemove != null) {
            subTypes.remove(toRemove);
            entityManager.merge(getSystem());
        }
    }

    @Override
    public List<SystemSubtypesPerSystemType> getPossibleSystemSubtypesPerSystemType() {
        LOG.debug("getPossibleSystemSubtypesPerSystemType");
        List<SystemSubtypesPerSystemType> subTypes = getSystemSystemSubtypesPerSystemType();
        Set<Integer> ids = new HashSet<Integer>();
        if (subTypes != null) {
            for (SystemSubtypesPerSystemType systemSubtypesPerSystemType : subTypes) {
                ids.add(systemSubtypesPerSystemType.getId());
            }
        }

        SystemSubtypesPerSystemTypeQuery query = new SystemSubtypesPerSystemTypeQuery();
        query.id().nin(ids);
        List<SystemSubtypesPerSystemType> result = query.getList();
        Collections.sort(result);
        return result;
    }

    @Override
    public SystemSubtypesPerSystemType getSystemSubtypesPerSystemType() {
        LOG.debug("getSystemSubtypesPerSystemType");
        return systemSubtypesPerSystemType;
    }

    @Override
    public void setSystemSubtypesPerSystemType(SystemSubtypesPerSystemType systemSubtypesPerSystemType) {
        LOG.debug("setSystemSubtypesPerSystemType");
        this.systemSubtypesPerSystemType = systemSubtypesPerSystemType;
    }

    @Override
    public void addSystemSubType() {
        LOG.debug("addSystemSubType");
        boolean doit = true;
        if (systemSubtypesPerSystemType != null) {
            List<SystemSubtypesPerSystemType> subTypes = getSystemSystemSubtypesPerSystemType();
            if (subTypes == null) {
                subTypes = new ArrayList<>();
                setSystemSystemSubtypesPerSystemType(subTypes);
            }
            for (SystemSubtypesPerSystemType subTypePerSystemType : subTypes) {
                if (subTypePerSystemType.equals(systemSubtypesPerSystemType)) {
                    doit = false;
                }
            }
            if (doit) {
                subTypes.add(systemSubtypesPerSystemType);
                entityManager.merge(getSystem());
            }
        }
    }

    /**
     * Reinitialize the URL entered in the modal panel used to register the HL7 Conformance statement (method used
     * when user clicks on 'Cancel' or for initialization)
     */
    @Override
    public void initializeNewHL7Url() {
        LOG.debug("initializeNewHL7Url");
        if (getSystemHL7ConformanceStatementUrl() != null) {
            newUrlForHL7 = getSystemHL7ConformanceStatementUrl();
        } else {
            newUrlForHL7 = "";
        }
    }

    /**
     * Reinitialize the URL entered in the modal panel used to register the Dicom Conformance statement (method used
     * when user clicks on 'Cancel' or for initialization)
     */
    @Override
    public void initializeNewDicomUrl() {
        LOG.debug("initializeNewDicomUrl");
        if (getSystemDicomConformanceStatementUrl() != null) {
            newUrlForDicom = getSystemDicomConformanceStatementUrl();
        } else {
            newUrlForDicom = "";
        }
    }

    /**
     * This method saves HL7 URL (Conformance statements).
     */
    @Override
    @Restrict("#{s:hasPermission('SystemInSessionEditor', 'saveHL7URLInModalPanel', null)}")
    public Boolean saveHL7URLInModalPanel() {
        if (validateURL(newUrlForHL7) && getSystem() != null) {
            setSystemHL7ConformanceStatementUrl(newUrlForHL7);
        }
        return true;
    }

    /**
     * This method saves DICOM URL (Conformance statements).
     */
    @Override
    @Restrict("#{s:hasPermission('SystemInSessionEditor', 'saveDicomURLInModalPanel', null)}")
    public Boolean saveDicomURLInModalPanel() {
        if (validateURL(newUrlForDicom) && getSystem() != null) {
            setSystemDicomConformanceStatementUrl(newUrlForDicom);
        }
        return true;
    }

    public boolean validateURL(String url) {
        boolean res = false;
        String urlPattern = "(^$)|(?i)(^http://.+$|^https://.+$|^ftp://.+$)";
        if (url != null && !url.isEmpty() && url.matches(urlPattern)) {
            res = true;
        } else {
            LOG.error("URL provided by user is invalid : " + url);
            StatusMessages.instance().addToControlFromResourceBundleOrDefault("hl7ConformanceInput", StatusMessage
                            .Severity.ERROR,
                    "You must enter a valid url !", "You must enter a valid url !");
            StatusMessages.instance().addToControlFromResourceBundleOrDefault("dicomConformanceInput", StatusMessage
                            .Severity.ERROR,
                    "You must enter a valid url !", "You must enter a valid url !");
        }
        return res;
    }

    /**
     * Update a system to the database This operation is allowed for some granted users (check the security.drl)
     */
    @Override
    @Restrict("#{s:hasPermission('SystemInSessionEditor', 'editSystem', null)}")
    public void editSystem() {
        LOG.debug("editSystem");
        if (TestingSession.getSelectedTestingSession() == null) {
            LOG.error(NO_ACTIVE_SESSION);
            StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.systems.error" +
                    ".noActivatedTestingSession");
        } else {
            sisModifier = new SystemInSessionModifier(institutionForCreation, systemInSession);
            try {
                editSystemForTM(sisModifier);
                if (ApplicationManager.instance().isProductRegistry()) {
                    editSystemForPR(sisModifier);
                }
                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.INFO, "gazelle.systems.system" +
                        ".faces.SystemSuccessfullyUpdated", getSystemName());
            } catch (SystemActionException e) {
                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, e.getMessage());
            }
        }
    }

    /**
     * Update a system to the database This operation is allowed for some granted users (check the security.drl)
     */
    private void editSystemForTM(SystemInSessionModifier sisModifier) throws SystemActionException {
        LOG.debug("editSystemForTM");
        sisModifier.editSystemForTM();
    }

    /**
     * Update a system to the database This operation is allowed for some granted users (check the security.drl)
     */
    @Override
    @Restrict("#{s:hasPermission('SystemInSessionEditor', 'addSystemForPR', null)}")
    public void editSystemForPR(SystemInSessionModifier sisModifier) throws SystemActionException {
        LOG.debug("addSystemForPR");
        sisModifier.addSystemForPR(sisModifier.getSystemInSession().getSystem());
    }

    /**
     * This method enables HL7 URL input (Conformance statements) and disables HL7 Documents upload.
     */
    @Override
    public void hl7URLRadioButtonSelected() {
        hl7URLRadioButtonSelected = true;
    }

    /**
     * This method enables DICOM URL input (Conformance statements) and disables DICOM Documents upload.
     */
    @Override
    public void dicomURLRadioButtonSelected() {
        dicomURLRadioButtonSelected = true;
    }

    /**
     * This method disables HL7 URL input (Conformance statements) and enables HL7 Documents upload.
     */
    @Override
    public void hl7DocumentsUploadRadioButtonSelected() {
        hl7URLRadioButtonSelected = false;
    }

    /**
     * This method enables DICOM URL input (Conformance statements) and enables DICOM Documents upload.
     */
    @Override
    public void dicomDocumentsUploadRadioButtonSelected() {
        dicomURLRadioButtonSelected = false;
    }

    @Override
    public Boolean getHl7URLRadioButtonSelected() {
        return hl7URLRadioButtonSelected;
    }

    @Override
    public void setHl7URLRadioButtonSelected(Boolean hl7URLRadioButtonSelected) {
        this.hl7URLRadioButtonSelected = hl7URLRadioButtonSelected;
    }

    @Override
    public Boolean getDicomURLRadioButtonSelected() {
        return dicomURLRadioButtonSelected;
    }

    @Override
    public void setDicomURLRadioButtonSelected(Boolean dicomURLRadioButtonSelected) {
        this.dicomURLRadioButtonSelected = dicomURLRadioButtonSelected;
    }

    /**
     * This method gets the type of HL7 conformance statements : - returns true if there are no HL7 conformance. -
     * returns true if the HL7 conformance is an entered URL. - returns false if the HL7
     * conformance are uploaded documents.
     *
     * @return Boolean : depending on the type (read above)
     */
    @Override
    public Boolean hl7ConformanceTypeIsURL() {
        LOG.debug("hl7ConformanceTypeIsURL");
        if (getSystemPathsToHL7Documents() == null || getSystemPathsToHL7Documents().isEmpty()) {
            hl7URLRadioButtonSelected = true;
        } else {
            hl7URLRadioButtonSelected = false;
        }
        return hl7URLRadioButtonSelected;
    }

    /**
     * This method gets the type of DICOM conformance statements : - returns true if there are no DICOM conformance.
     * - returns true if the DICOM conformance is an entered URL. - returns false if the
     * DICOM conformance are uploaded documents.
     *
     * @return Boolean : depending on the type (read above)
     */
    @Override
    public Boolean dicomConformanceTypeIsURL() {
        LOG.debug("dicomConformanceTypeIsURL");
        if (getSystemPathsToDicomDocuments() == null || getSystemPathsToDicomDocuments().isEmpty()) {
            dicomURLRadioButtonSelected = true;
        } else {
            dicomURLRadioButtonSelected = false;
        }
        return dicomURLRadioButtonSelected;
    }


    /**
     * Persist document-path (DB) and save (Documents-files on server) : HL7 conformance statements It calls a static
     * method which performing job for all kind of docs (HL7, DICOM...) This method is in
     * net.ihe.gazelle.common.util.DocumentFileUpload
     *
     * @param idForPath : Id of the system (used to determinate the path on server)
     */
    @Override
    @Restrict("#{s:hasPermission('SystemInSessionEditor', 'persistHL7DocumentsFiles', null)}")
    public void persistHL7DocumentsFiles(Integer idForPath) {
        LOG.debug("persistHL7DocumentsFiles");
        sisModifier = new SystemInSessionModifier(institutionForCreation, systemInSession);
        ArrayList<net.ihe.gazelle.common.util.File> hl7DocumentsFilesForSelectedSystem = this.hl7DocumentsFilesForSelectedSystem;
        try {
            if (hl7DocumentsFilesForSelectedSystem != null) {
                if ((idForPath != null) && (idForPath != 0)) {
                    List<PathLinkingADocument> hl7PathsLinkingADocumentForSystem = new Vector<PathLinkingADocument>();
                    if (getSystemPathsToHL7Documents() != null) {
                        hl7PathsLinkingADocumentForSystem = getSystemPathsToHL7Documents();
                    }
                    sisModifier.persistDocumentsFile(idForPath, SystemInSessionDAO.instance()
                                    .getHl7ConformanceStatementsPath(), hl7DocumentsFilesForSelectedSystem,
                            hl7PathsLinkingADocumentForSystem);
                    setSystemPathsToHL7Documents(hl7PathsLinkingADocumentForSystem);

                    entityManager.merge(getSystem());
                    systemInSession = entityManager.merge(systemInSession);
                    FacesMessages.instance().add(StatusMessage.Severity.INFO, "HL7 Conformance Statements added and " + "saved !");
                }
            }
        } catch (SystemActionException e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Unable to save, please contact your admin !");
            LOG.error(e.getMessage());
        }
    }

    /**
     * Persist document-path (DB) and save (Documents-files on server) : DICOM conformance statements It calls a
     * static method which performing job for all kind of docs (HL7, DICOM...) This method is
     * in net.ihe.gazelle.common.util.DocumentFileUpload
     *
     * @param idForPath : Id of the system (used to determinate the path on server)
     */
    @Override
    @Restrict("#{s:hasPermission('SystemInSessionEditor', 'persistDicomDocumentsFiles', null)}")
    public void persistDicomDocumentsFiles(Integer idForPath) {
        LOG.debug("persistDicomDocumentsFiles");
        sisModifier = new SystemInSessionModifier(institutionForCreation, systemInSession);
        ArrayList<net.ihe.gazelle.common.util.File> dicomDocumentsFilesForSelectedSystem = this.dicomDocumentsFilesForSelectedSystem;
        try {
            if (dicomDocumentsFilesForSelectedSystem != null) {
                if ((idForPath != null) && (idForPath != 0)) {
                    List<PathLinkingADocument> dicomPathsLinkingADocumentForSystem = new Vector<PathLinkingADocument>();
                    if (getSystemPathsToDicomDocuments() != null) {
                        dicomPathsLinkingADocumentForSystem = getSystemPathsToDicomDocuments();
                    }
                    sisModifier.persistDocumentsFile(idForPath, SystemInSessionDAO.instance()
                                    .getDicomConformanceStatementsPath(), dicomDocumentsFilesForSelectedSystem,
                            dicomPathsLinkingADocumentForSystem);
                    setSystemPathsToDicomDocuments(dicomPathsLinkingADocumentForSystem);

                    entityManager.merge(getSystem());
                    systemInSession = entityManager.merge(systemInSession);
                    FacesMessages.instance().add(StatusMessage.Severity.INFO, "DICOM Conformance Statements added and" + " saved !");
                }
            }
        } catch (SystemActionException e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Unable to save, please contact your admin !");
            LOG.error(e.getMessage());
        }
    }

    /**
     * This method removes HL7 URL from the system (Conformance statements).
     */
    @Override
    @Restrict("#{s:hasPermission('SystemInSessionEditor', 'removeHL7URL', null)}")
    public void removeHL7URL() {
        LOG.debug("removeHL7URL");
        setSystemHL7ConformanceStatementUrl(null);
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "HL7 URL removed !");
        FacesMessages.instance().add(StatusMessage.Severity.WARN, "System is not saved!");
    }

    /**
     * This method removes DICOM URL from the system (Conformance statements).
     */
    @Override
    @Restrict("#{s:hasPermission('SystemInSessionEditor', 'removeDicomURL', null)}")
    public void removeDicomURL() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeDicomURL");
        }
        setSystemDicomConformanceStatementUrl(null);
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "DICOM URL removed !");
        FacesMessages.instance().add(StatusMessage.Severity.WARN, "System is not saved!");
    }

    /**
     * This method removes HL7 Document from the system (Conformance statements).
     */
    @Override
    @Restrict("#{s:hasPermission('SystemInSessionEditor', 'removeHL7Document', null)}")
    public void removeHL7Document(net.ihe.gazelle.common.util.File file) {
        List<PathLinkingADocument> hl7PathLinkingADocumentForSystem = getSystemPathsToHL7Documents();
        sisModifier = new SystemInSessionModifier(institutionForCreation, systemInSession);
        systemInSession = sisModifier.deleteFile(file, SystemInSessionDAO.instance().getHl7ConformanceStatementsPath
                (), this.hl7DocumentsFilesForSelectedSystem, hl7PathLinkingADocumentForSystem);
    }

    /**
     * This method removes DICOM Document from the system (Conformance statements).
     */
    @Override
    @Restrict("#{s:hasPermission('SystemInSessionEditor', 'removeDicomDocument', null)}")
    public void removeDicomDocument(net.ihe.gazelle.common.util.File file) {
        List<PathLinkingADocument> dicomPathLinkingADocumentForSystem = getSystemPathsToDicomDocuments();
        sisModifier = new SystemInSessionModifier(institutionForCreation, systemInSession);
        systemInSession = sisModifier.deleteFile(file, SystemInSessionDAO.instance()
                        .getDicomConformanceStatementsPath(), this.dicomDocumentsFilesForSelectedSystem,
                dicomPathLinkingADocumentForSystem);
    }

    /**
     * Generate the system keyword, using the selected system type and the institution keyword eg. PACS_AGFA_
     */
    @Override
    @Restrict("#{s:hasPermission('SystemInSessionEditor', 'generateSystemKeyword', null)}")
    public void generateSystemKeyword() {
        LOG.debug("generateSystemKeyword");
        try {
            setSystemKeywordSuffix(SystemInSessionModifier.generateSystemKeywordSuffix(institutionForCreation, getSystemType()));
        } catch (SystemActionException e) {
            StatusMessages.instance().add(StatusMessage.Severity.ERROR, e.getMessage());
        }
    }

    @Override
    @Restrict("#{s:hasPermission('SystemInSessionEditor', 'updateSystemInSession', null)}")
    public void updateSystemInSession() {
        sisModifier = new SystemInSessionModifier(institutionForCreation, systemInSession);
        try {
            sisModifier.updateSystemInSession();
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "System saved");
        } catch (SystemActionException e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Failed to save system !");
            StatusMessages.instance().add(StatusMessage.Severity.ERROR, e.getMessage());
        }
    }

    @Override
    public synchronized void listenerHl7DocumentsFiles(FileUploadEvent event) throws Exception {
        UploadedFile item = event.getUploadedFile();
        net.ihe.gazelle.common.util.File file = new net.ihe.gazelle.common.util.File(item);
        hl7DocumentsFilesForSelectedSystem.add(file);
    }

    @Override
    public synchronized void listenerDicomDocumentsFiles(FileUploadEvent event) throws Exception {
        UploadedFile item = event.getUploadedFile();
        net.ihe.gazelle.common.util.File file = new net.ihe.gazelle.common.util.File(item);
        dicomDocumentsFilesForSelectedSystem.add(file);
    }

    /**
     * This method saves (auto-save) the "is a tool" boolean flag (admin operation editing a system)
     */
    @Override
    public void saveAsIsTool(System s) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveAsIsTool");
        }
        entityManager.merge(s);
        entityManager.flush();
        FacesMessages.instance().add(StatusMessage.Severity.INFO, "System saved");
    }
}
