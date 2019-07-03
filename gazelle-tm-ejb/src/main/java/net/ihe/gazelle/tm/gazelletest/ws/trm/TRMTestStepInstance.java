package net.ihe.gazelle.tm.gazelletest.ws.trm;

import net.ihe.gazelle.tf.model.Actor;
import net.ihe.gazelle.tm.configurations.model.AbstractConfiguration;
import net.ihe.gazelle.tm.gazelletest.model.definition.RoleInTest;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestRoles;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestSteps;
import net.ihe.gazelle.tm.gazelletest.model.instance.*;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TRMTestStepInstance {
    private static final Logger LOG = LoggerFactory.getLogger(TRMTestStepInstance.class);
    private Integer testStepId;

    private String status;

    private String status2;
    private String initiator;
    private String responder;

    private List<Integer> configurationsInitIds;
    private List<Integer> configurationsRespIds;

    private Integer roleInTestInitId;

    private Integer roleInTestRespId;

    private void addAndConvertConfiguration(Map<Integer, TRMConfiguration> configurations,
                                            AbstractConfiguration configuration) {
        if (configurations.get(configuration.getId()) == null) {
            TRMConfiguration conf = new TRMConfiguration();
            conf.loadFromConfiguration(configuration);
            if (conf != null) {
                configurations.put(configuration.getId(), conf);
            }
        }
    }

    public void copyFromTestStepsInstance(TestStepsInstance testStepsInstance, TestInstance testInstance,
                                          Map<Integer, TRMConfiguration> configurations) {
        TestSteps testSteps = testStepsInstance.getTestSteps();
        if (testSteps != null) {
            this.testStepId = testSteps.getId();
        }
        Status status3 = testStepsInstance.getStatus();
        if (status3 != null) {
            this.status = status3.getKeyword();
        }
        TestStepsInstanceStatus testStepsInstanceStatus = testStepsInstance.getTestStepsInstanceStatus();
        if (testStepsInstanceStatus != null) {
            this.status2 = testStepsInstanceStatus.getKeyword();
        }

        SystemInSession sysResp = testStepsInstance.getSystemInSessionResponder();
        SystemInSession sysInit = testStepsInstance.getSystemInSessionInitiator();
        this.initiator = sysInit.getSystem().getKeyword();
        this.responder = sysResp.getSystem().getKeyword();

        TestRoles testRolesInitiator = null;
        List<AbstractConfiguration> confsInit = null;
        List<AbstractConfiguration> confsResp = null;
        if (testSteps != null) {
            testRolesInitiator = testSteps.getTestRolesInitiator();

            RoleInTest roleInit = testRolesInitiator.getRoleInTest();
            this.roleInTestInitId = roleInit.getId();

            TestRoles testRolesResponder = testSteps.getTestRolesResponder();
            RoleInTest roleResp = testRolesResponder.getRoleInTest();
            this.roleInTestRespId = roleResp.getId();

            confsInit = getConfigurations(testInstance, roleInit, sysInit);
            confsResp = getConfigurations(testInstance, roleResp, sysResp);
        }
        configurationsInitIds = new ArrayList<Integer>();
        if (confsInit != null) {
            for (AbstractConfiguration configuration : confsInit) {
                addAndConvertConfiguration(configurations, configuration);
                configurationsInitIds.add(configuration.getId());
            }
        }
        configurationsRespIds = new ArrayList<Integer>();
        if (confsResp != null) {
            for (AbstractConfiguration configuration : confsResp) {
                addAndConvertConfiguration(configurations, configuration);
                configurationsRespIds.add(configuration.getId());
            }
        }

    }

    private List<AbstractConfiguration> getConfigurations(
            net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance testInstance, RoleInTest roleInTest,
            SystemInSession systemInSession) {
        List<AbstractConfiguration> confs = new ArrayList<AbstractConfiguration>();

        if (roleInTest != null) {
            TestInstanceParticipants tip = TestInstanceParticipants
                    .getTestInstanceParticipantsByTestInstanceBySiSByRoleInTest(testInstance, systemInSession,
                            roleInTest);
            if (tip != null) {
                Actor actor = tip.getActorIntegrationProfileOption().getActorIntegrationProfile().getActor();
                confs = AbstractConfiguration.listAllConfigurationsForAnActorAndASystem(actor, systemInSession, null);
            }
        }
        return confs;
    }

    public List<Integer> getConfigurationsInitIds() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getConfigurationsInitIds");
        }
        return configurationsInitIds;
    }

    public void setConfigurationsInitIds(List<Integer> configurationsInitIds) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setConfigurationsInitIds");
        }
        this.configurationsInitIds = configurationsInitIds;
    }

    public List<Integer> getConfigurationsRespIds() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getConfigurationsRespIds");
        }
        return configurationsRespIds;
    }

    public void setConfigurationsRespIds(List<Integer> configurationsRespIds) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setConfigurationsRespIds");
        }
        this.configurationsRespIds = configurationsRespIds;
    }

    public String getInitiator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInitiator");
        }
        return initiator;
    }

    public void setInitiator(String initiator) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setInitiator");
        }
        this.initiator = initiator;
    }

    public String getResponder() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getResponder");
        }
        return responder;
    }

    public void setResponder(String responder) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setResponder");
        }
        this.responder = responder;
    }

    public Integer getRoleInTestInitId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRoleInTestInitId");
        }
        return roleInTestInitId;
    }

    public void setRoleInTestInitId(Integer roleInTestInitId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setRoleInTestInitId");
        }
        this.roleInTestInitId = roleInTestInitId;
    }

    public Integer getRoleInTestRespId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRoleInTestRespId");
        }
        return roleInTestRespId;
    }

    public void setRoleInTestRespId(Integer roleInTestRespId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setRoleInTestRespId");
        }
        this.roleInTestRespId = roleInTestRespId;
    }

    public String getStatus() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getStatus");
        }
        return status;
    }

    public void setStatus(String status) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setStatus");
        }
        this.status = status;
    }

    public String getStatus2() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getStatus2");
        }
        return status2;
    }

    public void setStatus2(String status2) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setStatus2");
        }
        this.status2 = status2;
    }

    public Integer getTestStepId() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestStepId");
        }
        return testStepId;
    }

    public void setTestStepId(Integer testStepId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestStepId");
        }
        this.testStepId = testStepId;
    }

}
