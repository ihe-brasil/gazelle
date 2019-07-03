package net.ihe.gazelle.tm.tee.dao;

import net.ihe.gazelle.tm.tee.exception.TestExecutionException;
import net.ihe.gazelle.users.model.Institution;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;

/**
 * Contains DAO methods for Institution entity
 *
 * @author tnabeel
 */
public class InstitutionDAO {

    private static final Logger LOG = LoggerFactory.getLogger(InstitutionDAO.class);
    private EntityManager entityManager;

    public InstitutionDAO(EntityManager entityManager) {
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
     * Finds the institution by id
     *
     * @param id
     * @param mustFind
     * @return
     */
    public Institution findInstitutionById(Integer id, boolean mustFind) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findInstitutionById");
        }
        Institution institution = getEntityManager().find(Institution.class, id);
        if (institution == null && mustFind) {
            throw new TestExecutionException("Could not find Institution " + id);
        }
        return institution;
    }

}
