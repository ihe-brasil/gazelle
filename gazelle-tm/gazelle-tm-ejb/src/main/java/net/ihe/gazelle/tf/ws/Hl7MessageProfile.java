package net.ihe.gazelle.tf.ws;

/*
 * Copyright 2009 IHE International (http://www.ihe.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.tf.model.Actor;
import net.ihe.gazelle.tf.model.Domain;
import net.ihe.gazelle.tf.model.Transaction;
import org.hibernate.Hibernate;
import org.jboss.seam.annotations.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.soap.SOAPException;
import java.io.Serializable;
import java.util.List;

/**
 * <b>Class Description : </b>HL7MessageProfile<br>
 * <br>
 * This class implements web services used to get information about the HL7 Message Profiles recorded into database
 *
 * @author Anne-Gaelle Berge / INRIA Rennes IHE development Project
 * @version 1.0 - 2010, January 8th
 * @class Hl7MessageProfile.java
 * @package net.ihe.gazelle.tf.ws
 * @see > Aberge@irisa.fr - http://www.ihe-europe.org
 */

@Stateless
@Name("Hl7MessageProfilesService")
@WebService(name = "Hl7MessageProfiles", serviceName = "Hl7MessageProfilesService")
@GenerateInterface(value = "Hl7MessageProfileRemote", isLocal = false, isRemote = true)
public class Hl7MessageProfile implements Hl7MessageProfileRemote, Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(Hl7MessageProfile.class);

    private static final long serialVersionUID = 450914538351283760L;

    @Override
    @WebMethod
    @WebResult(name = "returnedMessageProfile")
    public net.ihe.gazelle.tf.model.Hl7MessageProfile getHL7MessageProfileByOID(
            @WebParam(name = "messageProfileOid") String inOID) throws SOAPException {
        if ((inOID == null) || (inOID.length() == 0)) {
            throw new SOAPException("You must provide a message profile OID");
        }

        inOID = inOID.trim();
        try {
            net.ihe.gazelle.tf.model.Hl7MessageProfile profile = net.ihe.gazelle.tf.model.Hl7MessageProfile
                    .getHl7MessageProfileByOID(inOID);
            if (profile == null) {
                throw new SOAPException("No message profile found for given oid : " + inOID);
            } else {
                Hibernate.initialize(profile.getTransaction().getTransactionLinks());
                return profile;
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new SOAPException("No message profile found for given oid : " + inOID, e);
        }
    }

    @Override
    @WebMethod
    @WebResult(name = "returnedActor")
    public List<Actor> getListOfPossibleActorsForGivenDomain(@WebParam(name = "domainKeyword") String inDomainKeyword)
            throws SOAPException {
        if ((inDomainKeyword == null) || (inDomainKeyword.length() == 0)) {
            throw new SOAPException("You must provide a domain keyword");
        }

        inDomainKeyword = inDomainKeyword.trim().toUpperCase();
        try {
            List<Actor> actors = net.ihe.gazelle.tf.model.Hl7MessageProfile
                    .getListOfAvailableActorsForGivenDomain(inDomainKeyword);
            if (actors == null) {
                throw new SOAPException("No actor found for domain with keyword : " + inDomainKeyword);
            } else {
                return actors;
            }
        } catch (Exception e) {
            LOG.error("", e.getMessage());
            throw new SOAPException("No actor found for domain with keyword : " + inDomainKeyword, e);
        }
    }

    @Override
    @WebMethod
    @WebResult(name = "returnedMessageOrderControlCode")
    public List<String> getListOfPossibleControlCodeForGivenMessageType(
            @WebParam(name = "messageType") String inMessageType) throws SOAPException {
        if ((inMessageType == null) || (inMessageType.length() == 0)) {
            throw new SOAPException("You must provide a message type");
        }

        inMessageType = inMessageType.trim().toUpperCase();
        try {
            List<String> codes = net.ihe.gazelle.tf.model.Hl7MessageProfile
                    .getListOfAvailableMessageCodesForGivenMessageType(inMessageType);
            if (codes == null) {
                throw new SOAPException("No message order control code found for message type : " + inMessageType);
            } else {
                return codes;
            }
        } catch (Exception e) {
            LOG.error("", e.getMessage());
            throw new SOAPException("No message order control code found for message type : " + inMessageType, e);
        }
    }

    @Override
    @WebMethod
    @WebResult(name = "returnedDomain")
    public List<Domain> getListOfPossibleDomains() throws SOAPException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfPossibleDomains");
        }
        try {
            List<Domain> domains = net.ihe.gazelle.tf.model.Hl7MessageProfile.getListOfAvailableDomains();
            if (domains == null) {
                throw new SOAPException("No domain found");
            } else {
                return domains;
            }
        } catch (Exception e) {
            LOG.error("", e.getMessage());
            throw new SOAPException("No domain found", e);
        }
    }

    @Override
    @WebMethod
    @WebResult(name = "returnedMessageProfile")
    public List<net.ihe.gazelle.tf.model.Hl7MessageProfile> getListOfPossibleHL7MessageProfileForGivenContext(
            @WebParam(name = "domainKeyword") String inDomainKeyword,
            @WebParam(name = "actorKeyword") String inActorKeyword,
            @WebParam(name = "transactionKeyword") String inTransactionKeyword,
            @WebParam(name = "messageType") String inMessageType,
            @WebParam(name = "messageOrderControlCode") String inControlCode,
            @WebParam(name = "hl7VersionNumber") String inHL7Version) throws SOAPException {
        try {
            List<net.ihe.gazelle.tf.model.Hl7MessageProfile> profileList = net.ihe.gazelle.tf.model.Hl7MessageProfile
                    .getHL7MessageProfilesFiltered(inDomainKeyword, inActorKeyword, inTransactionKeyword, inHL7Version,
                            inMessageType, inControlCode);

            if (profileList != null) {
                for (net.ihe.gazelle.tf.model.Hl7MessageProfile hl7MessageProfile : profileList) {
                    Hibernate.initialize(hl7MessageProfile.getTransaction().getTransactionLinks());
                }
                return profileList;
            } else {
                throw new SOAPException("Could not find message profile for the given context");
            }
        } catch (Exception e) {
            LOG.error("", e.getMessage());
            throw new SOAPException("Could not find message profile for the given context", e);
        }
    }

    @Override
    @WebMethod
    @WebResult(name = "returnedMessageProfile")
    public List<net.ihe.gazelle.tf.model.Hl7MessageProfile> getListOfPossibleHL7MessageProfileForGivenMessageType(
            @WebParam(name = "messageType") String inMessageType) throws SOAPException {

        if ((inMessageType == null) || (inMessageType.length() == 0)) {
            throw new SOAPException("You must provide a message type");
        }
        inMessageType = inMessageType.trim().toUpperCase();
        try {
            List<net.ihe.gazelle.tf.model.Hl7MessageProfile> profiles = net.ihe.gazelle.tf.model.Hl7MessageProfile
                    .getHL7MessageProfilesFiltered(null, null, null, null, inMessageType, null);
            if (profiles != null) {
                for (net.ihe.gazelle.tf.model.Hl7MessageProfile hl7MessageProfile : profiles) {
                    Hibernate.initialize(hl7MessageProfile.getTransaction().getTransactionLinks());
                }
                return profiles;
            } else {
                throw new SOAPException("No message profile found for message type " + inMessageType);
            }
        } catch (Exception e) {
            LOG.error("", e.getMessage());
            throw new SOAPException("No message profile found for message type " + inMessageType, e);
        }
    }

    @Override
    @WebMethod
    @WebResult(name = "returnedMessageType")
    public List<String> getListOfPossibleMessageTypesForGivenContext(
            @WebParam(name = "domainKeyword") String inDomainKeyword,
            @WebParam(name = "actorKeyword") String inActorKeyword,
            @WebParam(name = "transactionKeyword") String inTransactionKeyword) throws SOAPException {
        if ((inDomainKeyword == null) || (inDomainKeyword.length() == 0) || (inActorKeyword == null)
                || (inActorKeyword.length() == 0) || (inTransactionKeyword == null)
                || (inTransactionKeyword.length() == 0)) {
            throw new SOAPException("You must provide a keyword for domain, actor and transaction");
        }
        try {
            List<String> types = net.ihe.gazelle.tf.model.Hl7MessageProfile
                    .getListOfAvailableMessageTypesForGivenContext(inActorKeyword, inDomainKeyword,
                            inTransactionKeyword);
            if (types != null) {
                return types;
            } else {
                throw new SOAPException("No message type found for the given context");
            }
        } catch (Exception e) {
            LOG.error("", e.getMessage());
            throw new SOAPException("No message type found for the given context", e);
        }
    }

    @Override
    @WebMethod
    @WebResult(name = "returnedTransaction")
    public List<Transaction> getListOfPossibleTransactionsForGivenDomainAndActor(
            @WebParam(name = "domainKeyword") String inDomainKeyword,
            @WebParam(name = "actorKeyword") String inActorKeyword) throws SOAPException {
        if ((inDomainKeyword == null) || (inDomainKeyword.length() == 0) || (inActorKeyword == null)
                || (inActorKeyword.length() == 0)) {
            throw new SOAPException("You must provide a domain keyword and an actor keyword");
        }
        try {
            List<Transaction> transactions = net.ihe.gazelle.tf.model.Hl7MessageProfile
                    .getListOfAvailableTransactionsForGivenActorAndDomain(inActorKeyword, inDomainKeyword);
            if (transactions != null) {
                for (Transaction transaction : transactions) {
                    Hibernate.initialize(transaction.getTransactionLinks());
                }
                return transactions;
            } else {
                throw new SOAPException("No transaction found for actor and domain");
            }
        } catch (Exception e) {
            LOG.error("", e.getMessage());
            throw new SOAPException("No transaction found for actor and domain", e);
        }
    }

    @Override
    @WebMethod
    @WebResult(name = "returnedHl7Version")
    public List<String> getListOfPossibleHL7VersionForGivenMessageType(String inDomainKeyword, String inActorKeyword,
                                                                       String inTransactionKeyword, @WebParam(name = "messageType") String
                                                                               inMessageType) throws SOAPException {
        if (inMessageType == null) {
            throw new SOAPException("You must provide a message type");
        }
        try {
            List<String> versions = net.ihe.gazelle.tf.model.Hl7MessageProfile.getListOfAvailableHL7Version(
                    inDomainKeyword, inActorKeyword, inTransactionKeyword, inMessageType);
            if (versions != null) {
                return versions;
            } else {
                throw new SOAPException("no HL7 version found for message type " + inMessageType);
            }
        } catch (Exception e) {
            LOG.error("", e.getMessage());
            throw new SOAPException("no HL7 version found for message type " + inMessageType, e);
        }
    }

    @Override
    @WebMethod
    @WebResult(name = "success")
    public boolean addNewReference(
            @WebParam(name = "hl7MessageProfile") net.ihe.gazelle.tf.model.Hl7MessageProfile inProfile)
            throws SOAPException {
        if ((inProfile == null)) {
            throw new SOAPException("You must provide an HL7 Message Profile");
        }

        try {
            Domain domain;
            Actor actor;
            Transaction transaction;

            if (inProfile.getDomain() == null) {
                throw new SOAPException("You must provide a valid domain");
            } else {
                domain = Domain.getDomainByKeyword(inProfile.getDomain().getKeyword());
            }

            if (inProfile.getActor() == null) {
                throw new SOAPException("You must provide a valid actor");
            } else {
                actor = Actor.findActorWithKeyword(inProfile.getActor().getKeyword());
            }

            if (inProfile.getTransaction() == null) {
                throw new SOAPException("You must provide a valid transaction");
            } else {
                transaction = Transaction.GetTransactionByKeyword(inProfile.getTransaction().getKeyword());
            }

            net.ihe.gazelle.tf.model.Hl7MessageProfile hl7mp = new net.ihe.gazelle.tf.model.Hl7MessageProfile();
            hl7mp.setActor(actor);
            hl7mp.setDomain(domain);
            hl7mp.setTransaction(transaction);
            hl7mp.setMessageOrderControlCode(inProfile.getMessageOrderControlCode());
            hl7mp.setMessageType(inProfile.getMessageType());
            hl7mp.setHl7Version(inProfile.getHl7Version());
            hl7mp.setProfileOid(inProfile.getProfileOid());
            hl7mp.setProfileType(inProfile.getMessageType());
            hl7mp = net.ihe.gazelle.tf.model.Hl7MessageProfile.addMessageProfileReference(hl7mp);
            if (hl7mp != null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new SOAPException("An error occurred when recording the new Hl7MessageProfile " + e.getMessage());
        }
    }

    @Override
    @WebMethod
    @WebResult(name = "messageProfiles")
    public List<net.ihe.gazelle.tf.model.Hl7MessageProfile> getAllMessageProfiles() {
        List<net.ihe.gazelle.tf.model.Hl7MessageProfile> allHl7MessageProfiles = net.ihe.gazelle.tf.model.Hl7MessageProfile
                .getAllHl7MessageProfiles();
        if (allHl7MessageProfiles != null) {
            for (net.ihe.gazelle.tf.model.Hl7MessageProfile hl7MessageProfile : allHl7MessageProfiles) {
                if (hl7MessageProfile.getTransaction() != null) {
                    Hibernate.initialize(hl7MessageProfile.getTransaction().getTransactionLinks());
                }
            }
        }

        return allHl7MessageProfiles;
    }
}
