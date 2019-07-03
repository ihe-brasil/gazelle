package net.ihe.gazelle.tm.tee.dao;

import net.ihe.gazelle.tm.tee.exception.TestExecutionException;
import net.ihe.gazelle.tm.tee.model.ProxyType;
import net.ihe.gazelle.tm.tee.model.ProxyTypeEnum;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * Contains DAO methods for ProxyType entity
 *
 * @author tnabeel
 */
public class ProxyTypeDAO {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyTypeDAO.class);

    private EntityManager entityManager;

    public ProxyTypeDAO(EntityManager entityManager) {
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
    public ProxyType findProxyType(ProxyTypeEnum proxyTypeEnum) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findProxyType");
        }
        Query query = getEntityManager().createNamedQuery("findProxyTypeByKey");
        query.setParameter("key", proxyTypeEnum);
        query.setHint("org.hibernate.cacheable", true);

        try {
            return (ProxyType) query.getSingleResult();
        } catch (javax.persistence.NoResultException e) {
            @SuppressWarnings("unchecked")
            List<ProxyType> list = getEntityManager().createNamedQuery("findAllProxyTypes").getResultList();
            throw new TestExecutionException("Could not find proxyType '" + proxyTypeEnum.name() + "'.  Available proxyTypes in database: \n" +
                    list, e);
        }
    }
}
