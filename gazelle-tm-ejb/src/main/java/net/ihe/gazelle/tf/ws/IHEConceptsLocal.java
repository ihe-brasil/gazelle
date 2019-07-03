package net.ihe.gazelle.tf.ws;

import net.ihe.gazelle.tf.model.*;

import javax.ejb.Local;
import javax.jws.WebParam;
import javax.xml.soap.SOAPException;
import java.util.List;

@Local
public interface IHEConceptsLocal {
    public static final String TRANSACTION_KEYWORD = "transactionKeyword";
    public static final String INTEGRATION_PROFILE_KEYWORD = "integrationProfileKeyword";
    public static final String DOMAIN_KEYWORD = "domainKeyword";
    public static final String ACTOR_KEYWORD = "actorKeyword";

    /**
     * Returns the list of the keywords of all available domains defined in the technical framework
     *
     * @return
     */
    List<String> getListOfPossibleDomains();

    /**
     * Returns a domain selected by its keyword Note: some attributes are set to null in order to avoid error due to loop
     *
     * @param inDomainKeyword
     * @return
     * @throws SOAPException
     */
    Domain getDomainByKeyword(@WebParam(name = DOMAIN_KEYWORD) String inDomainKeyword) throws SOAPException;

    List<String> getListOfIntegrationProfilesForGivenDomainWithNodeId(String inDomainKeyword, Integer nodeId)
            throws SOAPException;

    /**
     * Returns the list of integration profiles for a given domain
     *
     * @param inDomainKeyword
     * @return
     * @throws SOAPException
     */
    List<String> getListOfIntegrationProfilesForGivenDomain(@WebParam(name = DOMAIN_KEYWORD) String inDomainKeyword)
            throws SOAPException;

    /**
     * Returns the list of all integration profiles recorded into database
     *
     * @return
     * @throws SOAPException
     */
    List<String> getListOfAllPossibleIP();

    /**
     * Returns an Integration profile selected by its keyword Note: some attributes are set to null in order to avoid error due to loop
     *
     * @param inIPKeyword
     * @return
     * @throws SOAPException
     */
    IntegrationProfile getIntegrationProfileByKeyword(@WebParam(name = INTEGRATION_PROFILE_KEYWORD) String inIPKeyword)
            throws SOAPException;

    /**
     * Returns a list of all actors for a given integration profile
     *
     * @param inIPKeyword
     * @return
     * @throws SOAPException
     */
    List<String> getListOfActorsForGivenIntegrationProfile(
            @WebParam(name = INTEGRATION_PROFILE_KEYWORD) String inIPKeyword) throws SOAPException;

    /**
     * Returns a list of all actors recorded into database
     *
     * @return
     */
    List<String> getListOfAllPossibleActors();

    /**
     * Returns a list of actors for a given domain
     *
     * @param inDomainKeyword
     * @return
     * @throws SOAPException
     */
    List<String> getListOfActorsForGivenDomain(@WebParam(name = DOMAIN_KEYWORD) String inDomainKeyword)
            throws SOAPException;

    /**
     * Returns an Actor selected by its keyword Note: some attributes are set to null in order to avoid error due to loop
     *
     * @param inActorKeyword
     * @return
     * @throws SOAPException
     */
    Actor getActorByKeyword(@WebParam(name = ACTOR_KEYWORD) String inActorKeyword) throws SOAPException;

    /**
     * Returns a list of Transactions linked to given actor and integration profile
     *
     * @param inIntegrationProfileKeyword
     * @param inActorKeyword
     * @return
     * @throws SOAPException
     */
    List<String> getListOfTransactionsForGivenActorAndIP(
            @WebParam(name = INTEGRATION_PROFILE_KEYWORD) String inIntegrationProfileKeyword,
            @WebParam(name = ACTOR_KEYWORD) String inActorKeyword) throws SOAPException;

    /**
     * Returns a list of Transactions linked to a given actor
     *
     * @param inActorKeyword
     * @return
     * @throws SOAPException
     */
    List<String> getListOfTransactionsForGivenActor(@WebParam(name = ACTOR_KEYWORD) String inActorKeyword)
            throws SOAPException;

    /**
     * Returns a list of Transactions linked to a given integration profile
     *
     * @param inIPKeyword
     * @return
     * @throws SOAPException
     */
    List<String> getListOfTransactionsForGivenIP(@WebParam(name = INTEGRATION_PROFILE_KEYWORD) String inIPKeyword)
            throws SOAPException;

    /**
     * Returns a list of Transactions linked to a given domain
     *
     * @param inDomainKeyword
     * @return
     * @throws SOAPException
     */
    List<String> getListOfTransactionsForGivenDomain(@WebParam(name = DOMAIN_KEYWORD) String inDomainKeyword)
            throws SOAPException;

