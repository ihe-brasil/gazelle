package net.ihe.gazelle.tm.tee.dao;

import net.ihe.gazelle.tm.tee.exception.TestExecutionException;
import net.ihe.gazelle.tm.tee.model.ValidationStatus;
import net.ihe.gazelle.tm.tee.model.ValidationStatusEnum;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * Contains DAO methods for ValidationStatus entity
 *
 * @author tnabeel
 */
public class ValidationStatusDAO {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationStatusDAO.class);

    private EntityManager entityManager;

    public ValidationStatusDAO(EntityManager entityManager) {
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
     * Retrieves the ProxyType entity based on the provided proxyTypeEnum
     *
     * @param proxyTypeEnum
     * @return
     */
    public ValidationStatus findValidationStatus(ValidationStatusEnum validationStatusEnum) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findValidationStatus");
        }
        Query query = getEntityManager().createNamedQuery("findValidationStatusByKey");
        query.setParameter("key", validationStatusEnum);
        query.setHint("org.hibernate.cacheable", true);

        try {
            return (ValidationStatus) query.getSingleResult();
        } catch (javax.persistence.NoResultException e) {
            @SuppressWarnings("unchecked")
            List<ValidationStatus> list = getEntityManager().createNamedQuery("findAllValidationStatus").getResultList();
            throw new TestExecutionException("Could not find validationStatus '" + validationStatusEnum.name() + "'.  Available validationStatuses " +
                    "in database: \n" + list, e);
        }
    }
}
