package net.ihe.gazelle.tf.ws;

import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.HQLRestriction;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.tf.dao.AuditMessageDAO;
import net.ihe.gazelle.tf.model.*;
import net.ihe.gazelle.tf.model.auditMessage.AuditMessage;
import net.ihe.gazelle.tf.model.auditMessage.AuditMessageQuery;
import net.ihe.gazelle.tf.model.auditMessage.TriggerEvent;
import net.ihe.gazelle.tf.ws.data.ActorWrapper;
import net.ihe.gazelle.tf.ws.data.AuditMessageWrapper;
import net.ihe.gazelle.tf.ws.data.AuditMessagesWrapper;
import net.ihe.gazelle.tf.ws.data.TransactionWrapper;
import net.ihe.gazelle.tm.systems.model.System;
import net.ihe.gazelle.tm.systems.model.SystemActorProfilesQuery;
import net.ihe.gazelle.tm.systems.model.SystemQuery;
import org.jboss.seam.annotations.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.List;

@Stateless
@Name("auditMessageWs")
public class AuditMessageWs implements AuditMessageWsApi {

    private static final String ALL = "all";
    private static final Logger LOG = LoggerFactory.getLogger(AuditMessageWs.class);

    private static AuditMessageWrapper wrapAuditMessage(AuditMessage message) {
        AuditMessageWrapper wrapper = new AuditMessageWrapper();
        if (message.getIssuingActor() != null) {
            wrapper.setIssuingActor(new ActorWrapper(message.getIssuingActor().getKeyword(), message.getIssuingActor()
                    .getName()));
        }
        if (message.getAuditedEvent() != null) {
            StringBuilder event = new StringBuilder(message.getAuditedEvent().getKeyword());
            if (message.getEventCodeType() != null && !message.getEventCodeType().isEmpty()) {
                event.append(" (");
                event.append(message.getEventCodeType());
                event.append(')');
            }
            wrapper.setAuditedEvent(event.toString());
        }
        if (message.getAuditedTransaction() != null) {
            wrapper.setAuditedTransaction(new TransactionWrapper(message.getAuditedTransaction().getKeyword(), message
                    .getAuditedTransaction().getName()));
        }
        if (message.getOid() != null && !message.getOid().isEmpty()) {
            wrapper.setOid(message.getOid());
        }
        return wrapper;
    }

