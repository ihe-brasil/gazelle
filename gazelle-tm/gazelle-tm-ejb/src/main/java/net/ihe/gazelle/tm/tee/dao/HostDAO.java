package net.ihe.gazelle.tm.tee.dao;

import net.ihe.gazelle.tm.configurations.model.Host;
import net.ihe.gazelle.tm.tee.exception.TestExecutionException;
import net.ihe.gazelle.tm.tee.util.CollectionUtil;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

/**
 * Contains DAO methods for Host entity
 *
 * @author tnabeel
 */
public class HostDAO {
    private static final Logger LOG = LoggerFactory.getLogger(HostDAO.class);
    private EntityManager entityManager;

    public HostDAO(EntityManager entityManager) {
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
     * Finds the host by id
     *
     * @param id
     * @param mustFind
     * @return
     */
    public Host findHostById(Integer id, boolean mustFind) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findHostById");
        }
        Host host = getEntityManager().find(Host.class, id);
        if (host == null && mustFind) {
            throw new TestExecutionException("Could not find Host " + id);
        }
        return host;
    }

    /**
     * Finds the host along with its system and institution
     *
     * @param hostIpAddress
     * @return
     */
    public Host findHostWithSystemAndInstitution(String hostIpAddress) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findHostWithSystemAndInstitution");
        }
        Query query = getEntityManager().createNamedQuery("findHostWithSystemAndInstitution");
        query.setParameter("hostIP", hostIpAddress);
        List<Host> list = query.getResultList();
        return !CollectionUtil.isEmpty(list) ? list.get(0) : null;
    }

}
