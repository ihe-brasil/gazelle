package net.ihe.gazelle.tf.ws;

import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.action.IntegrationProfileManager;
import net.ihe.gazelle.tf.model.*;
import net.ihe.gazelle.util.Pair;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.core.Conversation;
import org.kohsuke.graphviz.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.faces.context.FacesContext;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.SOAPException;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Stateless
@Name("IHEConceptsService")
@WebService(name = "IHEConcepts", serviceName = "IHEConceptsService")
public class IHEConcepts implements Serializable, IHEConceptsRemote, IHEConceptsLocal {
    private static final String YOU_MUST_PROVIDE_A_KEYWORD = "You must provide a keyword";
    private static final String TRANSACTION_KEYWORD = "transactionKeyword";
    private static final String INTEGRATION_PROFILE_KEYWORD = "integrationProfileKeyword";
    private static final String DOMAIN_KEYWORD = "domainKeyword";
    private static final String CANNOT_FIND_TRANSACTION_WITH_KEYWORD = "Cannot find transaction with keyword ";
    private static final String ACTOR_KEYWORD = "actorKeyword";
    private static final long serialVersionUID = 123562323577831L;
    private static final Logger LOG = LoggerFactory.getLogger(IHEConcepts.class);

    @Override
    @WebMethod
    @WebResult(name = DOMAIN_KEYWORD)
    @RequestWrapper(localName = "getListOfPossibleDomains", className = "net.ihe.gazelle.tf.ws.IHEConcepts.GetListOfPossibleDomains")
    @ResponseWrapper(localName = "getListOfPossibleDomainsResponse", className = "net.ihe.gazelle.tf.ws.IHEConcepts.GetListOfPossibleDomainsResponse")
    public List<String> getListOfPossibleDomains() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfPossibleDomains");
        }
        List<String> domains = Domain.getPossibleDomainKeywords();
        Collections.sort(domains);
        return domains;
    }

    @Override
    @WebMethod
    @WebResult(name = "returnedDomain")
    @RequestWrapper(localName = "getDomainByKeyword", className = "net.ihe.gazelle.tf.ws.IHEConcepts.GetDomainByKeyword")
    @ResponseWrapper(localName = "getDomainByKeywordResponse", className = "net.ihe.gazelle.tf.ws.IHEConcepts.GetDomainByKeywordResponse")
    public Domain getDomainByKeyword(@WebParam(name = DOMAIN_KEYWORD) String inDomainKeyword) throws SOAPException {
        Conversation.instance().changeFlushMode(FlushModeType.MANUAL);

        if ((null == inDomainKeyword) || (inDomainKeyword.length() == 0)) {
            throw new SOAPException("100: You need to provide a Domain keyword ");
        }
        inDomainKeyword = inDomainKeyword.trim();
        Domain returnedDomain;
        try {
            returnedDomain = Domain.getDomainByKeyword(inDomainKeyword);
            if (null == returnedDomain) {
                inDomainKeyword = inDomainKeyword.toUpperCase();
                returnedDomain = Domain.getDomainByKeyword(inDomainKeyword);
            }
        } catch (NoResultException e) {
            throw new SOAPException("200: Domain " + inDomainKeyword + " not found", e);
        }
        if (null == returnedDomain) {
            throw new SOAPException("200: Domain " + inDomainKeyword + " not found");
        }
        returnedDomain.setIntegrationProfilesForDP(null);
        return returnedDomain;
    }

    @Override
    public List<String> getListOfIntegrationProfilesForGivenDomainWithNodeId(String inDomainKeyword, Integer nodeId) throws SOAPException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfIntegrationProfilesForGivenDomainWithNodeId");
        }
        try {
            List<String> newList = new ArrayList<String>();
            newList.add(nodeId.toString());
            newList.addAll(getListOfIntegrationProfilesForGivenDomain(inDomainKeyword));
            Collections.sort(newList);
            return newList;
        } catch (Exception e) {
            throw new SOAPException(e);
        }

    }

    @Override
    @WebMethod
    @WebResult(name = INTEGRATION_PROFILE_KEYWORD)
    @RequestWrapper(localName = "getListOfIntegrationProfilesForGivenDomain", className = "net.ihe.gazelle.tf.ws.IHEConcepts" +
            ".GetListOfIntegrationProfilesForGivenDomain")
    @ResponseWrapper(localName = "getListOfIntegrationProfilesForGivenDomainResponse", className = "net.ihe.gazelle.tf.ws.IHEConcepts" +
            ".GetListOfIntegrationProfilesForGivenDomainResponse")
    public List<String> getListOfIntegrationProfilesForGivenDomain(
            @WebParam(name = DOMAIN_KEYWORD) String inDomainKeyword) throws SOAPException {
        if ((null == inDomainKeyword) || (inDomainKeyword.length() == 0)) {
            throw new SOAPException(
                    "100: You have to provide a domain keyword if you want to retrieve the linked integration profiles");
        }
        inDomainKeyword = inDomainKeyword.trim();
        try {
            Domain d = Domain.getDomainByKeyword(inDomainKeyword);
            if (null == d) {
                inDomainKeyword = inDomainKeyword.toUpperCase();
                d = Domain.getDomainByKeyword(inDomainKeyword);
            }
            if (null == d) {
                throw new SOAPException("200: No domain found for keyword " + inDomainKeyword);
            }
            return IntegrationProfile.getPossibleIntegrationProfilesKeyword(d);
        } catch (NoResultException e) {
            throw new SOAPException("200: No domain found for keyword " + inDomainKeyword, e);
        }

    }

    @Override
    @WebMethod
    @WebResult(name = INTEGRATION_PROFILE_KEYWORD)
    @RequestWrapper(localName = "getListOfAllPossibleIP", className = "net.ihe.gazelle.tf.ws.IHEConcepts.GetListOfAllPossibleIP")
    @ResponseWrapper(localName = "getListOfAllPossibleIPResponse", className = "net.ihe.gazelle.tf.ws.IHEConcepts.GetListOfAllPossibleIPResponse")
    public List<String> getListOfAllPossibleIP() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfAllPossibleIP");
        }
        List<String> ipKeywords = new ArrayList<String>();
        List<IntegrationProfile> list = IntegrationProfile.listAllIntegrationProfiles();
        for (IntegrationProfile ip : list) {
            ipKeywords.add(ip.getKeyword());
        }
        return ipKeywords;
    }

    @Override
    @WebMethod
    @WebResult(name = "returnedIntegrationProfile")
    @RequestWrapper(localName = "getIntegrationProfileByKeyword", className = "net.ihe.gazelle.tf.ws.IHEConcepts.GetIntegrationProfileByKeyword")
    @ResponseWrapper(localName = "getIntegrationProfileByKeywordResponse", className = "net.ihe.gazelle.tf.ws.IHEConcepts" +
            ".getIntegrationProfileByKeywordResponse")
    public IntegrationProfile getIntegrationProfileByKeyword(
            @WebParam(name = INTEGRATION_PROFILE_KEYWORD) String inIPKeyword) throws SOAPException {
        Conversation.instance().changeFlushMode(FlushModeType.MANUAL);
        if ((null == inIPKeyword) || (inIPKeyword.length() == 0)) {
            throw new SOAPException("100: You must provide an integration profile keyword");
        }
        inIPKeyword = inIPKeyword.trim();
        IntegrationProfile returnedIP = IntegrationProfile.findIntegrationProfileWithKeyword(inIPKeyword);
        if (null == returnedIP) {
            inIPKeyword = inIPKeyword.toUpperCase();
            returnedIP = IntegrationProfile.findIntegrationProfileWithKeyword(inIPKeyword);
        }

        if (returnedIP != null) {
            returnedIP.setDomainsForDP(null);
            returnedIP.setIntegrationProfileTypes(null);
            returnedIP.setListOfActorIntegrationProfileOption(null);
            returnedIP.setActorIntegrationProfiles(null);
            return returnedIP;
        } else {
            throw new SOAPException("200: no integration profile found with keyword " + inIPKeyword);
        }
    }

    @Override
    @WebMethod
    @WebResult(name = ACTOR_KEYWORD)
    @RequestWrapper(localName = "getListOfActorsForGivenIntegrationProfile", className = "net.ihe.gazelle.tf.ws.IHEConcepts" +
            ".GetListOfActorsForGivenIntegrationProfile")
    @ResponseWrapper(localName = "getListOfActorsForGivenIntegrationProfileResponse", className = "net.ihe.gazelle.tf.ws.IHEConcepts" +
            ".GetListOfActorsForGivenIntegrationProfileResponse")
    public List<String> getListOfActorsForGivenIntegrationProfile(
            @WebParam(name = INTEGRATION_PROFILE_KEYWORD) String inIPKeyword) throws SOAPException {
        if ((null == inIPKeyword) || (inIPKeyword.length() == 0)) {
            throw new SOAPException("100: You must provide an integration profile keyword");
        }
        inIPKeyword = inIPKeyword.trim();
        IntegrationProfile iProfile = IntegrationProfile.findIntegrationProfileWithKeyword(inIPKeyword);

        if (null == iProfile) {
            inIPKeyword = inIPKeyword.toUpperCase();
            iProfile = IntegrationProfile.findIntegrationProfileWithKeyword(inIPKeyword);
        }

        if (iProfile != null) {
            return ActorIntegrationProfileOption.getPossibleActorsKeywordFromAIPO(iProfile);
        } else {
            throw new SOAPException("200: No integration profile matches the provided keyword");
        }
    }

    @Override
    @WebMethod
    @WebResult(name = ACTOR_KEYWORD)
    @RequestWrapper(localName = "getListOfActorsForGivenDomain", className = "net.ihe.gazelle.tf.ws.IHEConcepts.GetListOfActorsForGivenDomain")
    @ResponseWrapper(localName = "getListOfActorsForGivenDomainResponse", className = "net.ihe.gazelle.tf.ws.IHEConcepts" +
            ".GetListOfActorsForGivenDomainResponse")
    public List<String> getListOfActorsForGivenDomain(@WebParam(name = DOMAIN_KEYWORD) String inDomainKeyword)
            throws SOAPException {
        if ((null == inDomainKeyword) || (inDomainKeyword.length() == 0)) {
            throw new SOAPException("100: You must provide a domain keyword");
        }
        inDomainKeyword = inDomainKeyword.trim();

        try {
            Domain d = Domain.getDomainByKeyword(inDomainKeyword);
            if (null == d) {
                inDomainKeyword = inDomainKeyword.toUpperCase();
                d = Domain.getDomainByKeyword(inDomainKeyword);
            }
            List<String> ipKeywords = IntegrationProfile.getPossibleIntegrationProfilesKeyword(d);
            List<String> actorKeywords = new ArrayList<String>();
            if (null == ipKeywords) {
                throw new SOAPException("200: no domain found for provided keyword " + inDomainKeyword);
            }
            for (String ipk : ipKeywords) {
                IntegrationProfile ip = IntegrationProfile.findIntegrationProfileWithKeyword(ipk);
                if (ip != null) {
                    actorKeywords.addAll(ActorIntegrationProfileOption.getPossibleActorsKeywordFromAIPO(ip));
                }
            }
            return actorKeywords;
        } catch (NoResultException e) {
            throw new SOAPException("200: no domain found for provided keyword " + inDomainKeyword, e);
        }
    }

    @Override
    @WebMethod
    @WebResult(name = ACTOR_KEYWORD)
    @RequestWrapper(localName = "getListOfAllPossibleActors", className = "net.ihe.gazelle.tf.ws.IHEConcepts.GetListOfAllPossibleActors")
    @ResponseWrapper(localName = "getListOfAllPossibleActorsResponse", className = "net.ihe.gazelle.tf.ws.IHEConcepts" +
            ".GetListOfAllPossibleActorsResponse")
    public List<String> getListOfAllPossibleActors() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfAllPossibleActors");
        }
        List<String> actorKeywords = new ArrayList<String>();
        List<Actor> list = Actor.listAllActors();

        for (Actor a : list) {
            actorKeywords.add(a.getKeyword());
        }
        return actorKeywords;
    }

    @Override
    @WebMethod
    @WebResult(name = "returnedActor")
    @RequestWrapper(localName = "getActorByKeyword", className = "net.ihe.gazelle.tf.ws.IHEConcepts.GetActorByKeyword")
    @ResponseWrapper(localName = "getActorByKeywordResponse", className = "net.ihe.gazelle.tf.ws.IHEConcepts.GetActorByKeywordResponse")
    public Actor getActorByKeyword(@WebParam(name = ACTOR_KEYWORD) String inActorKeyword) throws SOAPException {
        Conversation.instance().changeFlushMode(FlushModeType.MANUAL);
        if ((null == inActorKeyword) || (inActorKeyword.length() == 0)) {
            throw new SOAPException("100: You must provide an actor keyword");
        }
        inActorKeyword = inActorKeyword.trim();
        Actor returnedActor = Actor.findActorWithKeyword(inActorKeyword);
        if (null == returnedActor) {
            inActorKeyword = inActorKeyword.toUpperCase();
            returnedActor = Actor.findActorWithKeyword(inActorKeyword);
        }
        if (returnedActor != null) {
            returnedActor.setActorIntegrationProfileOption(null);
            returnedActor.setTransactionLinksWhereActingAsReceiver(null);
            returnedActor.setTransactionLinksWhereActingAsSource(null);
            return returnedActor;
        } else {
            throw new SOAPException("200: No actor found for keyword " + inActorKeyword);
        }
    }

    @Override
    @WebMethod
    @WebResult(name = TRANSACTION_KEYWORD)
    @RequestWrapper(localName = "getListOfTransactionsForGivenActorAndIP", className = "net.ihe.gazelle.tf.ws.IHEConcepts" +
            ".GetListOfTransactionsForGivenActorAndIP")
    @ResponseWrapper(localName = "getListOfTransactionsForGivenActorAndIPResponse", className = "net.ihe.gazelle.tf.ws.IHEConcepts" +
            ".GetListOfTransactionsForGivenActorAndIPResponse")
    public List<String> getListOfTransactionsForGivenActorAndIP(
            @WebParam(name = INTEGRATION_PROFILE_KEYWORD) String inIntegrationProfileKeyword,
            @WebParam(name = ACTOR_KEYWORD) String inActorKeyword) throws SOAPException {
        Conversation.instance().changeFlushMode(FlushModeType.MANUAL);
        if ((null == inIntegrationProfileKeyword) || (null == inActorKeyword) || (inActorKeyword.length() == 0) || (inIntegrationProfileKeyword
                .length() == 0)) {
            throw new SOAPException(
                    "100: You must provide an actor keyword AND an integration profile keyword");
        }

        inActorKeyword = inActorKeyword.trim();
        inIntegrationProfileKeyword = inIntegrationProfileKeyword.trim();

        Actor a = Actor.findActorWithKeyword(inActorKeyword);
        IntegrationProfile ip = IntegrationProfile.findIntegrationProfileWithKeyword(inIntegrationProfileKeyword);

        if (null == a) {
            inActorKeyword = inActorKeyword.toUpperCase();
            a = Actor.findActorWithKeyword(inActorKeyword);
        }
        if (null == ip) {
            inIntegrationProfileKeyword = inIntegrationProfileKeyword.toUpperCase();
            ip = IntegrationProfile.findIntegrationProfileWithKeyword(inIntegrationProfileKeyword);
        }

        if (a == null) {
            throw new SOAPException("200: no actor found for the keyword " + inActorKeyword);
        } else if (ip == null) {
            throw new SOAPException("200: no integration profile found for keyword "
                    + inIntegrationProfileKeyword);
        } else {
            List<Transaction> transactionList = ProfileLink.getTransactionsByActorAndIP(a, ip);
            if (transactionList == null) {
                throw new SOAPException(
                        "300: No Transaction found for the provided couple (Action, Integration Profile)");
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
    @WebMethod
    @WebResult(name = TRANSACTION_KEYWORD)
    @RequestWrapper(localName = "getListOfTransactionsForGivenActor", className = "net.ihe.gazelle.tf.ws.IHEConcepts" +
            ".GetListOfTransactionsForGivenActor")
    @ResponseWrapper(localName = "getListOfTransactionsForGivenActorReponse", className = "net.ihe.gazelle.tf.ws.IHEConcepts" +
            ".GetListOfTransactionsForGivenActorResponse")
    public List<String> getListOfTransactionsForGivenActor(@WebParam(name = ACTOR_KEYWORD) String inActorKeyword)
            throws SOAPException {

        if ((null == inActorKeyword) || (inActorKeyword.length() == 0)) {
            throw new SOAPException("100: You must provide an actor keyword");
        }
        inActorKeyword = inActorKeyword.trim();
        Actor a = Actor.findActorWithKeyword(inActorKeyword);
        if (null == a) {
            inActorKeyword = inActorKeyword.toUpperCase();
            a = Actor.findActorWithKeyword(inActorKeyword);
        }
        if (a == null) {
            throw new SOAPException("200: no actor found for the keyword " + inActorKeyword);
        }

        List<ActorIntegrationProfile> aipList = a.getActorIntegrationProfiles();

        if (aipList == null) {
            throw new SOAPException("200: no actor integration profile found for actor " + a.getName());
        }

        List<ProfileLink> plList = new ArrayList<ProfileLink>();

        for (ActorIntegrationProfile aip : aipList) {
            plList.addAll(aip.getProfileLinks());
        }
        List<String> transactionKeywords = new ArrayList<String>();

        for (ProfileLink pl : plList) {
            String kwd = pl.getTransaction().getKeyword();
            if (!transactionKeywords.contains(kwd)) {
                transactionKeywords.add(kwd);
            }
        }

        return transactionKeywords;
    }

    @Override
    @WebMethod
    @WebResult(name = TRANSACTION_KEYWORD)
    @RequestWrapper(localName = "getListOfTransactionsForGivenIP", className = "net.ihe.gazelle.tf.ws.IHEConcepts.GetListOfTransactionsForGivenIP")
    @ResponseWrapper(localName = "getListOfTransactionsForGivenIPResponse", className = "net.ihe.gazelle.tf.ws.IHEConcepts" +
            ".GetListOfTransactionsForGivenIPResponse")
    public List<String> getListOfTransactionsForGivenIP(@WebParam(name = INTEGRATION_PROFILE_KEYWORD) String inIPKeyword)
            throws SOAPException {
        if ((null == inIPKeyword) || (inIPKeyword.length() == 0)) {
            throw new SOAPException("100: You must provide an integration profile keyword");
        }

        inIPKeyword = inIPKeyword.trim();

        IntegrationProfile ip = IntegrationProfile.findIntegrationProfileWithKeyword(inIPKeyword);

        if (null == ip) {
            inIPKeyword = inIPKeyword.toUpperCase();
            ip = IntegrationProfile.findIntegrationProfileWithKeyword(inIPKeyword);
        }

        if (ip == null) {
            throw new SOAPException("200: no integration profile found for keyword " + inIPKeyword);
        }

        List<ActorIntegrationProfile> aipList = ip.getActorIntegrationProfiles();

        if (aipList == null) {
            throw new SOAPException("400: no actor integration profile found for integration profile "
                    + ip.getName());
        }
        List<ProfileLink> plList = new ArrayList<ProfileLink>();

        for (ActorIntegrationProfile aip : aipList) {
            plList.addAll(aip.getProfileLinks());
        }

        List<String> transactionKeywords = new ArrayList<String>();
        for (ProfileLink pl : plList) {
            String kwd = pl.getTransaction().getKeyword();
            if (!transactionKeywords.contains(kwd)) {
                transactionKeywords.add(kwd);
            }
        }

        return transactionKeywords;
    }

    @Override
    @WebMethod
    @WebResult(name = TRANSACTION_KEYWORD)
    @RequestWrapper(localName = "getListOfTransactionsForGivenDomain", className = "net.ihe.gazelle.tf.ws.IHEConcepts" +
            ".GetListOfTransactionsForGivenDomain")
    @ResponseWrapper(localName = "getListOfTransactionsForGivenDomainResponse", className = "net.ihe.gazelle.tf.ws.IHEConcepts" +
            ".GetListOfTransactionsForGivenDomainResponse")
    public List<String> getListOfTransactionsForGivenDomain(@WebParam(name = DOMAIN_KEYWORD) String inDomainKeyword)
            throws SOAPException {
        if ((null == inDomainKeyword) || (inDomainKeyword.length() == 0)) {
            throw new SOAPException("100: You must provide a domain keyword");
        }
        inDomainKeyword = inDomainKeyword.trim();
        try {
            Domain d = Domain.getDomainByKeyword(inDomainKeyword);
            if (null == d) {
                inDomainKeyword = inDomainKeyword.toUpperCase();
                d = Domain.getDomainByKeyword(inDomainKeyword);
            }
            List<IntegrationProfile> ipList = IntegrationProfile.getPossibleIntegrationProfiles(d);
            List<ActorIntegrationProfile> aipList = new ArrayList<ActorIntegrationProfile>();
            List<ProfileLink> plList = new ArrayList<ProfileLink>();

            for (IntegrationProfile ip : ipList) {
                aipList.addAll(ip.getActorIntegrationProfiles());
            }
            for (ActorIntegrationProfile aip : aipList) {
                plList.addAll(aip.getProfileLinks());
            }

            List<String> transactionKeywords = new ArrayList<String>();
            for (ProfileLink pl : plList) {
                String kwd = pl.getTransaction().getKeyword();
                if (!transactionKeywords.contains(kwd)) {
                    transactionKeywords.add(kwd);
                }
            }

            return transactionKeywords;
        } catch (NoResultException e) {
            throw new SOAPException("200: no domain matches the provided keyword " + inDomainKeyword, e);
        }
    }

    @Override
    @WebMethod
    @WebResult(name = "returnedTransaction")
    @RequestWrapper(localName = "getTransactionByKeyword", className = "net.ihe.gazelle.tf.ws.IHEConcepts.GetTransactionByKeyword")
    @ResponseWrapper(localName = "getTransactionByKeywordResponse", className = "net.ihe.gazelle.tf.ws.IHEConcepts.GetTransactionByKeywordResponse")
    public Transaction getTransactionByKeyword(@WebParam(name = TRANSACTION_KEYWORD) String inTransactionKeyword)
            throws SOAPException {
        Conversation.instance().changeFlushMode(FlushModeType.MANUAL);
        if ((null == inTransactionKeyword) || (inTransactionKeyword.length() == 0)) {
            throw new SOAPException("100: You must provide a transaction keyword");
        }

        inTransactionKeyword = inTransactionKeyword.trim();
        Transaction returnedTransaction = Transaction.GetTransactionByKeyword(inTransactionKeyword);

        if (null == returnedTransaction) {
            inTransactionKeyword = inTransactionKeyword.toUpperCase();
            returnedTransaction = Transaction.GetTransactionByKeyword(inTransactionKeyword);
        }

        if (returnedTransaction != null) {
            returnedTransaction.setProfileLinks(null);
            returnedTransaction.setTransactionLinks(null);
            return returnedTransaction;
        } else {
            throw new SOAPException("200: No Transaction found for the provided keyword " + inTransactionKeyword);
        }

    }

    @Override
    @WebMethod
    @WebResult(name = "integrationProfileOptionKeyword")
    @RequestWrapper(localName = "getListOfIPOForGivenActorAndIP", className = "net.ihe.gazelle.tf.ws.IHEConcepts.GetListOfIPOForGivenActorAndIP")
    @ResponseWrapper(localName = "getListOfIPOForGivenActorAndIPResponse", className = "net.ihe.gazelle.tf.ws.IHEConcepts" +
            ".GetListOfIPOForGivenActorAndIPResponse")
    public List<String> getListOfIPOForGivenActorAndIP(
            @WebParam(name = INTEGRATION_PROFILE_KEYWORD) String inIPKeyword,
            @WebParam(name = ACTOR_KEYWORD) String inActorKeyword) throws SOAPException {
        if ((null == inActorKeyword) || (null == inIPKeyword) || (inActorKeyword.length() == 0) || (inIPKeyword.length() == 0)) {
            throw new SOAPException(
                    "100: You must provide an actor keyword AND an integration profile keyword");
        }

        inActorKeyword = inActorKeyword.trim();
        inIPKeyword = inIPKeyword.trim();

        Actor a = Actor.findActorWithKeyword(inActorKeyword);
        IntegrationProfile ip = IntegrationProfile.findIntegrationProfileWithKeyword(inIPKeyword);

        if (null == a) {
            inActorKeyword = inActorKeyword.toUpperCase();
            a = Actor.findActorWithKeyword(inActorKeyword);
        }
        if (null == ip) {
            inIPKeyword = inIPKeyword.toUpperCase();
            ip = IntegrationProfile.findIntegrationProfileWithKeyword(inIPKeyword);
        }

        if (a == null) {
            throw new SOAPException("200: no actor found for the keyword " + inActorKeyword);
        } else if (ip == null) {
            throw new SOAPException("200: no integration profile found for keyword " + inIPKeyword);
        } else {
            List<String> ipoKeywords = new ArrayList<String>();
            List<IntegrationProfileOption> ipoList = IntegrationProfileOption.getListOfIntegrationProfileOptions(a, ip);

            if (ipoList == null) {
                throw new SOAPException(
                        "300: No Integration profile option found for provided actor and integration profile");
            }
            for (IntegrationProfileOption ipo : ipoList) {
                ipoKeywords.add(ipo.getKeyword());
            }
            return ipoKeywords;
        }
    }

    @Override
    @WebMethod
    @WebResult(name = "returnedIntegrationProfileOption")
    @RequestWrapper(localName = "getIntegrationProfileOptionByKeyword", className = "net.ihe.gazelle.tf.ws.IHEConcepts" +
            ".GetIntegrationProfileOptionByKeyword")
    @ResponseWrapper(localName = "getIntegrationProfileOptionByKeywordResponse", className = "net.ihe.gazelle.tf.ws.IHEConcepts" +
            ".GetIntegrationProfileOptionByKeywordResponse")
    public IntegrationProfileOption getIntegrationProfileOptionByKeyword(
            @WebParam(name = "integrationProfileOptionKeyword") String inIPOKeyword) throws SOAPException {
        Conversation.instance().changeFlushMode(FlushModeType.MANUAL);
        if ((null == inIPOKeyword) || (inIPOKeyword.length() == 0)) {
            throw new SOAPException("100: You must provide an integration profile option keyword");
        }
        inIPOKeyword = inIPOKeyword.trim();
        IntegrationProfileOption returnedIPO = IntegrationProfileOption.findIntegrationProfileOptionWithKeyword(inIPOKeyword);

        if (null == returnedIPO) {
            inIPOKeyword = inIPOKeyword.toUpperCase();
            returnedIPO = IntegrationProfileOption.findIntegrationProfileOptionWithKeyword(inIPOKeyword);
        }

        if (returnedIPO != null) {
            returnedIPO.setActorIntegrationProfileOption(null);
            return returnedIPO;
        } else {
            throw new SOAPException("200: No Integration profile option found for provided keyword");
        }
    }

    @Override
    @WebMethod
    @WebResult(name = ACTOR_KEYWORD)
    public List<String> getListOfActorsForGivenTransaction(
            @WebParam(name = TRANSACTION_KEYWORD) String inTransactionKeyword) throws SOAPException {
        if ((inTransactionKeyword == null) || (inTransactionKeyword.length() == 0)) {
            throw new SOAPException("You must provide a transaction keyword");
        }

        inTransactionKeyword = inTransactionKeyword.trim();

        Transaction t = Transaction.GetTransactionByKeyword(inTransactionKeyword);

        if (null == t) {
            inTransactionKeyword = inTransactionKeyword.toUpperCase();
            t = Transaction.GetTransactionByKeyword(inTransactionKeyword);
        }

        if (t == null) {
            throw new SOAPException(CANNOT_FIND_TRANSACTION_WITH_KEYWORD + inTransactionKeyword);
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
    @WebMethod
    @WebResult(name = "initiatorKeyword")
    public List<String> getListOfInitiatorsForGivenTransaction(
            @WebParam(name = TRANSACTION_KEYWORD) String inTransactionKeyword) throws SOAPException {
        if ((inTransactionKeyword == null) || (inTransactionKeyword.length() == 0)) {
            throw new SOAPException(YOU_MUST_PROVIDE_A_KEYWORD);
        }
        inTransactionKeyword = inTransactionKeyword.trim();


        try {
            Transaction t = Transaction.GetTransactionByKeyword(inTransactionKeyword);
            if (null == t) {
                inTransactionKeyword = inTransactionKeyword.toUpperCase();
                t = Transaction.GetTransactionByKeyword(inTransactionKeyword);
            }
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
            throw new SOAPException(CANNOT_FIND_TRANSACTION_WITH_KEYWORD + inTransactionKeyword, nrex.getCause());
        }
    }

    @Override
    @WebMethod
    @WebResult(name = "responderKeyword")
    public List<String> getListOfRespondersForGivenTransaction(
            @WebParam(name = TRANSACTION_KEYWORD) String inTransactionKeyword) throws SOAPException {
        if ((inTransactionKeyword == null) || (inTransactionKeyword.length() == 0)) {
            throw new SOAPException(YOU_MUST_PROVIDE_A_KEYWORD);
        }
        inTransactionKeyword = inTransactionKeyword.trim();

        try {
            Transaction t = Transaction.GetTransactionByKeyword(inTransactionKeyword);
            if (null == t) {
                inTransactionKeyword = inTransactionKeyword.toUpperCase();
                t = Transaction.GetTransactionByKeyword(inTransactionKeyword);
            }
            if (t == null) {
                throw new SOAPException(CANNOT_FIND_TRANSACTION_WITH_KEYWORD + inTransactionKeyword);
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
            throw new SOAPException(CANNOT_FIND_TRANSACTION_WITH_KEYWORD + inTransactionKeyword, nrex.getCause());
        }
    }

    @Override
    @WebMethod
    @WebResult(name = "AIPO")
    public List<ActorIntegrationProfileOption> getListOfAIP0ByTransactionForInitiators(
            @WebParam(name = TRANSACTION_KEYWORD) String inTransactionKeyword) throws SOAPException {
        Conversation.instance().changeFlushMode(FlushModeType.MANUAL);
        if ((inTransactionKeyword == null) || (inTransactionKeyword.length() == 0)) {
            throw new SOAPException(YOU_MUST_PROVIDE_A_KEYWORD);
        }
        inTransactionKeyword = inTransactionKeyword.trim();

        try {
            Transaction t = Transaction.GetTransactionByKeyword(inTransactionKeyword);
            if (null == t) {
                inTransactionKeyword = inTransactionKeyword.toUpperCase();
                t = Transaction.GetTransactionByKeyword(inTransactionKeyword);
            }

            if (t == null) {
                throw new SOAPException(CANNOT_FIND_TRANSACTION_WITH_KEYWORD + inTransactionKeyword);
            }
            try {
                List<ActorIntegrationProfileOption> aipoList = ProfileLink
                        .getAIPOByTransactionForActorActingAsFromActor(t);
                List<ActorIntegrationProfileOption> listOut = new ArrayList<ActorIntegrationProfileOption>();
                for (ActorIntegrationProfileOption aipo : aipoList) {
                    IntegrationProfileOption ipo = aipo.getIntegrationProfileOption();
                    ipo.setActorIntegrationProfileOption(null);
                    aipo.setIntegrationProfileOption(ipo);

                    ActorIntegrationProfile aip = aipo.getActorIntegrationProfile();
                    aip.setActorIntegrationProfileOption(null);
                    aip.setProfileLinks(null);

                    Actor a = aip.getActor();
                    a.setActorIntegrationProfileOption(null);
                    a.setTransactionLinksWhereActingAsReceiver(null);
                    a.setTransactionLinksWhereActingAsSource(null);
                    aip.setActor(a);

                    IntegrationProfile ip = aip.getIntegrationProfile();
                    ip.setDomainsForDP(null);
                    ip.setIntegrationProfileTypes(null);
                    ip.setListOfActorIntegrationProfileOption(null);
                    ip.setActorIntegrationProfiles(null);

                    aip.setIntegrationProfile(ip);
                    aipo.setActorIntegrationProfile(aip);

                    listOut.add(aipo);
                }
                return listOut;
            } catch (NoResultException nrex) {
                throw new SOAPException("Cannot find Actor Integration Profile Option for transaction " + t.getName(), nrex.getCause());
            }

        } catch (NoResultException nrex) {
            throw new SOAPException(CANNOT_FIND_TRANSACTION_WITH_KEYWORD + inTransactionKeyword, nrex.getCause());
        }
    }

    @Override
    @WebMethod
    @WebResult(name = "AIPO")
    public List<ActorIntegrationProfileOption> getListOfAIPOByTransactionForResponders(
            @WebParam(name = TRANSACTION_KEYWORD) String inTransactionKeyword) throws SOAPException {
        if ((inTransactionKeyword == null) || (inTransactionKeyword.length() == 0)) {
            throw new SOAPException(YOU_MUST_PROVIDE_A_KEYWORD);
        }
        inTransactionKeyword = inTransactionKeyword.trim();

        try {
            Transaction t = Transaction.GetTransactionByKeyword(inTransactionKeyword);
            if (null == t) {
                inTransactionKeyword = inTransactionKeyword.toUpperCase();
                t = Transaction.GetTransactionByKeyword(inTransactionKeyword);
            }
            if (t == null) {
                throw new SOAPException(CANNOT_FIND_TRANSACTION_WITH_KEYWORD + inTransactionKeyword);
            }
            try {
                List<ActorIntegrationProfileOption> aipoList = ProfileLink
                        .getAIPOByTransactionForActorActingAsToActor(t);
                List<ActorIntegrationProfileOption> listOut = new ArrayList<ActorIntegrationProfileOption>();
                for (ActorIntegrationProfileOption aipo : aipoList) {
                    IntegrationProfileOption ipo = aipo.getIntegrationProfileOption();
                    ipo.setActorIntegrationProfileOption(null);
                    aipo.setIntegrationProfileOption(ipo);

                    ActorIntegrationProfile aip = aipo.getActorIntegrationProfile();
                    aip.setActorIntegrationProfileOption(null);
                    aip.setProfileLinks(null);

                    Actor a = aip.getActor();
                    a.setActorIntegrationProfileOption(null);
                    a.setTransactionLinksWhereActingAsReceiver(null);
                    a.setTransactionLinksWhereActingAsSource(null);
                    aip.setActor(a);

                    IntegrationProfile ip = aip.getIntegrationProfile();
                    ip.setDomainsForDP(null);
                    ip.setIntegrationProfileTypes(null);
                    ip.setListOfActorIntegrationProfileOption(null);
                    ip.setActorIntegrationProfiles(null);

                    aip.setIntegrationProfile(ip);
                    aipo.setActorIntegrationProfile(aip);

                    listOut.add(aipo);
                }
                return listOut;
            } catch (NoResultException nrex) {
                throw new SOAPException("Cannot find Actor Integration Profile Option for transaction " + t.getName(), nrex.getCause());
            }

        } catch (NoResultException nrex) {
            throw new SOAPException(CANNOT_FIND_TRANSACTION_WITH_KEYWORD + inTransactionKeyword, nrex.getCause());
        }
    }

    @Override
    @WebMethod
    @WebResult(name = "AIPO")
    public List<ActorIntegrationProfileOption> getAllAIPO() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllAIPO");
        }
        Conversation.instance().changeFlushMode(FlushModeType.MANUAL);
        List<ActorIntegrationProfileOption> AIPOs = ActorIntegrationProfileOption
                .listAllActorIntegrationProfileOption();

        List<ActorIntegrationProfileOption> symplifiedAIPOsList = new ArrayList<ActorIntegrationProfileOption>();

        // for each AIPO, we must set all attributes of type "List" to null in order to avoid loops

        for (ActorIntegrationProfileOption aipo : AIPOs) {
            IntegrationProfileOption ipo = aipo.getIntegrationProfileOption();
            ipo.setActorIntegrationProfileOption(null);
            aipo.setIntegrationProfileOption(ipo);

            ActorIntegrationProfile aip = aipo.getActorIntegrationProfile();
            aip.setActorIntegrationProfileOption(null);
            aip.setProfileLinks(null);

            Actor a = aip.getActor();
            a.setActorIntegrationProfileOption(null);
            a.setTransactionLinksWhereActingAsReceiver(null);
            a.setTransactionLinksWhereActingAsSource(null);
            aip.setActor(a);

            IntegrationProfile ip = aip.getIntegrationProfile();
            ip.setDomainsForDP(null);
            ip.setIntegrationProfileTypes(null);
            ip.setListOfActorIntegrationProfileOption(null);
            ip.setActorIntegrationProfiles(null);

            aip.setIntegrationProfile(ip);
            aipo.setActorIntegrationProfile(aip);

            symplifiedAIPOsList.add(aipo);
        }

        return symplifiedAIPOsList;
    }

    @Override
    @WebMethod
    @WebResult(name = TRANSACTION_KEYWORD)
    public List<String> getListOfAllPossibleTransactions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfAllPossibleTransactions");
        }
        List<Transaction> transactionList = Transaction.getAllTransactions();
        List<String> returnedList = new ArrayList<String>();
        for (Transaction t : transactionList) {
            returnedList.add(t.getKeyword());
        }
        return returnedList;
    }

    @Override
    @WebMethod
    @WebResult(name = "initiatorKeyword")
    public List<String> getListOfAllInitiators() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfAllInitiators");
        }
        List<Actor> actorList = TransactionLink.getAllFromActors();
        List<String> returnedList = new ArrayList<String>();
        for (Actor a : actorList) {
            returnedList.add(a.getKeyword());
        }
        return returnedList;
    }

    @Override
    @WebMethod
    @WebResult(name = "responderKeyword")
    public List<String> getListOfAllResponders() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfAllResponders");
        }
        List<Actor> actorList = TransactionLink.getAllToActors();
        List<String> returnedList = new ArrayList<String>();
        for (Actor a : actorList) {
            returnedList.add(a.getKeyword());
        }
        return returnedList;
    }

    @Override
    @WebMethod
    @WebResult(name = "transactionLink")
    public List<TransactionLink> getListOfTransactionLinksForGivenIntegrationProfile(
            @WebParam(name = "integrationprofilekeyword") String inIPKeyword) throws SOAPException {

        if ((inIPKeyword == null) || (inIPKeyword.length() == 0)) {
            throw new SOAPException("You must provide an integration profile keyword");
        }
        inIPKeyword = inIPKeyword.trim();
        IntegrationProfile ip = IntegrationProfile.findIntegrationProfileWithKeyword(inIPKeyword);
        if (null == ip) {
            inIPKeyword = inIPKeyword.toUpperCase();
            ip = IntegrationProfile.findIntegrationProfileWithKeyword(inIPKeyword);
        }
        EntityManager em = EntityManagerService.provideEntityManager();
        Session s = (Session) em.getDelegate();
        List<Actor> actors = IntegrationProfile.getActors(inIPKeyword);
        if (actors == null) {
            throw new SOAPException("no actors for the given integration profile");
        }

        for (Actor a : actors) {

        }
        List<Transaction> transactions = IntegrationProfile.getTransactions(inIPKeyword);
        if (null == transactions) {
            throw new SOAPException("No transaction link found for integration profile with keyword : " + inIPKeyword);
        }
        for (Transaction t : transactions) {

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
    @WebMethod
    @WebResult(name = "returnedActors")
    public List<Actor> getListOfActorObjectsForGivenDomain(@WebParam(name = DOMAIN_KEYWORD) String inDomainKeyword)
            throws SOAPException {
        if ((null == inDomainKeyword) || (inDomainKeyword.length() == 0)) {
            throw new SOAPException("You must provide a domain keyword");
        }
        inDomainKeyword = inDomainKeyword.trim();

        try {
            Domain d = Domain.getDomainByKeyword(inDomainKeyword);
            if (null == d) {
                inDomainKeyword = inDomainKeyword.toUpperCase();
                d = Domain.getDomainByKeyword(inDomainKeyword);
            }
            List<String> ipKeywords = IntegrationProfile.getPossibleIntegrationProfilesKeyword(d);
            List<Actor> actors = new ArrayList<Actor>();
            if (null == ipKeywords) {
                throw new SOAPException("no domain found for provided keyword " + inDomainKeyword);
            }
            for (String ipk : ipKeywords) {
                IntegrationProfile ip = IntegrationProfile.findIntegrationProfileWithKeyword(ipk);
                if (ip != null) {
                    actors.addAll(ActorIntegrationProfileOption.getActorsForIntegrationProfile(ip));
                }
            }
            return actors;
        } catch (NoResultException e) {
            throw new SOAPException("no domain found for provided keyword " + inDomainKeyword, e);
        }
    }

    @Override
    @WebMethod
    @WebResult(name = "returnedDomains")
    public List<Domain> getListOfPossibleDomainObjects() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfPossibleDomainObjects");
        }
        return Domain.getPossibleDomains();
    }

    @Override
    @WebMethod
    @WebResult(name = "returnedTransactions")
    public List<Transaction> getListOfTransactionObjectsForGivenActor(
            @WebParam(name = ACTOR_KEYWORD) String inActorKeyword) throws SOAPException {
        if ((inActorKeyword == null) || (inActorKeyword.length() == 0)) {
            throw new SOAPException("You must provide an actor keyword");
        }
        inActorKeyword = inActorKeyword.trim();
        Actor a = Actor.findActorWithKeyword(inActorKeyword);
        if (null == a) {
            inActorKeyword = inActorKeyword.toUpperCase();
            a = Actor.findActorWithKeyword(inActorKeyword);
        }
        if (a == null) {
            throw new SOAPException("no actor found for the keyword " + inActorKeyword);
        }

        List<ActorIntegrationProfile> aipList = a.getActorIntegrationProfiles();

        if (aipList == null) {
            throw new SOAPException("no actor integration profile found for actor " + a.getName());
        }

        List<ProfileLink> plList = new ArrayList<ProfileLink>();

        for (ActorIntegrationProfile aip : aipList) {
            plList.addAll(aip.getProfileLinks());
        }

        List<Transaction> transactions = new ArrayList<Transaction>();

        for (ProfileLink pl : plList) {
            Transaction t = pl.getTransaction();
            Hibernate.initialize(t.getTransactionLinks());
            if (!transactions.contains(t)) {
                transactions.add(t);
            }
        }

        return transactions;
    }

    @Override
    @WebMethod
    @WebResult(name = "returnedGraph")
    public String getStringOfGraphForGivenListOfTransactionLinks(
            @WebParam(name = "integrationprofilekeyword") String inIPKeyword) throws SOAPException {
        if ((inIPKeyword == null) || (inIPKeyword.length() == 0)) {
            throw new SOAPException("You must provide an integration profile keyword");
        }
        inIPKeyword = inIPKeyword.trim();

        List<TransactionLink> tlList = this.getListOfTransactionLinksForGivenIntegrationProfile(inIPKeyword);

        if (null == tlList) {
            inIPKeyword = inIPKeyword.toUpperCase();
            tlList = this.getListOfTransactionLinksForGivenIntegrationProfile(inIPKeyword);
        }

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
        inIPKeyword = inIPKeyword.trim();

        List<TransactionLink> tlList = this.getListOfTransactionLinksForGivenIntegrationProfile(inIPKeyword);

        if (null == tlList) {
            inIPKeyword = inIPKeyword.toUpperCase();
            tlList = this.getListOfTransactionLinksForGivenIntegrationProfile(inIPKeyword);
        }

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
    @WebMethod
    @WebResult(name = "actors")
    public List<Actor> getActorObjectsForIntegrationProfile(
            @WebParam(name = INTEGRATION_PROFILE_KEYWORD) String inProfileKeyword) {
        if ((inProfileKeyword == null) || (inProfileKeyword.length() == 0)) {
            return null;
        }
        inProfileKeyword = inProfileKeyword.trim();
        IntegrationProfile profile = IntegrationProfile.findIntegrationProfileWithKeyword(inProfileKeyword);
        if (null == profile) {
            inProfileKeyword = inProfileKeyword.toUpperCase();
            profile = IntegrationProfile.findIntegrationProfileWithKeyword(inProfileKeyword);
        }

        if (profile != null) {
            List<Actor> actors = profile.getActors();
            Collections.sort(actors);
            return actors;
        }
        return null;
    }

    @Override
    @WebMethod
    @WebResult(name = "domains")
    public List<Domain> getAllDomainsObject() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllDomainsObject");
        }
        List<Domain> domains = Domain.getPossibleDomains();
        Collections.sort(domains);
        return domains;
    }

    @Override
    @WebMethod
    @WebResult(name = "initiators")
    public List<Actor> getInitiatorObjectsForTransaction(
            @WebParam(name = TRANSACTION_KEYWORD) String inTransactionKeyword) {
        if ((inTransactionKeyword == null) || (inTransactionKeyword.length() == 0)) {
            return null;
        }
        inTransactionKeyword = inTransactionKeyword.trim();
        Transaction transaction = Transaction.GetTransactionByKeyword(inTransactionKeyword);

        if (null == transaction) {
            inTransactionKeyword = inTransactionKeyword.toUpperCase();
            transaction = Transaction.GetTransactionByKeyword(inTransactionKeyword);
        }

        if (transaction != null) {
            List<Actor> initiators = TransactionLink.getFromActorsForTransaction(transaction);
            Collections.sort(initiators);
            if (initiators.size()<1){
                return null;
            }else {
                return initiators;
            }
        } else {
            return null;
        }
    }

    @Override
    @WebMethod
    @WebResult(name = "integrationProfiles")
    public List<IntegrationProfile> getIntegrationProfileObjectsForDomain(
            @WebParam(name = DOMAIN_KEYWORD) String inDomainKeyword) {
        if ((inDomainKeyword == null) || (inDomainKeyword.length() == 0)) {
            return null;
        }
        inDomainKeyword = inDomainKeyword.trim();
        Domain domain = Domain.getDomainByKeyword(inDomainKeyword);
        if (null == domain) {
            inDomainKeyword = inDomainKeyword.toUpperCase();
            Domain.getDomainByKeyword(inDomainKeyword);
        }
        if (domain != null) {
            List<IntegrationProfile> profiles = domain.getIntegrationProfilesForDP();
            Collections.sort(profiles);
            return profiles;
        }
        return null;
    }

    @Override
    @WebMethod
    @WebResult(name = "responders")
    public List<Actor> getResponderObjectsForTransaction(
            @WebParam(name = TRANSACTION_KEYWORD) String inTransactionKeyword) {
        if ((inTransactionKeyword == null) || (inTransactionKeyword.length() == 0)) {
            return null;
        }
        inTransactionKeyword = inTransactionKeyword.trim();
        Transaction transaction = Transaction.GetTransactionByKeyword(inTransactionKeyword);

        if (null == transaction) {
            inTransactionKeyword = inTransactionKeyword.toUpperCase();
            transaction = Transaction.GetTransactionByKeyword(inTransactionKeyword);
        }
        if (transaction != null) {
            List<Actor> responders = TransactionLink.getToActorsForTransaction(transaction);
            Collections.sort(responders);
            if (responders.size()<1){
                return null;
            }else {
                return responders;
            }
        }
        return null;
    }

    @Override
    @WebMethod
    @WebResult(name = "transactions")
    public List<Transaction> getTransactionObjectsForActorAndIP(
            @WebParam(name = INTEGRATION_PROFILE_KEYWORD) String inProfileKeyword,
            @WebParam(name = ACTOR_KEYWORD) String inActorKeyword) {
        if ((inProfileKeyword == null) || (inProfileKeyword.length() == 0) || (inActorKeyword == null)
                || (inActorKeyword.length() == 0)) {
            return null;
        }
        inActorKeyword = inActorKeyword.trim();
        inProfileKeyword = inProfileKeyword.trim();

        IntegrationProfile ip = IntegrationProfile.findIntegrationProfileWithKeyword(inProfileKeyword);
        Actor actor = Actor.findActorWithKeyword(inActorKeyword);
        if (null == actor) {
            inActorKeyword = inActorKeyword.toUpperCase();
            actor = Actor.findActorWithKeyword(inActorKeyword);
        }
        if (null == ip) {
            inProfileKeyword = inProfileKeyword.toUpperCase();
            ip = IntegrationProfile.findIntegrationProfileWithKeyword(inProfileKeyword);
        }
        if ((ip != null) && (actor != null)) {
            List<Transaction> transactions = ProfileLink.getTransactionsByActorAndIP(actor, ip);
            for (Transaction t : transactions) {
                t.setProfileLinks(null);
                t.setTransactionLinks(null);
            }
            Collections.sort(transactions);
            return transactions;
        }
        return null;
    }

}
