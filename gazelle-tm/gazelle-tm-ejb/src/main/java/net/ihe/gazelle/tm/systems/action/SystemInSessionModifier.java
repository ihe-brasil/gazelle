package net.ihe.gazelle.tm.systems.action;

import net.ihe.gazelle.common.log.ExceptionLogging;
import net.ihe.gazelle.common.model.PathLinkingADocument;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.SystemType;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.User;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gthomazon on 17/07/17.
 */
public class SystemInSessionModifier extends AbstractSystemInSessionBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(SystemInSessionModifier.class);

    public SystemInSessionModifier() {
    }

    public SystemInSessionModifier(SystemType sysType, String sysversion, String syskeywordSuffix, String sysname, Institution
            institutionForCreation, User sysOwnerUser) {
        super(sysType, sysversion, syskeywordSuffix, sysname, institutionForCreation, sysOwnerUser);
    }

    public SystemInSessionModifier(SystemType sysType, String sysversion, String syskeywordSuffix, String sysname, Institution
            institutionForCreation, User sysOwnerUser, SystemInSession systemInSession) {
        super(sysType, sysversion, syskeywordSuffix, sysname, institutionForCreation, sysOwnerUser, systemInSession);
    }

    public SystemInSessionModifier(Institution institutionForCreation, SystemInSession systemInSession) {
        super(null, null, null, null, institutionForCreation, null, systemInSession);
    }

    /**
     * Update a system to the database This operation is allowed for some granted users (check the security.drl)
     */
    public void editSystemForTM() throws SystemActionException {
        LOG.debug("editSystemForTM");
        updateSystemInSession();
        calculateFinancialSummaryDTO();
    }

    public void updateSystemInSession() throws SystemActionException {
        System updateSystem = systemInSession.getSystem();
        if (isSystemKeywordValid()) {
            updateSystem.setKeyword(updateSystem.computeKeyword());
            updateSystem = entityManager.merge(updateSystem);
            systemInSession.setSystem(updateSystem);
            entityManager.merge(systemInSession);
            entityManager.flush();
        }
    }

    public SystemInSession deleteFile(net.ihe.gazelle.common.util.File file, String conformanceStatementsPath, ArrayList<net.ihe.gazelle.common
            .util.File> documentsFilesForSelectedSystem, List<PathLinkingADocument> pathLinkingADocumentForSystem) {
        deleteFileFromList(file, documentsFilesForSelectedSystem);
        deleteFileOnServer(file, conformanceStatementsPath);
        deleteFileFromSystem(file, pathLinkingADocumentForSystem);
        return systemInSession;
    }

    public void deleteFileFromList(net.ihe.gazelle.common.util.File file, ArrayList<net.ihe.gazelle.common.util.File>
            documentsFilesForSelectedSystem) {
        if (documentsFilesForSelectedSystem != null) {
            for (int i = 0; i < documentsFilesForSelectedSystem.size(); i++) {
                if (documentsFilesForSelectedSystem.get(i).getName().equals(file.getName())) {
                    documentsFilesForSelectedSystem.remove(i);
                }
            }
        }
    }

    public void deleteFileOnServer(net.ihe.gazelle.common.util.File file, String conformanceStatementsPath) {
        String filePathForDocument = conformanceStatementsPath + File.separatorChar
                + systemInSession.getSystem().getId() + File.separatorChar + file.getName();

        File fileToStore = new File(filePathForDocument);
        if (fileToStore.delete()) {
            LOG.info("fileToStore deleted");
        } else {
            LOG.error("Failed to delete fileToStore");
        }
    }

    public void deleteFileFromSystem(net.ihe.gazelle.common.util.File file, List<PathLinkingADocument> pathLinkingADocumentForSystem) {
        if (pathLinkingADocumentForSystem != null) {
            for (int inum = 0; inum < pathLinkingADocumentForSystem.size(); inum++) {
                PathLinkingADocument pathLinkingADocument = entityManager.find(PathLinkingADocument.class, pathLinkingADocumentForSystem.get(inum)
                        .getId());
                if (pathLinkingADocument.getPath().equals(file.getName())) {
                    pathLinkingADocumentForSystem.remove(inum);
                    entityManager.merge(systemInSession.getSystem());
                    entityManager.remove(pathLinkingADocument);
                    systemInSession = entityManager.merge(systemInSession);
                    FacesMessages.instance().add(StatusMessage.Severity.INFO, "Document removed and system saved ! ");
                }
            }
        }
    }


    public void persistDocumentsFile(Integer idForPath, String conformanceStatementsPath, ArrayList<net.ihe.gazelle.common.util.File>
            documentsFilesForSelectedSystem, List<PathLinkingADocument> pathsLinkingADocumentForSystem) throws SystemActionException {
        File newDirectoryForDocuments = new File(conformanceStatementsPath + File.separatorChar + idForPath);
        newDirectoryForDocuments.mkdirs();
        if (!newDirectoryForDocuments.exists()) {
            throw new SystemActionException("Problem creating directory (rights trouble) : " + newDirectoryForDocuments.toString());
        }
        for (net.ihe.gazelle.common.util.File f : documentsFilesForSelectedSystem) {
            String filePathForDocument = conformanceStatementsPath + File.separatorChar + idForPath + File.separatorChar + f.getName();
            File fileToStore = new File(filePathForDocument);
            FileOutputStream fos = null;
            try {
                if (!fileToStore.exists()) {

                    fos = new FileOutputStream(fileToStore);
                    fos.write(f.getData());
                    fos.close();

                    PathLinkingADocument newPathToDocument = new PathLinkingADocument();
                    newPathToDocument.setPath(f.getName());
                    newPathToDocument.setType(f.getMime());
                    newPathToDocument = entityManager.merge(newPathToDocument);

                    pathsLinkingADocumentForSystem.add(newPathToDocument);
                }
            } catch (FileNotFoundException e) {
                ExceptionLogging.logException(e, LOG);
                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, e.getMessage());
            } catch (IOException e) {
                ExceptionLogging.logException(e, LOG);
                StatusMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, e.getMessage());
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        LOG.error(e.getMessage());
                    }
                }
            }
        }
    }
}
