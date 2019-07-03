package net.ihe.gazelle.tm.gazelletest.bean;

import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.menu.Authorizations;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestRoles;
import net.ihe.gazelle.tm.gazelletest.model.instance.SAPResultTestRoleComment;
import net.ihe.gazelle.tm.gazelletest.model.instance.SAPResultTestRoleCommentQuery;
import net.ihe.gazelle.tm.gazelletest.model.instance.SystemAIPOResultForATestingSession;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class SAPResultDetailTestRoleItem extends SAPResultDetailItem {

    private static final Logger LOG = LoggerFactory.getLogger(SAPResultDetailTestRoleItem.class);

    private TestRoles testRoles;
    private SystemAIPOResultForATestingSession systemAIPOResult;
    private SAPResultDetailMetaTestItem resultMetaTest;

    public SAPResultDetailTestRoleItem(TestRoles testRoles, SystemAIPOResultForATestingSession systemAIPOResult) {
        super(systemAIPOResult.getTestSession());
        this.testRoles = testRoles;
        this.systemAIPOResult = systemAIPOResult;
    }

    public SAPResultDetailMetaTestItem getResultMetaTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getResultMetaTest");
        }
        return resultMetaTest;
    }

    public void setResultMetaTest(SAPResultDetailMetaTestItem resultMetaTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setResultMetaTest");
        }
        this.resultMetaTest = resultMetaTest;
    }

    public TestRoles getTestRoles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestRoles");
        }
        return testRoles;
    }

    public SystemAIPOResultForATestingSession getSystemAIPOResult() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemAIPOResult");
        }
        return systemAIPOResult;
    }

    @Override
    protected boolean isSupported(Test test) {
        return isSupported(test, testRoles);
    }

    @Override
    public String getTitle() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTitle");
        }
        if (testRoles.getMetaTest() != null) {
            return testRoles.getMetaTest().getKeyword();
        } else {
            return testRoles.getTest().getKeyword();
        }
    }

    @Override
    public String getLink() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLink");
        }
        if (testRoles.getMetaTest() != null) {
            return getPermalinkToMetaTest(testRoles.getMetaTest());
        } else {
            return getTestPermalink(testRoles.getTest());
        }
    }

    @Override
    protected String getComparableTitle() {
        if (testRoles.getMetaTest() != null) {
            return "a" + testRoles.getMetaTest().getKeyword() + testRoles.getRoleInTest().getKeyword();
        } else {
            return "b" + testRoles.getTest().getKeyword();
        }
    }

    @Override
    public String getCount() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCount");
        }
        if (testRoles.getMetaTest() != null) {
            return "";
        }
        return Integer.toString(testRoles.getNumberOfTestsToRealize());
    }

    @Override
    public String getOpt() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOpt");
        }
        if (testRoles.getMetaTest() != null) {
            return "";
        }
        return testRoles.getTestOption().getKeyword();
    }

    @Override
    public boolean isMetaTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isMetaTest");
        }
        return testRoles.getMetaTest() != null;
    }

    @Override
    public String getTitle2() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTitle2");
        }
        return testRoles.getTest().getKeyword();
    }

    @Override
    public String getLink2() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLink2");
        }
        return getTestPermalink(testRoles.getTest());
    }

    @Override
    public List<TestRoles> getTestRolesList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestRolesList");
        }
        return Collections.singletonList(testRoles);
    }

    @Override
    public boolean isAllowComment() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isAllowComment");
        }
        try {
            if (Authorizations.VENDOR.isGranted()) {
                Institution institution = User.loggedInUser().getInstitution();
                System system = systemAIPOResult.getSystemActorProfile().getSystem();
                return system.isOwnedBy(institution);
            }

        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private SAPResultTestRoleComment getCommentEntity() {
        SAPResultTestRoleCommentQuery query = new SAPResultTestRoleCommentQuery();
        query.systemAIPOResultForATestingSession().eq(systemAIPOResult);
        query.testRoles().eq(testRoles);
        SAPResultTestRoleComment uniqueResult = query.getUniqueResult();
        return uniqueResult;
    }

    @Override
    public String getComment() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getComment");
        }
        SAPResultTestRoleComment uniqueResult = getCommentEntity();
        if (uniqueResult == null) {
            return null;
        } else {
            return uniqueResult.getComment();
        }
    }

    @Override
    public void setComment(String comment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setComment");
        }
        SAPResultTestRoleComment commentEntity = getCommentEntity();
        if (commentEntity == null) {
            commentEntity = new SAPResultTestRoleComment();
            commentEntity.setSystemAIPOResultForATestingSession(systemAIPOResult);
            commentEntity.setTestRoles(testRoles);
        }
        commentEntity.setComment(comment);
        EntityManagerService.provideEntityManager().persist(commentEntity);
        EntityManagerService.provideEntityManager().flush();
    }
}