    /**
     * Returns a transaction selected by its keyword Note: some attributes are set to null in order to avoid error due to loop
     *
     * @param inTransactionKeyword
     * @return
     * @throws SOAPException
     */
    Transaction getTransactionByKeyword(@WebParam(name = TRANSACTION_KEYWORD) String inTransactionKeyword)
            throws SOAPException;

    /**
     * Returns a list of Integration Profile Options knowing the actor and the integration profile
     *
     * @param inIPKeyword
     * @param inActorKeyword
     * @return
     * @throws SOAPException
     */
    List<String> getListOfIPOForGivenActorAndIP(@WebParam(name = INTEGRATION_PROFILE_KEYWORD) String inIPKeyword,
                                                @WebParam(name = ACTOR_KEYWORD) String inActorKeyword) throws SOAPException;

    /**
     * Returns an integration profile option selected by its keyword Note: some attributes are set to null in order to avoid error due to loop
     *
     * @param inIPOKeyword
     * @return
     * @throws SOAPException
     */
    IntegrationProfileOption getIntegrationProfileOptionByKeyword(
            @WebParam(name = "integrationProfileOptionKeyword") String inIPOKeyword) throws SOAPException;

    /**
     * Returns the list of all actors involved in the given transaction
     *
     * @param inTransactionKeyword
     * @return
     * @throws SOAPException
     */
    List<String> getListOfActorsForGivenTransaction(@WebParam(name = TRANSACTION_KEYWORD) String inTransactionKeyword)
            throws SOAPException;

    /**
     * Returns the list of actors which are able to initiate a given transaction
     *
     * @param inTransactionKeyword
     * @return
     * @throws SOAPException
     */
    List<String> getListOfInitiatorsForGivenTransaction(
            @WebParam(name = TRANSACTION_KEYWORD) String inTransactionKeyword) throws SOAPException;

    /**
     * Returns the list of actors which are able to respond to a given transaction
     *
     * @param inTransationKeyword
     * @return
     * @throws SOAPException
     */
    List<String> getListOfRespondersForGivenTransaction(
            @WebParam(name = TRANSACTION_KEYWORD) String inTransactionKeyword) throws SOAPException;

    /**
     * Returns the list of Actor Integration Profile Option in which the actor plays initiator role in the given transaction
     *
     * @param inTransactionKeyword
     * @return
     * @throws SOAPException
     */
    List<ActorIntegrationProfileOption> getListOfAIP0ByTransactionForInitiators(
            @WebParam(name = TRANSACTION_KEYWORD) String inTransactionKeyword) throws SOAPException;

    /**
     * Returns the list of Actor Integration Profile Option in which the actor plays responder role in the given transaction
     *
     * @param inTransactionKeyword
     * @return
     * @throws SOAPException
     */
    List<ActorIntegrationProfileOption> getListOfAIPOByTransactionForResponders(
            @WebParam(name = TRANSACTION_KEYWORD) String inTransactionKeyword) throws SOAPException;

    /**
     * Returns the list of all available Actor Integration Profile Option
     *
     * @return
     */
    List<ActorIntegrationProfileOption> getAllAIPO();

    /**
     * Returns the list of transactionLinks knowing the integration profile
     *
     * @param inIPKeyword
     * @return
     * @throws SOAPException
     */
    List<TransactionLink> getListOfTransactionLinksForGivenIntegrationProfile(
            @WebParam(name = "integrationprofilekeyword") String inIPKeyword) throws SOAPException;

    /**
     * Returns a string of graph knowing the list of transactionlinks for a given integration profile
     *
     * @param inIPKeyword
     * @return
     * @throws SOAPException
     */
    String getStringOfGraphForGivenListOfTransactionLinks(
            @WebParam(name = "integrationprofilekeyword") String inIPKeyword) throws SOAPException;

    /**
     * Generate a png file with graph knowing the list of transactionlinks for a given integration profile
     *
     * @param inIPKeyword
     * @return
     * @throws SOAPException
     */
    void getProfileDiagram(@WebParam(name = "integrationprofilekeyword") String inIPKeyword) throws SOAPException;

    List<Domain> getAllDomainsObject();

    List<IntegrationProfile> getIntegrationProfileObjectsForDomain(String inDomainKeyword);

    List<Actor> getActorObjectsForIntegrationProfile(String inProfileKeyword);

    List<Transaction> getTransactionObjectsForActorAndIP(String inProfileKeyword, String inActorKeyword);

    List<Actor> getInitiatorObjectsForTransaction(String inTransactionKeyword);

    List<Actor> getResponderObjectsForTransaction(String inTransactionKeyword);

}
