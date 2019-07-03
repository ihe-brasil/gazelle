package net.ihe.gazelle.IO;

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.async.QuartzDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Name("TestImporterJob")
@AutoCreate
@Scope(ScopeType.APPLICATION)
@GenerateInterface("TestImporterJobLocal")
public class TestImporterJob implements TestImporterJobLocal, Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(TestImporterJob.class);

    @Override
    @Observer("importXmlTests")
    @Transactional
    @Asynchronous
    /*
     * imports are done in a background job, to avoid transaction timeout, and return quickly to the user
	 * */
    public void importXmlTests(String filePath, boolean deleteImportedFile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("importXmlTests");
        }
        File testsToImportXml = new File(filePath);

        if (testsToImportXml.isFile()) {
            TestXmlPackager testPackage = unmarshallXml(testsToImportXml);
            List<Test> tests = testPackage.getTests();
            QuartzDispatcher.instance().scheduleAsynchronousEvent("importTest", tests, tests.size(), 1,
                    deleteImportedFile, testsToImportXml);

        } else if (testsToImportXml.isDirectory()) {

            GenericExtFilter filter = new GenericExtFilter("xml");
            File[] files = testsToImportXml.listFiles(filter);
            if (files != null) {
                List<File> listOfFiles = new ArrayList<File>(Arrays.asList(files));
                QuartzDispatcher.instance().scheduleAsynchronousEvent("importTestFromFile", listOfFiles,
                        listOfFiles.size(), 1, deleteImportedFile);
            }
        }
    }

    @Override
    @Observer("importTestFromFile")
    @Transactional
    @Asynchronous
    /*A new call to saveOrMergeFromFile is issued for each test to import.
     * This is done in order to avoid transaction timeout.
	 * Each method call has it's own transaction
	 * A test import can take time and when importing a lot of tests it could take more than the allowed transaction time.
	 * */
    public void saveOrMergeFromFile(List<File> xmlFiles, int nbFiles, int counter, boolean deleteImportedFile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveOrMergeFromFile");
        }
        if (xmlFiles.size() > 0) {
            File testsToImportXml = xmlFiles.get(0);
            xmlFiles.remove(0);

            counter++;
            TestXmlPackager testPackage = unmarshallXml(testsToImportXml);
            testPackage.saveOrMerge(EntityManagerService.provideEntityManager());
            if (deleteImportedFile) {
                if (testsToImportXml.delete()) {
                    LOG.info("testsToImportXml deleted");
                } else {
                    LOG.error("Failed to delete testsToImportXml");
                }
            }
            QuartzDispatcher.instance().scheduleAsynchronousEvent("importTestFromFile", xmlFiles, nbFiles, counter,
                    deleteImportedFile);
        }
    }

    @Override
    @Observer("importTest")
    @Transactional
    @Asynchronous
    /*A new call to saveOrMerge is issued for each test to import.
     * This is done in order to avoid transaction timeout.
	 * Each method call has it's own transaction
	 * A test import can take time and when importing a lot of tests it could take more than the allowed transaction time.
	 * */
    public void saveOrMerge(List<Test> tests, int nbTests, int counter, boolean deleteImportedFile, File xmlFile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveOrMerge");
        }
        if (tests.size() > 0) {
            Test test = tests.get(0);
            test.saveOrMerge(EntityManagerService.provideEntityManager(), counter, nbTests);
            tests.remove(0);
            counter++;
            QuartzDispatcher.instance().scheduleAsynchronousEvent("importTest", tests, nbTests, counter,
                    deleteImportedFile, xmlFile);
        } else {
            if (deleteImportedFile) {
                if (xmlFile.delete()) {
                    LOG.info("xmlFile deleted");
                } else {
                    LOG.error("Failed to delete xmlFile");
                }
            }

        }
    }

    private TestXmlPackager unmarshallXml(File testsToImportXml) {
        TestXmlPackager testPackage = null;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(TestXmlPackager.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            testPackage = (TestXmlPackager) jaxbUnmarshaller.unmarshal(testsToImportXml);
        } catch (JAXBException e) {
            LOG.error("Failed to import tests");
            LOG.error("JAXBContext cannot unmarshal " + testsToImportXml.getAbsolutePath());
            LOG.error("JAXBContext cannot unmarshal " + e.getCause());
        }
        return testPackage;
    }

    private static class GenericExtFilter implements FilenameFilter {

        private String ext;

        GenericExtFilter(String ext) {
            this.ext = ext;
        }

        public boolean accept(File dir, String name) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("accept");
            }
            return (name.endsWith(ext));
        }
    }
}
