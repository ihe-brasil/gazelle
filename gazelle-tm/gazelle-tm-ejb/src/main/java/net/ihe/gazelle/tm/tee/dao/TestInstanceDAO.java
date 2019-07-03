package net.ihe.gazelle.tm.tee.dao;

import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import net.ihe.gazelle.tm.systems.model.SystemInSession;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * Contains DAO methods for TestInstance entity.
 *
 * @author tnabeel
 */
public class TestInstanceDAO {

    private static final Logger LOG = LoggerFactory.getLogger(TestInstanceDAO.class);

    private EntityManager entityManager;

    public TestInstanceDAO(EntityManager entityManager) {
        Validate.notNull(entityManager);
        this.entityManager = entityManager;
    }

    public EntityManager getEntityManager() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEntityManager");
        }
        return entityManager;
    }

    /**
     * Updates the database with changes in provided in testInstance
     *
     * @param testInstance
     */
    public void updateTestInstance(TestInstance testInstance) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateTestInstance");
        }
        Validate.notNull(testInstance);
        getEntityManager().merge(testInstance);
    }

    /**
     * This method returns the list of Test Instances to deactivate when a test instance is activated
     *
     * @param testInstance
     * @param initiatorSystemInSession
     * @return
     */
    public List<TestInstance> findTisToDeactivate(TestInstance testInstance, SystemInSession initiatorSystemInSession) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findTisToDeactivate");
        }
        Validate.notNull(testInstance);
        Validate.notNull(initiatorSystemInSession);

        Query query = getEntityManager().createNamedQuery("findTisToDeactivate");
        query.setParameter("tiId", testInstance.getId());
        query.setParameter("sisId", initiatorSystemInSession.getId());

        @SuppressWarnings("unchecked")
        List<TestInstance> list = (List<TestInstance>) query.getResultList();

        return list;
    }
}
