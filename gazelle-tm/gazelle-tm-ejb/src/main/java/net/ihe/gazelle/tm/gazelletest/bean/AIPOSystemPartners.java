package net.ihe.gazelle.tm.gazelletest.bean;

import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOption;
import net.ihe.gazelle.tm.gazelletest.model.definition.RoleInTest;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestParticipants;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AIPOSystemPartners {

    private static final Logger LOG = LoggerFactory.getLogger(AIPOSystemPartners.class);

    private Map<RoleInTest, Set<Integer>> testedPartners;
    private Map<RoleInTest, Set<Integer>> possiblePartners;
    private TestingSession testingSession;

    public AIPOSystemPartners() {
        super();
    }

    public AIPOSystemPartners(TestingSession testingSession) {
        super();
        testedPartners = new HashMap<RoleInTest, Set<Integer>>();
        possiblePartners = new HashMap<RoleInTest, Set<Integer>>();
        this.testingSession = testingSession;
    }

    public Map<RoleInTest, Set<Integer>> getTestedPartners() {
        return testedPartners;
    }

    public void setTestedPartners(Map<RoleInTest, Set<Integer>> testedPartners) {
        this.testedPartners = testedPartners;
    }

    public Map<RoleInTest, Set<Integer>> getPossiblePartners() {
        return possiblePartners;
    }

    public void setPossiblePartners(Map<RoleInTest, Set<Integer>> possiblePartners) {
        this.possiblePartners = possiblePartners;
    }

    public TestingSession getTestingSession() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTestingSession");
        }
        return testingSession;
    }

    public void add(RoleInTest roleInTest, Set<Integer> testedSystems, Set<Integer> possibleSystems) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("add");
        }
        testedPartners.put(roleInTest, testedSystems);
        possiblePartners.put(roleInTest, possibleSystems);
    }

    public Set<Integer> getTested(RoleInTest roleInTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTested");
        }
        return testedPartners.get(roleInTest);
    }

    public Set<Integer> getPossible(RoleInTest roleInTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossible");
        }
        return possiblePartners.get(roleInTest);
    }

    public List<RoleInTest> getRolesInTest() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRolesInTest");
        }
        return new ArrayList<RoleInTest>(testedPartners.keySet());
    }

    public String getCounters(RoleInTest roleInTest) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCounters");
        }
        StringBuilder sb = new StringBuilder("");
        Set<Integer> tested = testedPartners.get(roleInTest);
        Set<Integer> possible = possiblePartners.get(roleInTest);

        sb.append(tested.size());
        sb.append("/");
        sb.append(possible.size());

        return sb.toString();
    }

    public List<Partner> showPartnersFor(RoleInTest rit) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showPartnersFor");
        }
        List<Partner> possiblePartnersShow;
        this.testingSession = getTestingSession();
        Set<Integer> testedPartners = getTested(rit);
        HQLQueryBuilder<SystemInSession> queryBuilderSystem = new HQLQueryBuilder<SystemInSession>(SystemInSession.class);
        queryBuilderSystem.addIn("system.id", getPossible(rit));
        List<SystemInSession> systemsInSession = queryBuilderSystem.getList();
        possiblePartnersShow = new ArrayList<Partner>();
        TestingType testingType = new TestingType();
        List<ActorIntegrationProfileOption> actorIntegrationProfileOption = getRoleintestAIPO(rit);

        for (SystemInSession systemInSession : systemsInSession) {
            System system = systemInSession.getSystem();
            Set<SystemActorProfiles> systemActorProfiles = system.getSystemActorProfiles();
            testingType = getAIPOTestingType(testingType, actorIntegrationProfileOption, systemActorProfiles);

            String tableKeyword = getTablekeyword(systemInSession);
            boolean tested = testedPartners.contains(system.getId());
            possiblePartnersShow.add(new Partner(system, tested, testingType, systemInSession.getSkype(), tableKeyword));
        }
        return possiblePartnersShow;
    }

    private TestingType getAIPOTestingType(TestingType testingType, List<ActorIntegrationProfileOption> actorIntegrationProfileOption,
                                           Set<SystemActorProfiles> systemActorProfiles) {
        for (SystemActorProfiles systemActorProfile : systemActorProfiles) {
            if (actorIntegrationProfileOption.contains(systemActorProfile.getActorIntegrationProfileOption())) {
                testingType = systemActorProfile.getTestingType();
            }
        }
        return testingType;
    }

    private String getTablekeyword(SystemInSession systemInSession) {
        TableSession tableSession = systemInSession.getTableSession();
        String tableKeyword = "";
        if (tableSession != null) {
            tableKeyword = tableSession.getTableKeyword();
        }
        return tableKeyword;
    }

    private List<ActorIntegrationProfileOption> getRoleintestAIPO(RoleInTest rit) {

        List<ActorIntegrationProfileOption> actorIntegrationProfileOption = new ArrayList<ActorIntegrationProfileOption>();
        List<TestParticipants> testParticipantsList = rit.getTestParticipantsList();
        for (TestParticipants testParticipants : testParticipantsList) {
            actorIntegrationProfileOption.add(testParticipants.getActorIntegrationProfileOption());
        }
        return actorIntegrationProfileOption;
    }

}
