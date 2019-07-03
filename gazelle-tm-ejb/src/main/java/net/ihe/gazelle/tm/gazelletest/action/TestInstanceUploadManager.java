package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.fineuploader.FineuploaderListener;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.servletfilter.FileGenerator;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.gazelletest.model.instance.*;
import org.apache.commons.io.IOUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.StringTokenizer;

//
// TODO remove and test if upload is still ok
@Name("testInstanceUploadManager")
@Scope(ScopeType.APPLICATION)
@Synchronized(timeout = 10000)
@GenerateInterface("TestInstanceUploadManagerLocal")
@MetaInfServices(FileGenerator.class)
public class TestInstanceUploadManager implements TestInstanceUploadManagerLocal, FineuploaderListener, FileGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(TestInstanceUploadManager.class);

    @Override
    @Transactional
    public void uploadedFile(File tmpFile, String filename, String id, String param) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("uploadedFile");
        }
        synchronized (LOG) {
            TestStepsData tsd = persistTestStepsDataFile(filename, "");
            saveFileUploaded(tsd, tmpFile);
            if ("testInstanceUploads".equals(id)) {
                addTestStepsDataToTestInstance(tsd, Integer.parseInt(param));
            } else {
                addTestStepsDataToTestStepsInstance(tsd, Integer.parseInt(param));
            }
        }
    }

    @Override
    @Transactional
    public TestStepsData persistTestStepsDataFile(String name, String comment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistTestStepsDataFile");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        TestStepsData testStepsData = new TestStepsData();
        testStepsData.setValue(name);
        testStepsData.setDataType(DataType.getDataTypeByKeyword(DataType.FILE_DT));
        testStepsData.setComment(comment);
        testStepsData.setWithoutFileName(true);
        testStepsData = entityManager.merge(testStepsData);
        entityManager.flush();
        return testStepsData;
    }

    @Override
    public void saveFileUploaded(TestStepsData testStepsData, File tmpFile) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveFileUploaded");
        }
        InputStream inputStream = new FileInputStream(tmpFile);
        saveFileUploaded(testStepsData, inputStream);
        if (tmpFile.delete()) {
            LOG.info("tmpFile deleted");
        } else {
            LOG.error("Failed to delete tmpFile");
        }
    }

    @Override
    public void saveFileUploaded(TestStepsData testStepsData, InputStream inputStream) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveFileUploaded");
        }
        String dataPath = ApplicationPreferenceManager.instance().getGazelleDataPath();
        String fileStepsPath = ApplicationPreferenceManager.getStringValue("file_steps_path");

        String filePathForThisFileInstance = dataPath + java.io.File.separatorChar + fileStepsPath
                + java.io.File.separatorChar + testStepsData.getId();
        java.io.File fileToStore = new java.io.File(filePathForThisFileInstance);
        if (fileToStore.getParentFile().mkdirs()) {
            LOG.info("fileToStore directory created");
        } else {
            LOG.error("Failed to create fileToStore");
        }
        FileOutputStream fos;
        try {
            if (fileToStore.exists()) {
                if (fileToStore.delete()) {
                    LOG.info("fileToStore deleted");
                } else {
                    LOG.error("Failed to delete fileToStore");
                }
            }
            fos = new FileOutputStream(fileToStore);
            IOUtils.copy(inputStream, fos);
            inputStream.close();
            fos.close();
        } catch (FileNotFoundException e) {
            LOG.error("", e);
            FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, e.getMessage());
        } catch (IOException e) {
            LOG.error("", e);
            FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, e.getMessage());
        }
    }

    @Override
    public TestStepsInstance addTestStepsDataToTestStepsInstance(TestStepsData tsd, Integer testStepsInstanceId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addTestStepsDataToTestStepsInstance");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        TestStepsInstanceQuery query = new TestStepsInstanceQuery();
        query.id().eq(testStepsInstanceId);
        TestStepsInstance cleanTestStepsInstance = query.getUniqueResult();
        cleanTestStepsInstance.addTestStepsData(tsd);
        entityManager.merge(cleanTestStepsInstance);
        entityManager.flush();
        return cleanTestStepsInstance;
    }

    @Override
    public TestInstance addTestStepsDataToTestInstance(TestStepsData tsd, Integer testInstanceId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addTestStepsDataToTestInstance");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        TestInstanceQuery query = new TestInstanceQuery();
        query.id().eq(testInstanceId);
        TestInstance cleanTestInstance = query.getUniqueResult();
        cleanTestInstance.addTestStepsData(tsd);
        entityManager.merge(cleanTestInstance);
        entityManager.flush();
        return cleanTestInstance;
    }

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    @Override
    public String getPath() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPath");
        }
        return "/testInstanceData/";
    }

    @Override
    public void process(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("process");
        }
        String servletPath = httpServletRequest.getServletPath();
        StringTokenizer stringTokenizer = new StringTokenizer(servletPath, "/");
        stringTokenizer.nextToken();
        String type = stringTokenizer.nextToken();
        String filepath = null;
        String fileName = null;
        if ("ti".equals(type)) {
            String id = stringTokenizer.nextToken();
            TestStepsDataQuery testStepsDataQuery = new TestStepsDataQuery();
            testStepsDataQuery.id().eq(Integer.parseInt(id));
            TestStepsData tsd = testStepsDataQuery.getUniqueResult();
            filepath = tsd.getCompleteFilePath();
            fileName = tsd.getValue();
        } else if ("preti".equals(type)) {
            String id = stringTokenizer.nextToken();
            TestInstancePathToLogFileQuery query = new TestInstancePathToLogFileQuery();
            query.id().eq(Integer.parseInt(id));
            TestInstancePathToLogFile testInstancePathToLogFile = query.getUniqueResult();

            filepath = testInstancePathToLogFile.getFile().getAbsolutePath();
            fileName = testInstancePathToLogFile.getName();
        }
        try {
            net.ihe.gazelle.common.util.DocumentFileUpload.showFile(httpServletRequest, httpServletResponse,
                    new FileInputStream(filepath), fileName, false);
        } catch (FileNotFoundException e) {
            LOG.error("File not found : " + filepath, e);
            throw new IllegalArgumentException("File not found : " + filepath, e);
        }
    }

}
