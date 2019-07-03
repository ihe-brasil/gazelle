package net.ihe.gazelle.tm.tee.status.dao;

import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.tm.tee.model.TestInstanceExecutionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.List;

/**
 * Data access object for a TestInstance
 */

public class TestInstanceDAO implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(TestInstanceDAO.class);

    public TestInstanceDAO() {

    }

    public void persistTestInstance(TestInstance instance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistTestInstance");
        }

        if (instance != null) {
            EntityManager em = EntityManagerService
                    .provideEntityManager();
            em.persist(instance);
            em.flush();
        } else {
            LOG.error("TestInstanceDAO.persistTestInstance: Provided TestInstance obj is NULL. Can't persist NULL to DB.");
        }

    }

    public TestInstance mergeTestInstance(TestInstance instance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("mergeTestInstance");
        }

        if (instance != null) {
            EntityManager em = EntityManagerService
                    .provideEntityManager();
            instance = em.merge(instance);
            em.flush();
        } else {
            LOG.error("TestInstanceDAO.mergeTestInstance: Provided TestInstance obj is NULL.");
        }

        return instance;
    }

    public TestInstance find(Integer id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("find");
        }
        if (id != null) {
            EntityManager em = EntityManagerService
                    .provideEntityManager();
            Query query = em.createQuery("FROM TestInstance ti where ti.id=:id");
            query.setParameter("id", id);
            query.setHint("org.hibernate.cacheable", true);
            return (TestInstance) query.getSingleResult();
        }
        return null;
    }

    /**
     * Returns a List of TestInstanceExecutionStatus objs
     */
    @SuppressWarnings("unchecked")
    public List<TestInstanceExecutionStatus> getListOfTestInstanceExecutionStatuses() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfTestInstanceExecutionStatuses");
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        Query query = em.createQuery("FROM TestInstanceExecutionStatus");
        query.setHint("org.hibernate.cacheable", true);
        return (List<TestInstanceExecutionStatus>) query.getResultList();
    }
}
