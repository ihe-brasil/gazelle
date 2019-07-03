package net.ihe.gazelle.tf.ws;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.auditMessage.AuditMessage;
import net.ihe.gazelle.tf.model.auditMessage.AuditMessageQuery;
import net.ihe.gazelle.tf.ws.data.AuditMessageWrapper;
import net.ihe.gazelle.tf.ws.data.AuditMessagesWrapper;
import org.jboss.seam.annotations.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;

@Stateless
@Name("AuditMessagesTextWs")
public class AuditMessagesTextWs implements AuditMessagesTextWsApi {

    private static final Logger LOG = LoggerFactory.getLogger(AuditMessagesTextWs.class);

    @Override
    public AuditMessagesWrapper getAuditMessages(String domainKeyword, String transactionKeyword, String actorKeyword) throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAuditMessages");
        }
        AuditMessageQuery query = new AuditMessageQuery();
        if (domainKeyword != null) {
            query.issuingActor().actorIntegrationProfiles().integrationProfile().domainsForDP().keyword().eq(domainKeyword);
        }
        if (actorKeyword != null) {
            query.issuingActor().keyword().eq(actorKeyword);
        }
        if (transactionKeyword != null) {
            query.auditedTransaction().keyword().eq(transactionKeyword);
        }

        List<AuditMessage> auditMessages = query.getListDistinct();

        List<AuditMessageWrapper> auditMessagesList = new ArrayList<AuditMessageWrapper>();
        String keyword;
        for (AuditMessage auditMessage : auditMessages) {
            keyword = auditMessage.getAuditedEvent().getKeyword() + " - "
                    + auditMessage.getAuditedTransaction().getKeyword() + " - "
                    + auditMessage.getIssuingActor().getKeyword();

            auditMessagesList.add(new AuditMessageWrapper(auditMessage.getId(), keyword, keyword));
        }
        AuditMessagesWrapper auditMessagesWrapper = new AuditMessagesWrapper(auditMessagesList);

        auditMessagesWrapper.setBaseUrl(ApplicationPreferenceManager.instance().getApplicationUrl());

        return auditMessagesWrapper;
    }

    @Override
    public AuditMessageWrapper getAuditMessage(String id) throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAuditMessage");
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        AuditMessage auditMessage = em.find(AuditMessage.class, Integer.valueOf(id));
        String keyword = auditMessage.getAuditedEvent().getKeyword() + " - "
                + auditMessage.getAuditedTransaction().getKeyword() + " - "
                + auditMessage.getIssuingActor().getKeyword();
        AuditMessageWrapper auditMessageWrapper = new AuditMessageWrapper(auditMessage.getId(), keyword, keyword);

        auditMessageWrapper.setBaseUrl(ApplicationPreferenceManager.instance().getApplicationUrl());
        return auditMessageWrapper;
    }
}
