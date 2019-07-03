package net.ihe.gazelle.tf.remote;

import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.action.IntegrationProfileManager;
import net.ihe.gazelle.tf.model.*;
import net.ihe.gazelle.util.Pair;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Conversation;
import org.kohsuke.graphviz.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.context.FacesContext;
import javax.jws.WebParam;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.SOAPException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Stateful
@Name("IHEConceptsRemoteService")
@Scope(ScopeType.SESSION)
public class IHEConceptsRemoting implements Serializable, IHEConceptsRemotingLocal {
    private static final long serialVersionUID = 123562323577831L;
    private static final Logger LOG = LoggerFactory.getLogger(IHEConceptsRemoting.class);
    public Domain returnedDomain;
    public IntegrationProfile returnedIP;
    public Actor returnedActor;
    public Transaction returnedTransaction;
    public IntegrationProfileOption returnedIPO;

    @Override
    public List<String> getListOfPossibleDomains() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfPossibleDomains");
        }
        return Domain.getPossibleDomainKeywords();
    }

    @Override
    public Domain getDomainByKeyword(String inDomainKeyword) throws SOAPException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDomainByKeyword");
        }
        Conversation.instance().changeFlushMode(FlushModeType.MANUAL);
        if (inDomainKeyword.length() == 0) {
            SOAPException soapEx = new SOAPException("100: You need to provide a Domain keyword ");
            throw soapEx;
        }
        try {
            returnedDomain = Domain.getDomainByKeyword(inDomainKeyword);
        } catch (NoResultException e) {
            SOAPException soapEx = new SOAPException("200: Domain " + inDomainKeyword + " not found", e);
            throw soapEx;
        }
        returnedDomain.setIntegrationProfilesForDP(null);
        return returnedDomain;
    }

    @Override
    public List<String> getListOfIntegrationProfilesForGivenDomain(String inDomainKeyword) throws SOAPException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfIntegrationProfilesForGivenDomain");
        }
        if (inDomainKeyword.length() == 0) {
            SOAPException soapEx = new SOAPException(
                    "100: You have to provide a domain keyword if you want to retrieve the linked integration profiles");
            throw soapEx;
        }
        try {
            Domain d = Domain.getDomainByKeyword(inDomainKeyword);
            return IntegrationProfile.getPossibleIntegrationProfilesKeyword(d);
        } catch (NoResultException e) {
            SOAPException soapEx = new SOAPException("200: No domain found for keyword " + inDomainKeyword, e);
            throw soapEx;
        }
    }

    @Override
    public IntegrationProfile getIntegrationProfileByKeyword(String inIPKeyword) throws SOAPException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntegrationProfileByKeyword");
        }
        Conversation.instance().changeFlushMode(FlushModeType.MANUAL);
        if (inIPKeyword.length() == 0) {
            SOAPException soapEx = new SOAPException("100: You must provide an integration profile keyword");
            throw soapEx;
        }

        returnedIP = IntegrationProfile.findIntegrationProfileWithKeyword(inIPKeyword);
        if (returnedIP != null) {
            returnedIP.setDomainsForDP(null);
            returnedIP.setIntegrationProfileTypes(null);
            returnedIP.setListOfActorIntegrationProfileOption(null);
            return returnedIP;
        } else {
            SOAPException soapEx = new SOAPException("200: no integration profile found with keyword " + inIPKeyword);
            throw soapEx;
        }
    }

    @Override
    public List<String> getListOfActorsForGivenIntegrationProfile(
            @WebParam(name = "integrationProfileKeyword") String inIPKeyword) throws SOAPException {
        if (inIPKeyword.length() == 0) {
            SOAPException soapEx = new SOAPException("100: You must provide an integration profile keyword");
            throw soapEx;
        }

        IntegrationProfile iProfile = IntegrationProfile.findIntegrationProfileWithKeyword(inIPKeyword);
        if (iProfile != null) {
            return ActorIntegrationProfileOption.getPossibleActorsKeywordFromAIPO(iProfile);
        } else {
            SOAPException soapEx = new SOAPException("200: No integration profile matches the provided keyword");
            throw soapEx;
        }
    }

    @Override
    public Actor getActorByKeyword(String inActorKeyword) throws SOAPException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorByKeyword");
        }
        Conversation.instance().changeFlushMode(FlushModeType.MANUAL);
        if (inActorKeyword.length() == 0) {
            SOAPException soapex = new SOAPException("100: You must provide an actor keyword");
            throw soapex;
        }
        returnedActor = Actor.findActorWithKeyword(inActorKeyword);
        if (returnedActor != null) {
            returnedActor.setActorIntegrationProfileOption(null);
            returnedActor.setTransactionLinksWhereActingAsReceiver(null);
            returnedActor.setTransactionLinksWhereActingAsSource(null);
            return returnedActor;
        } else {
            SOAPException soapex = new SOAPException("200: No actor found for keyword " + inActorKeyword);
            throw soapex;
        }
    }

    @Override
    public Transaction getTransactionByKeyword(String inTransactionKeyword) throws SOAPException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTransactionByKeyword");
        }
        Conversation.instance().changeFlushMode(FlushModeType.MANUAL);
        if ((inTransactionKeyword == null) || (inTransactionKeyword.length() == 0)) {
            throw new SOAPException("100: You must provide a transaction keyword");
        }

        returnedTransaction = Transaction.GetTransactionByKeyword(inTransactionKeyword);

        if (returnedTransaction != null) {
            returnedTransaction.setProfileLinks(null);
            returnedTransaction.setTransactionLinks(null);
            return returnedTransaction;
        } else {
            throw new SOAPException("200: No Transaction found for the provided keyword " + inTransactionKeyword);
        }
    }

    @Override
    public IntegrationProfileOption getIntegrationProfileOptionByKeyword(
            @WebParam(name = "integrationProfileOptionKeyword") String inIPOKeyword) throws SOAPException {
        Conversation.instance().changeFlushMode(FlushModeType.MANUAL);
        if (inIPOKeyword.length() == 0) {
            throw new SOAPException("100: You must provide an integration profile option keyword");
        }

        returnedIPO = IntegrationProfileOption.findIntegrationProfileOptionWithKeyword(inIPOKeyword);

        if (returnedIPO != null) {
            returnedIPO.setActorIntegrationProfileOption(null);
            return returnedIPO;
        } else {
            throw new SOAPException("200: No Integration profile option found for provided keyword");
        }
    }

    @Override
    public List<String> getListOfIPOForGivenActorAndIP(
            @WebParam(name = "integrationProfileKeyword") String inIPKeyword,
            @WebParam(name = "actorKeyword") String inActorKeyword) throws SOAPException {
        if ((inActorKeyword.length() == 0) || (inIPKeyword.length() == 0)) {
            SOAPException soapex = new SOAPException(
                    "100: You must provide an actor keyword AND an integration profile keyword");
            throw soapex;
        }

        Actor a = Actor.findActorWithKeyword(inActorKeyword);
        IntegrationProfile ip = IntegrationProfile.findIntegrationProfileWithKeyword(inIPKeyword);

        if (a == null) {
            SOAPException soapex = new SOAPException("200: no actor found for the keyword " + inActorKeyword);
            throw soapex;
        } else if (ip == null) {
            SOAPException soapex = new SOAPException("200: no integration profile found for keyword " + inIPKeyword);
            throw soapex;
        } else {
            List<String> ipoKeywords = new ArrayList<String>();
            List<IntegrationProfileOption> ipoList = IntegrationProfileOption.getListOfIntegrationProfileOptions(a, ip);

            if (ipoList == null) {
                SOAPException soapex = new SOAPException(
                        "300: No Integration profile option found for provided actor and integration profile");
                throw soapex;
            }
            for (IntegrationProfileOption ipo : ipoList) {
                ipoKeywords.add(ipo.getKeyword());
            }

            return ipoKeywords;
        }
    }

    @Override
    public List<String> getListOfTransactionsForGivenActorAndIP(String inIntegrationProfileKeyword,
                                                                String inActorKeyword) throws SOAPException {
        Conversation.instance().changeFlushMode(FlushModeType.MANUAL);
        if ((inActorKeyword.length() == 0) || (inIntegrationProfileKeyword.length() == 0)) {
            SOAPException soapex = new SOAPException(
                    "100: You must provide an actor keyword AND an integration profile keyword");
            throw soapex;
        }

        Actor a = Actor.findActorWithKeyword(inActorKeyword);
        IntegrationProfile ip = IntegrationProfile.findIntegrationProfileWithKeyword(inIntegrationProfileKeyword);

        if (a == null) {
            SOAPException soapex = new SOAPException("200: no actor found for the keyword " + inActorKeyword);
            throw soapex;
        } else if (ip == null) {
            SOAPException soapex = new SOAPException("200: no integration profile found for keyword "
                    + inIntegrationProfileKeyword);
            throw soapex;
        } else {
            List<Transaction> transactionList = ProfileLink.getTransactionsByActorAndIP(a, ip);
            if (transactionList == null) {
                SOAPException soapex = new SOAPException(
                        "300: No Transaction found for the provided couple (Action, Integration Profile)");
                throw soapex;
            } else {
                List<String> transactionKeywords = new ArrayList<String>();
                for (Transaction t : transactionList) {
                    transactionKeywords.add(t.getKeyword());
                }

                return transactionKeywords;
            }
        }
    }

    @Override
    public List<String> getListOfActorsForGivenTransaction(String inTransactionKeyword) throws SOAPException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfActorsForGivenTransaction");
        }
        if ((inTransactionKeyword == null) || (inTransactionKeyword.length() == 0)) {
            throw new SOAPException("You must provide a transaction keyword");
        }

        Transaction t = Transaction.GetTransactionByKeyword(inTransactionKeyword);

        if (t == null) {
            throw new SOAPException("Cannot find transaction with keyword " + inTransactionKeyword);
        }

        List<ProfileLink> plList = t.getProfileLinks();
        if ((plList == null) || (plList.size() == 0)) {
            throw new SOAPException("No ProfileLink objects found for transation " + t.getName());
        }

        List<String> actors = new ArrayList<String>();

        for (ProfileLink pl : plList) {
            ActorIntegrationProfile aip = pl.getActorIntegrationProfile();
            if (aip != null) {
                if (!actors.contains(aip.getActor().getKeyword())) {
                    actors.add(aip.getActor().getKeyword());
                }
            }
        }
        return actors;
    }

    @Override
    public List<String> getListOfInitiatorsForGivenTransaction(String inTransactionKeyword) throws SOAPException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfInitiatorsForGivenTransaction");
        }
        if ((inTransactionKeyword == null) || (inTransactionKeyword.length() == 0)) {
            throw new SOAPException("You must provide a keyword");
        }

        try {
            Transaction t = Transaction.GetTransactionByKeyword(inTransactionKeyword);
            if (t == null) {
                throw new SOAPException("the returned transaction is null");
            }

            List<Actor> actors = TransactionLink.getFromActorsForTransaction(t);

            if ((actors == null) || (actors.size() == 0)) {
                throw new SOAPException("No initiator found for transaction " + t.getName());
            }

            List<String> actorKeywords = new ArrayList<String>();
            for (Actor a : actors) {
                actorKeywords.add(a.getKeyword());
            }

            return actorKeywords;
        } catch (NoResultException nrex) {
            throw new SOAPException("Cannot find transaction with keyword " + inTransactionKeyword, nrex.getCause());
        }
    }

    @Override
    public List<String> getListOfRespondersForGivenTransaction(String inTransactionKeyword) throws SOAPException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfRespondersForGivenTransaction");
        }
        if ((inTransactionKeyword == null) || (inTransactionKeyword.length() == 0)) {
            throw new SOAPException("You must provide a keyword");
        }

        try {
            Transaction t = Transaction.GetTransactionByKeyword(inTransactionKeyword);
            if (t == null) {
                throw new SOAPException("Cannot find transaction with keyword " + inTransactionKeyword);
            }

            List<Actor> actors = TransactionLink.getToActorsForTransaction(t);

            if ((actors == null) || (actors.size() == 0)) {
                throw new SOAPException("No responder found for transaction " + t.getName());
            }

            List<String> actorKeywords = new ArrayList<String>();
            for (Actor a : actors) {
                actorKeywords.add(a.getKeyword());
            }

            return actorKeywords;
        } catch (NoResultException nrex) {
            throw new SOAPException("Cannot find transaction with keyword " + inTransactionKeyword, nrex.getCause());
        }
    }

    @Override
    public List<TransactionLink> getListOfTransactionLinksForGivenIntegrationProfile(String inIPKeyword) throws SOAPException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfTransactionLinksForGivenIntegrationProfile");
        }

        if ((inIPKeyword == null) || (inIPKeyword.length() == 0)) {
            throw new SOAPException("You must provide an integration profile keyword");
        }

        IntegrationProfile ip = IntegrationProfile.findIntegrationProfileWithKeyword(inIPKeyword);

        EntityManager em = EntityManagerService.provideEntityManager();
        Session s = (Session) em.getDelegate();
        List<Actor> actors = IntegrationProfile.getActors(inIPKeyword);
        List<Transaction> transactions = IntegrationProfile.getTransactions(inIPKeyword);

        if (actors == null) {
            throw new SOAPException("no actors for the given integration profile");
        }

        if (transactions == null) {
            throw new SOAPException("no transactions for the given integration profile");
        }

        Criteria c = s.createCriteria(TransactionLink.class);
        c.add(Restrictions.and(
                Restrictions.and(Restrictions.in("toActor", actors), Restrictions.in("fromActor", actors)),
                Restrictions.in("transaction", transactions)));
        List<TransactionLink> tlList = c.list();

        if (tlList.size() == 0) {
            throw new SOAPException("No transaction link found for integration profile with keyword : " + inIPKeyword);
        } else {
            List<ProfileLink> plList = ProfileLink.FindProfileLinksForIntegrationProfile(ip);
            List<TransactionLink> returnedList = new ArrayList<TransactionLink>();
            List<Pair<ActorIntegrationProfile, Transaction>> pairList = new ArrayList<Pair<ActorIntegrationProfile, Transaction>>();
            for (ProfileLink pl : plList) {
                pairList.add(new Pair<ActorIntegrationProfile, Transaction>(new ActorIntegrationProfile(pl
                        .getActorIntegrationProfile().getActor(), pl.getActorIntegrationProfile()
                        .getIntegrationProfile()), pl.getTransaction()));
            }
            for (TransactionLink tl : tlList) {
                ActorIntegrationProfile aipWithFromActor = new ActorIntegrationProfile(tl.getFromActor(), ip);
                Pair<ActorIntegrationProfile, Transaction> pairWithFromActor = new Pair<ActorIntegrationProfile, Transaction>(
                        aipWithFromActor, tl.getTransaction());
                ActorIntegrationProfile aipWithToActor = new ActorIntegrationProfile(tl.getToActor(), ip);
                Pair<ActorIntegrationProfile, Transaction> pairWithToActor = new Pair<ActorIntegrationProfile, Transaction>(
                        aipWithToActor, tl.getTransaction());
                if (pairList.contains(pairWithFromActor) && pairList.contains(pairWithToActor)) {
                    returnedList.add(tl);
                }
            }

            return returnedList;
        }
    }

    @Override
    public String getStringOfGraphForGivenListOfTransactionLinks(String inIPKeyword) throws SOAPException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getStringOfGraphForGivenListOfTransactionLinks");
        }
        if ((inIPKeyword == null) || (inIPKeyword.length() == 0)) {
            throw new SOAPException("You must provide an integration profile keyword");
        }

        List<TransactionLink> tlList = this.getListOfTransactionLinksForGivenIntegrationProfile(inIPKeyword);

        if (tlList.size() == 0) {
            throw new SOAPException("No transaction link found for integration profile with keyword : " + inIPKeyword);
        } else {
            Graph g = IntegrationProfileManager.generateGraphForGivenListOfTransactionLinks(tlList);

            String returnedString;

            List<String> commands = new ArrayList<String>();

            commands.add("dot");
            commands.add("-Txdot");

            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                g.generateTo(commands, out);
                returnedString = out.toString();
            } catch (InterruptedException e) {
                throw new SOAPException("No response from server", e);
            } catch (IOException e) {
                throw new SOAPException("I/O operation failed", e);
            }

            if ((returnedString == null) || (returnedString.length() == 0)) {
                throw new SOAPException("String returned doesn't have content");
            }

            return returnedString;
        }
    }

    @Override
    public void getProfileDiagram(String inIPKeyword) throws SOAPException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProfileDiagram");
        }
        if ((inIPKeyword == null) || (inIPKeyword.length() == 0)) {
            throw new SOAPException("You must provide an integration profile keyword");
        }

        List<TransactionLink> tlList = this.getListOfTransactionLinksForGivenIntegrationProfile(inIPKeyword);

        if (tlList.size() == 0) {
            throw new SOAPException("No transaction link found for integration profile with keyword : " + inIPKeyword);
        } else {
            ByteArrayOutputStream outputStream;
            Graph g = IntegrationProfileManager.generateGraphForGivenListOfTransactionLinks(tlList);

            List<String> commands = new ArrayList<String>();

            commands.add("dot");
            commands.add("-Tgif");

            try {
                outputStream = new ByteArrayOutputStream();
                g.generateTo(commands, outputStream);

                FacesContext context = FacesContext.getCurrentInstance();
                HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

                response.setContentType("image/gif");
                response.setHeader("Content-Disposition", "attachment;filename=\"" + inIPKeyword + "_graph\"");

                ServletOutputStream servletOutputStream;
                servletOutputStream = response.getOutputStream();

                servletOutputStream.write(outputStream.toByteArray());

                servletOutputStream.flush();
                servletOutputStream.close();

                context.responseComplete();
            } catch (Exception e) {
                LOG.error("", e);
            }
        }
    }

    @Override
    @Destroy
    @Remove
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }
    }
}
