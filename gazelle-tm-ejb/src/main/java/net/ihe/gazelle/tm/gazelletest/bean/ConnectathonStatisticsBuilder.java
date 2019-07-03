package net.ihe.gazelle.tm.gazelletest.bean;

import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOption;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestParticipants;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestRoles;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestType;
import net.ihe.gazelle.tm.gazelletest.model.instance.*;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.*;
import org.apache.commons.collections.map.MultiKeyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.*;

public class ConnectathonStatisticsBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectathonStatisticsBuilder.class);
    private EntityManager entityManager;
    private TestingSession filterTestingSession;
    // List of all results computed, including new ones (created as they don't
    // exist)
    private List<SystemAIPOResultForATestingSession> sapResults = new ArrayList<SystemAIPOResultForATestingSession>();

    // aipo/system -> Map<TestRoles, Map<TestInstanceStatus, Integer>>
    private MultiKeyMap counters = new MultiKeyMap();

    public ConnectathonStatisticsBuilder(TestingSession filterTestingSession) {
        super();
        this.filterTestingSession = filterTestingSession;
        this.entityManager = EntityManagerService.provideEntityManager();
    }

    public void updateStatistics() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateStatistics");
        }
        // First retrieve all SAP with matching result for testing session
        // If result doesn't exist, create it
        createResults();

        // Retrieve all test roles for this testing session
        // Init all counters by SAP/TR
        getTestRoles();

        // Retrieve all test instances and increment counters for each TIP
        countTestInstances();

        // Browse all SAP and compute statistics by retrieving all TR stored in
        // counters (init in getTestRoles())
        updateResults();

    }

    public void createResults() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createResults");
        }
        Set<Integer> sapResultIds = new HashSet<Integer>();

        List<SystemActorProfiles> saps = getSystemActorProfilesForTestingSession();

        for (SystemActorProfiles systemActorProfiles : saps) {
            Set<SystemInSession> sSis = systemActorProfiles.getSystem().getSystemsInSession();
            for (SystemInSession sis : sSis) {
                if (sis.getClass() != SimulatorInSession.class) {
                    LOG.debug("systemActorProfiles id:" + systemActorProfiles.getId() + " system: "
                            + systemActorProfiles.getSystem().getKeyword());
                    SystemAIPOResultForATestingSession sapResult = null;

                    List<SystemAIPOResultForATestingSession> results = systemActorProfiles.getResults();

                    for (SystemAIPOResultForATestingSession result : results) {
                        if (result.getTestSession().getId().equals(filterTestingSession.getId())) {
                            sapResult = result;
                            if (!result.getEnabled()) {
                                sapResult.setEnabled(true);
                                sapResult = entityManager.merge(sapResult);
                                entityManager.flush();
                            }
                        }
                    }
                    if (sapResult == null) {
                        // Double check SAPresult doesn't exist
                        if (getSAPResultFor(systemActorProfiles) == null) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("CREATE result for systemActorProfiles:" + systemActorProfiles.getId() + " System"
                                        + systemActorProfiles.getSystem().getKeyword());
                            }
                            sapResult = new SystemAIPOResultForATestingSession();
                            sapResult.setSystemActorProfile(systemActorProfiles);
                            sapResult.setTestSession(filterTestingSession);
                            sapResult.setComment("");
                            sapResult.setEnabled(true);
                            sapResult = entityManager.merge(sapResult);
                            entityManager.flush();
                        } else {
                            sapResult = getSAPResultFor(systemActorProfiles);
                        }
                    }

                    sapResults.add(sapResult);
                    sapResultIds.add(sapResult.getId());
                }
            }
        }

        entityManager.flush();
        SystemAIPOResultForATestingSessionQuery querySAPResult = new SystemAIPOResultForATestingSessionQuery();
        querySAPResult.testSession().id().eq(filterTestingSession.getId());

        List<SystemAIPOResultForATestingSession> resultsInDB = querySAPResult.getList();
        for (SystemAIPOResultForATestingSession resultInDB : resultsInDB) {
            if (!sapResultIds.contains(resultInDB.getId())) {
                resultInDB.setEnabled(false);
                entityManager.merge(resultInDB);
            }
        }
    }

    /**
     * A systemActorProfile has only one SystemAIPOResultForATestingSession
     *
     * @param systemActorProfiles
     * @return
     */
    private SystemAIPOResultForATestingSession getSAPResultFor(SystemActorProfiles systemActorProfiles) {
        SystemAIPOResultForATestingSessionQuery query = new SystemAIPOResultForATestingSessionQuery();
        query.testSession().id().eq(filterTestingSession.getId());
        query.systemActorProfile().id().eq(systemActorProfiles.getId());
        SystemAIPOResultForATestingSession uniqueResult = query.getUniqueResult();
        return uniqueResult;
    }

    private List<SystemActorProfiles> getSystemActorProfilesForTestingSession() {
        // Retrieve all SystemActorProfiles
        SystemActorProfilesQuery querySAP = new SystemActorProfilesQuery();
        querySAP.system().systemsInSession().testingSession().id().eq(filterTestingSession.getId());
        querySAP.actorIntegrationProfileOption().actorIntegrationProfile().integrationProfile()
                .in(filterTestingSession.getIntegrationProfilesUnsorted());
        List<SystemActorProfiles> saps = querySAP.getList();
        return saps;
    }

    public void getTestRoles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestRoles");
        }
        HQLQueryBuilder<TestRoles> queryBuilderTestRoles = new HQLQueryBuilder<TestRoles>(TestRoles.class);

        queryBuilderTestRoles.addEq("roleInTest.testParticipantsList.tested", Boolean.TRUE);
        queryBuilderTestRoles.addIn("test.testType", TestType.getTestTypesWithoutMESA());
        queryBuilderTestRoles.addEq("test.testStatus.keyword", "ready");

        queryBuilderTestRoles.addFetch("roleInTest");
        queryBuilderTestRoles.addFetch("roleInTest.testParticipantsList");
        queryBuilderTestRoles.addFetch("roleInTest.testParticipantsList.actorIntegrationProfileOption");
        queryBuilderTestRoles.addFetch("roleInTest.testParticipantsList.aipo");

        List<TestRoles> roles = queryBuilderTestRoles.getList();
        for (TestRoles testRole : roles) {
            if (checkTest(testRole.getTest())) {
                List<TestParticipants> testParticipantsList = testRole.getRoleInTest().getTestParticipantsList();
                for (TestParticipants testParticipant : testParticipantsList) {
                    if (checkTestParticipant(testParticipant)) {
                        ActorIntegrationProfileOption aipo = testParticipant.getActorIntegrationProfileOption();
                        List<SystemActorProfiles> systemActorProfiles = testParticipant.getAipo()
                                .getSystemActorProfiles();
                        for (SystemActorProfiles systemActorProfile : systemActorProfiles) {
                            System system = systemActorProfile.getSystem();
                            counterInit(system, aipo, testRole);
                        }
                    }
                }
            }
        }
    }

    private Boolean checkTestParticipant(TestParticipants testParticipant) {
        return testParticipant.getTested();
    }

    private boolean checkTest(Test test) {
        if (!test.getTestStatus().getKeyword().equals("ready")) {
            return false;
        }
        return filterTestingSession.getTestTypes().contains(test.getTestType());
    }

    public void countTestInstances() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("countTestInstances");
        }
        // Find all test instance of the session
        HQLQueryBuilder<TestInstance> queryBuilderTestInstance = new HQLQueryBuilder<TestInstance>(TestInstance.class);
        queryBuilderTestInstance.addEq("testingSession", filterTestingSession);

        queryBuilderTestInstance.addIn("test.testType", TestType.getTestTypesWithoutMESA());
        queryBuilderTestInstance.addEq("test.testStatus.keyword", "ready");
        List<TestInstance> instances = queryBuilderTestInstance.getList();

        for (TestInstance testInstance : instances) {
            if (!Status.ABORTED.equals(testInstance.getLastStatus()) && checkTest(testInstance.getTest())) {
                // for all participants, we'll find it's test role and increment
                // it's status for this test role
                try {
                    List<TestInstanceParticipants> testInstanceParticipants = testInstance.getTestInstanceParticipants();
                    List<TestRoles> testRoles = testInstance.getTest().getTestRoles();
                    Status status = testInstance.getLastStatus();
                    for (TestInstanceParticipants testInstanceParticipant : testInstanceParticipants) {

                        // find the test role of the tip
                        TestRoles matchingTestRole = null;
                        for (TestRoles testRole : testRoles) {
                            if (testRole.getRoleInTest().getId().equals(testInstanceParticipant.getRoleInTest().getId())) {
                                matchingTestRole = testRole;
                            }
                        }

                        // system aipos that may replace the aipo of the TIP
                        Map<Integer, ActorIntegrationProfileOption> systemAIPOforRIT = new HashMap<Integer, ActorIntegrationProfileOption>();

                        System system = getSystemForTIP(testInstanceParticipant);

                        // all system AIPOs
                        Map<Integer, ActorIntegrationProfileOption> systemAIPOs = new HashMap<Integer, ActorIntegrationProfileOption>();
                        Set<SystemActorProfiles> systemActorProfiles = system.getSystemActorProfiles();
                        for (SystemActorProfiles systemActorProfile : systemActorProfiles) {
                            try {
                                systemAIPOs.put(systemActorProfile.getActorIntegrationProfileOption().getId(),
                                        systemActorProfile.getActorIntegrationProfileOption());
                            } catch (NullPointerException e){
                                LOG.warn("Null pointer exception for system actor profile : " + systemActorProfile.getId() +
                                        ". It is probably attached to no AIPO. It will be ignored for the result update.");
                            }
                        }
                        // check if tp aipos are in system aipo
                        // tp must be tested
                        List<TestParticipants> tps = testInstanceParticipant.getRoleInTest().getTestParticipantsList();
                        for (TestParticipants testParticipants : tps) {
                            if ((testParticipants.getTested() != null) && testParticipants.getTested()) {
                                ActorIntegrationProfileOption aipo = testParticipants.getActorIntegrationProfileOption();
                                if (systemAIPOs.containsKey(aipo.getId())) {
                                    systemAIPOforRIT.put(aipo.getId(), aipo);
                                }
                            }
                        }

                        Collection<ActorIntegrationProfileOption> aipos = systemAIPOforRIT.values();
                        for (ActorIntegrationProfileOption aipo : aipos) {

                            if ((system != null) && (aipo != null) && (matchingTestRole != null)) {
                                counterIncrement(system, aipo, matchingTestRole, status);
                            } else {
                                LOG.info("Missing test role (ti : " + testInstance + " ; system : " + system + " ; aipo : "
                                        + aipo + " ; role in test : " + testInstanceParticipant.getRoleInTest() + ")");
                            }
                        }

                    }
                } catch (javax.persistence.EntityNotFoundException e) {
                    LOG.error(e.getMessage());
                    LOG.error("The role in test must have been deleted in GMM");
                }
            }
        }
    }

    public void updateResults() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateResults");
        }
        for (SystemAIPOResultForATestingSession sapResult : sapResults) {
            Collection<TestRoles> testRoles = counterGetTestRoles(sapResult.getSystemActorProfile());

            sapResult.resetCounters();

            for (TestRoles testRole : testRoles) {
                Map<Status, Integer> counters = counterGet(sapResult.getSystemActorProfile().getSystem(), sapResult
                        .getSystemActorProfile().getActorIntegrationProfileOption(), testRole);

                sapResult.addCounterValues(testRole, counters);
            }

            sapResult.updateIndicators();
            entityManager.merge(sapResult);
        }
    }

    private void counterInit(System system, ActorIntegrationProfileOption aipo, TestRoles testRole) {
        counterGet(system, aipo, testRole);
    }

    private void counterIncrement(System system, ActorIntegrationProfileOption aipo, TestRoles testRole, Status status) {
        System sys = system;
        Map<Status, Integer> map = counterGet(sys, aipo, testRole);
        Integer value = map.get(status);
        if (value == null) {
            value = 1;
        } else {
            value = value + 1;
        }
        map.put(status, value);
    }

    private Map<Status, Integer> counterGet(System system, ActorIntegrationProfileOption aipo, TestRoles testRole) {
        Map<TestRoles, Map<Status, Integer>> subCounter = counterGet(system, aipo);
        Map<Status, Integer> map = subCounter.get(testRole);
        if (map == null) {
            map = new HashMap<Status, Integer>();
            for (Status testInstanceStatus : Status.values()) {
                map.put(testInstanceStatus, Integer.valueOf(0));
            }
            subCounter.put(testRole, map);
        }
        return map;
    }

    private Map<TestRoles, Map<Status, Integer>> counterGet(System system, ActorIntegrationProfileOption aipo) {
        Map<TestRoles, Map<Status, Integer>> subCounter = (Map<TestRoles, Map<Status, Integer>>) counters.get(system,
                aipo);
        if (subCounter == null) {
            subCounter = new HashMap<TestRoles, Map<Status, Integer>>();
            counters.put(system, aipo, subCounter);
        }
        return subCounter;
    }

    private Collection<TestRoles> counterGetTestRoles(SystemActorProfiles systemActorProfile) {
        Map<TestRoles, Map<Status, Integer>> subCounter = counterGet(systemActorProfile.getSystem(),
                systemActorProfile.getActorIntegrationProfileOption());
        return subCounter.keySet();
    }

    private System getSystemForTIP(TestInstanceParticipants testInstanceParticipant) {
        System system = null;
        SystemInSessionUser systemInSessionUser = testInstanceParticipant.getSystemInSessionUser();
        if (systemInSessionUser != null) {
            SystemInSession systemInSession = systemInSessionUser.getSystemInSession();
            if (systemInSession != null) {
                system = systemInSession.getSystem();
            }
        }
        return system;
    }

}
