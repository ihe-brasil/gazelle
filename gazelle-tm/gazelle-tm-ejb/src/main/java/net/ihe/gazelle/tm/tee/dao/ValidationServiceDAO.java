package net.ihe.gazelle.tm.tee.dao;

import net.ihe.gazelle.tm.tee.model.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;

/**
 * @author rizwan.tanoli
 */
public class ValidationServiceDAO implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationServiceDAO.class);

    private static final long serialVersionUID = 6587554800052370839L;

    private EntityManager entityManager;

    public ValidationServiceDAO(EntityManager em) {
        this.entityManager = em;
    }

    @SuppressWarnings("unchecked")
    public List<ValidationService> findAll() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findAll");
        }
        return entityManager.createQuery("FROM ValidationService").getResultList();
    }
}
