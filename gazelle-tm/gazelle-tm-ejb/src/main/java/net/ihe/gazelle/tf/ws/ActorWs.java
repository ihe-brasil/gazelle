package net.ihe.gazelle.tf.ws;

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.Actor;
import net.ihe.gazelle.tf.model.ActorQuery;
import net.ihe.gazelle.tf.ws.data.ActorWrapper;
import net.ihe.gazelle.tf.ws.data.ActorsNameWrapper;
import net.ihe.gazelle.tf.ws.data.ActorsWrapper;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestQuery;
import org.jboss.seam.annotations.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.ws.rs.QueryParam;
import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;

@Stateless
@Name("actorWs")
public class ActorWs implements ActorWsApi {
    private static final Logger LOG = LoggerFactory.getLogger(ActorWs.class);

    @Override
    public ActorWrapper getActor(String id) throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActor");
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        Actor actor = em.find(Actor.class, Integer.valueOf(id));
        ActorWrapper actorWrapper = new ActorWrapper(actor.getId(), actor.getKeyword(), actor.getName(),
                actor.getDescription());

        actorWrapper.setBaseUrl(ApplicationPreferenceManager.instance().getApplicationUrl());
        return actorWrapper;
    }

    @Override
    public ActorsWrapper getActors(String domain, String integrationProfile) throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActors");
        }
        ActorHelper query = new ActorHelper(domain, integrationProfile);
        List<Actor> actors = query.getListDistinct();

        List<ActorWrapper> actorWrapperList = new ArrayList<ActorWrapper>();

        for (Actor actor : actors) {
            actorWrapperList.add(new ActorWrapper(actor.getId(), actor.getKeyword(), actor.getName()));
        }
        ActorsWrapper actorsWrapper = new ActorsWrapper(actorWrapperList);

        actorsWrapper.setBaseUrl(ApplicationPreferenceManager.instance().getApplicationUrl());

        return actorsWrapper;
    }

    @Override
    public ActorsNameWrapper getActorsNames(String domain, String integrationProfile, String transactionKeyword)
            throws JAXBException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorsNames");
        }
        ActorHelper query = new ActorHelper(domain, integrationProfile);
        query.addTransactionConstraint(transactionKeyword);
        List<Actor> actors = query.getListDistinct();

        List<String> actorsNames = new ArrayList<String>();

        for (Actor actor : actors) {
            actorsNames.add(actor.getKeyword());
        }

        return new ActorsNameWrapper(actorsNames);
    }

    @Override
    public ActorsNameWrapper getActorsNamesWithAuditMessage(@QueryParam("domain") String domain,
                                                            @QueryParam("integrationProfile") String integrationProfile,
                                                            @QueryParam("transactionKeyword") String transactionKeyword) throws JAXBException {

        ActorHelper query = new ActorHelper(domain, integrationProfile);
        query.addAuditMessageTransactionConstraint(transactionKeyword);
        query.auditMessages().isNotEmpty();
        List<Actor> actors = query.getListDistinct();

        List<String> actorsNames = new ArrayList<String>();

        for (Actor actor : actors) {
            actorsNames.add(actor.getKeyword());
        }

        return new ActorsNameWrapper(actorsNames);
    }

    public ActorsNameWrapper getActorsNamesWithTest(@QueryParam("domain") String domain,
                                                    @QueryParam("integrationProfile") String integrationProfile,
                                                    @QueryParam("transactionKeyword") String transactionKeyword) throws JAXBException {

        TestQuery query = new TestQuery();
        if (domain != null && !"".equals(domain)) {
            query.testRoles().roleInTest().testParticipantsList().actorIntegrationProfileOption()
                    .actorIntegrationProfile().integrationProfile().domainsProfile().domain().keyword().eq(domain);
        }
        if (integrationProfile != null && !"".equals(integrationProfile)) {
            query.testRoles().roleInTest().testParticipantsList().actorIntegrationProfileOption()
                    .actorIntegrationProfile().integrationProfile().keyword().eq(integrationProfile);
        }
        if (transactionKeyword != null && !"".equals(transactionKeyword)) {
            query.testStepsList().transaction().keyword().eq(transactionKeyword);
        }
        List<String> actorsNames = query.testRoles().roleInTest().testParticipantsList().actorIntegrationProfileOption()
                .actorIntegrationProfile().actor().keyword().getListDistinct();

        return new ActorsNameWrapper(actorsNames);
    }

    private class ActorHelper extends ActorQuery {
        ActorHelper(String domain, String integrationProfile) {
            addDomainConstraint(domain);
            addIntegrationProfileConstraint(integrationProfile);
        }

        void addTransactionConstraint(String transactionKeyword) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addTransactionConstraint");
            }
            if (transactionKeyword != null && !"".equals(transactionKeyword)) {
                this.actorIntegrationProfiles().profileLinks().transaction().keyword().eq(transactionKeyword);
            }
        }

        void addIntegrationProfileConstraint(String integrationProfile) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addIntegrationProfileConstraint");
            }
            if (integrationProfile != null && !"".equals(integrationProfile)) {
                this.actorIntegrationProfiles().integrationProfile().keyword().eq(integrationProfile);
            }
        }

        void addDomainConstraint(String domain) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addDomainConstraint");
            }
            if (domain != null && !"".equals(domain)) {
                this.actorIntegrationProfiles().integrationProfile().domainsForDP().keyword().eq(domain);
            }
        }

        void addAuditMessageTransactionConstraint(String transactionKeyword) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("addAuditMessageTransactionConstraint");
            }
            if (transactionKeyword != null && !"".equals(transactionKeyword)) {
                this.auditMessages().auditedTransaction().keyword().eq(transactionKeyword);
            }
        }
    }
}

