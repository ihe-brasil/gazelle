package net.ihe.gazelle.tm.tee.dao;

import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.tee.exception.TestExecutionException;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;

/**
 * Contains DAO methods for System entity
 *
 * @author tnabeel
 */
public class SystemDAO {
    private static final Logger LOG = LoggerFactory.getLogger(SystemDAO.class);

    private EntityManager entityManager;

    public SystemDAO(EntityManager entityManager) {
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
     * Finds the System by its id.
     *
     * @param id
     * @param mustFind
     * @return
     */
    public System findSystemById(Integer id, boolean mustFind) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findSystemById");
        }
        System system = getEntityManager().find(System.class, id);
        if (system == null && mustFind) {
            throw new TestExecutionException("Could not find System " + id);
        }
        return system;
    }
}
