package net.ihe.gazelle.tf.dao;

import net.ihe.gazelle.tf.model.Actor;
import net.ihe.gazelle.tf.model.Transaction;
import net.ihe.gazelle.tf.model.auditMessage.AuditMessage;
import net.ihe.gazelle.tf.model.auditMessage.AuditMessageQuery;
import net.ihe.gazelle.tf.model.auditMessage.TriggerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AuditMessageDAO {
    private static final Logger LOG = LoggerFactory.getLogger(AuditMessageDAO.class);

    public static List<AuditMessage> getAllAuditMessages() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllAuditMessages");
        }
        AuditMessageQuery query = new AuditMessageQuery();
        return query.getList();
    }

    public static List<AuditMessage> getAuditMessagesFiltered(Actor actor, Transaction transaction, TriggerEvent triggerEvent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAuditMessagesFiltered");
        }
        AuditMessageQuery query = new AuditMessageQuery();
        if (actor != null) {
            query.issuingActor().eq(actor);
        }
        if (transaction != null) {
            query.auditedTransaction().eq(transaction);
        }
        if (triggerEvent != null) {
            query.auditedEvent().eq(triggerEvent);
        }
        return query.getList();
    }
}
