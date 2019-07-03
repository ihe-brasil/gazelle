package net.ihe.gazelle.tm.gazelletest.bean;

import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.tm.gazelletest.model.definition.RoleInTest;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestRoles;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestType;
import net.ihe.gazelle.tm.gazelletest.model.instance.Status;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceParticipants;
import net.ihe.gazelle.tm.systems.model.SystemActorProfiles;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import org.apache.commons.collections.map.MultiKeyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PartnersStatisticsBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(PartnersStatisticsBuilder.class);

    private static Map<Integer, PartnersStatisticsBuilder> instances = new ConcurrentHashMap<Integer, PartnersStatisticsBuilder>();

    private TestingSession testingSession;
    private Map<Integer, Set<Integer>> rolesInTestBySap;
    private Map<Integer, Set<Integer>> testRolesBySap;
    private Map<Integer, Set<Integer>> systemsByRoleInTest;
    private MultiKeyMap tiByRIT_T_S;
    private MultiKeyMap systemsByTI_RIT;

    private PartnersStatisticsBuilder(TestingSession filterTestingSession) {
        super();
        this.testingSession = filterTestingSession;
    }

    public static PartnersStatisticsBuilder update(TestingSession testingSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("PartnersStatisticsBuilder update");
        }
        PartnersStatisticsBuilder instance = new PartnersStatisticsBuilder(testingSession);
        instance.partnersFinderPrepare(EntityManagerService.provideEntityManager());
        instances.put(testingSession.getId(), instance);
        return instance;
    }

    public static PartnersStatisticsBuilder get(TestingSession testingSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("PartnersStatisticsBuilder get");
        }
        Integer testingSessionId = testingSession.getId();
        PartnersStatisticsBuilder instance = instances.get(testingSessionId);
        if (instance == null) {
            instance = update(testingSession);
        }
        return instance;
    }

    public TestingSession getTestingSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestingSession");
        }
        return testingSession;
    }

    private <K, T> void addItem(K id, T item, Map<K, Set<T>> map) {
        Set<T> set = map.get(id);
        if (set == null) {
            set = new HashSet<T>();
            map.put(id, set);
        }
        set.add(item);
    }

    public void partnersFinderPrepare(EntityManager entityManager) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("partnersFinderPrepare");
        }
        rolesInTestBySap = new HashMap<Integer, Set<Integer>>();
        testRolesBySap = new HashMap<Integer, Set<Integer>>();
        Map<Integer, Set<Integer>> testsBySap = new HashMap<Integer, Set<Integer>>();
        Map<Integer, Set<Integer>> rolesInTestByTest = new HashMap<Integer, Set<Integer>>();
        systemsByRoleInTest = new HashMap<Integer, Set<Integer>>();
        tiByRIT_T_S = new MultiKeyMap();
        systemsByTI_RIT = new MultiKeyMap();

        HQLQueryBuilder<SystemActorProfiles> sapBuilder = new HQLQueryBuilder<SystemActorProfiles>(
                SystemActorProfiles.class);
        sapBuilder.addEq("aipo.testParticipants.tested", true);
        sapBuilder
                .addIn("aipo.testParticipants.roleInTest.testRoles.test.testType", TestType.getTestTypesWithoutMESA());
        sapBuilder.addEq("aipo.testParticipants.roleInTest.testRoles.test.testStatus.keyword", "ready");

        List<List<?>> multiSelect = (List<List<?>>) sapBuilder.getMultiSelect("id",
                "aipo.testParticipants.roleInTest.id", "aipo.testParticipants.roleInTest.testRoles.test.id",
                "aipo.testParticipants.roleInTest.testRoles.id");
        for (List<?> list : multiSelect) {
            Integer sapId = ((Number) list.get(0)).intValue();
            Integer roleInTestId = ((Number) list.get(1)).intValue();
            Integer testId = ((Number) list.get(2)).intValue();
            Integer testRoleId = ((Number) list.get(3)).intValue();

            addItem(sapId, roleInTestId, rolesInTestBySap);
            addItem(sapId, testId, testsBySap);
            addItem(testId, roleInTestId, rolesInTestByTest);
            addItem(sapId, testRoleId, testRolesBySap);
        }

        HQLQueryBuilder<RoleInTest> ritBuilder = new HQLQueryBuilder<RoleInTest>(RoleInTest.class);
        ritBuilder.addEq("testParticipantsList.tested", true);
        ritBuilder.addEq("testParticipantsList.aipo.systemActorProfiles.system.systemsInSession.acceptedToSession",
                true);
        ritBuilder.addEq("testParticipantsList.aipo.systemActorProfiles.system.systemsInSession.testingSession",
                testingSession);
        ritBuilder.addEq("testParticipantsList.aipo.systemActorProfiles.system.isTool", false);

        multiSelect = (List<List<?>>) ritBuilder.getMultiSelect("id",
                "testParticipantsList.aipo.systemActorProfiles.system.id");
        for (List<?> list : multiSelect) {
            Integer ritId = ((Number) list.get(0)).intValue();
            Integer systemId = ((Number) list.get(1)).intValue();
            addItem(ritId, systemId, systemsByRoleInTest);
        }

        HQLQueryBuilder<TestInstanceParticipants> tipBuilder = new HQLQueryBuilder<TestInstanceParticipants>(
                TestInstanceParticipants.class);
        List<Status> statuses = new ArrayList<Status>();
        statuses.add(Status.ABORTED);
        statuses.add(Status.FAILED);
        tipBuilder.addRestriction(HQLRestrictions.nin("testInstance.lastStatus", statuses));
        tipBuilder.addEq("testInstance.testingSession", testingSession);

        multiSelect = (List<List<?>>) tipBuilder.getMultiSelect("testInstance.id", "testInstance.test.id",
                "systemInSessionUser.systemInSession.system.id", "roleInTest.id");
        for (List<?> list : multiSelect) {
            boolean ok = true;
            for (Object object : list) {
                if (object == null) {
                    ok = false;
                }
            }
            if (ok) {
                Integer tiId = ((Number) list.get(0)).intValue();
                Integer testId = ((Number) list.get(1)).intValue();
                Integer systemId = ((Number) list.get(2)).intValue();
                Integer roleInTestId = ((Number) list.get(3)).intValue();
                Set<Integer> tiIds = (Set<Integer>) tiByRIT_T_S.get(roleInTestId, testId, systemId);
                if (tiIds == null) {
                    tiIds = new HashSet<Integer>();
                    tiByRIT_T_S.put(roleInTestId, testId, systemId, tiIds);
                }
                tiIds.add(tiId);

                Set<Integer> systemIds = (Set<Integer>) systemsByTI_RIT.get(tiId, roleInTestId);
                if (systemIds == null) {
                    systemIds = new HashSet<Integer>();
                    systemsByTI_RIT.put(tiId, roleInTestId, systemIds);
                }
                systemIds.add(systemId);
            }
        }
    }

    /**
     * List the RoleInTest partners of this SAP in provided tests
     *
     * @param systemActorProfile
     * @param tests
     * @param entityManager
     * @return
     */
    private List<RoleInTest> getRolesInTest(EntityManager entityManager, SystemActorProfiles systemActorProfile,
                                            Set<Test> tests) {
        List<RoleInTest> roleInTests = new ArrayList<RoleInTest>();
        if ((tests != null) && (tests.size() > 0)) {
            // Get all RoleInTest for this SAP
            Set<Integer> roles = rolesInTestBySap.get(systemActorProfile.getId());
            if ((roles != null) && (roles.size() > 0)) {

                // retrieve all rit for these tests
                for (Test test : tests) {
                    for (TestRoles testRole : test.getTestRoles()) {
                        if (!roles.contains(testRole.getRoleInTest().getId())) {
                            roleInTests.add(testRole.getRoleInTest());
                        }
                    }

                }
            }
        }
        return roleInTests;
    }

    /**
     * Get the list of system ids possible for provided role in test. sap system is removed from result
     *
     * @param sap
     * @param roleInTest
     * @return
     */
    private Set<Integer> getPossiblePartnersForRoleInTest(SystemActorProfiles sap, RoleInTest roleInTest) {
        Set<Integer> ids = new HashSet<Integer>();
        Set<Integer> set = systemsByRoleInTest.get(roleInTest.getId());
        if (set != null) {
            ids.addAll(set);
        }
        return ids;
    }

    public void addPartners(EntityManager entityManager, AIPOSystemPartners partners,
                            SystemActorProfiles systemActorProfile, List<TestRoles> testRolesList) {
        if (testRolesList != null) {

            Set<Test> tests = new HashSet<Test>();
            Set<Integer> allTiIds = new HashSet<Integer>();
            for (TestRoles testRoles : testRolesList) {
                tests.add(testRoles.getTest());

                Set<Integer> tiIds = (Set<Integer>) tiByRIT_T_S.get(testRoles.getRoleInTest().getId(), testRoles
                        .getTest().getId(), systemActorProfile.getSystem().getId());
                if (tiIds != null) {
                    allTiIds.addAll(tiIds);
                }
            }
            List<RoleInTest> partnersRoleInTest = getRolesInTest(entityManager, systemActorProfile, tests);
            for (RoleInTest roleInTest : partnersRoleInTest) {
                Set<Integer> testedSystems = getTestedPartnersForRoleInTest(systemActorProfile, roleInTest, allTiIds);
                Set<Integer> possibleSystems = getPossiblePartnersForRoleInTest(systemActorProfile, roleInTest);
                partners.add(roleInTest, testedSystems, possibleSystems);
            }
        }
    }

    private Set<Integer> getTestedPartnersForRoleInTest(SystemActorProfiles systemActorProfile, RoleInTest roleInTest,
                                                        Set<Integer> allTiIds) {
        Set<Integer> result = new HashSet<Integer>();

        for (Integer tiId : allTiIds) {
            Set<Integer> systemIds = (Set<Integer>) systemsByTI_RIT.get(tiId, roleInTest.getId());
            if (systemIds != null) {
                result.addAll(systemIds);
            }
        }

        result.remove(systemActorProfile.getSystem().getId());
        return result;
    }

    public void addPartners(EntityManager entityManager, AIPOSystemPartners partners,
                            SystemActorProfiles systemActorProfile) {
        Set<Integer> testRoleIds = testRolesBySap.get(systemActorProfile.getId());
        if (testRoleIds != null) {
            HQLQueryBuilder<TestRoles> queryBuilder = new HQLQueryBuilder<TestRoles>(TestRoles.class);
            queryBuilder.addIn("id", testRoleIds);
            List<TestRoles> testRolesList = queryBuilder.getList();
            addPartners(entityManager, partners, systemActorProfile, testRolesList);
        }
    }

}
