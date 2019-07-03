/*
 * Copyright 2008 IHE International (http://www.ihe.net)
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
package net.ihe.gazelle.tf.action;

import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.log.ExceptionLogging;
import net.ihe.gazelle.hql.providers.EntityManagerService;
import net.ihe.gazelle.tf.model.*;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Name("profileLinksManager")
@Scope(ScopeType.SESSION)
@GenerateInterface("ProfileLinksManagerLocal")
public class ProfileLinksManager implements Serializable, ProfileLinksManagerLocal {

    /**
     * Serial ID version of this object
     */
    private static final long serialVersionUID = -1357012043456235100L;
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ProfileLinksManager.class);
    @In
    private EntityManager entityManager;
    /**
     * facesMessages is the interface used to display the information/error messages in the JSF.
     */
    @In
    private FacesMessages facesMessages;
    @In(required = false)
    @Out(required = false)
    private ProfileLink profileLink;
    private ProfileLink selectedProfileLink;
    @DataModel
    private List<Actor> profileLinkActors;
    private List<Actor> profileLinkSelectedActors;
    @In(required = false)
    @Out(required = false)
    private Actor profileLinkActor;
    @Out(required = false)
    private String actorSize;
    @Out(required = false)
    private Boolean actorReadOnly;
    @DataModel
    private List<IntegrationProfile> profileLinkIntegrationProfiles;
    @In(required = false)
    @Out(required = false)
    private IntegrationProfile profileLinkIntegrationProfile;
    @Out(required = false)
    private String integrationProfileSize;
    @Out(required = false)
    private Boolean integrationProfileReadOnly;
    private List<IntegrationProfile> profileLinkSelectedIntegrationProfiles;
    @DataModel
    private List<Transaction> profileLinkTransactions;
    @In(required = false)
    @Out(required = false)
    private Transaction profileLinkTransaction;
    @Out(required = false)
    private String transactionSize;
    @Out(required = false)
    private Boolean transactionReadOnly;
    private List<Transaction> profileLinkSelectedTransactions;
    @DataModel
    private List<ProfileLink> profileLinks;
    @DataModel
    private List<TransactionOptionType> transactionOptionTypes;
    @In(required = false)
    @Out(required = false)
    private TransactionOptionType selectedTransactionOptionType;
    private boolean linkExist = true;
    // ************************************* Variable screen messages and labels
    @In
    private Map<String, String> messages;

    private String addTitle;
    private String addTitleName;
    private CallerClass callerClass;

    public ProfileLink getSelectedProfileLink() {
        return selectedProfileLink;
    }

    public void setSelectedProfileLink(ProfileLink selectedProfileLink) {
        this.selectedProfileLink = selectedProfileLink;
    }

    @Override
    public String getAddTitle() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAddTitle");
        }
        return messages.get(addTitle) + " " + addTitleName;
    }

    @Override
    public String getCallerClass() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCallerClass");
        }
        return callerClass.name();
    }

    @Override
    public boolean isCallerClass(String n) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isCallerClass");
        }
        return callerClass.name().equalsIgnoreCase(n);
    }

    @Override
    public Boolean getActorReadOnly() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorReadOnly");
        }
        return actorReadOnly;
    }

    @Override
    public void setActorReadOnly(Boolean actorReadOnly) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setActorReadOnly");
        }
        this.actorReadOnly = actorReadOnly;
    }

    @Override
    public Boolean getIntegrationProfileReadOnly() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntegrationProfileReadOnly");
        }
        return integrationProfileReadOnly;
    }

    @Override
    public void setIntegrationProfileReadOnly(Boolean integrationProfileReadOnly) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setIntegrationProfileReadOnly");
        }
        this.integrationProfileReadOnly = integrationProfileReadOnly;
    }

    public List<Actor> getProfileLinkSelectedActors() {
        return profileLinkSelectedActors;
    }

    public void setProfileLinkSelectedActors(List<Actor> profileLinkSelectedActors) {
        this.profileLinkSelectedActors = profileLinkSelectedActors;
    }

    public List<IntegrationProfile> getProfileLinkSelectedIntegrationProfiles() {
        return profileLinkSelectedIntegrationProfiles;
    }

    public void setProfileLinkSelectedIntegrationProfiles(List<IntegrationProfile> profileLinkSelectedIntegrationProfiles) {
        this.profileLinkSelectedIntegrationProfiles = profileLinkSelectedIntegrationProfiles;
    }

    public List<Transaction> getProfileLinkSelectedTransactions() {
        return profileLinkSelectedTransactions;
    }

    public void setProfileLinkSelectedTransactions(List<Transaction> profileLinkSelectedTransactions) {
        this.profileLinkSelectedTransactions = profileLinkSelectedTransactions;
    }

    public Boolean getTransactionReadOnly() {
        return transactionReadOnly;
    }

    public void setTransactionReadOnly(Boolean transactionReadOnly) {
        this.transactionReadOnly = transactionReadOnly;
    }

    public boolean isLinkExist() {
        return linkExist;
    }

    public void setLinkExist(boolean linkExist) {
        this.linkExist = linkExist;
    }

    @Override
    public String addProfileLinks(Object obj) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addProfileLinks");
        }

        setLinkExist(true);
        profileLinkActors = null;
        profileLinkSelectedActors = new ArrayList<Actor>();
        profileLinkTransactions = null;
        profileLinkSelectedTransactions = new ArrayList<Transaction>();
        profileLinkIntegrationProfiles = null;
        profileLinkSelectedIntegrationProfiles = new ArrayList<IntegrationProfile>();
        profileLinks = null;
        actorSize = "20";
        integrationProfileSize = "20";
        transactionSize = "20";
        actorReadOnly = false;
        integrationProfileReadOnly = false;
        transactionReadOnly = false;

        // -------------------------------------- determine and load passed class
        if (obj.getClass() == Actor.class) {

            Actor a = entityManager.find(Actor.class, ((Actor) obj).getId());
            profileLinkActors = new ArrayList<Actor>();
            profileLinkActors.add(a);
            profileLinkSelectedActors.add(a);
            actorSize = "2";
            callerClass = CallerClass.ACTOR;
            addTitle = "gazelle.tf.addProfileLinks.Actor";
            addTitleName = a.getName();
            profileLinkActor = a;
            actorReadOnly = true;
        }
        if (obj.getClass() == Transaction.class) {

            Transaction t = entityManager.find(Transaction.class, ((Transaction) obj).getId());
            profileLinkTransactions = new ArrayList<Transaction>();
            profileLinkTransactions.add(t);
            profileLinkSelectedTransactions.add(t);
            transactionSize = "2";
            callerClass = CallerClass.TRANSACTION;
            addTitle = "gazelle.tf.addProfileLinks.Transaction";
            addTitleName = t.getName();
            profileLinkTransaction = t;
            transactionReadOnly = true;
        }

        if (obj.getClass() == IntegrationProfile.class) {

            IntegrationProfile p = entityManager.find(IntegrationProfile.class, ((IntegrationProfile) obj).getId());
            profileLinkIntegrationProfiles = new ArrayList<IntegrationProfile>();
            profileLinkIntegrationProfiles.add(p);
            profileLinkSelectedIntegrationProfiles.add(p);
            integrationProfileSize = "2";
            callerClass = CallerClass.INTEGRATION_PROFILE;
            addTitle = "gazelle.tf.integrationProfile.profileLinks.Add";
            addTitleName = p.getName();
            profileLinkIntegrationProfile = p;
            integrationProfileReadOnly = true;
        }
        // ----------------------- Now load the other two lists, not loaded above
        if (profileLinkActors == null) {
            if (callerClass == CallerClass.INTEGRATION_PROFILE) {
                profileLinkActors = entityManager.createQuery(
                        "Select a from Actor a "
                                + "join a.actorIntegrationProfiles aip where aip.integrationProfile.id = "
                                + profileLinkIntegrationProfiles.get(0).getId() + " order by a.keyword")
                        .getResultList();
            } else {
                profileLinkActors = entityManager.createQuery("select a from Actor a order by a.keyword")
                        .getResultList();
            }
        }
        if (profileLinkIntegrationProfiles == null) {
            if (callerClass == CallerClass.ACTOR) {
                profileLinkIntegrationProfiles = entityManager.createQuery(
                        "Select i from IntegrationProfile i "
                                + "join i.actorIntegrationProfiles aip where aip.actor.id = "
                                + profileLinkActors.get(0).getId() + " order by i.keyword").getResultList();
            } else {
                profileLinkIntegrationProfiles = entityManager.createQuery(
                        "select i from IntegrationProfile i order by i.keyword").getResultList();
            }
        }
        if (profileLinkTransactions == null) {
            profileLinkTransactions = entityManager.createQuery("select t from Transaction t order by t.keyword")
                    .getResultList();
        }
        // ******************* load the transaction Option types (R|O)
        transactionOptionTypes = entityManager.createQuery("select o from TransactionOptionType o order by o.keyword")
                .getResultList();
        TransactionOptionTypeQuery query = new TransactionOptionTypeQuery();
        query.keyword().eq("R");
        selectedTransactionOptionType = query.getUniqueResult();
        if (selectedTransactionOptionType == null) {
            selectedTransactionOptionType = transactionOptionTypes.get(0);
        }

        return "/tf/profileLink/addProfileLinks.seam";
    }

    /**
     * createLinks() - action method which creates Profile Links for the selected items in the lists
     */

    @Override
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public String createLinks(List<Actor> plsa, List<IntegrationProfile> plsip, List<Transaction> plst,
                              TransactionOptionType stot) {
        profileLinkSelectedActors = plsa;
        profileLinkSelectedIntegrationProfiles = plsip;
        profileLinkSelectedTransactions = plst;
        selectedTransactionOptionType = stot;
        return createLinks();
    }

    @Override
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public String createLinks() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createLinks");
        }
        if (entityManager == null) {
            entityManager = EntityManagerService.provideEntityManager();
        }
        if (facesMessages == null) {
            facesMessages = FacesMessages.instance();
        }
        ActorIntegrationProfile aip;
        int countAIP = 0;
        int countDup = 0;
        int countCreated = 0;
        // ***************************************** Make sure selections are made
        boolean err = false;
        if (profileLinkSelectedActors.size() == 0) {
            err = true;
            facesMessages.add(StatusMessage.Severity.ERROR, "No Actors selected");
        }
        if (profileLinkSelectedIntegrationProfiles.size() == 0) {
            err = true;
            facesMessages.add(StatusMessage.Severity.ERROR, "No Integration Profiles selected");
        }
        if (profileLinkSelectedTransactions.size() == 0) {
            err = true;
            facesMessages.add(StatusMessage.Severity.ERROR, "No Transactions selected");
        }
        if (selectedTransactionOptionType == null) {
            err = true;
            facesMessages.add(StatusMessage.Severity.ERROR, "No Transaction Option Type selected");
        }
        if (err) {
            return null;
        }
        // ***************************** iterate through the various permutations
        for (int ac = 0; ac < profileLinkSelectedActors.size(); ac++) {
            Actor a = profileLinkSelectedActors.get(ac);
            for (int ic = 0; ic < profileLinkSelectedIntegrationProfiles.size(); ic++) {
                IntegrationProfile i = profileLinkSelectedIntegrationProfiles.get(ic);
                // ******* verify that Actor/IntegrationProfile entity exists
                ActorIntegrationProfileQuery query = new ActorIntegrationProfileQuery();
                query.actor().id().eq(a.getId());
                query.integrationProfile().id().eq(i.getId());
                try {
                    aip = (ActorIntegrationProfile) query.getUniqueResult();
                    if (aip == null) {
                        setLinkExist(false);
                        return null;
                    }

                } catch (Exception e) {
                    ExceptionLogging.logException(e, LOG);

                    countAIP++;
                    continue;
                }
                for (int tc = 0; tc < profileLinkSelectedTransactions.size(); tc++) {

                    Query q = entityManager
                            .createQuery("select transaction from Transaction transaction fetch all properties where transaction = :inTransaction");

                    q.setParameter("inTransaction", profileLinkSelectedTransactions.get(tc));

                    Transaction t = (Transaction) q.getSingleResult();

                    // ****************** Verify that Profile Link does not exist
                    ProfileLinkQuery query1 = new ProfileLinkQuery();
                    query1.actorIntegrationProfile().id().eq(aip.getId());
                    query1.transaction().id().eq(t.getId());

                    Long plc = (long) query1.id().getCountOnPath();
                    if (plc != 0) {
                        countDup++;
                        continue;
                    }
                    // ************************************* Make new ProfileLink
                    ProfileLink pl = new ProfileLink();
                    pl.setActorIntegrationProfile(aip);
                    pl.setTransaction(t);
                    pl.setTransactionOptionType(selectedTransactionOptionType);
                    pl = entityManager.merge(pl);

                    t.addProfileLink(pl);
                    entityManager.merge(t);
                    aip.addProfileLink(pl);
                    entityManager.merge(aip);
                    countCreated++;
                }
            }
        }

        if (countAIP > 0) {
            facesMessages.add(StatusMessage.Severity.ERROR, "Invalid Actor/Integration Profile pairs: " + countAIP);
        }
        if (countDup > 0) {
            facesMessages.add(StatusMessage.Severity.ERROR, "Profile links already present: " + countDup);
        }
        facesMessages.add(StatusMessage.Severity.INFO, "Profile Links created: " + countCreated);
        // ************************************************ Reset list selections
        if (callerClass != CallerClass.ACTOR) {
            profileLinkSelectedActors = new ArrayList<Actor>();
        }
        if (callerClass != CallerClass.INTEGRATION_PROFILE) {
            profileLinkSelectedIntegrationProfiles = new ArrayList<IntegrationProfile>();
        }
        if (callerClass != CallerClass.TRANSACTION) {
            profileLinkSelectedTransactions = new ArrayList<Transaction>();
        }
        return null;
    }

    public String addActorIntegrationProfile() {
        for (int ac = 0; ac < profileLinkSelectedActors.size(); ac++) {
            Actor a = profileLinkSelectedActors.get(ac);
            for (int ic = 0; ic < profileLinkSelectedIntegrationProfiles.size(); ic++) {
                IntegrationProfile i = profileLinkSelectedIntegrationProfiles.get(ic);
                ActorManagerLocal am = (ActorManagerLocal) Component.getInstance("actorManager");
                List<IntegrationProfile> ips = new ArrayList<IntegrationProfile>();
                ips.add(i);
                am.setLinkedProfiles(ips);
                am.saveActorProfiles(a);
                setLinkExist(true);
                facesMessages.add(StatusMessage.Severity.INFO, "Link created between " + i.getKeyword() + " and " + a.getKeyword());
            }
        }
        return null;
    }

    public void forceLinkCreation() {
        addActorIntegrationProfile();
        createLinks();
    }

    @Override
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public void updateProfileLink() {
        if (selectedProfileLink != null) {
            entityManager.merge(selectedProfileLink);
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Optionality for id " + selectedProfileLink.getId() +
                    " updated to " + selectedProfileLink.getTransactionOptionType().getName());
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Failed to update optionality");
        }
    }

    /**
     * finished() - action method which returns to calling screen
     *
     * @return screen to display
     */
    @Override
    public String finished() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("finished");
        }

        switch (callerClass) {
            case ACTOR:
                ActorManagerLocal am = (ActorManagerLocal) Component.getInstance("actorManager");
                return am.editActor(profileLinkActors.get(0), true);
            case INTEGRATION_PROFILE:
                IntegrationProfileManagerLocal im = (IntegrationProfileManagerLocal) Component
                        .getInstance("integrationProfileManager");
                return im.editIntegrationProfile(profileLinkIntegrationProfiles.get(0));
            case TRANSACTION:
                TransactionManagerLocal tm = (TransactionManagerLocal) Component.getInstance("transactionManager");
                return tm.editTransaction(profileLinkTransactions.get(0), true);
        }

        return null;
    }

    /**
     * delProfileLink - deletes profile link @ pl ProfileLink to delete @ obj - object being edited There is no delete effects display for this
     * action.
     *
     * @return - returns to calling screen.
     */
    @Override
    public String delProfileLink(Object obj, ProfileLink pl) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("delProfileLink");
        }

        ProfileLink p = entityManager.find(ProfileLink.class, pl.getId());
        Transaction t = p.getTransaction();
        ActorIntegrationProfile aip = p.getActorIntegrationProfile();
        t.delProfileLink(p);
        aip.delProfileLink(p);

        entityManager.remove(p);
        entityManager.flush();

        FacesMessages.instance().add(StatusMessage.Severity.INFO, "Link removed");

        if ((obj != null) && (obj.getClass() == Actor.class)) {
            Actor a = entityManager.find(Actor.class, ((Actor) obj).getId());
            ActorManagerLocal am = (ActorManagerLocal) Component.getInstance("actorManager");
            return am.editActor(a, true);
        }

        if ((obj != null) && (obj.getClass() == Transaction.class)) {

            Transaction tr = entityManager.find(Transaction.class, ((Transaction) obj).getId());
            TransactionManagerLocal tm = (TransactionManagerLocal) Component.getInstance("transactionManager");
            return tm.editTransaction(tr, true);
        }

        if ((obj != null) && (obj.getClass() == IntegrationProfile.class)) {

            IntegrationProfile ip = entityManager.find(IntegrationProfile.class, ((IntegrationProfile) obj).getId());
            IntegrationProfileManagerLocal im = (IntegrationProfileManagerLocal) Component
                    .getInstance("integrationProfileManager");
            return im.editIntegrationProfile(ip, im.getEdit());
        }
        LOG.warn("invalid calling obj to delProfileLink");
        return null;
    }

    // ** Build list of Actors which are linked to a specific Integration Profile and Transaction ** //
    @Override
    public List<Actor> getActorsForIpAndTransaction(IntegrationProfile ip, Transaction trans) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorsForIpAndTransaction");
        }
        String queryString = "SELECT pl FROM ProfileLink pl WHERE pl.integrationProfile.id = '" + ip.getId()
                + "' AND pl.transaction.id = '" + trans.getId() + "'";
        Query query = entityManager.createQuery(queryString);
        List<ProfileLink> plList = query.getResultList();
        List<Actor> actorsForIpAndTransaction = new ArrayList<Actor>();
        for (ProfileLink pl : plList) {
            actorsForIpAndTransaction.add(pl.getActorIntegrationProfile().getActor());
        }

        return actorsForIpAndTransaction;
    }

    @Override
    public Long getCountActorsForIpAndTransaction(IntegrationProfile ip, Transaction trans) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCountActorsForIpAndTransaction");
        }
        String queryString = "SELECT COUNT(pl.id) FROM ProfileLink pl WHERE pl.integrationProfile.id = '" + ip.getId()
                + "' AND pl.transaction.id = '" + trans.getId() + "'";
        Query query = entityManager.createQuery(queryString);
        Long count = (Long) query.getSingleResult();
        return count;
    }

    // ** Build list of Integration Profiles which are linked to a specific Actor and Transaction ** //
    @Override
    public List<IntegrationProfile> getIntegrationProfilesForActorAndTransaction(Actor a, Transaction trans) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntegrationProfilesForActorAndTransaction");
        }
        String queryString = "SELECT pl FROM ProfileLink pl WHERE pl.actor.id = '" + a.getId()
                + "' AND pl.transaction.id = '" + trans.getId() + "'";
        Query query = entityManager.createQuery(queryString);
        List<ProfileLink> plList = query.getResultList();
        List<IntegrationProfile> integrationProfilesForActorAndTransaction = new ArrayList<IntegrationProfile>();
        for (ProfileLink pl : plList) {
            integrationProfilesForActorAndTransaction.add(pl.getActorIntegrationProfile().getIntegrationProfile());
        }
        return integrationProfilesForActorAndTransaction;
    }

    @Override
    public Long getCountIntegrationProfilesForActorAndTransaction(Actor a, Transaction trans) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCountIntegrationProfilesForActorAndTransaction");
        }
        String queryString = "SELECT COUNT(pl.id) FROM ProfileLink pl WHERE pl.actor.id = '" + a.getId()
                + "' AND pl.transaction.id = '" + trans.getId() + "'";
        Query query = entityManager.createQuery(queryString);
        Long count = (Long) query.getSingleResult();
        return count;
    }

    // ** Build list of Transactions which are linked to a specific Actor and Integration Profile ** //
    @Override
    public List<Transaction> getTransactionsForActorAndIp(Actor a, IntegrationProfile ip) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTransactionsForActorAndIp");
        }
        String queryString = "SELECT pl FROM ProfileLink pl WHERE pl.actor.id = '" + a.getId()
                + "' AND pl.integrationProfile.id = '" + ip.getId() + "'";
        Query query = entityManager.createQuery(queryString);
        List<ProfileLink> plList = query.getResultList();
        List<Transaction> transactionsForActorAndIntegrationProfiles = new ArrayList<Transaction>();
        for (ProfileLink pl : plList) {
            transactionsForActorAndIntegrationProfiles.add(pl.getTransaction());
        }
        return transactionsForActorAndIntegrationProfiles;
    }

    @Override
    public Long getCountTransactionsForActorAndIp(Actor a, IntegrationProfile ip) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCountTransactionsForActorAndIp");
        }
        String queryString = "SELECT COUNT (pl.id) FROM ProfileLink pl WHERE pl.actor.id = '" + a.getId()
                + "' AND pl.integrationProfile.id = '" + ip.getId() + "'";
        Query query = entityManager.createQuery(queryString);
        Long count = (Long) query.getSingleResult();
        return count;
    }

    // ** Build list of ProfileLinks which are linked to a specific Actor and Integration Profile ** //
    @Override
    public List<ProfileLink> getProfileLinksForActorAndIp(Actor a, IntegrationProfile ip) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProfileLinksForActorAndIp");
        }
        String queryString = "SELECT pl FROM ProfileLink pl WHERE pl.actor.id = '" + a.getId()
                + "' AND pl.integrationProfile.id = '" + ip.getId() + "'";
        Query query = entityManager.createQuery(queryString);
        List<ProfileLink> plList = query.getResultList();

        return plList;
    }

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    /**
     * AddProfileLinks: method called from .xhtml tabs which list Profile Links related to [actor|transaction|integration profile] to add profile
     * links to the calling entity. Displays an appropriately
     * set up set of lists.
     *
     * @param obj The entity to which the links are added.
     * @return String the add profile links .xhtml
     */
    enum CallerClass {
        ACTOR,
        INTEGRATION_PROFILE,
        TRANSACTION
    }

}
