package net.ihe.gazelle.factories;

import net.ihe.gazelle.junit.DatabaseManager;
import net.ihe.gazelle.tm.gazelletest.model.factories.TestingSessionFactory;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstanceQuery;

import java.util.List;

public class TestInstanceFactory {

    public static TestInstance createTestInstance() {
        TestInstance testInstance = new TestInstance();
        testInstance.setDescription("Super testInstance");
        testInstance.setTestingSession(TestingSessionFactory.createTestingSessionWithMandatoryFields());
        testInstance = (TestInstance) DatabaseManager.writeObject(testInstance);
        return testInstance;
    }

    public static void cleanTestInstance() {
        TestInstanceQuery tiQuery = new TestInstanceQuery();
        List<TestInstance> testInstances = tiQuery.getListDistinct();
        for (TestInstance ti : testInstances) {
            DatabaseManager.removeObject(ti);
        }
    }
}
