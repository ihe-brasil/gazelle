package net.ihe.gazelle.ws.tm.tests;

import net.ihe.gazelle.junit.AbstractTestQueryJunit4;
import net.ihe.gazelle.junit.DatabaseManager;
import net.ihe.gazelle.tm.gazelletest.model.definition.RoleInTest;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestParticipants;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestRoles;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestStatus;
import net.ihe.gazelle.tm.gazelletest.model.definition.factories.*;
import net.ihe.gazelle.tm.ws.data.TestsWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.util.List;
import static org.junit.Assert.assertEquals;

public class TestsWsTest extends AbstractTestQueryJunit4 {

    private String integrationProfileKeyword;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        TestStatusFactory.createTestDefaultTestStatus();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void getTestsMustReturnOnlyReadyTests() throws JAXBException {

        net.ihe.gazelle.tm.gazelletest.model.definition.Test readyTest = createReadyTest();
        createToBeCompletedTest();

        TestsWs ws = new TestsWs();
        TestsWrapper tests = ws.getTests(null, null, null, null, null);
        assertEquals(1, tests.getTests().size());
        assertEquals(readyTest.getKeyword(), tests.getTests().get(0).getKeyword());
    }

    private void createToBeCompletedTest() {
        net.ihe.gazelle.tm.gazelletest.model.definition.Test notReadyTest = TestFactory.createConnectathonTest();
        notReadyTest.setTestStatus(TestStatus.getSTATUS_TOBECOMPLETED());
        notReadyTest = (net.ihe.gazelle.tm.gazelletest.model.definition.Test) DatabaseManager.writeObject(notReadyTest);
    }

    private net.ihe.gazelle.tm.gazelletest.model.definition.Test createReadyTest() {
        net.ihe.gazelle.tm.gazelletest.model.definition.Test readyTest = TestFactory.createConnectathonTest();
        readyTest.setTestStatus(TestStatus.getSTATUS_READY());
        readyTest = (net.ihe.gazelle.tm.gazelletest.model.definition.Test) DatabaseManager.writeObject(readyTest);
        return readyTest;
    }

    private net.ihe.gazelle.tm.gazelletest.model.definition.Test createTests() {
        net.ihe.gazelle.tm.gazelletest.model.definition.Test readyTest = TestFactory.createConnectathonTest();
        readyTest.setTestStatus(TestStatus.getSTATUS_READY());
        readyTest = (net.ihe.gazelle.tm.gazelletest.model.definition.Test) DatabaseManager.writeObject(readyTest);

        List<TestRoles> testRoles = TestRolesFactory.createTestRolesWithMandatoryFields(readyTest);

        List<RoleInTest> roleInTest = RoleInTestFactory.createRoleInTestWithMandatoryFields(testRoles);

        testRoles.get(0).setRoleInTest(roleInTest.get(0));
        TestParticipants createTestParticipantWithMandatoryFields = TestParticipantsFactory
                .createTestParticipantWithMandatoryFields(roleInTest);

        integrationProfileKeyword = createTestParticipantWithMandatoryFields.getActorIntegrationProfileOption()
                .getActorIntegrationProfile().getIntegrationProfile().getKeyword();

        for (RoleInTest role : roleInTest) {
            DatabaseManager.update(role);
        }

        for (TestRoles role : testRoles) {
            DatabaseManager.update(role);
        }

        DatabaseManager.update(createTestParticipantWithMandatoryFields);
        DatabaseManager.update(readyTest);

        net.ihe.gazelle.tm.gazelletest.model.definition.Test notReadyTest = TestFactory.createConnectathonTest();
        notReadyTest.setTestStatus(TestStatus.getSTATUS_TOBECOMPLETED());
        notReadyTest = (net.ihe.gazelle.tm.gazelletest.model.definition.Test) DatabaseManager.writeObject(notReadyTest);

        testRoles = TestRolesFactory.createTestRolesWithMandatoryFields(notReadyTest);
        testRoles.get(0).setRoleInTest(roleInTest.get(0));
        List<TestRoles> testRoles2 = roleInTest.get(0).getTestRoles();
        testRoles2.add(testRoles.get(0));
        roleInTest.get(0).setTestRoles(testRoles2);

        createTestParticipantWithMandatoryFields.setRoleInTest(roleInTest);

        DatabaseManager.update(createTestParticipantWithMandatoryFields);
        DatabaseManager.update(readyTest);
        DatabaseManager.update(notReadyTest);
        return readyTest;
    }

    @Test
    @Ignore
    public void getIntegrationProfileReadyTest() {
        net.ihe.gazelle.tm.gazelletest.model.definition.Test readyTest = createTests();
        TestsWs ws = new TestsWs();
        try {
            TestsWrapper tests = ws.getTests(integrationProfileKeyword, "", "", null, null);
            assertEquals(1, tests.getTests().size());
            assertEquals(readyTest.getKeyword(), tests.getTests().get(0).getKeyword());
        } catch (Exception e) {
        }
    }
}
