package net.ihe.gazelle.documents;

import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.junit.AbstractTestQueryJunit4;
import org.junit.Ignore;
import org.junit.Test;

import javax.persistence.EntityManager;

import static org.junit.Assert.assertEquals;

@Ignore
public class BackgroundUrlCheckerTest extends AbstractTestQueryJunit4 {
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void test_backgroundJob() {
        BackgroundUrlChecker buc = new BackgroundUrlChecker();
        EntityManager em = EntityManagerService.provideEntityManager();
        buc.setEntityManager(em);
        em.getTransaction().begin();
        assertEquals(120, buc.checkDocumentsUrlPointsToPDF());
        em.getTransaction().commit();
    }
}
