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

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.documents.DocumentSectionsFilter;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.tf.datamodel.TransactionDataModel;
import net.ihe.gazelle.tf.model.*;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.security.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.jboss.seam.ScopeType.SESSION;

/**
 * <b>Class Description : </b>TransactionManager<br>
 * <br>
 * This class manage the Transaction object. It corresponds to the Business Layer. All operations to implement are done in this class : <li>Add a
 * transaction</li> <li>Delete a transaction</li> <li>
 * Show a transaction</li> <li>Edit a transaction</li> <li>etc...</li>
 *
 * @author Ralph Moulton / MIR WUSM IHE development Project
 * @version 1.0 - 2009, February 13
 * @class Transactionanager.java
 * @package net.ihe.gazelle.tf.action
 * @see > moultonr@mir.wustl.edu - http://www.ihe-europe.org
 */

@Name("transactionManager")
@Scope(SESSION)
@GenerateInterface("TransactionManagerLocal")
public class TransactionManager implements Serializable, TransactionManagerLocal {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionManager.class);

    private static final String UNCHECKED = "unchecked";

    private static final String TF_TRANSACTION_LIST_TRANSACTIONS_SEAM = "/tf/transaction/listTransactions.seam";

    private static final String ORDER_BY_IP_KEYWORD = " order by ip.keyword";

    private static final long serialVersionUID = -135701171456235656L;
    /**
     * em is the interface used to interact with the persistence context.
     */
    @In
    private EntityManager entityManager;
    /**
     * Transaction objects managed by this bean
     */

    private Transaction selectedTransaction;
    private TransactionDataModel transactions;
    private Filter<IntegrationProfile> integrationProfilesForTransactionFilter;
    private Filter<ProfileLink> profileLinkForTransactionFilter;
    private Filter<Actor> actorsForTransactionFilter;
    private Filter<TransactionLink> transactionLinksForTransactionFilter;
    // ************************************* Variable screen messages and labels
    @In
    private Map<String, String> messages;
    private String lstTitle;
    private String linkTitle;
    private String linkSubTitle;
    private String linkSourceHeader;
    private String linkTargetHeader;
    private String linkAddAll;
    private String linkSubAll;
    private String linkAddOne;
    private String linkSubOne;
    private String converter;
    private Boolean renderLinks = false;
    // Is Edit on, or just view?
    private Boolean edit = false;
    // Number of integrationProfiles for trans
    private Integer ipCount = 0;
    // Number of profileLinks for trans
    private Integer plCount = 0;
    // Number of Transaction Links for trans
    private Integer tlCount = 0;
    private Integer actorCount = 0;
    private LinkType linkType;
    private String editTitle;
    private String editSubTitle;
    private Actor selectedActorFrom;
    private Actor selectedActorTo;
    private Domain selectedDomain;
    private String selectedSearch = "Domains/Integration Profiles";
    private IntegrationProfile selectedIntegrationProfile;
    /**
     * Field filter.
     */
    private DocumentSectionsFilter filter;
    private FilterDataModel<DocumentSection> documentSections;
    private List<Object> source;
    private List<Object> target;
    private String callerPage;

    @Override
    public String getLstTitle() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLstTitle");
        }
        return messages.get(lstTitle);
    }

    @Override
    public String getEditTitle() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEditTitle");
        }
        return messages.get(editTitle);
    }

    @Override
    public String getEditSubTitle() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEditSubTitle");
        }
        return messages.get(editSubTitle);
    }

    @Override
    public String getLinkTitle() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLinkTitle");
        }
        return messages.get(linkTitle);
    }

    @Override
    public String getLinkSubTitle() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLinkSubTitle");
        }
        return messages.get(linkSubTitle);
    }

    @Override
    public String getLinkSourceHeader() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLinkSourceHeader");
        }
        return messages.get(linkSourceHeader);
    }

    @Override
    public String getLinkTargetHeader() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLinkTargetHeader");
        }
        return messages.get(linkTargetHeader);
    }

    @Override
    public String getLinkAddAll() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLinkAddAll");
        }
        return messages.get(linkAddAll);
    }

    @Override
    public String getLinkSubAll() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLinkSubAll");
        }
        return messages.get(linkSubAll);
    }

    @Override
    public String getLinkAddOne() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLinkAddOne");
        }
        return messages.get(linkAddOne);
    }

    @Override
    public String getLinkSubOne() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLinkSubOne");
        }
        return messages.get(linkSubOne);
    }

    @Override
    public String getConverter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getConverter");
        }
        return converter;
    }

    @Override
    public Transaction getSelectedTransaction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTransaction");
        }
        return selectedTransaction;
    }

    @Override
    public void setSelectedTransaction(Transaction selectedTransaction) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTransaction");
        }
        this.selectedTransaction = selectedTransaction;
    }

    @Override
    public TransactionDataModel getTransactions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTransactions");
        }
        if (this.transactions == null) {
            this.transactions = new TransactionDataModel();
        }
        return transactions;
    }

    @Override
    public void setTransactions(TransactionDataModel transactions) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTransactions");
        }
        this.transactions = transactions;
    }

    @Override
    public FilterDataModel<IntegrationProfile> getIntegrationProfilesForTransaction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntegrationProfilesForTransaction");
        }
        return new FilterDataModel<IntegrationProfile>(getIntegrationProfileFilter()) {
            @Override
            protected Object getId(IntegrationProfile integrationProfile) {
                return integrationProfile.getId();
            }
        };
    }

    private Filter<IntegrationProfile> getIntegrationProfileFilter() {
        if (integrationProfilesForTransactionFilter == null) {
            IntegrationProfileQuery q = new IntegrationProfileQuery();
            HQLCriterionsForFilter<IntegrationProfile> result = q.getHQLCriterionsForFilter();
            result.addQueryModifier(new QueryModifier<IntegrationProfile>() {
                @Override
                public void modifyQuery(HQLQueryBuilder<IntegrationProfile> hqlQueryBuilder, Map<String, Object> map) {
                    if (selectedTransaction != null) {
                        IntegrationProfileQuery q = new IntegrationProfileQuery();
                        hqlQueryBuilder.addRestriction(q.actorIntegrationProfiles().profileLinks().transaction().id().eqRestriction
                                (selectedTransaction.getId()));
                    }
                }
            });
            this.integrationProfilesForTransactionFilter = new Filter<IntegrationProfile>(result);
        }
        return this.integrationProfilesForTransactionFilter;
    }

    @Override
    public FilterDataModel<ProfileLink> getProfileLinkForTransaction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getprofileLinksForTransaction");
        }
        return new FilterDataModel<ProfileLink>(getProfileLinkFilter()) {
            @Override
            protected Object getId(ProfileLink pl) {
                return pl.getId();
            }
        };
    }

    private Filter<ProfileLink> getProfileLinkFilter() {
        if (profileLinkForTransactionFilter == null) {
            ProfileLinkQuery q = new ProfileLinkQuery();
            HQLCriterionsForFilter<ProfileLink> result = q.getHQLCriterionsForFilter();
            result.addQueryModifier(new QueryModifier<ProfileLink>() {
                @Override
                public void modifyQuery(HQLQueryBuilder<ProfileLink> hqlQueryBuilder, Map<String, Object> map) {
                    if (selectedTransaction != null) {
                        ProfileLinkQuery q = new ProfileLinkQuery();
                        hqlQueryBuilder.addRestriction(q.transaction().id().eqRestriction(selectedTransaction.getId()));
                    }
                }
            });
            this.profileLinkForTransactionFilter = new Filter<ProfileLink>(result);
        }
        return this.profileLinkForTransactionFilter;
    }

    @Override
    public FilterDataModel<Actor> getActorsForTransaction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorsForTransaction");
        }
        return new FilterDataModel<Actor>(getActorsForTransactionFilter()) {
            @Override
            protected Object getId(Actor actor) {
                return actor.getId();
            }
        };
    }

    private Filter<Actor> getActorsForTransactionFilter() {
        if (actorsForTransactionFilter == null) {
            ActorQuery q = new ActorQuery();
            HQLCriterionsForFilter<Actor> result = q.getHQLCriterionsForFilter();
            result.addQueryModifier(new QueryModifier<Actor>() {
                @Override
                public void modifyQuery(HQLQueryBuilder<Actor> hqlQueryBuilder, Map<String, Object> map) {
                    if (selectedTransaction != null) {
                        ActorQuery q = new ActorQuery();
                        hqlQueryBuilder.addRestriction(q.actorIntegrationProfiles().profileLinks().transaction().id().eqRestriction
                                (selectedTransaction.getId()));
                    }
                }
            });
            this.actorsForTransactionFilter = new Filter<Actor>(result);
        }
        return this.actorsForTransactionFilter;
    }

    @Override
    public FilterDataModel<TransactionLink> getTransactionLinksForTransaction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTransactionLinksForTransaction");
        }
        return new FilterDataModel<TransactionLink>(getTransactionLinksForTransactionFilter()) {
            @Override
            protected Object getId(TransactionLink transactionLink) {
                return transactionLink.getId();
            }
        };
    }

    private Filter<TransactionLink> getTransactionLinksForTransactionFilter() {
        if (transactionLinksForTransactionFilter == null) {
            TransactionLinkQuery q = new TransactionLinkQuery();
            HQLCriterionsForFilter<TransactionLink> result = q.getHQLCriterionsForFilter();
            result.addQueryModifier(new QueryModifier<TransactionLink>() {
                @Override
                public void modifyQuery(HQLQueryBuilder<TransactionLink> hqlQueryBuilder, Map<String, Object> map) {
                    if (selectedTransaction != null) {
                        TransactionLinkQuery q = new TransactionLinkQuery();
                        hqlQueryBuilder.addRestriction(q.transaction().id().eqRestriction(selectedTransaction.getId()));
                    }
                }
            });
            this.transactionLinksForTransactionFilter = new Filter<TransactionLink>(result);
        }
        return this.transactionLinksForTransactionFilter;
    }

    @Override
    public List<Object> getSource() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSource");
        }

        return source;
    }

    @Override
    public void setSource(List<Object> l) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSource");
        }
        source = l;

    }

    @Override
    public List<Object> getTarget() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTarget");
        }

        return target;
    }

    @Override
    public void setTarget(List<Object> l) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTarget");
        }
        target = l;

    }

    /**
     * Method used to enter transaction maintenance from outside (i.e. menu). Generates complete list of transactions and displays them in a paged
     * table format (listTransactions.seam). If user has
     * MasterModel:edit permission, shows management buttons, otherwise shows only view buttons.
     *
     * @return String next .xhtml page.
     */
    @Override
    public String listTransactions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listTransactions");
        }

        if (Identity.instance().hasPermission("MasterModel", "edit", null)) {
            lstTitle = "gazelle.tf.menu.TransactionsManagement";
        } else {
            lstTitle = "gazelle.tf.menu.TransactionsBrowsing";
        }
        return TF_TRANSACTION_LIST_TRANSACTIONS_SEAM;
    }

	/* *************************************************************************
     * Main functional methods, called from .xhtml pages***********************************************************************
	 */

    /**
     * Action Method for Add New Transaction CommandButton. Creates new, empty transaction and allows it to be edited on (editTransaction.seam)
     *
     * @return String next .xhtml page.
     */

    @Override
    public String addNewTransactionButton() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNewTransactionButton");
        }

        selectedTransaction = new Transaction();
        selectedTransaction.setProfileLinks(new ArrayList<ProfileLink>());
        selectedTransaction.setTransactionLinks(new ArrayList<TransactionLink>());

        edit = true;
        editTitle = "gazelle.tf.transaction.button.AddNewTransaction";
        editSubTitle = "gazelle.tf.Transaction.SubAdd";
        renderLinks = false;
        // **************************** load the integration profile status types

        return "/tf/transaction/editTransaction.seam";
    }

    /**
     * View/Edit the selected transaction information. Displays the detail information for the selected transaction, allowing view or edit as
     * appropriate. Also generates tabs of information related to
     * the selected transaction, including: actors in trans, integration profiles in trans, and profile links in trans. exits to editTransaction.seam.
     *
     * @param t    : transaction to edit
     * @param edit : boolean true=edit, false=view
     * @return String next .xhtml page.
     */
    // ----------------------------------------------- data relating to edit/view
    @Override
    public Boolean renderLinks() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("renderLinks");
        }
        return renderLinks;
    }

    @Override
    public Boolean getEdit() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEdit");
        }
        return edit;
    }

    @Override
    public void setEdit(Boolean edit) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setEdit");
        }
        this.edit = edit;
    }

    @Override
    public Boolean getView() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getView");
        }
        return !edit;
    }

    @Override
    public Integer getIpCount() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIpCount");
        }
        return ipCount;
    }

    @Override
    public Integer getPlCount() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPlCount");
        }
        return plCount;
    }

    @Override
    public Integer getTlCount() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTlCount");
        }
        return tlCount;
    }

    // Number of actors for trans
    @Override
    public Integer getActorCount() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorCount");
        }
        return actorCount;
    }

    @Override
    public String showTransaction(Transaction t) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showTransaction");
        }
        return editTransaction(t, false);
    }

    @Override
    @SuppressWarnings(UNCHECKED)
    public String editTransaction(Transaction t, boolean e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editTransaction");
        }

        selectedTransaction = entityManager.find(Transaction.class, t.getId());
        edit = e;
        renderLinks = true;

        actorCount = getActorsForTransaction().getAllItems(FacesContext.getCurrentInstance()).size();

        ipCount = getIntegrationProfilesForTransaction().getAllItems(FacesContext.getCurrentInstance()).size();

        plCount = selectedTransaction.getProfileLinks().size();

        tlCount = selectedTransaction.getTransactionLinks().size();
        // --------- set editTransaction.seam title and subtitle for edit or view

        if (edit) {

            editTitle = "gazelle.tf.transaction.Edit";
            editSubTitle = "gazelle.tf.transaction.SubEdit";
        } else {

            editTitle = "gazelle.tf.transaction.View";
            editSubTitle = "gazelle.tf.transaction.SubView";
        }

        return "/tf/transaction/editTransaction.seam";
    }

    /**
     * Add or Update transaction information. ends conversation. returns to list transactions page.
     *
     * @return String next .xhtml page.
     */
    @Override
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public String updateTransaction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateTransaction");
        }

        if (selectedTransaction.getKeyword() == null || selectedTransaction.getKeyword().isEmpty()) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Transaction not saved ! No keyword define for this transaction.");
        } else {
            entityManager.merge(selectedTransaction);
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Transaction #{transaction.name} updated");
        }
        return TF_TRANSACTION_LIST_TRANSACTIONS_SEAM;
    }

    @Override
    @SuppressWarnings(UNCHECKED)
    public String showTransactionDeleteSideEffects(Transaction inSelectedTransaction) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showTransactionDeleteSideEffects");
        }
        try {
            selectedTransaction = entityManager.find(Transaction.class, inSelectedTransaction.getId());

            plCount = selectedTransaction.getProfileLinks().size();

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Error : " + e.getMessage());
        }
        return "/tf/transaction/transactionDeleteSideEffects.seam";
    }

    /**
     * Delete the selected transaction
     *
     * @return String : JSF page to render
     */
    @Override
    public String deleteTransaction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteTransaction");
        }
        selectedTransaction = entityManager.find(Transaction.class, selectedTransaction.getId());
        if (selectedTransaction != null) {
            try {

                for (ProfileLink pl : selectedTransaction.getProfileLinks()) {
                    pl = entityManager.find(ProfileLink.class, pl.getId());
                    entityManager.remove(pl);
                    entityManager.flush();
                }
                for (TransactionLink tl : selectedTransaction.getTransactionLinks()) {
                    tl = entityManager.find(TransactionLink.class, tl.getId());
                    entityManager.remove(tl);
                    entityManager.flush();
                }

                entityManager.remove(selectedTransaction);
                entityManager.flush();
                FacesMessages.instance().add(StatusMessage.Severity.INFO, "Transaction removed");
            } catch (Exception e) {
                LOG.error("" + e);
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "An error occured trying to delete the transaction : " + e.getMessage());
            }
            return TF_TRANSACTION_LIST_TRANSACTIONS_SEAM;
        } else {
            return null;
        }
    }

    /**
     * Method used to cancel a transaction
     *
     * @return String : JSF page to render
     */
    @Override
    public String cancelTransaction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelTransaction");
        }

        return TF_TRANSACTION_LIST_TRANSACTIONS_SEAM;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String linkProfiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("linkProfiles");
        }

        linkType = LinkType.INTEGRATIONPROFILES;

        linkTitle = linkSubTitle = "gazelle.tf.transaction.profile.Link";
        linkSourceHeader = "gazelle.tf.transaction.profile.Link.SourceHeader";
        linkTargetHeader = "gazelle.tf.transaction.profile.Link.TargetHeader";
        linkAddAll = "gazelle.tf.transaction.profile.Link.AddAll";
        linkSubAll = "gazelle.tf.transaction.profile.Link.SubAll";
        linkAddOne = "gazelle.tf.transaction.profile.Link.AddOne";
        linkSubOne = "gazelle.tf.transaction.profile.Link.SubOne";
        converter = "IntegrationProfileConverter";

        return "/tf/transaction/linkTransaction.seam";
    }

    // --------------- Action method for link button on transaction profiles tab

    @SuppressWarnings("unchecked")
    @Override
    public String linkActors() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("linkActors");
        }
        linkType = LinkType.ACTORS;

        linkTitle = linkSubTitle = "gazelle.tf.transaction.actors.Link";
        linkSourceHeader = "gazelle.tf.transaction.actor.Link.SourceHeader";
        linkTargetHeader = "gazelle.tf.transaction.actor.Link.TargetHeader";
        linkAddAll = "gazelle.tf.transaction.actor.Link.AddAll";
        linkSubAll = "gazelle.tf.transaction.actor.Link.SubAll";
        linkAddOne = "gazelle.tf.transaction.actor.Link.AddOne";
        linkSubOne = "gazelle.tf.transaction.actor.Link.SubOne";
        converter = "ActorConverter";

        return "/tf/transaction/linkTransaction.seam";
    }

    // ----------------- Action method for link button on transaction actors tab

    // ------------------------------- Common action method for link save button
    @Override
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public String saveLinks() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveLinks");
        }
        switch (linkType) {
            case INTEGRATIONPROFILES:
                break;
            case ACTORS:
                break;
            default:
                break;
        }
        entityManager.merge(selectedTransaction);
        entityManager.flush();

        return TF_TRANSACTION_LIST_TRANSACTIONS_SEAM;
    }

    @Override
    public String cancelLinks() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelLinks");
        }

        return TF_TRANSACTION_LIST_TRANSACTIONS_SEAM;
    }

    /**
     * Get transaction from keyword got in URL Change selected transaction with transaction returned Load the viewing IP Method used by redirection
     * link
     */
    @Override
    public void getSpecificTransactionFromKeyword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSpecificTransactionFromKeyword");
        }
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String transactionkeyword = params.get("keyword");

        selectedTransaction = Transaction.GetTransactionByKeyword(transactionkeyword);
        this.editTransaction(selectedTransaction, false);
    }

    @Override
    public Actor getSelectedActorFrom() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedActorFrom");
        }
        return selectedActorFrom;
    }

    @Override
    public void setSelectedActorFrom(Actor selectedActorFrom) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedActorFrom");
        }
        this.selectedActorFrom = selectedActorFrom;
        this.getTransactions().setSelectedActorFrom(this.selectedActorFrom);
    }

    @Override
    public Actor getSelectedActorTo() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedActorTo");
        }
        return selectedActorTo;
    }

    @Override
    public void setSelectedActorTo(Actor selectedActorTo) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedActorTo");
        }
        this.selectedActorTo = selectedActorTo;
        this.getTransactions().setSelectedActorTo(this.selectedActorTo);
    }

    @Override
    public Domain getSelectedDomain() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedDomain");
        }
        return selectedDomain;
    }

    @Override
    public void setSelectedDomain(Domain selectedDomain) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedDomain");
        }
        this.selectedDomain = selectedDomain;
        this.getTransactions().setSelectedDomain(this.selectedDomain);
    }

    @Override
    public IntegrationProfile getSelectedIntegrationProfile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedIntegrationProfile");
        }
        return selectedIntegrationProfile;
    }

    @Override
    public void setSelectedIntegrationProfile(IntegrationProfile selectedIntegrationProfile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedIntegrationProfile");
        }
        this.selectedIntegrationProfile = selectedIntegrationProfile;
        this.getTransactions().setSelectedIntegrationProfile(this.selectedIntegrationProfile);
    }

    @Override
    public String getSelectedSearch() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedSearch");
        }
        return selectedSearch;
    }

    @Override
    public void setSelectedSearch(String selectedSearch) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedSearch");
        }
        this.selectedSearch = selectedSearch;
    }

    @Override
    public List<Domain> getPossibleDomains() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleDomains");
        }
        List<Domain> res = Domain.getPossibleDomains();
        Collections.sort(res);
        return res;
    }

    @Override
    public List<IntegrationProfile> getPossibleIntegrationProfiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleIntegrationProfiles");
        }
        HQLQueryBuilder<IntegrationProfile> hh = new HQLQueryBuilder<IntegrationProfile>(IntegrationProfile.class);
        if (this.selectedDomain != null) {
            hh.addEq("domainsForDP.keyword", this.selectedDomain.getKeyword());
        }
        List<IntegrationProfile> res = hh.getList();
        Collections.sort(res);
        return res;
    }

    @Override
    public List<Actor> getPossibleActorFroms() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleActorFroms");
        }
        HQLQueryBuilder<Actor> hh = new HQLQueryBuilder<Actor>(Actor.class);
        if (this.selectedActorTo != null) {
            hh.addRestriction(HQLRestrictions.in("this", this.getListActorFrom(selectedActorTo)));
        }
        List<Actor> res = hh.getList();
        Collections.sort(res);
        return res;
    }

    @SuppressWarnings(UNCHECKED)
    private List<Actor> getListActorTo(Actor actFrom) {
        Query query = entityManager
                .createQuery("SELECT distinct tl.toActor FROM TransactionLink tl WHERE tl.fromActor=:inActor");
        query.setParameter("inActor", actFrom);
        return query.getResultList();
    }

    @SuppressWarnings(UNCHECKED)
    private List<Actor> getListActorFrom(Actor actTo) {
        Query query = entityManager
                .createQuery("SELECT distinct tl.fromActor FROM TransactionLink tl WHERE tl.toActor=:inActor");
        query.setParameter("inActor", actTo);
        return query.getResultList();
    }

    @Override
    public List<Actor> getPossibleActorTos() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleActorTos");
        }
        HQLQueryBuilder<Actor> hh = new HQLQueryBuilder<Actor>(Actor.class);
        if (this.selectedActorFrom != null) {
            hh.addRestriction(HQLRestrictions.in("this", this.getListActorTo(selectedActorFrom)));
        }
        List<Actor> res = hh.getList();
        Collections.sort(res);
        return res;
    }

    @Override
    public void resetFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetFilter");
        }
        this.setSelectedDomain(null);
        this.setSelectedIntegrationProfile(null);
        this.setSelectedActorFrom(null);
        this.setSelectedActorTo(null);
    }

    @Override
    public void refreshSelectedTransaction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("refreshSelectedTransaction");
        }
        selectedTransaction = entityManager.find(Transaction.class, selectedTransaction.getId());
    }

    @Override
    public List<String> getPossibleSearchValues() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getPossibleSearchValues");
        }
        List<String> dd = new ArrayList<String>();
        dd.add("Domains/Integration Profiles");
        dd.add("Actors");
        return dd;
    }

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    @Override
    public String addSectionPage(Transaction transaction, String callerPage) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addSectionPage");
        }

        selectedTransaction = transaction;
        this.callerPage = callerPage;

        return addSectionPage();
    }

    @Override
    public String addSectionPage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addSectionPage");
        }

        filter = null;
        documentSections = null;
        getFilter();

        return "/tf/transaction/addDocumentSection.xhtml";
    }

    private String getReturnPage() {
        String pageToReturn = null;
        if (callerPage != null) {
            if (callerPage.equals("transactionList")) {
                pageToReturn = TF_TRANSACTION_LIST_TRANSACTIONS_SEAM;
            } else if (callerPage.equals("consistencyCheck")) {
                pageToReturn = "/tf/utilities/tfConsistencyCheckList.seam";
            } else {
                pageToReturn = "/tf/transaction/editTransaction.xhtml";
            }
        } else {
            pageToReturn = "/tf/transaction/editTransaction.xhtml";
        }

        callerPage = null;
        return pageToReturn;
    }

    @Override
    public DocumentSectionsFilter getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();
            filter = new DocumentSectionsFilter(requestParameterMap);
        }
        return filter;
    }

    @Override
    public FilterDataModel<DocumentSection> getDocumentSections() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDocumentSections");
        }
        if (documentSections == null) {
            documentSections = new FilterDataModel<DocumentSection>(getFilter()) {
                @Override
                protected Object getId(DocumentSection t) {
                    // TODO Auto-generated method stub
                    return t.getId();
                }
            };
        }
        return documentSections;
    }

    @Override
    public String addThisSection(DocumentSection documentSection) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addThisSection");
        }
        selectedTransaction.setDocumentSection(documentSection);
        try {
            entityManager.merge(selectedTransaction);
            entityManager.flush();
        } catch (Exception e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Cannot link this document section");

        }
        return getReturnPage();
    }

    @Override
    public void deleteSection(Transaction transaction) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSection");
        }

        selectedTransaction = transaction;
        deleteSection();
    }

    /**
     * Delete a selected Document
     */
    @Override
    public void deleteSection() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSection");
        }

        selectedTransaction.setDocumentSection(null);
        try {
            entityManager.merge(selectedTransaction);
            entityManager.flush();
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Section removed");
        } catch (Exception e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "This section cannot be deleted");

        }
    }

    @Override
    public String cancelAddDocumentSection(Transaction transaction) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelAddDocumentSection");
        }
        edit = true;
        selectedTransaction = entityManager.find(Transaction.class, transaction.getId());
        renderLinks = true;
        return getReturnPage();
    }

    /**
     * Manage standards referenced by the transaction
     */
    public void deleteStandard(Standard standardToDelete) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteStandard");
        }
        selectedTransaction.getStandards().remove(standardToDelete);
        selectedTransaction.saveOrMerge(entityManager);
    }

    private static enum LinkType {
        INTEGRATIONPROFILES,
        ACTORS
    }

}
