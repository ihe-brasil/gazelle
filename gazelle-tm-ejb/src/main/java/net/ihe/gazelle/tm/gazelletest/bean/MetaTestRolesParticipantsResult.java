package net.ihe.gazelle.tm.gazelletest.bean;

import net.ihe.gazelle.tm.gazelletest.model.definition.MetaTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MetaTestRolesParticipantsResult implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(MetaTestRolesParticipantsResult.class);

    private static final long serialVersionUID = 1L;

    private MetaTest metaTest;

    private Integer numberOfTestsToRealize;

    private List<TestRolesParticipantsResult> testRolesParticipantsResultList;

    public MetaTestRolesParticipantsResult() {

    }

    public MetaTestRolesParticipantsResult(MetaTest metaTest, Integer numberOfTestsToRealize,
                                           List<TestRolesParticipantsResult> testRolesParticipantsResultList) {
        this.metaTest = metaTest;
        this.numberOfTestsToRealize = numberOfTestsToRealize;
        this.testRolesParticipantsResultList = testRolesParticipantsResultList;
    }

    public MetaTest getMetaTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMetaTest");
        }
        return metaTest;
    }

    public void setMetaTest(MetaTest metaTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMetaTest");
        }
        this.metaTest = metaTest;
    }

    public Integer getNumberOfTestsToRealize() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getNumberOfTestsToRealize");
        }
        return numberOfTestsToRealize;
    }

    public void setNumberOfTestsToRealize(Integer numberOfTestsToRealize) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setNumberOfTestsToRealize");
        }
        this.numberOfTestsToRealize = numberOfTestsToRealize;
    }

    public List<TestRolesParticipantsResult> getTestRolesParticipantsResultList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestRolesParticipantsResultList");
        }
        return testRolesParticipantsResultList;
    }

    public void setTestRolesParticipantsResultList(List<TestRolesParticipantsResult> testRolesParticipantsResultList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestRolesParticipantsResultList");
        }
        this.testRolesParticipantsResultList = testRolesParticipantsResultList;
    }

    public void addTestRolesParticipantsResult(TestRolesParticipantsResult inTestRolesParticipantsResult) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addTestRolesParticipantsResult");
        }
        if (testRolesParticipantsResultList == null) {
            testRolesParticipantsResultList = new ArrayList<TestRolesParticipantsResult>();
        }
        testRolesParticipantsResultList.add(inTestRolesParticipantsResult);
    }

    @Override
    public int hashCode() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("hashCode");
        }
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((metaTest == null) ? 0 : metaTest.hashCode());
        result = (prime * result) + ((numberOfTestsToRealize == null) ? 0 : numberOfTestsToRealize.hashCode());
        result = (prime * result)
                + ((testRolesParticipantsResultList == null) ? 0 : testRolesParticipantsResultList.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("equals");
        }
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MetaTestRolesParticipantsResult other = (MetaTestRolesParticipantsResult) obj;
        if (metaTest == null) {
            if (other.metaTest != null) {
                return false;
            }
        } else if (!metaTest.equals(other.metaTest)) {
            return false;
        }
        if (numberOfTestsToRealize == null) {
            if (other.numberOfTestsToRealize != null) {
                return false;
            }
        } else if (!numberOfTestsToRealize.equals(other.numberOfTestsToRealize)) {
            return false;
        }
        if (testRolesParticipantsResultList == null) {
            if (other.testRolesParticipantsResultList != null) {
                return false;
            }
        } else if (!testRolesParticipantsResultList.equals(other.testRolesParticipantsResultList)) {
            return false;
        }
        return true;
    }

}
