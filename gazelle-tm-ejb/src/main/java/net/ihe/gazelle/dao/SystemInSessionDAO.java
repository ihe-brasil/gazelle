package net.ihe.gazelle.dao;

import net.ihe.gazelle.common.log.ExceptionLogging;
import net.ihe.gazelle.common.model.PathLinkingADocument;
import net.ihe.gazelle.common.util.File;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gthomazon on 17/07/17.
 */
public class SystemInSessionDAO {

    private static final Logger LOG = LoggerFactory.getLogger(SystemInSessionDAO.class);
    private static SystemInSessionDAO sisDAO;
    public String hl7ConformanceStatementsPath = net.ihe.gazelle.common.application.action.ApplicationPreferenceManager.instance()
            .getGazelleHL7ConformanceStatementsPath();
    public String dicomConformanceStatementsPath = net.ihe.gazelle.common.application.action.ApplicationPreferenceManager.instance()
            .getGazelleDicomConformanceStatementsPath();

    public static SystemInSessionDAO instance() {
        if (sisDAO == null) {
            sisDAO = new SystemInSessionDAO();
        }
        return sisDAO;
    }

    public String getHl7ConformanceStatementsPath() {
        return hl7ConformanceStatementsPath;
    }

    public String getDicomConformanceStatementsPath() {
        return dicomConformanceStatementsPath;
    }

    /**
     * This method retrieves HL7 Conformance Statements for a selected system. This is called when to get that objects/files before editing/viewing
     * a system
     */
    public ArrayList<File> retrieveHL7ConformanceStatementForSelectedSystemToViewOrEdit(SystemInSession systemInSession) {
        LOG.debug("retrieveHL7ConformanceStatementForSelectedSystemToViewOrEdit");
        return findDocumentsFilesForSelectedSystem(hl7ConformanceStatementsPath, systemInSession.getSystem(), systemInSession.getSystem()
                .getPathsToHL7Documents());
    }

    /**
     * This method retrieves HL7 Conformance Statements for a selected system. This is called when to get that objects/files before editing/viewing
     * a system
     */
    public ArrayList<net.ihe.gazelle.common.util.File> retrieveDicomConformanceStatementForSelectedSystemToViewOrEdit(SystemInSession
                                                                                                                              systemInSession) {
        LOG.debug("retrieveDicomConformanceStatementForSelectedSystemToViewOrEdit");
        return findDocumentsFilesForSelectedSystem(dicomConformanceStatementsPath, systemInSession.getSystem(), systemInSession.getSystem()
                .getPathsToDicomDocuments());

    }

    private ArrayList<File> findDocumentsFilesForSelectedSystem(String fileServerPath, System system, List<PathLinkingADocument>
            systemPathsToFileDocuments) {
        ArrayList<net.ihe.gazelle.common.util.File> documentsFilesForSelectedSystem = new ArrayList<net.ihe.gazelle.common.util.File>();
        if (systemPathsToFileDocuments != null) {
            for (int iElements = 0; iElements < systemPathsToFileDocuments.size(); iElements++) {
                String pathname = fileServerPath + java.io.File.separatorChar
                        + system.getId() + java.io.File.separatorChar
                        + systemPathsToFileDocuments.get(iElements).getPath();
                java.io.File f = new java.io.File(pathname);
                try {
                    net.ihe.gazelle.common.util.File file = new net.ihe.gazelle.common.util.File(pathname, IOUtils.toByteArray(new FileInputStream
                            (f)));
                    documentsFilesForSelectedSystem.add(file);
                } catch (FileNotFoundException e) {
                    ExceptionLogging.logException(e, LOG);
                } catch (IOException e) {
                    ExceptionLogging.logException(e, LOG);
                }
            }
        }
        return documentsFilesForSelectedSystem;
    }
}
