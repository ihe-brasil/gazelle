package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.filter.list.GazelleListDataModel;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.simulator.ws.GazelleSimulatorManagerWSServiceStub;
import net.ihe.gazelle.simulator.ws.OidConfiguration;
import net.ihe.gazelle.simulator.ws.SetTestPartnerConfigurations;
import net.ihe.gazelle.simulator.ws.SetTestPartnerConfigurationsE;
import net.ihe.gazelle.tf.model.Actor;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOption;
import net.ihe.gazelle.tf.model.IntegrationProfile;
import net.ihe.gazelle.tf.model.IntegrationProfileOption;
import net.ihe.gazelle.tm.configurations.model.OIDSystemAssignment;
import net.ihe.gazelle.tm.gazelletest.bean.RoleInTestSystemInSession;
import net.ihe.gazelle.tm.gazelletest.model.definition.*;
import net.ihe.gazelle.tm.gazelletest.model.instance.*;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.*;
import net.ihe.gazelle.tm.tee.execution.TEEManagerLocal;
import net.ihe.gazelle.tm.tee.model.TestInstanceExecutionStatus;
import net.ihe.gazelle.tm.tee.model.TestInstanceExecutionStatusEnum;
import net.ihe.gazelle.tm.tee.model.TestStepInstanceExecutionStatus;
import net.ihe.gazelle.tm.tee.model.TestStepInstanceExecutionStatusEnum;
import net.ihe.gazelle.tm.tee.status.dao.TestInstanceExecutionStatusDAO;
import net.ihe.gazelle.tm.tee.status.dao.TestStepsInstanceExecutionStatusDAO;
import net.ihe.gazelle.users.model.Institution;
import net.ihe.gazelle.users.model.User;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.security.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Name("startTestInstance")
@Scope(ScopeType.PAGE)
public class StartTestInstance implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(StartTestInstance.class);

    private static final long serialVersionUID = 877314959585601692L;

    private boolean showEditParticipantsPanel = false;

    private TestRoles selectedTestRoles;

    private List<SystemInSession> systemInSessionList;

    private List<RoleInTestSystemInSession> roleInTestSystemInSessionList;

    private TestInstanceParticipants selectedTestInstanceParticipants;

    private List<SimulatorInSession> simulatorInSessionList;

    private TestInstance selectedTestInstance;

    private SystemInSession selectedSystemInSession;

    public SystemInSession getSelectedSystemInSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedSystemInSession");
        }
        return selectedSystemInSession;
    }

    public void setSelectedSystemInSession(SystemInSession selectedSystemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedSystemInSession");
        }
        this.selectedSystemInSession = selectedSystemInSession;
    }

    public boolean isShowEditParticipantsPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isShowEditParticipantsPanel");
        }
        return showEditParticipantsPanel;
    }

    public void setShowEditParticipantsPanel(boolean showEditParticipantsPanel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setShowEditParticipantsPanel");
        }
        this.showEditParticipantsPanel = showEditParticipantsPanel;
    }

    public TestInstance getSelectedTestInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestInstance");
        }
        return selectedTestInstance;
    }

    public void setSelectedTestInstance(TestInstance selectedTestInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestInstance");
        }
        this.selectedTestInstance = selectedTestInstance;
    }

    public TestRoles getSelectedTestRoles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestRoles");
        }
        return selectedTestRoles;
    }

    public void setSelectedTestRoles(TestRoles selectedTestRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestRoles");
        }
        this.selectedTestRoles = selectedTestRoles;
    }

    public List<SimulatorInSession> getSimulatorInSessionList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSimulatorInSessionList");
        }
        if ((selectedTestInstanceParticipants != null) && (selectedTestRoles != null)) {
            simulatorInSessionList = new ArrayList<SimulatorInSession>();
            if (roleInTestSystemInSessionList != null) {
                for (RoleInTestSystemInSession roleInTestSystemInSession : roleInTestSystemInSessionList) {
                    if ((roleInTestSystemInSession.getRoleInTest().equals(selectedTestRoles.getRoleInTest()))
                            && (!roleInTestSystemInSession.getSystemInSession().equals(
                            selectedTestInstanceParticipants.getSystemInSessionUser().getSystemInSession()))) {
                        if (roleInTestSystemInSession.getSystemInSession() instanceof SimulatorInSession) {
                            simulatorInSessionList.add(SimulatorInSession.class.cast(roleInTestSystemInSession
                                    .getSystemInSession()));
                        }
                    }
                }
            }
        } else {
            simulatorInSessionList = new ArrayList<SimulatorInSession>();
        }
        return simulatorInSessionList;
    }

    public void setSimulatorInSessionList(List<SimulatorInSession> simulatorInSessionList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSimulatorInSessionList");
        }
        this.simulatorInSessionList = simulatorInSessionList;
    }

    public TestInstanceParticipants getSelectedTestInstanceParticipants() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTestInstanceParticipants");
        }
        return selectedTestInstanceParticipants;
    }

    public void setSelectedTestInstanceParticipants(TestInstanceParticipants selectedTestInstanceParticipants) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTestInstanceParticipants");
        }
        this.selectedTestInstanceParticipants = selectedTestInstanceParticipants;
    }

    public void initializeTestInstanceParticipantsCreation(SystemInSession inSystemInSession, TestRoles inTestRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initializeTestInstanceParticipantsCreation");
        }
        TestInstance testInstance = new TestInstance();
        testInstance.setContainsSameSystemsOrFromSameCompany(false);
        testInstance.setLastStatus(Status.getSTATUS_STARTED());
        selectedTestInstanceParticipants = new TestInstanceParticipants();
        if (inSystemInSession != null) {
            User loginUser = User.loggedInUser();
            SystemInSessionUser systemInSessionUser = SystemInSessionUser
                    .getSystemInSessionUserBySystemInSessionByUser(inSystemInSession, loginUser);
            if (systemInSessionUser == null) {
                systemInSessionUser = new SystemInSessionUser(inSystemInSession, loginUser);
            }
            selectedTestInstanceParticipants.setSystemInSessionUser(systemInSessionUser);
        }
        if (inTestRoles != null) {
            testInstance.setTest(inTestRoles.getTest());
            selectedTestInstanceParticipants.setRoleInTest(inTestRoles.getRoleInTest());
        }
        selectedTestInstanceParticipants.setTestInstance(testInstance);
    }

    public List<RoleInTestSystemInSession> getRoleInTestSystemInSessionList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRoleInTestSystemInSessionList");
        }
        return roleInTestSystemInSessionList;
    }

    public void setRoleInTestSystemInSessionList(List<RoleInTestSystemInSession> roleInTestSystemInSessionList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setRoleInTestSystemInSessionList");
        }
        this.roleInTestSystemInSessionList = roleInTestSystemInSessionList;
    }

    public void initTestInstanceParticipantsManagement() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initTestInstanceParticipantsManagement");
        }
        Integer sisId = (Integer) Contexts.getSessionContext().get("startTestInstanceSISid");
        Integer trId = (Integer) Contexts.getSessionContext().get("startTestInstanceTRid");

        EntityManager entityManager = EntityManagerService.provideEntityManager();

        SystemInSession sis = entityManager.find(SystemInSession.class, sisId);
        TestRoles tr = entityManager.find(TestRoles.class, trId);

        initializeTestInstanceParticipantsCreation(sis, tr);
        selectedSystemInSession = sis;
        selectedTestRoles = null;
        systemInSessionList = null;
        roleInTestSystemInSessionList = new ArrayList<RoleInTestSystemInSession>();
        roleInTestSystemInSessionList.add(new RoleInTestSystemInSession(selectedTestInstanceParticipants
                .getRoleInTest(), selectedTestInstanceParticipants.getSystemInSessionUser().getSystemInSession()));

        autoSelectPartnerSystemForRoleIfPossible();
    }

    public List<SystemInSession> getSystemInSessionList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemInSessionList");
        }
        if ((selectedTestInstanceParticipants != null) && (selectedTestRoles != null)) {
            systemInSessionList = new ArrayList<SystemInSession>();
            if (roleInTestSystemInSessionList != null) {
                for (RoleInTestSystemInSession roleInTestSystemInSession : roleInTestSystemInSessionList) {

                    if ((roleInTestSystemInSession.getRoleInTest().equals(selectedTestRoles.getRoleInTest()))
                            && (!roleInTestSystemInSession.getSystemInSession().equals(
                            selectedTestInstanceParticipants.getSystemInSessionUser().getSystemInSession()))) {
                        addSiSinSystemInSessionList(roleInTestSystemInSession);
                    }
                }
            }
        } else {
            systemInSessionList = new ArrayList<SystemInSession>();
        }
        Collections.sort(systemInSessionList);
        return systemInSessionList;
    }

    public void setSystemInSessionList(List<SystemInSession> systemInSessionList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSystemInSessionList");
        }
        this.systemInSessionList = systemInSessionList;
    }

    private void addSiSinSystemInSessionList(RoleInTestSystemInSession roleInTestSystemInSession) {
        if (roleInTestSystemInSession.getRoleInTest().getIsRolePlayedByATool()
                && roleInTestSystemInSession.getSystemInSession().getSystem().getIsTool()) {
            systemInSessionList.add(roleInTestSystemInSession.getSystemInSession());
        } else if (!(roleInTestSystemInSession.getSystemInSession() instanceof SimulatorInSession)
                && roleInTestSystemInSession.getSystemInSession().getAcceptedToSession()) {
            systemInSessionList.add(roleInTestSystemInSession.getSystemInSession());
        }
    }

    public void setupSelectedTestInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setupSelectedTestInstance");
        }
        if (selectedTestInstanceParticipants != null) {
            selectedTestInstance = selectedTestInstanceParticipants.getTestInstance();
            selectedTestInstance.setProxyUsed(TestingSession.getSelectedTestingSession().getIsProxyUseEnabled());
            selectedTestInstance.setTestingSession(TestingSession.getSelectedTestingSession());
            selectedTestInstance = persistTestInstance(selectedTestInstance);
            selectedTestInstanceParticipants.setTestInstance(selectedTestInstance);
            if (selectedTestInstance == null) {
                LOG.error("Manager failed to persist test instance");
            }
        }
    }

    private TestInstance persistTestInstance(TestInstance testInstance) {
        if (testInstance != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            AuditReader reader = AuditReaderFactory.get(entityManager);
            List<Number> revNumbers = reader.getRevisions(Test.class, testInstance.getTest().getId());
            if (revNumbers != null) {
                if (revNumbers.size() > 0) {
                    testInstance.setTestVersion((Integer) revNumbers.get(revNumbers.size() - 1));
                } else {
                    LOG.error("------a problem on the revNumbers : " + testInstance.getTest().getKeyword());
                }
            } else {
                LOG.error("------ there are no revNumbers for : " + testInstance.getTest().getKeyword());
            }
            testInstance = entityManager.merge(testInstance);
            entityManager.flush();
            return testInstance;
        }
        return null;
    }

    public ActorIntegrationProfileOption getActorIntegrationProfileOptionByTestRolesBySystemInSession(
            SystemInSession inSystemInSession, TestRoles inTestRoles) {
        if ((inSystemInSession != null) && (inTestRoles != null)) {
            List<ActorIntegrationProfileOption> actorIntegrationProfileOptions = TestRoles
                    .getActorIntegrationProfileOptionByTestRolesBySystemInSession(inSystemInSession, inTestRoles);
            if (actorIntegrationProfileOptions == null) {
                actorIntegrationProfileOptions = TestRoles
                        .getActorIntegrationProfileOptionByTestRolesBySystemInSessionForAllTestParticipants(
                                inSystemInSession, inTestRoles);
            }
            if (actorIntegrationProfileOptions != null) {
                if (actorIntegrationProfileOptions.size() == 1) {
                    return actorIntegrationProfileOptions.get(0);
                }
                for (ActorIntegrationProfileOption actorIntegrationProfileOption : actorIntegrationProfileOptions) {
                    if (actorIntegrationProfileOption.getIntegrationProfileOption().equals(
                            IntegrationProfileOption.getNoneOption())) {
                        return actorIntegrationProfileOption;
                    }
                }
                return actorIntegrationProfileOptions.get(0);
            }
        }
        return null;
    }

    private void addSystemInSessionToSelectedTest(SystemInSession inSystemInSession) {
        TestInstanceParticipants testInstanceParticipants = new TestInstanceParticipants();
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        if (selectedTestInstanceParticipants != null) {
            if (selectedTestInstanceParticipants.getTestInstance() != null) {
                testInstanceParticipants.setTestInstance(selectedTestInstanceParticipants.getTestInstance());
            }
        }
        if (selectedTestRoles != null) {
            testInstanceParticipants.setRoleInTest(selectedTestRoles.getRoleInTest());
        }

        SystemInSessionUser systemInSessionUser = SystemInSessionUser.getSystemInSessionUserBySystemInSessionByUser(
                inSystemInSession, inSystemInSession.getSystem().getOwnerUser());
        if (systemInSessionUser == null) {
            systemInSessionUser = new SystemInSessionUser(inSystemInSession, inSystemInSession.getSystem()
                    .getOwnerUser());
            systemInSessionUser = entityManager.merge(systemInSessionUser);
        }
        testInstanceParticipants.setSystemInSessionUser(systemInSessionUser);
        testInstanceParticipants.setStatus(TestInstanceParticipantsStatus.getSTATUS_UNVERIFIED());

        ActorIntegrationProfileOption actorIntegrationProfileOption = getActorIntegrationProfileOptionByRoleInTestBySystemInSession(
                inSystemInSession, selectedTestRoles.getRoleInTest());

        testInstanceParticipants.setActorIntegrationProfileOption(actorIntegrationProfileOption);
        entityManager.merge(testInstanceParticipants);
    }

    public void addTestInstanceParticipantsList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addTestInstanceParticipantsList");
        }
        if ((systemInSessionList != null) && (selectedTestInstanceParticipants != null)) {
            TestInstanceParticipants.deleteTestInstanceParticipantsByRoleInTestByTestInstance(
                    selectedTestInstanceParticipants.getTestInstance(), selectedTestRoles.getRoleInTest());
            for (SystemInSession systemInSession : systemInSessionList) {
                addSystemInSessionToSelectedTest(systemInSession);
            }
        }
        selectedTestRoles = null;
    }

    public String startTestInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("startTestInstance");
        }

        setupSelectedTestInstance();

        List<TestRoles> testRolesList = TestRoles.getTestRolesListForATest(selectedTestInstanceParticipants
                .getTestInstance().getTest());
        if (testRolesList != null) {
            for (TestRoles testRoles : testRolesList) {
                if (roleInTestSystemInSessionList != null) {
                    selectedTestRoles = testRoles;
                    systemInSessionList = new ArrayList<SystemInSession>();
                    for (RoleInTestSystemInSession roleInTestSystemInSession : roleInTestSystemInSessionList) {
                        if (roleInTestSystemInSession.getRoleInTest().equals(selectedTestRoles.getRoleInTest())) {
                            systemInSessionList.add(roleInTestSystemInSession.getSystemInSession());
                        }
                    }
                    addTestInstanceParticipantsList();
                }
            }
        }

        generateTestStepsInstanceForSelectedTestInstance();

        // added default execution status for a newly created test instance
        if (selectedTestInstance.getExecutionStatus() == null) {
            TestInstanceExecutionStatus defaultExecutionStatus = new TestInstanceExecutionStatusDAO()
                    .getTestInstanceExecutionStatusByEnum(TestInstanceExecutionStatusEnum.ACTIVE);
            selectedTestInstance.setExecutionStatus(defaultExecutionStatus);
        }

        if (selectedTestInstance.getProxyUsed()) {
            ProxyStartTestInstanceJobInterface proxyStartTestInstanceJob = new ProxyStartTestInstanceJob();
            proxyStartTestInstanceJob.proxyStartTestInstanceJob(selectedTestInstance.getId());
        }

        startSimulators();
        if (selectedTestInstanceParticipants.getTestInstance().isOrchestrated()) {
            callTestEngine3();
        }

        String returnUrl;

        if (selectedTestInstance.getTest().isTypeInteroperability()) {
            TEEManagerLocal teeManager = (TEEManagerLocal) Component.getInstance("teeManager");
            teeManager.activateFirstTestStepInstance(getSelectedTestInstance(), getSelectedSystemInSession());
            returnUrl = "/testingITB/test/test/TestInstance.seam?id=" + selectedTestInstance.getId();
        } else {
            returnUrl = "/testing/test/test/TestInstance.seam?id=" + selectedTestInstance.getId();
        }
        return returnUrl;
    }

    public ActorIntegrationProfileOption getActorIntegrationProfileOptionByRoleInTestBySystemInSession(
            SystemInSession inSystemInSession, RoleInTest inRoleInTest) {
        // TODO Just return the first element because database can't contain a list for the moment
        return getActorIntegrationProfileOptionsListByRoleInTestBySystemInSession(inSystemInSession, inRoleInTest).get(
                0);
    }

    public List<ActorIntegrationProfileOption> getActorIntegrationProfileOptionsListByRoleInTestBySystemInSession(
            SystemInSession inSystemInSession, RoleInTest inRoleInTest) {
        if ((inSystemInSession != null) && (inRoleInTest != null)) {
            List<ActorIntegrationProfileOption> actorIntegrationProfileOptionsList = TestRoles
                    .getActorIntegrationProfileOptionByRoleInTestBySystemInSession(inSystemInSession, inRoleInTest);
            List<ActorIntegrationProfileOption> actorIntegrationProfileOptionsResult = new ArrayList<ActorIntegrationProfileOption>();
            if (actorIntegrationProfileOptionsList == null) {
                actorIntegrationProfileOptionsList = TestRoles
                        .getActorIntegrationProfileOptionByRoleInTestBySystemInSessionForAllTestParticipants(
                                inSystemInSession, inRoleInTest);
            }
            if (actorIntegrationProfileOptionsList != null) {
                if (actorIntegrationProfileOptionsList.size() == 1) {
                    actorIntegrationProfileOptionsResult = actorIntegrationProfileOptionsList;
                } else {
                    List<ActorIntegrationProfileOption> actorIntegrationProfileOptionsNone = new ArrayList<ActorIntegrationProfileOption>();
                    List<ActorIntegrationProfileOption> actorIntegrationProfileOptionsOther = new ArrayList<ActorIntegrationProfileOption>();
                    for (SystemActorProfiles sap : inSystemInSession.getSystem().getSystemActorProfiles()) {
                        for (ActorIntegrationProfileOption aipo : actorIntegrationProfileOptionsList) {
                            if (aipo.getIntegrationProfileOption().equals(IntegrationProfileOption.getNoneOption())
                                    && aipo.equals(sap.getActorIntegrationProfileOption())) {
                                actorIntegrationProfileOptionsNone.add(aipo);
                            } else if (aipo.getIntegrationProfileOption().equals(
                                    sap.getActorIntegrationProfileOption().getIntegrationProfileOption())
                                    && aipo.equals(sap.getActorIntegrationProfileOption())) {
                                actorIntegrationProfileOptionsOther.add(aipo);
                            }
                        }
                    }
                    actorIntegrationProfileOptionsResult = actorIntegrationProfileOptionsNone;
                    if (actorIntegrationProfileOptionsNone.isEmpty()) {
                        actorIntegrationProfileOptionsResult = actorIntegrationProfileOptionsOther;
                    }
                }
                return actorIntegrationProfileOptionsResult;
            }
        }
        return null;
    }

    private void startSimulators() {
        if (selectedTestInstance != null) {
            List<TestInstanceParticipants> testInstanceParticipantsList = TestInstanceParticipants
                    .getTestInstanceParticipantsListForATest(selectedTestInstance);
            for (TestInstanceParticipants testInstanceParticipants : testInstanceParticipantsList) {
                SystemInSession sIs = testInstanceParticipants.getSystemInSessionUser().getSystemInSession();
                if (SimulatorInSession.class.isInstance(sIs) && Simulator.class.isInstance(sIs.getSystem())) {
                    try {
                        GazelleSimulatorManagerWSServiceStub client = new GazelleSimulatorManagerWSServiceStub(
                                Simulator.class.cast(sIs.getSystem()).getUrl());

                        String testInstanceId = selectedTestInstance.getTestInstanceIdForSimulator();
                        boolean startRes = ClientCommonSimulator.startTestInstance(testInstanceId, Simulator.class
                                .cast(sIs.getSystem()).getUrl());

                        if (startRes) {
                            SetTestPartnerConfigurations setTestPartnerConfigurations = new SetTestPartnerConfigurations();
                            setTestPartnerConfigurations.setTestInstanceId(testInstanceId);
                            for (TestInstanceParticipants testInstanceParticipants2 : testInstanceParticipantsList) {
                                if (!testInstanceParticipants2.equals(testInstanceParticipants)) {
                                    net.ihe.gazelle.simulator.ws.TestInstanceParticipants simulatorTestInstanceParticipants = new net.ihe.gazelle
                                            .simulator.ws.TestInstanceParticipants();
                                    ActorIntegrationProfileOption actorIntegrationProfileOption =
                                            getActorIntegrationProfileOptionByRoleInTestBySystemInSession(
                                            testInstanceParticipants2.getSystemInSessionUser().getSystemInSession(),
                                            testInstanceParticipants2.getRoleInTest());
                                    net.ihe.gazelle.simulator.ws.ActorIntegrationProfileOption simulatorActorIntegrationProfileOption =
                                            Gazelle2SimulatorClassConverter
                                            .convertActorIntegrationProfileOption(actorIntegrationProfileOption);

                                    // System Info
                                    net.ihe.gazelle.simulator.ws.System system = new net.ihe.gazelle.simulator.ws.System();
                                    system.setKeyword(testInstanceParticipants2.getSystemInSessionUser()
                                            .getSystemInSession().getSystem().getKeyword());
                                    List<Institution> institutions = System
                                            .getInstitutionsForASystem(testInstanceParticipants2
                                                    .getSystemInSessionUser().getSystemInSession().getSystem());
                                    StringBuffer institutionKeyword = new StringBuffer();
                                    institutionKeyword.append(institutions.get(0).getKeyword());
                                    for (int i = 1; i < institutions.size(); i++) {
                                        institutionKeyword.append("-").append(institutions.get(i).getKeyword());
                                    }
                                    system.setInstitutionKeyword(institutionKeyword.toString());
                                    system.setSystemOwner(testInstanceParticipants2.getSystemInSessionUser()
                                            .getSystemInSession().getSystem().getOwnerUser().getUsername());
                                    simulatorTestInstanceParticipants.setSystem(system);
                                    simulatorTestInstanceParticipants
                                            .setActorIntegrationProfileOption(simulatorActorIntegrationProfileOption);

                                    // add remaining oid
                                    List<OIDSystemAssignment> losa = OIDSystemAssignment
                                            .getOIDSystemAssignmentFiltered(testInstanceParticipants2
                                                            .getSystemInSessionUser().getSystemInSession(), null, null,
                                                    actorIntegrationProfileOption);

                                    for (OIDSystemAssignment oidSystemAssignment : losa) {
                                        OidConfiguration oidconf = Gazelle2SimulatorClassConverter
                                                .convertOIDConfiguration(oidSystemAssignment);
                                        oidconf.setSystem(system);
                                        setTestPartnerConfigurations.addOidList(oidconf);
                                    }

                                    simulatorTestInstanceParticipants
                                            .setServerAipoId(getActorIntegrationProfileOptionByRoleInTestBySystemInSession(
                                                    testInstanceParticipants2.getSystemInSessionUser()
                                                            .getSystemInSession(),
                                                    testInstanceParticipants2.getRoleInTest()).getId().toString());
                                    simulatorTestInstanceParticipants
                                            .setServerTestInstanceParticipantsId(testInstanceParticipants2.getId()
                                                    .toString());
                                    setTestPartnerConfigurations
                                            .addTestInstanceParticipantsList(simulatorTestInstanceParticipants);
                                }
                            }
                            SetTestPartnerConfigurationsE setTestPartnerConfigurationsE = new SetTestPartnerConfigurationsE();
                            setTestPartnerConfigurationsE.setSetTestPartnerConfigurations(setTestPartnerConfigurations);
                            try {
                                client.setTestPartnerConfigurations(setTestPartnerConfigurationsE);
                            } catch (Exception e) {
                                LOG.error("", e);
                            }
                        }
                    } catch (org.apache.axis2.AxisFault e) {
                        LOG.error("", e);
                    } catch (RemoteException e) {
                        LOG.error("", e);
                    }
                }
            }
        }
    }

    public void generateTestStepsInstanceForSelectedTestInstance() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateTestStepsInstanceForSelectedTestInstance");
        }
        if (selectedTestInstanceParticipants != null) {
            EntityManager entityManager = EntityManagerService.provideEntityManager();
            selectedTestInstance = selectedTestInstanceParticipants.getTestInstance();
            TestStepsInstanceStatus statusTobeAppliedToTestSteps = TestStepsInstanceStatus.getSTATUS_DISACTIVATED();

            if (!selectedTestInstance.isOrchestrated()) {
                statusTobeAppliedToTestSteps = TestStepsInstanceStatus.getSTATUS_ACTIVATED();
            }
            List<TestSteps> testStepsList = selectedTestInstance.getTest().getTestStepsList();
            List<TestStepsInstance> testStepsInstanceList = null;
            if (testStepsList != null) {
                testStepsInstanceList = new ArrayList<TestStepsInstance>();
                selectedTestInstance.setTestStepsInstanceList(null);
                for (TestSteps testSteps : testStepsList) {
                    TestRoles testRolesInitiator = testSteps.getTestRolesInitiator();
                    TestRoles testRolesResponder = testSteps.getTestRolesResponder();
                    List<SystemInSession> systemInSessionInitiatorList = TestInstanceParticipants
                            .getSystemInSessionListByTestInstanceByRoleInTest(selectedTestInstance,
                                    testRolesInitiator.getRoleInTest());
                    if (!testRolesInitiator.getRoleInTest().equals(testRolesResponder.getRoleInTest())) {
                        List<SystemInSession> systemInSessionResponderList = TestInstanceParticipants
                                .getSystemInSessionListByTestInstanceByRoleInTest(selectedTestInstance,
                                        testRolesResponder.getRoleInTest());
                        if ((systemInSessionInitiatorList != null) && (systemInSessionResponderList != null)) {
                            for (SystemInSession systemInSessionInitiator : systemInSessionInitiatorList) {
                                for (SystemInSession systemInSessionResponder : systemInSessionResponderList) {
                                    TestStepsInstance testStepsInstance = new TestStepsInstance(testSteps,
                                            systemInSessionInitiator, systemInSessionResponder);
                                    testStepsInstance.setTestStepsInstanceStatus(statusTobeAppliedToTestSteps);

                                    if (selectedTestInstance.getTest().isTypeInteroperability()) {
                                        TestStepInstanceExecutionStatus executionStatusToBeAppliedToTestSteps = new
                                                TestStepsInstanceExecutionStatusDAO()
                                                .getTestStepExecutionStatusByEnum(TestStepInstanceExecutionStatusEnum.INACTIVE);
                                        // setting execution status
                                        testStepsInstance.setExecutionStatus(executionStatusToBeAppliedToTestSteps);
                                    }
                                    AuditReader reader = AuditReaderFactory.get(entityManager);

                                    List<Number> revNumbers = reader.getRevisions(TestSteps.class, testStepsInstance
                                            .getTestSteps().getId());
                                    if (revNumbers != null) {
                                        if (revNumbers.size() > 0) {
                                            testStepsInstance.setTestStepsVersion((Integer) revNumbers.get(revNumbers
                                                    .size() - 1));
                                        } else {
                                            LOG.error("------a problem on the revNumbers of teststeps : "
                                                    + testStepsInstance.getTestSteps().getId());
                                        }
                                    } else {
                                        LOG.error("------ there are no revNumbers for tespsteps: "
                                                + testStepsInstance.getTestSteps().getId());
                                    }
                                    List<ContextualInformation> inputContextualInformations = testSteps
                                            .getInputContextualInformationList();
                                    if (inputContextualInformations != null) {
                                        for (ContextualInformation contextualInformation : inputContextualInformations) {
                                            ContextualInformationInstance contextualInformationInstance = new ContextualInformationInstance();
                                            contextualInformationInstance
                                                    .setContextualInformation(contextualInformation);
                                            contextualInformationInstance.setValue(contextualInformation.getValue());
                                            contextualInformationInstance = entityManager
                                                    .merge(contextualInformationInstance);
                                            testStepsInstance
                                                    .addInputContextualInformationInstance(contextualInformationInstance);
                                        }
                                    }
                                    List<ContextualInformation> outputContextualInformations = testSteps
                                            .getOutputContextualInformationList();
                                    if (outputContextualInformations != null) {
                                        for (ContextualInformation contextualInformation : outputContextualInformations) {
                                            ContextualInformationInstance contextualInformationInstance = new ContextualInformationInstance();
                                            contextualInformationInstance
                                                    .setContextualInformation(contextualInformation);
                                            contextualInformationInstance.setValue(contextualInformation.getValue());
                                            contextualInformationInstance = entityManager
                                                    .merge(contextualInformationInstance);
                                            testStepsInstance
                                                    .addOutputContextualInformationInstance(contextualInformationInstance);
                                        }
                                    }
                                    testStepsInstance = entityManager.merge(testStepsInstance);
                                    entityManager.flush();
                                    testStepsInstanceList.add(testStepsInstance);
                                }
                            }
                        } else {
                            LOG.error("missing one test step in the test instance");
                        }
                    } else {
                        if (systemInSessionInitiatorList != null) {
                            for (SystemInSession systemInSessionInitiator : systemInSessionInitiatorList) {
                                TestStepsInstance testStepsInstance = new TestStepsInstance(testSteps,
                                        systemInSessionInitiator, systemInSessionInitiator);
                                testStepsInstance.setTestStepsInstanceStatus(statusTobeAppliedToTestSteps);

                                if (selectedTestInstance.getTest().isTypeInteroperability()) {
                                    TestStepInstanceExecutionStatus executionStatusToBeAppliedToTestSteps = new TestStepsInstanceExecutionStatusDAO()
                                            .getTestStepExecutionStatusByEnum(TestStepInstanceExecutionStatusEnum.INACTIVE);
                                    // setting execution status of the teststeps involved
                                    testStepsInstance.setExecutionStatus(executionStatusToBeAppliedToTestSteps);
                                }
                                AuditReader reader = AuditReaderFactory.get(entityManager);

                                List<Number> revNumbers = reader.getRevisions(TestSteps.class, testStepsInstance
                                        .getTestSteps().getId());
                                if (revNumbers != null) {
                                    if (revNumbers.size() > 0) {
                                        testStepsInstance.setTestStepsVersion((Integer) revNumbers.get(revNumbers
                                                .size() - 1));
                                    } else {
                                        LOG.error("------a problem on the revNumbers of teststeps : "
                                                + testStepsInstance.getTestSteps().getId());
                                    }
                                } else {
                                    LOG.error("------ there are no revNumbers for tespsteps: "
                                            + testStepsInstance.getTestSteps().getId());
                                }
                                testStepsInstance = entityManager.merge(testStepsInstance);
                                entityManager.flush();
                                testStepsInstanceList.add(testStepsInstance);
                            }
                        } else {
                            LOG.error("missing one test step in the test instance");
                        }
                    }
                }
            }
            selectedTestInstance.setTestStepsInstanceList(testStepsInstanceList);
            selectedTestInstance = entityManager.merge(selectedTestInstance);
            entityManager.flush();
        }
    }

    private void callTestEngine3() {
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        LocalGPClient.startTestInstanceActivating(selectedTestInstance, entityManager);
    }

    public List<SystemInSession> getSystemsByTestRoles(TestRoles inTestRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemsByTestRoles");
        }
        if (inTestRoles != null) {
            List<SystemInSession> systemInSessionList = RoleInTest
                    .getSystemInSessionByRoleInTestByTestingSessionBySISStatus(inTestRoles.getRoleInTest(),
                            TestingSession.getSelectedTestingSession(), null, true);
            if (systemInSessionList != null) {
                if (inTestRoles.getRoleInTest().equals(selectedTestInstanceParticipants.getRoleInTest())) {
                    systemInSessionList.remove(selectedTestInstanceParticipants.getSystemInSessionUser()
                            .getSystemInSession());
                }
                Collections.sort(systemInSessionList);
                return systemInSessionList;
            }
        }
        return new ArrayList<SystemInSession>();
    }

    public List<SystemInSession> getSystemsByTestRolesWithoutSimulator(TestRoles inTestRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemsByTestRolesWithoutSimulator");
        }
        List<SystemInSession> res = new ArrayList<SystemInSession>();
        if (inTestRoles != null) {
            List<SystemInSession> systemInSessionList = RoleInTest
                    .getSystemInSessionByRoleInTestByTestingSessionBySISStatus(inTestRoles.getRoleInTest(),
                            TestingSession.getSelectedTestingSession(), null, true);
            if (systemInSessionList != null) {
                if (inTestRoles.getRoleInTest().equals(selectedTestInstanceParticipants.getRoleInTest())) {
                    systemInSessionList.remove(selectedTestInstanceParticipants.getSystemInSessionUser()
                            .getSystemInSession());
                }
                // Exclude tools
                for (SystemInSession sis : systemInSessionList) {
                    if (!sis.getSystem().getIsTool() && !(sis instanceof SimulatorInSession)) {
                        res.add(sis);
                    }
                }
            }
        }
        if (res != null) {
            Collections.sort(res);
        }
        return res;
    }

    public List<SystemInSession> getSystemsByTestRolesIsATool(TestRoles inTestRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemsByTestRolesIsATool");
        }
        List<SystemInSession> res = new ArrayList<SystemInSession>();
        if (inTestRoles != null) {
            List<SystemInSession> systemInSessionList = RoleInTest
                    .getSystemInSessionByRoleInTestByTestingSessionBySISStatus(inTestRoles.getRoleInTest(),
                            TestingSession.getSelectedTestingSession(), null, true);
            if (systemInSessionList != null) {
                if (inTestRoles.getRoleInTest().equals(selectedTestInstanceParticipants.getRoleInTest())) {
                    systemInSessionList.remove(selectedTestInstanceParticipants.getSystemInSessionUser()
                            .getSystemInSession());
                }
                for (SystemInSession sis : systemInSessionList) {
                    if (sis.getSystem().getIsTool()) {
                        res.add(sis);
                    }
                }
            }
        }
        if (res != null) {
            Collections.sort(res);
        }
        return res;
    }

    public List<RoleInTestSystemInSession> getTestParticipantSystemsByTestRoles(TestRoles inTestRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestParticipantSystemsByTestRoles");
        }
        List<RoleInTestSystemInSession> result = new ArrayList<RoleInTestSystemInSession>();
        for (RoleInTestSystemInSession roleInTestSystemInSession : roleInTestSystemInSessionList) {
            if (roleInTestSystemInSession.getRoleInTest().equals(inTestRoles.getRoleInTest())) {
                result.add(roleInTestSystemInSession);
            }
        }
        return result;
    }

    public void editTestParticipantsForATestRoles(TestRoles inTestRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editTestParticipantsForATestRoles");
        }
        this.selectedTestRoles = inTestRoles;
        setShowEditParticipantsPanel(true);
    }

    public void addSelectedSystemInSessionListForSelectedRole() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addSelectedSystemInSessionListForSelectedRole");
        }
        if (selectedTestRoles != null) {
            int systemListSize = systemInSessionList.size();
            if (selectedTestRoles.getRoleInTest().equals(selectedTestInstanceParticipants.getRoleInTest())) {
                systemListSize += 1;
            }
            if (simulatorInSessionList != null) {
                systemListSize += simulatorInSessionList.size();
            }
            if (systemListSize <= selectedTestRoles.getCardMax()) {
                if (roleInTestSystemInSessionList == null) {
                    roleInTestSystemInSessionList = new ArrayList<RoleInTestSystemInSession>();
                }
                List<RoleInTestSystemInSession> listOfRoleInTestSystemInSessionToDelete = new ArrayList<RoleInTestSystemInSession>();
                for (RoleInTestSystemInSession roleInTestSystemInSession : roleInTestSystemInSessionList) {
                    if (roleInTestSystemInSession.getRoleInTest().equals(selectedTestRoles.getRoleInTest())) {
                        listOfRoleInTestSystemInSessionToDelete.add(roleInTestSystemInSession);
                    }
                }
                for (RoleInTestSystemInSession roleInTestSystemInSession : listOfRoleInTestSystemInSessionToDelete) {
                    roleInTestSystemInSessionList.remove(roleInTestSystemInSession);
                }
                for (SystemInSession systemInSession : systemInSessionList) {
                    roleInTestSystemInSessionList.add(new RoleInTestSystemInSession(selectedTestRoles.getRoleInTest(),
                            systemInSession));
                }

                if (simulatorInSessionList != null) {
                    for (SimulatorInSession simulatorInSession : simulatorInSessionList) {
                        roleInTestSystemInSessionList.add(new RoleInTestSystemInSession(selectedTestRoles
                                .getRoleInTest(), simulatorInSession));
                    }
                }

                if (selectedTestRoles.getRoleInTest().equals(selectedTestInstanceParticipants.getRoleInTest())) {
                    roleInTestSystemInSessionList.add(new RoleInTestSystemInSession(selectedTestInstanceParticipants
                            .getRoleInTest(), selectedTestInstanceParticipants.getSystemInSessionUser()
                            .getSystemInSession()));
                }
                setShowEditParticipantsPanel(false);
            } else {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                        "The number of added systems can not exceed " + selectedTestRoles.getCardMax());
            }
            TestInstance testInstance = selectedTestInstanceParticipants.getTestInstance();
            DuplicateFinder.findDuplicates(testInstance, roleInTestSystemInSessionList, FacesMessages.instance());
        }
    }


    public void removeSystemInSessionFromTestParticipants(RoleInTestSystemInSession roleInTestSystemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeSystemInSessionFromTestParticipants");
        }
        if (roleInTestSystemInSession != null) {
            roleInTestSystemInSessionList.remove(roleInTestSystemInSession);
        }
    }

    public boolean testInstanceShallBeForced() {
        TestInstance testInstance = selectedTestInstanceParticipants.getTestInstance();
        return testInstance.isContainsSameSystemsOrFromSameCompany();
    }

    public boolean checkTestInstanceFeasibility() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("checkTestInstanceFeasibility");
        }
        TestInstance testInstance = selectedTestInstanceParticipants.getTestInstance();
        // GZL-4279: Only an admin or a testing session manager is allowed to start a test where several roles are acted
        // by the same system or by several systems from the same company
        // Now we are adding a preference in the testing session. This may be not enforced (relaxed for the current session)
        Identity identity = Identity.instance();
        boolean OverrideFromTestingSessionPreferences = TestingSession.getSelectedTestingSession().isAllowSameCompanyTesting();
        boolean userAllowedToForceTest = identity.hasRole("admin_role") || identity.hasRole("testing_session_admin_role");
        if (testInstance.isContainsSameSystemsOrFromSameCompany()) {
            if (!userAllowedToForceTest && !OverrideFromTestingSessionPreferences) {
                return false;
            }
        } else {
            List<TestRoles> testRolesList = TestRoles.getTestRolesListForATest(testInstance.getTest());
            for (TestRoles testRoles : testRolesList) {
                int systemListSize = getTestParticipantSystemsByTestRoles(testRoles).size();
                if ((systemListSize < testRoles.getCardMin()) || (systemListSize > testRoles.getCardMax())) {
                    return false;
                } else {
                    continue;
                }
            }
        }
        return true;
    }

    public GazelleListDataModel<TestInstanceParticipants> getTestInstanceParticipantsForSelectedSISForSelectedRoleInTest() {
        List<TestInstanceParticipants> res = TestInstanceParticipants.getTestInstanceParticipantsBySiSByRoleInTest(selectedSystemInSession,
                selectedTestRoles, selectedTestInstanceParticipants.getRoleInTest());
        return new GazelleListDataModel<TestInstanceParticipants>(res);
    }

    public IntegrationProfile getIntegrationProfileByRoleInTestBySystemInSession(
            SystemInSession selectedSystemInSession, RoleInTest inRoleInTest) {
        ActorIntegrationProfileOption actorIntegrationProfileOption = getActorIntegrationProfileOptionByRoleInTestBySystemInSession(
                selectedSystemInSession, inRoleInTest);
        if (actorIntegrationProfileOption != null) {
            return actorIntegrationProfileOption.getActorIntegrationProfile().getIntegrationProfile();
        }
        return null;
    }

    public Actor getActorByRoleInTestBySystemInSession(SystemInSession selectedSystemInSession, RoleInTest inRoleInTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorByRoleInTestBySystemInSession");
        }
        ActorIntegrationProfileOption actorIntegrationProfileOption = getActorIntegrationProfileOptionByRoleInTestBySystemInSession(
                selectedSystemInSession, inRoleInTest);
        if (actorIntegrationProfileOption != null) {
            return actorIntegrationProfileOption.getActorIntegrationProfile().getActor();
        }
        return null;
    }

    public List<SimulatorInSession> getSimulatorsByTestRoles(TestRoles inTestRoles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSimulatorsByTestRoles");
        }
        if (inTestRoles != null) {
            TestingSession ts = TestingSession.getSelectedTestingSession();
            SimulatorInSessionQuery query = new SimulatorInSessionQuery();
            query.testingSession().eq(ts);
            query.system().systemActorProfiles().aipo().testParticipants().roleInTest().testRoles().id()
                    .eq(inTestRoles.getId());
        }
        return null;
    }

    /*
     * This method select a partner system for a specific role if there is just one partner system
     */
    public void autoSelectPartnerSystemForRoleIfPossible() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("autoSelectPartnerSystemForRoleIfPossible");
        }
        List<TestRoles> testRolesList = TestRoles.getTestRolesListForATest(selectedTestInstanceParticipants
                .getTestInstance().getTest());
        getSimulatorInSessionList();

        for (TestRoles tr : testRolesList) {
            List<SystemInSession> SiSList = getSystemsListByTestRoles(tr);
            if (SiSList.size() == 1) {
                setSelectedTestRoles(tr);
                setSystemInSessionList(SiSList);
                addSelectedSystemInSessionListForSelectedRole();
                FacesMessages.instance().add(StatusMessage.Severity.INFO,
                        SiSList.get(0).getSystem().getKeyword()
                                + " is automatically selected has partner (only one partner was available)");
            }
        }
    }

    public List<SystemInSession> getSystemsListByTestRoles(TestRoles tr) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemsListByTestRoles");
        }
        List<SystemInSession> SiSList;
        if (tr.getRoleInTest().getIsRolePlayedByATool()) {
            SiSList = getSystemsByTestRolesIsATool(tr);
        } else {
            SiSList = getSystemsByTestRolesWithoutSimulator(tr);
        }
        return SiSList;
    }
}
