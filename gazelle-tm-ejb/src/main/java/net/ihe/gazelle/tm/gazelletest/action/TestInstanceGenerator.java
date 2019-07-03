package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOption;
import net.ihe.gazelle.tf.model.IntegrationProfileOption;
import net.ihe.gazelle.tm.gazelletest.model.definition.*;
import net.ihe.gazelle.tm.gazelletest.model.instance.*;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.users.model.Institution;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author abderrazek boufahja
 */
public class TestInstanceGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(TestInstanceGenerator.class);

    /*
     * Random Test Instance generation
     */
    public static void generateTestInstanceForAllSystemsInTestingSession(TestingSession inTestingSession,
                                                                         EntityManager entityManager) {
        Query query = entityManager
                .createQuery("SELECT DISTINCT sIs FROM SystemInSession sIs where sIs.testingSession=:inTestingSession");
        query.setParameter("inTestingSession", inTestingSession);
        List<SystemInSession> systemInSessionList = query.getResultList();
        Collections.sort(systemInSessionList);
        generateTestInstanceForAListOfSystems(systemInSessionList, entityManager);
    }

    public static void generateTestInstanceForAListOfSystems(List<SystemInSession> systemInSessionList,
                                                             EntityManager entityManager) {
        if ((systemInSessionList != null) && (systemInSessionList.size() > 0)) {
            for (SystemInSession systemInSession : systemInSessionList) {

                generateRandomlyTestInstanceForASystemInSession(systemInSession, entityManager);
            }
        }
    }

    public static void generateRandomlyTestInstanceForSelectedInstitution(Institution selectedInstitution,
                                                                          TestingSession activatedTestingSession, EntityManager em) {
        if (selectedInstitution != null) {
            List<SystemInSession> systemInSessionList = getSystemListByInstitution(selectedInstitution,
                    activatedTestingSession, em);
            Collections.sort(systemInSessionList);
            generateTestInstanceForAListOfSystems(systemInSessionList, em);
        }
    }

    public static List<SystemInSession> getSystemListByInstitution(Institution inInstitution,
                                                                   TestingSession activatedTestingSession, EntityManager em) {
        if ((inInstitution != null) && (activatedTestingSession != null)) {
            List<SystemInSession> systemInSessionList = SystemInSession.getSystemsInSessionForCompanyForSession(em,
                    inInstitution, activatedTestingSession);
            Collections.sort(systemInSessionList);
            if (systemInSessionList.size() > 0) {
                return systemInSessionList;
            }
        }
        return null;
    }

    public static void generateRandomlyTestInstanceForASystemInSession(SystemInSession inSystemInSession,
                                                                       EntityManager entityManager) {
        List<Status> statusList = new ArrayList<Status>();
        statusList.add(Status.STARTED);
        statusList.add(Status.COMPLETED);
        statusList.add(Status.VERIFIED);
        List<TestType> testTypes = inSystemInSession.getTestingSession().getTestTypes();
        List<TestRoles> testRolesList = TestRoles.getTestsFiltered(inSystemInSession.getSystem(), null, null, null,
                null, null, testTypes, null, null,
                TestStatus.getStatusByKeyword(entityManager, TestStatus.STATUS_READY_STRING), true);

        if (testRolesList != null) {
            Collections.sort(testRolesList);
            for (TestRoles testRoles : testRolesList) {
                for (Status status : statusList) {
                    int testInstanceParticipantsSize = getTestInstanceParticipantsSizeByRoleInTestBySystemInSessionByStatus(
                            inSystemInSession, testRoles, status, entityManager);
                    if (testInstanceParticipantsSize == 0) {
                        generateTestInstanceForASystemInSession(inSystemInSession, testRoles, status, entityManager);
                    }
                }
            }
        }
    }

    public static Integer getTestInstanceParticipantsSizeByRoleInTestBySystemInSessionByStatus(
            SystemInSession inSystemInSession, TestRoles inTestRoles, Status inStatus, EntityManager entityManager) {
        Query query = entityManager.createQuery("SELECT count(DISTINCT tip) FROM TestInstanceParticipants tip "
                + "WHERE tip.roleInTest= :inRoleInTest " + "AND tip.testInstance.test= :inTest "
                + "AND tip.systemInSessionUser.systemInSession= :inSystemInSession "
                + "AND tip.testInstance.lastStatus= :inStatus");
        query.setParameter("inRoleInTest", inTestRoles.getRoleInTest());
        query.setParameter("inTest", inTestRoles.getTest());
        query.setParameter("inSystemInSession", inSystemInSession);
        query.setParameter("inStatus", inStatus);
        return ((Long) query.getSingleResult()).intValue();
    }

    public static void generateTestInstanceForASystemInSession(SystemInSession inSystemInSession,
                                                               TestRoles inTestRoles, Status inStatus, EntityManager entityManager) {
        TestInstance testInstance = new TestInstance();
        testInstance.setTest(inTestRoles.getTest());
        testInstance.setLastStatus(inStatus);
        testInstance.setTestingSession(inSystemInSession.getTestingSession());
        testInstance = persistTestInstance(testInstance);
        persistANewTestInstanceParticipants(testInstance, inSystemInSession, inTestRoles, entityManager);
        List<TestRoles> testRolesList = TestRoles.getTestRolesListForATest(inTestRoles.getTest());
        testRolesList.remove(inTestRoles);
        for (TestRoles testRoles : testRolesList) {
            List<SystemInSession> systemInSessionList = RoleInTest
                    .getSystemInSessionByRoleInTestByTestingSessionBySISStatus(testRoles.getRoleInTest(),
                            TestingSession.getSelectedTestingSession(), null, true);
            if ((systemInSessionList != null) && (systemInSessionList.size() > 0)) {
                systemInSessionList.remove(inSystemInSession);
                if (systemInSessionList.size() > testRoles.getCardMax()) {
                    for (int i = 0; i < (testRoles.getCardMax() + 1); i++) {
                        persistANewTestInstanceParticipants(testInstance, systemInSessionList.get(i), testRoles,
                                entityManager);
                    }
                } else {
                    for (SystemInSession systemInSession : systemInSessionList) {
                        persistANewTestInstanceParticipants(testInstance, systemInSession, testRoles, entityManager);
                    }
                }
            }
        }
        List<TestSteps> testStepsList = inTestRoles.getTest().getTestStepsList();
        List<TestStepsInstance> testStepsInstanceList = new ArrayList<TestStepsInstance>();
        if ((testStepsList != null) && (testStepsList.size() > 0)) {
            for (TestSteps testSteps : testStepsList) {
                if (testSteps.getTestRolesInitiator().equals(testSteps.getTestRolesResponder())) {
                    List<SystemInSession> systemInSessionList = TestInstanceParticipants
                            .getSystemInSessionListByTestInstanceByRoleInTest(testInstance, testSteps
                                    .getTestRolesInitiator().getRoleInTest());
                    if ((systemInSessionList != null) && (systemInSessionList.size() > 0)) {
                        for (SystemInSession systemInSession : systemInSessionList) {
                            testStepsInstanceList.add(persistTestStepsInstance(testSteps, systemInSession,
                                    systemInSession));
                        }
                    }
                } else {
                    List<SystemInSession> systemInSessionInitiatorList = TestInstanceParticipants
                            .getSystemInSessionListByTestInstanceByRoleInTest(testInstance, testSteps
                                    .getTestRolesInitiator().getRoleInTest());
                    List<SystemInSession> systemInSessionResponderList = TestInstanceParticipants
                            .getSystemInSessionListByTestInstanceByRoleInTest(testInstance, testSteps
                                    .getTestRolesResponder().getRoleInTest());
                    if ((systemInSessionInitiatorList != null) && (systemInSessionResponderList != null)
                            && (systemInSessionInitiatorList.size() > 0) && (systemInSessionResponderList.size() > 0)) {
                        for (SystemInSession intiator : systemInSessionInitiatorList) {
                            for (SystemInSession responder : systemInSessionResponderList) {
                                testStepsInstanceList.add(persistTestStepsInstance(testSteps, intiator, responder));
                            }
                        }
                    }
                }
            }
        }
        testInstance.setTestStepsInstanceList(testStepsInstanceList);
        entityManager.merge(testInstance);
        entityManager.setFlushMode(FlushModeType.COMMIT);
        entityManager.flush();
    }

    private static TestStepsInstance persistTestStepsInstance(TestSteps inTestSteps, SystemInSession initiator,
                                                              SystemInSession responder) {
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        TestStepsInstance testStepsInstance = new TestStepsInstance();
        testStepsInstance.setTestSteps(inTestSteps);
        testStepsInstance.setTestStepsInstanceStatus(TestStepsInstanceStatus.getSTATUS_ACTIVATED());
        AuditReader reader = AuditReaderFactory.get(entityManager);

        List<Number> revNumbers = reader.getRevisions(TestSteps.class, testStepsInstance.getTestSteps().getId());
        if (revNumbers != null) {
            if (revNumbers.size() > 0) {
                testStepsInstance.setTestStepsVersion((Integer) revNumbers.get(revNumbers.size() - 1));
            } else {
                LOG.error("------a problem on the revNumbers of teststeps  2588: "
                        + testStepsInstance.getTestSteps().getId());
            }
        } else {
            LOG.error("------ there are no revNumbers for tespsteps 2588: " + testStepsInstance.getTestSteps().getId());
        }

        testStepsInstance.setSystemInSessionInitiator(initiator);
        testStepsInstance.setSystemInSessionResponder(responder);
        testStepsInstance = entityManager.merge(testStepsInstance);
        entityManager.flush();
        return testStepsInstance;
    }

    private static TestInstanceParticipants persistANewTestInstanceParticipants(TestInstance inTestInstance,
                                                                                SystemInSession inSystemInSession, TestRoles inTestRoles,
                                                                                EntityManager entityManager) {
        TestInstanceParticipants testInstanceParticipants = new TestInstanceParticipants();
        testInstanceParticipants.setTestInstance(inTestInstance);
        testInstanceParticipants.setRoleInTest(inTestRoles.getRoleInTest());
        SystemInSessionUser systemInSessionUser = SystemInSessionUser.getSystemInSessionUserBySystemInSessionByUser(
                inSystemInSession, inSystemInSession.getSystem().getOwnerUser());
        if (systemInSessionUser == null) {
            systemInSessionUser = new SystemInSessionUser(inSystemInSession, inSystemInSession.getSystem()
                    .getOwnerUser());
            systemInSessionUser = entityManager.merge(systemInSessionUser);
        }
        testInstanceParticipants.setSystemInSessionUser(systemInSessionUser);
        ActorIntegrationProfileOption actorIntegrationProfileOption = getActorIntegrationProfileOptionByTestRolesBySystemInSession(
                inSystemInSession, inTestRoles);
        testInstanceParticipants.setActorIntegrationProfileOption(actorIntegrationProfileOption);
        testInstanceParticipants = entityManager.merge(testInstanceParticipants);
        entityManager.flush();
        entityManager.getTransaction().commit();
        return testInstanceParticipants;
    }

    private static TestInstance persistTestInstance(TestInstance testInstance) {
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
            entityManager.getTransaction().commit();
            return testInstance;
        }
        return null;
    }

    private static ActorIntegrationProfileOption getActorIntegrationProfileOptionByTestRolesBySystemInSession(
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

}
