package net.ihe.gazelle.tm.gazelletest.action;

import junitx.framework.ListAssert;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.junit.AbstractTestQueryJunit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

@Ignore
public class TMCheckListManagerTest extends AbstractTestQueryJunit4 {
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void getAIPOWithNoTests() {
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        String querr1 = "SELECT DISTINCT participants.actorIntegrationProfileOption.id FROM TestRoles tr JOIN tr.roleInTest.testParticipantsList " +
                "participants ";
        Query query = entityManager.createQuery(querr1);
        @SuppressWarnings("unchecked")
        List<Integer> limplId = query.getResultList();

        TMCheckListManager tmCheckListManager = new TMCheckListManager();
        List<Integer> aipoWithTests = tmCheckListManager.getAipoWithTests();

        ListAssert.assertEquals(limplId, aipoWithTests);
    }

    @Override
    protected String getDb() {
        return "gazelle-junit";
    }
}
