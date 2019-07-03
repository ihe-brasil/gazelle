package net.ihe.gazelle.tm.tee.dao;

import net.ihe.gazelle.tm.configurations.model.Configuration;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;

/**
 * Contains DAO methods for TmConfiguration entity.
 *
 * @author tnabeel
 */
public class TmConfigurationDAO {

    private static final Logger LOG = LoggerFactory.getLogger(TmConfigurationDAO.class);

    private EntityManager entityManager;

    public TmConfigurationDAO(EntityManager entityManager) {
        Validate.notNull(entityManager);
        this.entityManager = entityManager;
    }

    public EntityManager getEntityManager() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEntityManager");
        }
        return entityManager;
    }

    public Configuration findTmConfiguration(int tmConfigId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findTmConfiguration");
        }
        Configuration configuration = getEntityManager().find(Configuration.class, tmConfigId);
        return configuration;
    }
}
