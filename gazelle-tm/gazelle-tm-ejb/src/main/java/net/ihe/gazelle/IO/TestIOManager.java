package net.ihe.gazelle.IO;

import net.ihe.gazelle.common.fineuploader.FineuploaderListener;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.tm.datamodel.TestDataModel;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestQuery;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.async.QuartzDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Name("testIOManager")
@Scope(ScopeType.CONVERSATION)
@GenerateInterface("TestIOManagerLocal")
public class TestIOManager implements TestIOManagerLocal, Serializable, FineuploaderListener {

    private static final long serialVersionUID = 2740330132656079658L;
    private static final Logger LOG = LoggerFactory.getLogger(TestIOManager.class);
    private String filePath = "";
    private boolean deleteImportedFile = false;
    private String fileName;

    public void export(Test currentTest) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("export");
        }
        TestsExporter testExporter = new TestsExporter();
        try {
            fileName = currentTest.getKeyword() + "_" + getDate() + ".xml";
            TestQuery testQuery = new TestQuery();
            testQuery.id().eq(currentTest.getId());
            Test test = testQuery.getUniqueResult();
            testExporter.exportTest(test);
            redirectExport(testExporter);
        } catch (JAXBException e) {
            LOG.error("" + e);
        }
    }

    public void exportFilteredTests(TestDataModel tests) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("exportFilteredTests");
        }
        TestsExporter testExporter = new TestsExporter();
        @SuppressWarnings("unchecked")
        List<Test> testList = (List<Test>) tests.getAllItems(FacesContext.getCurrentInstance());
        try {
            testExporter.exportTests(testList);
            redirectExport(testExporter);
        } catch (JAXBException e) {

        }
    }

    private void redirectExport(TestsExporter testsExporter) throws IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();

        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
        ServletOutputStream servletOutputStream = response.getOutputStream();
        response.setContentType("application/xml");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + getFileName() + "\"");

        testsExporter.write(servletOutputStream);
        servletOutputStream.flush();
        servletOutputStream.close();
        facesContext.responseComplete();
    }

    private String getFileName() {
        if (fileName == null) {
            provideFileName();
        }
        return fileName;
    }

    private String getDate() {
        Date date = new Date();
        SimpleDateFormat formater = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
        return formater.format(date.getTime());
    }

    private void provideFileName() {
        fileName = "Tests_" + getDate() + ".xml";
    }

    @Override
    @Transactional
    public void uploadedFile(File tmpFile, String filename, String id, String param) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("uploadedFile");
        }
        if ("testsToUpload".equals(id)) {
            TestsImporter testImporter = new TestsImporter();
            try {
                testImporter.importTests(tmpFile);
                if (tmpFile.delete()) {
                    LOG.info("tmpFile deleted");
                } else {
                    LOG.error("Failed to delete tmpFile");
                }
            } catch (JAXBException e) {
                LOG.error("cannot import tests " + tmpFile.getAbsolutePath());
            }
        }
    }

    public void importAction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("importAction");
        }
        if (!"".equals(getFilePath())) {
            QuartzDispatcher.instance().scheduleAsynchronousEvent("importXmlTests", getFilePath(),
                    isDeleteImportedFile());
        }
    }

    public String getFilePath() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilePath");
        }

        return filePath;
    }

    public void setFilePath(String filePath) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setFilePath");
        }
        this.filePath = filePath;
    }

    public boolean isDeleteImportedFile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isDeleteImportedFile");
        }
        return deleteImportedFile;
    }

    public void setDeleteImportedFile(boolean deleteImportedFile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDeleteImportedFile");
        }
        this.deleteImportedFile = deleteImportedFile;
    }
}
