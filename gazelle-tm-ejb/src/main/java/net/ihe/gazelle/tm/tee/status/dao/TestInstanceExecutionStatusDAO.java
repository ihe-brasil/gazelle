package net.ihe.gazelle.tm.tee.status.dao;

import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tm.tee.exception.TestExecutionException;
import net.ihe.gazelle.tm.tee.model.TestInstanceExecutionStatus;
import net.ihe.gazelle.tm.tee.model.TestInstanceExecutionStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.List;

public class TestInstanceExecutionStatusDAO implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(TestInstanceExecutionStatusDAO.class);

    public TestInstanceExecutionStatusDAO() {

    }

    public TestInstanceExecutionStatus getTestInstanceExecutionStatusByEnum(
            TestInstanceExecutionStatusEnum status) {

        EntityManager em = EntityManagerService
                .provideEntityManager();
        Query query = em
                .createQuery("FROM TestInstanceExecutionStatus ties where ties.key=:key");
        query.setParameter("key", status);
        query.setHint("org.hibernate.cacheable", true);

        try {
            return (TestInstanceExecutionStatus) query.getSingleResult();
        } catch (javax.persistence.NoResultException e) {
            @SuppressWarnings("unchecked")
            List<TestInstanceExecutionStatus> statuses = EntityManagerService
                    .provideEntityManager().createQuery("FROM TestInstanceExecutionStatus").getResultList();

            throw new TestExecutionException("Could not find status '" + status.name() + "'.  Available statuses in database: \n" + statuses, e);
        } finally {

        }
    }
}
