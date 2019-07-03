package net.ihe.gazelle.tm.gazelletest.ws.trm;

import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestDescription;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestRoles;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TRMTest {
    private static final Logger LOG = LoggerFactory.getLogger(TRMTest.class);

    private Integer id;
    private String keyword;
    private String name;
    private String shortDescription;
    private String testPeerType;
    private String testStatus;
    private String testType;
    private String version;
    private List<TRMTestDescription> trmTestDescriptions;
    private List<TRMTestRole> trmTestRoles;
    private List<TRMTestStep> trmTestSteps;

    public void copyFromTest(Test test) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("copyFromTest");
        }
        this.id = test.getId();
        this.keyword = test.getKeyword();
        this.name = test.getName();
        this.shortDescription = test.getShortDescription();

        trmTestDescriptions = new ArrayList<TRMTestDescription>();
        List<TestDescription> testDescriptions = test.getTestDescription();
        for (TestDescription testDescription : testDescriptions) {
            TRMTestDescription trmTestDescription = new TRMTestDescription();
            trmTestDescription.copyFromTestDescription(testDescription);
            trmTestDescriptions.add(trmTestDescription);
        }

        this.testPeerType = test.getTestPeerType().getKeyword();

        trmTestRoles = new ArrayList<TRMTestRole>();
        List<TestRoles> testRoles = test.getTestRoles();
        for (TestRoles testRole : testRoles) {
            TRMTestRole trmTestRole = new TRMTestRole();
            trmTestRole.copyFromTestRole(testRole);
            trmTestRoles.add(trmTestRole);
        }

        this.testStatus = test.getTestStatus().getKeyword();

        trmTestSteps = new ArrayList<TRMTestStep>();
        List<TestSteps> testStepsList = test.getTestStepsList();
        for (TestSteps testStep : testStepsList) {
            TRMTestStep trmTestStep = new TRMTestStep();
            trmTestStep.copyFromTestStep(testStep);
            trmTestSteps.add(trmTestStep);
        }

        this.testType = test.getTestType().getKeyword();
        this.version = test.getVersion();
    }

    public Integer getId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getId");
        }
        return id;
    }

    public void setId(Integer id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setId");
        }
        this.id = id;
    }

    public String getKeyword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getKeyword");
        }
        return keyword;
    }

    public void setKeyword(String keyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setKeyword");
        }
        this.keyword = keyword;
    }

    public String getName() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getName");
        }
        return name;
    }

    public void setName(String name) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setName");
        }
        this.name = name;
    }

    public String getShortDescription() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getShortDescription");
        }
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setShortDescription");
        }
        this.shortDescription = shortDescription;
    }

    public String getTestPeerType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestPeerType");
        }
        return testPeerType;
    }

    public void setTestPeerType(String testPeerType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestPeerType");
        }
        this.testPeerType = testPeerType;
    }

    public String getTestStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestStatus");
        }
        return testStatus;
    }

    public void setTestStatus(String testStatus) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestStatus");
        }
        this.testStatus = testStatus;
    }

    public String getTestType() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestType");
        }
        return testType;
    }

    public void setTestType(String testType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestType");
        }
        this.testType = testType;
    }

    public String getVersion() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getVersion");
        }
        return version;
    }

    public void setVersion(String version) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setVersion");
        }
        this.version = version;
    }

    public List<TRMTestDescription> getTrmTestDescriptions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTrmTestDescriptions");
        }
        return trmTestDescriptions;
    }

    public void setTrmTestDescriptions(List<TRMTestDescription> trmTestDescriptions) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTrmTestDescriptions");
        }
        this.trmTestDescriptions = trmTestDescriptions;
    }

    public List<TRMTestRole> getTrmTestRoles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTrmTestRoles");
        }
        return trmTestRoles;
    }

    public void setTrmTestRoles(List<TRMTestRole> trmTestRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTrmTestRoles");
        }
        this.trmTestRoles = trmTestRoles;
    }

    public List<TRMTestStep> getTrmTestSteps() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTrmTestSteps");
        }
        return trmTestSteps;
    }

    public void setTrmTestSteps(List<TRMTestStep> trmTestSteps) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTrmTestSteps");
        }
        this.trmTestSteps = trmTestSteps;
    }

}
