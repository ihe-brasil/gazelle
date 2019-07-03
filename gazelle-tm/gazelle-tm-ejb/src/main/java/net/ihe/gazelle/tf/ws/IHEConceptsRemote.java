package net.ihe.gazelle.tf.ws;

import net.ihe.gazelle.tf.model.*;

import javax.ejb.Remote;
import javax.jws.WebParam;
import javax.xml.soap.SOAPException;
import java.util.List;

@Remote
public interface IHEConceptsRemote {
    public static final String TRANSACTION_KEYWORD = "transactionKeyword";
    public static final String INTEGRATION_PROFILE_KEYWORD = "integrationProfileKeyword";
    public static final String DOMAIN_KEYWORD = "domainKeyword";
    public static final String ACTOR_KEYWORD = "actorKeyword";

    /**
     * Returns the list of the keywords of all available domains defined in the technical framework
     *
     * @return
     */
    public List<String> getListOfPossibleDomains();

    public List<Domain> getListOfPossibleDomainObjects();

    /**
     * Returns a domain selected by its keyword Note: some attributes are set to null in order to avoid error due to loop
     *
     * @param inDomainKeyword
     * @return
     * @throws SOAPException
     */
    public Domain getDomainByKeyword(@WebParam(name = DOMAIN_KEYWORD) String inDomainKeyword) throws SOAPException;

    public List<String> getListOfIntegrationProfilesForGivenDomainWithNodeId(String inDomainKeyword, Integer nodeId)
            throws SOAPException;

    /**
     * Returns the list of integration profiles for a given domain
     *
     * @param inDomainKeyword
     * @return
     * @throws SOAPException
     */
    public List<String> getListOfIntegrationProfilesForGivenDomain(
            @WebParam(name = DOMAIN_KEYWORD) String inDomainKeyword) throws SOAPException;

    /**
     * Returns the list of all integration profiles recorded into database
     *
     * @return
     * @throws SOAPException
     */
    public List<String> getListOfAllPossibleIP();

    /**
     * Returns an Integration profile selected by its keyword Note: some attributes are set to null in order to avoid error due to loop
     *
     * @param inIPKeyword
     * @return
     * @throws SOAPException
     */
    public IntegrationProfile getIntegrationProfileByKeyword(
            @WebParam(name = INTEGRATION_PROFILE_KEYWORD) String inIPKeyword) throws SOAPException;

    /**
     * Returns a list of all actors for a given integration profile
     *
     * @param inIPKeyword
     * @return
     * @throws SOAPException
     */
    public List<String> getListOfActorsForGivenIntegrationProfile(
            @WebParam(name = INTEGRATION_PROFILE_KEYWORD) String inIPKeyword) throws SOAPException;

    /**
     * Returns a list of all actors recorded into database
     *
     * @return
     */
    public List<String> getListOfAllPossibleActors();

    /**
     * Returns a list of actors for a given domain
     *
     * @param inDomainKeyword
     * @return
     * @throws SOAPException
     */
    public List<String> getListOfActorsForGivenDomain(@WebParam(name = DOMAIN_KEYWORD) String inDomainKeyword)
            throws SOAPException;

    public List<Actor> getListOfActorObjectsForGivenDomain(@WebParam(name = DOMAIN_KEYWORD) String inDomainKeyword)
            throws SOAPException;

    /**
     * Returns an Actor selected by its keyword Note: some attributes are set to null in order to avoid error due to loop
     *
     * @param inActorKeyword
     * @return
     * @throws SOAPException
     */
    public Actor getActorByKeyword(@WebParam(name = ACTOR_KEYWORD) String inActorKeyword) throws SOAPException;

    /**
     * Returns a list of Transactions linked to given actor and integration profile
     *
     * @param inIntegrationProfileKeyword
     * @param inActorKeyword
     * @return
     * @throws SOAPException
     */
    public List<String> getListOfTransactionsForGivenActorAndIP(
            @WebParam(name = INTEGRATION_PROFILE_KEYWORD) String inIntegrationProfileKeyword,
            @WebParam(name = ACTOR_KEYWORD) String inActorKeyword) throws SOAPException;

    /**
     * Returns a list of Transactions linked to a given actor
     *
     * @param inActorKeyword
     * @return
     * @throws SOAPException
     */
    public List<String> getListOfTransactionsForGivenActor(@WebParam(name = ACTOR_KEYWORD) String inActorKeyword)
            throws SOAPException;

    public List<Transaction> getListOfTransactionObjectsForGivenActor(
            @WebParam(name = ACTOR_KEYWORD) String inActorKeyword) throws SOAPException;

