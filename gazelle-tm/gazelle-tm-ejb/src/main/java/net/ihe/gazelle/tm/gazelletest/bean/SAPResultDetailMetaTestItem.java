package net.ihe.gazelle.tm.gazelletest.bean;

import net.ihe.gazelle.tm.gazelletest.model.definition.MetaTest;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestRoles;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SAPResultDetailMetaTestItem extends SAPResultDetailItem {

    private static final Logger LOG = LoggerFactory.getLogger(SAPResultDetailMetaTestItem.class);

    private MetaTest metaTest;
    private List<TestRoles> testRoles = new ArrayList<TestRoles>();

    public SAPResultDetailMetaTestItem(TestingSession testingSession, MetaTest metaTest) {
        super(testingSession);
        this.metaTest = metaTest;
    }

    public static void addAndCreateIfNeeded(SAPResultDetailTestRoleItem detailTR, List<SAPResultDetailItem> result) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addAndCreateIfNeeded");
        }
        MetaTest metaTest = detailTR.getTestRoles().getMetaTest();

        SAPResultDetailMetaTestItem item = null;
        for (SAPResultDetailItem sapResultDetailItem : result) {
            if ((item == null) && (sapResultDetailItem instanceof SAPResultDetailMetaTestItem)) {
                SAPResultDetailMetaTestItem testItem = (SAPResultDetailMetaTestItem) sapResultDetailItem;
                if (testItem.getMetaTest().getId().equals(metaTest.getId())) {
                    item = testItem;
                }
            }
        }
        if (item == null) {
            item = new SAPResultDetailMetaTestItem(detailTR.getSystemAIPOResult().getTestSession(), metaTest);
            result.add(item);
        }
        item.addTestRole(detailTR.getTestRoles());
    }

    public MetaTest getMetaTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMetaTest");
        }
        return metaTest;
    }

    private void addTestRole(TestRoles testRole) {
        testRoles.add(testRole);
    }

    @Override
    protected boolean isSupported(Test test) {
        for (TestRoles testRole : testRoles) {
            if (isSupported(test, testRole)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getTitle() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTitle");
        }
        return metaTest.getKeyword();
    }

    @Override
    protected String getComparableTitle() {
        return "a" + metaTest.getKeyword();
    }

    @Override
    public String getCount() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCount");
        }
        int numberOfTestsToRealize = 0;
        for (TestRoles testRole : testRoles) {
            numberOfTestsToRealize = Math.max(testRole.getNumberOfTestsToRealize(), numberOfTestsToRealize);
        }
        return Integer.toString(numberOfTestsToRealize);
    }

    @Override
    public String getOpt() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOpt");
        }
        return "R";
    }

    @Override
    public boolean isMetaTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isMetaTest");
        }
        return true;
    }

    @Override
    public String getTitle2() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTitle2");
        }
        return getTitle() + " (All tests)";
    }

    @Override
    public String getLink() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLink");
        }
        return getPermalinkToMetaTest(metaTest);
    }

    @Override
    public String getLink2() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLink2");
        }
        return null;
    }

    @Override
    public List<TestRoles> getTestRolesList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestRolesList");
        }
        return testRoles;
    }

    @Override
    public boolean isAllowComment() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isAllowComment");
        }
        return false;
    }

    @Override
    public String getComment() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getComment");
        }
        // noop
        return null;
    }

    @Override
    public void setComment(String comment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setComment");
        }
        // noop
    }
}
