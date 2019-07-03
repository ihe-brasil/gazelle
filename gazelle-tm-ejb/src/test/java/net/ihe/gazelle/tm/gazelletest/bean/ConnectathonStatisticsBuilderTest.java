package net.ihe.gazelle.tm.gazelletest.bean;

import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.junit.AbstractTestQueryJunit4;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import net.ihe.gazelle.tm.systems.model.TestingSessionQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.persistence.EntityManager;

@Ignore
public class ConnectathonStatisticsBuilderTest extends AbstractTestQueryJunit4 {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testGetIntegrationProiles() {
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        TestingSessionQuery query = new TestingSessionQuery();
        query.id().eq(32);
        TestingSession uniqueResult = query.getUniqueResult();
        uniqueResult.getIntegrationProfilesUnsorted();

        entityManager.flush();
    }

    @Test
    public void testConnectathonStatisticsBuilder() {
        TestingSessionQuery query = new TestingSessionQuery();
        query.id().eq(32);
        TestingSession uniqueResult = query.getUniqueResult();

        ConnectathonStatisticsBuilder csb = new ConnectathonStatisticsBuilder(uniqueResult);
        csb.updateStatistics();
        assert (true);
    }

    @Override
    protected String getDb() {
        return "gazelle-dev";
    }
}
