package net.ihe.gazelle.tf.dao;

import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.Standard;
import net.ihe.gazelle.tf.model.StandardQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;

public class StandardDAO {

    private static final Logger LOG = LoggerFactory.getLogger(StandardDAO.class);

    public static Standard getStandardByKeyword(String keyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Standard getStandardByKeyword");
        }
        if (keyword == null) {
            return null;
        } else {
            StandardQuery query = new StandardQuery();
            query.keyword().eq(keyword);
            return query.getUniqueResult();
        }
    }

    public static Standard save(Standard selectedStandard) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Standard save");
        }
        EntityManager entityManager = EntityManagerService.provideEntityManager();
        Standard saved = entityManager.merge(selectedStandard);
        entityManager.flush();
        return saved;
    }
}