    /**
     * Returns a list of Transactions linked to a given integration profile
     *
     * @param inIPKeyword
     * @return
     * @throws SOAPException
     */
    public List<String> getListOfTransactionsForGivenIP(@WebParam(name = INTEGRATION_PROFILE_KEYWORD) String inIPKeyword)
            throws SOAPException;

    /**
     * Returns a list of Transactions linked to a given domain
     *
     * @param inDomainKeyword
     * @return
     * @throws SOAPException
     */
    public List<String> getListOfTransactionsForGivenDomain(@WebParam(name = DOMAIN_KEYWORD) String inDomainKeyword)
            throws SOAPException;

    /**
     * Returns a transaction selected by its keyword Note: some attributes are set to null in order to avoid error due to loop
     *
     * @param inTransactionKeyword
     * @return
     * @throws SOAPException
     */
    public Transaction getTransactionByKeyword(@WebParam(name = TRANSACTION_KEYWORD) String inTransactionKeyword)
            throws SOAPException;

    /**
     * Returns a list of Integration Profile Options knowing the actor and the integration profile
     *
     * @param inIPKeyword
     * @param inActorKeyword
     * @return
     * @throws SOAPException
     */
    public List<String> getListOfIPOForGivenActorAndIP(
            @WebParam(name = INTEGRATION_PROFILE_KEYWORD) String inIPKeyword,
            @WebParam(name = ACTOR_KEYWORD) String inActorKeyword) throws SOAPException;

    /**
     * Returns an integration profile option selected by its keyword Note: some attributes are set to null in order to avoid error due to loop
     *
     * @param inIPOKeyword
     * @return
     * @throws SOAPException
     */
    public IntegrationProfileOption getIntegrationProfileOptionByKeyword(
            @WebParam(name = "integrationProfileOptionKeyword") String inIPOKeyword) throws SOAPException;

    /**
     * Returns the list of all actors involved in the given transaction
     *
     * @param inTransactionKeyword
     * @return
     * @throws SOAPException
     */
    public List<String> getListOfActorsForGivenTransaction(
            @WebParam(name = TRANSACTION_KEYWORD) String inTransactionKeyword) throws SOAPException;

    /**
     * Returns the list of actors which are able to initiate a given transaction
     *
     * @param inTransactionKeyword
     * @return
     * @throws SOAPException
     */
    public List<String> getListOfInitiatorsForGivenTransaction(
            @WebParam(name = TRANSACTION_KEYWORD) String inTransactionKeyword) throws SOAPException;

    /**
     * Returns the list of actors which are able to respond to a given transaction
     *
     * @param inTransationKeyword
     * @return
     * @throws SOAPException
     */
    public List<String> getListOfRespondersForGivenTransaction(
            @WebParam(name = TRANSACTION_KEYWORD) String inTransactionKeyword) throws SOAPException;

    /**
     * Returns the list of Actor Integration Profile Option in which the actor plays initiator role in the given transaction
     *
     * @param inTransactionKeyword
     * @return
     * @throws SOAPException
     */

    public List<ActorIntegrationProfileOption> getListOfAIP0ByTransactionForInitiators(
            @WebParam(name = TRANSACTION_KEYWORD) String inTransactionKeyword) throws SOAPException;

    /**
     * Returns the list of Actor Integration Profile Option in which the actor plays responder role in the given transaction
     *
     * @param inTransactionKeyword
     * @return
     * @throws SOAPException
     */

    public List<ActorIntegrationProfileOption> getListOfAIPOByTransactionForResponders(
            @WebParam(name = TRANSACTION_KEYWORD) String inTransactionKeyword) throws SOAPException;

    /**
     * Returns the list of all available Actor Integration Profile Option N.B. some attributes are set to null in order to avoid loopsl'optu
     *
     * @return
     */
    public List<ActorIntegrationProfileOption> getAllAIPO();

    /**
     * Returs the list of all available transactions
     *
     * @return
     */
    public List<String> getListOfAllPossibleTransactions();

    public List<String> getListOfAllInitiators();

    public List<String> getListOfAllResponders();

    public List<TransactionLink> getListOfTransactionLinksForGivenIntegrationProfile(
            @WebParam(name = "integrationprofilekeyword") String inIPKeyword) throws SOAPException;

    public String getStringOfGraphForGivenListOfTransactionLinks(
            @WebParam(name = "integrationprofilekeyword") String inIPKeyword) throws SOAPException;

    public void getProfileDiagram(@WebParam(name = "integrationprofilekeyword") String inIPKeyword)
            throws SOAPException;
}
