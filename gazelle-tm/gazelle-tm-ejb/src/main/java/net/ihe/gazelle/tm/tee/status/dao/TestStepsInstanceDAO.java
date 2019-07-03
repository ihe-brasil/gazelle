package net.ihe.gazelle.tm.tee.status.dao;

import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestStepsInstance;
import net.ihe.gazelle.tm.tee.model.TestStepInstanceExecutionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.List;

/**
 * Data access object class for TestStepsInstance entity.
 *
 * @see TestStepsInstance
 */
public class TestStepsInstanceDAO implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(TestStepsInstanceDAO.class);

    public TestStepsInstanceDAO() {

    }

    public void persistTestStepsInstance(TestStepsInstance instance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("persistTestStepsInstance");
        }

        if (instance != null) {
            EntityManager em = EntityManagerService
                    .provideEntityManager();
            em.persist(instance);
            em.flush();
        } else {
            LOG.error("TestStepsInstanceDAO.persistTestStepsInstance: Provided TestStepsInstance obj is NULL. Can't persist NULL to DB.");
        }

    }

    public TestStepsInstance mergeTestStepsInstance(TestStepsInstance instance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("mergeTestStepsInstance");
        }

        if (instance != null) {
            EntityManager em = EntityManagerService
                    .provideEntityManager();
            instance = em.merge(instance);
            em.flush();
        }

        return instance;
    }

    @SuppressWarnings("unchecked")
    public List<TestStepInstanceExecutionStatus> getListOfTestStepInstanceExcecutionStatuses() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfTestStepInstanceExcecutionStatuses");
        }

        EntityManager em = EntityManagerService.provideEntityManager();
        Query query = em.createQuery("FROM TestStepInstanceExecutionStatus");
        query.setHint("org.hibernate.cacheable", true);
        return (List<TestStepInstanceExecutionStatus>) query.getResultList();
    }

    public TestStepsInstance find(Integer id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("find");
        }
        if (id != null) {
            EntityManager em = EntityManagerService
                    .provideEntityManager();
            return em.find(TestStepsInstance.class, id);
        }
        return null;
    }
}
