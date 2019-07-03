package net.ihe.gazelle.tm.gazelletest.ws.trm;

import net.ihe.gazelle.tm.gazelletest.model.definition.RoleInTest;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestOption;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TRMTestRole {
    private static final Logger LOG = LoggerFactory.getLogger(TRMTestRole.class);

    private Integer id;
    private Integer cardMin;
    private Integer cardMax;
    private String testOption;
    private TRMRoleInTest roleInTest;

    public void copyFromTestRole(TestRoles testRole) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("copyFromTestRole");
        }
        this.id = testRole.getId();
        this.cardMin = testRole.getCardMin();
        this.cardMax = testRole.getCardMax();
        RoleInTest roleInTest2 = testRole.getRoleInTest();
        if (roleInTest2 != null) {
            this.roleInTest = new TRMRoleInTest();
            this.roleInTest.copyFromRoleInTest(roleInTest2);
        }
        TestOption testOption2 = testRole.getTestOption();
        if (testOption2 != null) {
            this.testOption = testOption2.getKeyword();
        }
    }

    public Integer getCardMax() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCardMax");
        }
        return cardMax;
    }

    public void setCardMax(Integer cardMax) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCardMax");
        }
        this.cardMax = cardMax;
    }

    public Integer getCardMin() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCardMin");
        }
        return cardMin;
    }

    public void setCardMin(Integer cardMin) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setCardMin");
        }
        this.cardMin = cardMin;
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

    public TRMRoleInTest getRoleInTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRoleInTest");
        }
        return roleInTest;
    }

    public void setRoleInTest(TRMRoleInTest roleInTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setRoleInTest");
        }
        this.roleInTest = roleInTest;
    }

    public String getTestOption() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestOption");
        }
        return testOption;
    }

    public void setTestOption(String testOption) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestOption");
        }
        this.testOption = testOption;
    }

}