    @Override
    public Response getAuditMessages() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAuditMessages");
        }
        AuditMessagesWrapper wrapper = new AuditMessagesWrapper();
        for (AuditMessage message : AuditMessageDAO.getAllAuditMessages()) {
            wrapper.addAuditMessageWrapper(wrapAuditMessage(message));
        }
        return Response.ok(wrapper).build();
    }

    @Override
    public Response getAuditMessagesFiltered(String actorKeyword, String transactionKeyword, String event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAuditMessagesFiltered");
        }
        AuditMessagesWrapper wrapper = new AuditMessagesWrapper();
        Actor actor = null;
        Transaction transaction = null;
        TriggerEvent triggerEvent = null;
        if (!actorKeyword.isEmpty() && !actorKeyword.equalsIgnoreCase(ALL)) {
            actor = Actor.findActorWithKeyword(actorKeyword);
            if (actor == null) {
                return Response.status(Status.NOT_FOUND)
                        .header("Warning", actorKeyword + " does not reference a known actor").build();
            }
        }
        if (!transactionKeyword.isEmpty() && !transactionKeyword.equalsIgnoreCase(ALL)) {
            transaction = Transaction.GetTransactionByKeyword(transactionKeyword);
            if (transaction == null) {
                return Response.status(Status.NOT_FOUND)
                        .header("Warning", transactionKeyword + " does not reference a known transaction").build();
            }
        }
        if (!event.isEmpty() && !event.equalsIgnoreCase(ALL)) {
            triggerEvent = TriggerEvent.getTriggerEventForKeyword(event);
            if (triggerEvent == null) {
                return Response.status(Status.NOT_FOUND)
                        .header("Warning", event + " does not reference an existing trigger event").build();
            }
        }

        for (AuditMessage message : AuditMessageDAO.getAuditMessagesFiltered(actor, transaction, triggerEvent)) {
            wrapper.addAuditMessageWrapper(wrapAuditMessage(message));
        }
        return Response.ok(wrapper).build();
    }

    @Override
    public Response getAuditMessagesToBeProducedBySystemInSession(String systemKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAuditMessagesToBeProducedBySystem");
        }
        AuditMessagesWrapper wrapper = new AuditMessagesWrapper();
        if (systemKeyword.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).header("Warning", "The keyword of the system is mandatory")
                    .build();
        } else {
            System system = System.getSystemByAllKeyword(systemKeyword);
            if (system == null) {
                return Response.status(Status.NOT_FOUND)
                        .header("Warning", systemKeyword + " does not reference a known system").build();
            }
            getAuditMessagesForSystem(wrapper, system);
        }
        return Response.ok(wrapper).build();
    }

    private void getAuditMessagesForSystem(AuditMessagesWrapper wrapper, System system) {
        SystemActorProfilesQuery squery = new SystemActorProfilesQuery();
        squery.system().eq(system);
        List<ActorIntegrationProfileOption> aipos = squery.actorIntegrationProfileOption().getListDistinct();
        List<HQLRestriction> singleRestrictions = new ArrayList<HQLRestriction>();
        singleRestrictions.addAll(getHQLRestrictionOnTransactionLinksForSystem(true, aipos));
        singleRestrictions.addAll(getHQLRestrictionOnTransactionLinksForSystem(false, aipos));
        HQLRestriction[] array2 = new HQLRestriction[singleRestrictions.size()];
        HQLQueryBuilder<AuditMessage> builder2 = new HQLQueryBuilder<AuditMessage>(AuditMessage.class);
        builder2.addRestriction(HQLRestrictions.or(singleRestrictions.toArray(array2)));
        List<AuditMessage> auditMessages = builder2.getList();
        for (AuditMessage message : auditMessages) {
            wrapper.addAuditMessageWrapper(wrapAuditMessage(message));
        }
    }


    @Override
    @GET
    @Path("{testingSessionId}/{system}/auditMessages")
    @Produces("text/xml")
    public Response getAuditMessagesToBeProducedBySystemForSession(@PathParam("system") String systemKeyword, @PathParam("testingSessionId") String
            testingSessionId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAuditMessagesToBeProducedBySystem");
        }
        AuditMessagesWrapper wrapper = new AuditMessagesWrapper();
        if (systemKeyword.isEmpty() || testingSessionId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).header("Warning", "The keyword of the system and the testing session id are mandatory")
                    .build();
        } else {
            Integer intTestingSessionId;
            try {
                intTestingSessionId = Integer.parseInt(testingSessionId);
            } catch (NumberFormatException e) {
                return Response.status(Status.NOT_FOUND)
                        .header("Warning", " testingSessionId should be an integer, found " + testingSessionId).build();
            }
            SystemQuery query = new SystemQuery();
            query.keyword().eq(systemKeyword);
            query.systemsInSession().testingSession().id().eq(intTestingSessionId);
            System system = query.getUniqueResult();
            if (system == null) {
                return Response.status(Status.NOT_FOUND)
                        .header("Warning", systemKeyword + " does not reference a known system").build();
            }
            getAuditMessagesForSystem(wrapper, system);
        }
        return Response.ok(wrapper).build();
    }

    private List<HQLRestriction> getHQLRestrictionOnTransactionLinksForSystem(boolean initiator,
                                                                              List<ActorIntegrationProfileOption> aipos) {
        List<HQLRestriction> restrictionsAnd = new ArrayList<HQLRestriction>();
        for (ActorIntegrationProfileOption aipo : aipos) {
            TransactionLinkQuery tquery = new TransactionLinkQuery();
            HQLRestriction and = null;
            if (initiator) {
                and = HQLRestrictions.and(tquery.fromActor()
                        .eqRestriction(aipo.getActorIntegrationProfile().getActor()), tquery.transaction()
                        .profileLinks().actorIntegrationProfile().eqRestriction(aipo.getActorIntegrationProfile()));
            } else {
                and = HQLRestrictions.and(
                        tquery.toActor().eqRestriction(aipo.getActorIntegrationProfile().getActor()),
                        tquery.transaction().profileLinks().actorIntegrationProfile()
                                .eqRestriction(aipo.getActorIntegrationProfile()));
            }
            restrictionsAnd.add(and);
        }
        HQLRestriction[] array = new HQLRestriction[restrictionsAnd.size()];
        HQLQueryBuilder<TransactionLink> builder = new HQLQueryBuilder<TransactionLink>(TransactionLink.class);
        builder.addRestriction(HQLRestrictions.or(restrictionsAnd.toArray(array)));
        List<TransactionLink> links = builder.getList();
        List<HQLRestriction> singleRestrictions = new ArrayList<HQLRestriction>();
        for (TransactionLink tlink : links) {
            AuditMessageQuery query = new AuditMessageQuery();
            HQLRestriction and = null;
            if (initiator) {
                and = HQLRestrictions.and(query.issuingActor().eqRestriction(tlink.getFromActor()),
                        query.auditedTransaction().eqRestriction(tlink.getTransaction()));
            } else {
                and = HQLRestrictions.and(query.issuingActor().eqRestriction(tlink.getToActor()), query
                        .auditedTransaction().eqRestriction(tlink.getTransaction()));
            }
            singleRestrictions.add(and);
        }
        return singleRestrictions;
    }

}
