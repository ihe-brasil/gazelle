package net.ihe.gazelle.tm.tee.dao;

import net.ihe.gazelle.tm.tee.model.MessageValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.io.Serializable;

public class MessageValidationServiceDAO implements Serializable {

    private static final long serialVersionUID = -8555807260906185184L;

    private static final Logger LOG = LoggerFactory.getLogger(MessageValidationServiceDAO.class);
    private EntityManager entityManager;

    public MessageValidationServiceDAO(EntityManager em) {
        this.entityManager = em;
    }

    public void deleteMessageValidationService(MessageValidationService srv) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteMessageValidationService");
        }

        if (srv == null) {

            return;
        }

        if (srv.getTmStepInstanceMsgValidations() != null) {
            if (srv.getTmStepInstanceMsgValidations().size() > 0) {

                return;
            }
        }

        srv = entityManager.find(MessageValidationService.class, srv.getId());
        entityManager.remove(srv);
        entityManager.flush();
    }
}
