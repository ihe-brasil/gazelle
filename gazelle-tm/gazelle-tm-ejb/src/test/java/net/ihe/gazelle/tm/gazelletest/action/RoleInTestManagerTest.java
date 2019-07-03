package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.junit.AbstractTestQueryJunit4;
import net.ihe.gazelle.tf.model.factories.IntegrationProfileOptionFactory;
import net.ihe.gazelle.tm.gazelletest.model.definition.RoleInTest;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestParticipants;
import net.ihe.gazelle.tm.gazelletest.model.definition.factories.RoleInTestFactory;
import net.ihe.gazelle.tm.gazelletest.model.definition.factories.TestParticipantsFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RoleInTestManagerTest extends AbstractTestQueryJunit4 {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        TestParticipantsFactory.cleanTestParticipants();
        super.tearDown();
    }

    @Test
    public void testaddParticipantToRoleInTest_selectedRoleInTest_and_selectedTestParticipants_null() {
        RoleInTestManager ritm = new RoleInTestManager();
        ritm.addParticipantToRoleInTest();
    }

    @Test
    public void testaddParticipantToRoleInTest_selectedRoleInTest_null_and_selectedTestParticipants_not_null() {
        RoleInTestManager ritm = new RoleInTestManager();
        TestParticipants selectedTestParticipants = TestParticipantsFactory.createTestParticipantWithMandatoryFields();
        ritm.setSelectedTestParticipants(selectedTestParticipants);
        ritm.addParticipantToRoleInTest();
    }

    @Test
    @Ignore
    public void testaddParticipantToRoleInTest_selectedRoleInTest_and_selectedTestParticipants_not_null() {
        RoleInTestManager ritm = new RoleInTestManager();
        TestParticipants selectedTestParticipants = TestParticipantsFactory.createTestParticipantWithMandatoryFields();
        ritm.setSelectedTestParticipants(selectedTestParticipants);
        RoleInTest rit = RoleInTestFactory.createOneRoleInTestWithMandatoryFields();
        setRoleInTestManagerState(ritm, selectedTestParticipants, rit);

        IntegrationProfileOptionFactory.createNoneIntegrationProfileOption();

        EntityManagerService.provideEntityManager().getTransaction().begin();
        EntityManagerService.provideEntityManager().flush();
        ritm.addParticipantToRoleInTest();
    }

    @Test
    @Ignore
    public void testaddParticipantToRoleInTest_WithoutExistingParticipants() {
        RoleInTestManager ritm = new RoleInTestManager();
        TestParticipants testParticipant = TestParticipantsFactory.provideTestParticipantWithMandatoryFields();

        TestParticipants testParticipants2 = TestParticipantsFactory.createTestParticipantWithMandatoryFields();
        testParticipants2.setActorIntegrationProfileOption(testParticipant.getActorIntegrationProfileOption());
        testParticipants2.setTested(false);

        EntityManagerService.provideEntityManager().merge(testParticipants2);

        TestParticipants testParticipants3 = TestParticipantsFactory.createTestParticipantWithMandatoryFields();
        testParticipants3.setActorIntegrationProfileOption(testParticipant.getActorIntegrationProfileOption());
        EntityManagerService.provideEntityManager().merge(testParticipants3);

        ritm.setSelectedTestParticipants(testParticipant);
        RoleInTest rit = RoleInTestFactory.createOneRoleInTestWithMandatoryFields();
        List<TestParticipants> set = new ArrayList<TestParticipants>();
        rit.setTestParticipantsList(set);
        setRoleInTestManagerState(ritm, testParticipant, rit);

        IntegrationProfileOptionFactory.createNoneIntegrationProfileOption();

        EntityManagerService.provideEntityManager().getTransaction().begin();
        EntityManagerService.provideEntityManager().flush();
        ritm.addParticipantToRoleInTest();
        assertEquals(1, rit.getTestParticipantsList().size());
    }

    @Test
    @Ignore
    public void testaddParticipantToRoleInTest_withAssignedRoleinTest() {
        RoleInTestManager ritm = new RoleInTestManager();
        TestParticipants testParticipant = TestParticipantsFactory.provideTestParticipantWithMandatoryFields();

        TestParticipants testParticipants2 = TestParticipantsFactory.createTestParticipantWithMandatoryFields();
        testParticipants2.setActorIntegrationProfileOption(testParticipant.getActorIntegrationProfileOption());
        testParticipants2.setTested(false);

        EntityManagerService.provideEntityManager().merge(testParticipants2);

        TestParticipants testParticipants3 = TestParticipantsFactory.createTestParticipantWithMandatoryFields();
        testParticipants3.setActorIntegrationProfileOption(testParticipant.getActorIntegrationProfileOption());
        EntityManagerService.provideEntityManager().merge(testParticipants3);

        ritm.setSelectedTestParticipants(testParticipant);
        RoleInTest rit = RoleInTestFactory.createOneRoleInTestWithMandatoryFields();
        List<TestParticipants> set = new ArrayList<TestParticipants>();
        set.add(testParticipants2);
        set.add(testParticipants3);

        rit.setTestParticipantsList(set);
        setRoleInTestManagerState(ritm, testParticipant, rit);

        IntegrationProfileOptionFactory.createNoneIntegrationProfileOption();

        EntityManagerService.provideEntityManager().getTransaction().begin();
        EntityManagerService.provideEntityManager().flush();
        ritm.addParticipantToRoleInTest();
        assertEquals(1, rit.getTestParticipantsList().size());
    }

    @Test
    @Ignore
    public void testaddParticipantToRoleInTest__() {
        RoleInTestManager ritm = new RoleInTestManager();
        TestParticipants testParticipant = TestParticipantsFactory.provideTestParticipantWithMandatoryFields();

        TestParticipants testParticipants2 = TestParticipantsFactory.createTestParticipantWithMandatoryFields();
        testParticipants2.setActorIntegrationProfileOption(testParticipant.getActorIntegrationProfileOption());
        testParticipants2.setTested(false);

        EntityManagerService.provideEntityManager().merge(testParticipants2);

        TestParticipants testParticipants3 = TestParticipantsFactory.createTestParticipantWithMandatoryFields();
        testParticipants2.setActorIntegrationProfileOption(testParticipant.getActorIntegrationProfileOption());
        testParticipants3.setTested(true);
        EntityManagerService.provideEntityManager().merge(testParticipants3);

        ritm.setSelectedTestParticipants(testParticipant);
        RoleInTest rit = RoleInTestFactory.createOneRoleInTestWithMandatoryFields();
        List<TestParticipants> set = new ArrayList<TestParticipants>();
        set.add(testParticipants2);
        set.add(testParticipants3);

        rit.setTestParticipantsList(set);
        setRoleInTestManagerState(ritm, testParticipant, rit);

        IntegrationProfileOptionFactory.createNoneIntegrationProfileOption();

        EntityManagerService.provideEntityManager().getTransaction().begin();
        EntityManagerService.provideEntityManager().flush();
        ritm.addParticipantToRoleInTest();

        assertEquals(2, rit.getTestParticipantsList().size());
    }

    @Test
    @Ignore
    public void testaddParticipantToRoleInTest___() {
        RoleInTestManager ritm = new RoleInTestManager();
        TestParticipants testParticipant = TestParticipantsFactory.provideTestParticipantWithMandatoryFields();

        TestParticipants testParticipants2 = TestParticipantsFactory.createTestParticipantWithMandatoryFields();
        testParticipants2.setActorIntegrationProfileOption(testParticipant.getActorIntegrationProfileOption());
        testParticipants2.setTested(false);

        EntityManagerService.provideEntityManager().merge(testParticipants2);

        TestParticipants testParticipants3 = TestParticipantsFactory.createTestParticipantWithMandatoryFields();
        testParticipants2.setActorIntegrationProfileOption(testParticipant.getActorIntegrationProfileOption());
        testParticipants3.setTested(true);
        EntityManagerService.provideEntityManager().merge(testParticipants3);

        TestParticipants testParticipants4 = TestParticipantsFactory.createTestParticipantWithMandatoryFields();
        TestParticipants testParticipants5 = TestParticipantsFactory.createTestParticipantWithMandatoryFields();
        TestParticipants testParticipants6 = TestParticipantsFactory.createTestParticipantWithMandatoryFields();

        ritm.setSelectedTestParticipants(testParticipant);
        RoleInTest rit = RoleInTestFactory.createOneRoleInTestWithMandatoryFields();
        List<TestParticipants> set = new ArrayList<TestParticipants>();
        set.add(testParticipants2);
        set.add(testParticipants3);
        set.add(testParticipants4);
        set.add(testParticipants5);
        set.add(testParticipants6);

        rit.setTestParticipantsList(set);
        ritm.setSelectedRoleInTest(rit);
        int i = 0;
        while (i < 10) {
            i++;

            setRoleInTestManagerState(ritm, testParticipant, rit);

            IntegrationProfileOptionFactory.createNoneIntegrationProfileOption();

            EntityManagerService.provideEntityManager().getTransaction().begin();
            EntityManagerService.provideEntityManager().flush();
            ritm.addParticipantToRoleInTest();

            testParticipant = TestParticipantsFactory.provideTestParticipantWithMandatoryFields();
        }
        assertEquals(14, rit.getTestParticipantsList().size());
    }

    private void setRoleInTestManagerState(RoleInTestManager ritm, TestParticipants testParticipant, RoleInTest rit) {
        ritm.setSelectedRoleInTest(rit);
        ritm.setSelectedActor(testParticipant.getActorIntegrationProfileOption().getActorIntegrationProfile()
                .getActor());
        ritm.setSelectedIntegrationProfileOption(testParticipant.getActorIntegrationProfileOption()
                .getIntegrationProfileOption());
        ritm.setSelectedIntegrationProfile(testParticipant.getActorIntegrationProfileOption()
                .getActorIntegrationProfile().getIntegrationProfile());
    }

    protected boolean getShowSql() {
        return false;
    }

    protected String getDb() {
        return "gazelle-junit";
    }
}
