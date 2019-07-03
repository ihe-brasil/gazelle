package net.ihe.gazelle.tf.remote;

import net.ihe.gazelle.tf.model.*;
import org.jboss.seam.annotations.remoting.WebRemote;

import javax.ejb.Local;
import javax.xml.soap.SOAPException;
import java.util.List;

@Local
public interface IHEConceptsRemotingLocal {
    /**
     * Returns the list of the keywords of all available domains defined in the technical framework
     *
     * @return
     */
    @WebRemote
    public List<String> getListOfPossibleDomains();

    /**
     * Returns a domain selected by its keyword Note: some attributes are set to null in order to avoid error due to loop
     *
     * @param inDomainKeyword
     * @return
     * @throws SOAPException
     */
    @WebRemote(exclude = {"integrationProfilesForDP", "lastModifierId", "lastChanged"})
    public Domain getDomainByKeyword(String inDomainKeyword) throws SOAPException;

    /**
     * Returns the list of integration profiles for a given domain
     *
     * @param inDomainKeyword
     * @return
     * @throws SOAPException
     */
    @WebRemote
    public List<String> getListOfIntegrationProfilesForGivenDomain(String inDomainKeyword) throws SOAPException;

    /**
     * Returns an Integration profile selected by its keyword Note: some attributes are set to null in order to avoid error due to loop
     *
     * @param inIPKeyword
     * @return
     * @throws SOAPException
     */
    @WebRemote(exclude = {"lastModifierId", "lastChanged", "integrationProfileStatusType",
            "listOfActorIntegrationProfileOption", "domainsForDP", "actorIntegrationProfileOption", "toDisplay",
            "integrationProfileTypes", "reference"})
    public IntegrationProfile getIntegrationProfileByKeyword(String inIPKeyword) throws SOAPException;

    /**
     * Returns a list of all actors for a given integration profile
     *
     * @param inIPKeyword
     * @return
     * @throws SOAPException
     */
    @WebRemote
    public List<String> getListOfActorsForGivenIntegrationProfile(String inIPKeyword) throws SOAPException;

    /**
     * Returns an Actor selected by its keyword Note: some attributes are set to null in order to avoid error due to loop
     *
     * @param inActorKeyword
     * @return
     * @throws SOAPException
     */
    @WebRemote(exclude = {"lastModifierId", "lastChanged", "actorIntegrationProfiles",
            "actorIntegrationProfileOption", "toDisplay", "transactionLinksWhereActingAsReceiver",
            "transactionLinksWhereActingAsSource", "combined"})
    public Actor getActorByKeyword(String inActorKeyword) throws SOAPException;

    /**
     * Returns a transaction selected by its keyword Note: some attributes are set to null in order to avoid error due to loop
     *
     * @param inTransactionKeyword
     * @return
     * @throws SOAPException
     */
    @WebRemote(exclude = {"lastModifierId", "lastChanged", "transactionStatusType", "transactionLinks",
            "profileLinks", "reference"})
    public Transaction getTransactionByKeyword(String inTransactionKeyword) throws SOAPException;

    /**
     * Returns an integration profile option selected by its keyword Note: some attributes are set to null in order to avoid error due to loop
     *
     * @param inIPOKeyword
     * @return
     * @throws SOAPException
     */
    @WebRemote(exclude = {"lastChanged", "lastModifierId", "actorIntegrationProfileOption", "toDisplay", "reference"})
    public IntegrationProfileOption getIntegrationProfileOptionByKeyword(String inIPOKeyword) throws SOAPException;

    /**
     * Returns a list of Integration Profile Options knowing the actor and the integration profile
     *
     * @param inIPKeyword
     * @param inActorKeyword
     * @return
     * @throws SOAPException
     */
    @WebRemote
    public List<String> getListOfIPOForGivenActorAndIP(String inIPKeyword, String inActorKeyword) throws SOAPException;

    /**
     * Returns a list of Transactions linked to given actor and integration profile
     *
     * @param inIntegrationProfileKeyword
     * @param inActorKeyword
     * @return
     * @throws SOAPException
     */
    @WebRemote
    public List<String> getListOfTransactionsForGivenActorAndIP(String inIntegrationProfileKeyword,
                                                                String inActorKeyword) throws SOAPException;

    /**
     * Returns the list of all actors involved in the given transaction
     *
     * @param inTransactionKeyword
     * @return
     * @throws SOAPException
     */
    @WebRemote
    public List<String> getListOfActorsForGivenTransaction(String inTransactionKeyword) throws SOAPException;

    /**
     * Returns the list of actors which are able to initiate a given transaction
     *
     * @param inTransactionKeyword
     * @return
     * @throws SOAPException
     */
    @WebRemote
    public List<String> getListOfInitiatorsForGivenTransaction(String inTransactionKeyword) throws SOAPException;

    /**
     * Returns the list of actors which are able to respond to a given transaction
     *
     * @param inTransationKeyword
     * @return
     * @throws SOAPException
     */
    @WebRemote
    public List<String> getListOfRespondersForGivenTransaction(String inTransactionKeyword) throws SOAPException;

    /**
     * Returns the list of transactionlinks which are able to respond to a given integration profile
     *
     * @param inIPKeyword
     * @return
     * @throws SOAPException
     */
    @WebRemote(exclude = {"lastChanged", "lastModifierId", "id", "[actor].lastChanged", "[actor].lastModifierId",
            "[actor].id", "[actor].actorIntegrationProfileOption", "[actor].actorIntegrationProfiles",
            "[actor].transactionLinksWhereActingAsReceiver", "[actor].transactionLinksWhereActingAsSource",
            "[transaction].lastChanged", "[transaction].lastModifierId", "[transaction].id",
            "[transaction].transactionStatusType", "[transaction].transactionLinks", "[transaction].profileLinks",
            "[transaction].reference"})
    public List<TransactionLink> getListOfTransactionLinksForGivenIntegrationProfile(String inIPKeyword)
            throws SOAPException;

    /**
     * Returns string with the xdot content corresponding to the list of transactionLinks which are able to respond to a given integration profile
     *
     * @return
     * @throws SOAPException
     */
    @WebRemote
    public String getStringOfGraphForGivenListOfTransactionLinks(String inIPKeyword) throws SOAPException;

    /**
     * Generate a png file with graph corresponding to the list of transactionsLinks which are able to respond to a given integration profile
     *
     * @throws SOAPException
     */
    public void getProfileDiagram(String inIPKeyword) throws SOAPException;

    public void destroy();

}
