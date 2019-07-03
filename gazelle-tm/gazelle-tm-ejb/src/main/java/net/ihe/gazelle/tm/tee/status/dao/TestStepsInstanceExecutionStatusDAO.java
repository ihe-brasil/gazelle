package net.ihe.gazelle.tm.tee.status.dao;

import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.tee.exception.TestExecutionException;
import net.ihe.gazelle.tm.tee.model.TestStepInstanceExecutionStatus;
import net.ihe.gazelle.tm.tee.model.TestStepInstanceExecutionStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.List;

/**
 * Data access object for TestStepsInstance execution status entity
 *
 * @see TestStepInstanceExecutionStatus
 */
public class TestStepsInstanceExecutionStatusDAO implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(TestStepsInstanceExecutionStatusDAO.class);

    public TestStepsInstanceExecutionStatusDAO() {

    }

    public TestStepInstanceExecutionStatus getTestStepExecutionStatusByEnum(
            TestStepInstanceExecutionStatusEnum status) {

        EntityManager em = EntityManagerService
                .provideEntityManager();
        Query query = em
                .createQuery("FROM TestStepInstanceExecutionStatus tsies where tsies.key=:key");
        query.setParameter("key", status);
        query.setHint("org.hibernate.cacheable", true);

        try {
            return (TestStepInstanceExecutionStatus) query.getSingleResult();
        } catch (NoResultException e) {
            @SuppressWarnings("unchecked")
            List<TestStepInstanceExecutionStatus> statuses = EntityManagerService
                    .provideEntityManager().createQuery("FROM TestStepInstanceExecutionStatus tsies").getResultList();

            throw new TestExecutionException("Could not find status '" + status.name() + "'.  Available statuses in database: \n" + statuses, e);
        } finally {

        }
    }
}
