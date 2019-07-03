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
import net.ihe.gazelle.common.log.ExceptionLogging;
import net.ihe.gazelle.documents.DocumentSectionsFilter;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.tf.model.*;
import net.ihe.gazelle.users.model.User;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.security.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.*;


@Name("actorManager")
@Scope(ScopeType.SESSION)
@GenerateInterface("ActorManagerLocal")
public class ActorManager implements Serializable, ActorManagerLocal, QueryModifier<Actor> {

    private static final String FOUND = "Found ";

    /**
     * Serial ID version of this object
     */
    private static final long serialVersionUID = -1357012043456235100L;
    private static final Logger LOG = LoggerFactory.getLogger(ActorManager.class);

    @In
    private EntityManager entityManager;

    private Actor selectedActor;

    private String keywordSearchString;

    private List<Actor> actors;

    private Filter<Actor> filterForActor;

    private List<ActorIntegrationProfileOption> linkIntegrationProfileActorDependentAIPOs;

    private List<ProfileLink> linkIntegrationProfileActorDependentPLs;

    private List<IntegrationProfile> linkedProfiles = new ArrayList<IntegrationProfile>();

    private boolean displayLinkedActorsErrorModelPanel = false;

    @In
    private Map<String, String> messages;

    private String lstTitle;

    private String editTitle;

    private String editSubTitle;

    private String editDescription;

    private List<Hl7MessageProfile> hl7MessageProfilesForActor;
    private Boolean edit = false;
    private Integer ipCount = 0;
    private Integer ipIpoCount = 0;
    private Integer ipTransCount = 0;
    private Integer allTransactionCount = 0;

    private Domain selectedDomain;

    private boolean actorReadOnly = false;

    private Filter<IntegrationProfile> integrationProfilesFilter;
    private Filter<ActorIntegrationProfileOption> integrationProfilesAndIntegrationProfileOptionsForActorFilter;
    private Filter<ProfileLink> integrationProfilesAndTransactionsForActorFilter;
    private Filter<TransactionLink> transactionLinksForActorFilter;
    private Filter<Transaction> allTransactionsForActorFilter;

    // ----------------------------------------------------------------------------------- accessors
    private List<IntegrationProfile> originalLinkedProfiles = new ArrayList<IntegrationProfile>();
    /**
     * Fill integrationProfilesForActor list with entries from ActorIntegrationProfile associated with actor
     *
     * @param a
     * : Actor
     */
    private List<ActorIntegrationProfile> actorIntegrationProfileList = null;
    private ActorIntegrationProfileOption selectedAipo;
    private DocumentSectionsFilter filter;
    private FilterDataModel<DocumentSection> documentSections;

    public static Actor getActor(String keyword, EntityManager em) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Actor getActor");
        }

        Actor tmp = (Actor) em.createQuery("select distinct a from Actor a where keyword = '" + keyword + "'")
                .getResultList().get(0);
        return tmp;
    }

    public Filter<Actor> getFilterForActor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilterForActor");
        }
        if (filterForActor == null) {
            filterForActor = new Filter<Actor>(getHQLCriterionForFilter());
        }
        return filterForActor;
    }

    private HQLCriterionsForFilter<Actor> getHQLCriterionForFilter() {
        ActorQuery query = new ActorQuery();
        HQLCriterionsForFilter<Actor> criterionsForFilter = query.getHQLCriterionsForFilter();
        criterionsForFilter.addQueryModifier(this);
        return criterionsForFilter;
    }

    @Override
    public void modifyQuery(HQLQueryBuilder<Actor> queryBuilder, Map<String, Object> params) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyQuery");
        }
        if (selectedDomain != null) {
            ActorQuery query = new ActorQuery();
            queryBuilder.addRestriction(query.actorIntegrationProfiles().integrationProfile().domainsForDP().id().eqRestriction(selectedDomain
                    .getId()));
        }
    }

    public FilterDataModel<Actor> getFilteredActors() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilteredActors");
        }
        return new FilterDataModel<Actor>(getFilterForActor()) {
            @Override
            protected Object getId(Actor actor) {
                return actor.getId();
            }
        };
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
    public EntityManager getEntityManager() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEntityManager");
        }
        return entityManager;
    }

    @Override
    public void setEntityManager(EntityManager entityManager) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setEntityManager");
        }
        this.entityManager = entityManager;
    }

    @Override
    public Actor getSelectedActor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedActor");
        }
        return selectedActor;
    }

    @Override
    public void setSelectedActor(Actor selectedActor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedActor");
        }
        this.selectedActor = selectedActor;
    }

    @Override
    public List<Actor> getActors() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActors");
        }
        return actors;
    }

    @Override
    public void setActors(List<Actor> actors) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setActors");
        }
        this.actors = actors;
    }

    @Override
    public FilterDataModel<IntegrationProfile> getIntegrationProfilesForActor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntegrationProfilesForActor");
        }
        return new FilterDataModel<IntegrationProfile>(getFilter()) {
            @Override
            protected Object getId(IntegrationProfile integrationProfile) {
                return integrationProfile.getId();
            }
        };
    }

    private Filter<IntegrationProfile> getFilter() {
        if (integrationProfilesFilter == null) {
            IntegrationProfileQuery q = new IntegrationProfileQuery();
            HQLCriterionsForFilter<IntegrationProfile> result = q.getHQLCriterionsForFilter();
            result.addQueryModifier(new QueryModifier<IntegrationProfile>() {
                @Override
                public void modifyQuery(HQLQueryBuilder<IntegrationProfile> hqlQueryBuilder, Map<String, Object> map) {
                    if (selectedActor != null) {
                        IntegrationProfileQuery q = new IntegrationProfileQuery();
                        hqlQueryBuilder.addRestriction(q.actorIntegrationProfiles().actor().id().eqRestriction(selectedActor.getId()));
                    }
                }
            });
            this.integrationProfilesFilter = new Filter<IntegrationProfile>(result);
        }
        return this.integrationProfilesFilter;
    }

    @Override
    public FilterDataModel<Transaction> getAllTransactionsForActor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllTransactionsForActor");
        }
        return new FilterDataModel<Transaction>(getAllTransactionsForActorFilter()) {
            @Override
            protected Object getId(Transaction transaction) {
                return transaction.getId();
            }
        };
    }

    private Filter<Transaction> getAllTransactionsForActorFilter() {
        if (allTransactionsForActorFilter == null) {
            TransactionQuery q = new TransactionQuery();
            HQLCriterionsForFilter<Transaction> result = q.getHQLCriterionsForFilter();
            result.addQueryModifier(new QueryModifier<Transaction>() {
                @Override
                public void modifyQuery(HQLQueryBuilder<Transaction> hqlQueryBuilder, Map<String, Object> map) {
                    if (selectedActor != null) {
                        TransactionQuery q = new TransactionQuery();
                        hqlQueryBuilder.addRestriction(q.profileLinks().actorIntegrationProfile().actor().id().eqRestriction(selectedActor.getId()));
                    }
                }
            });
            this.allTransactionsForActorFilter = new Filter<Transaction>(result);
        }
        return this.allTransactionsForActorFilter;
    }

    @Override
    public FilterDataModel<ActorIntegrationProfileOption> getIntegrationProfilesAndIntegrationProfileOptionsForActor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntegrationProfilesAndIntegrationProfileOptionsForActor");
        }
        return new FilterDataModel<ActorIntegrationProfileOption>(getAipoListFilter()) {
            @Override
            protected Object getId(ActorIntegrationProfileOption actorIntegrationProfileOption) {
                return actorIntegrationProfileOption.getId();
            }
        };
    }

    private Filter<ActorIntegrationProfileOption> getAipoListFilter() {
        if (integrationProfilesAndIntegrationProfileOptionsForActorFilter == null) {
            ActorIntegrationProfileOptionQuery query = new ActorIntegrationProfileOptionQuery();
            HQLCriterionsForFilter<ActorIntegrationProfileOption> result = query.getHQLCriterionsForFilter();
            result.addQueryModifier(new QueryModifier<ActorIntegrationProfileOption>() {
                @Override
                public void modifyQuery(HQLQueryBuilder<ActorIntegrationProfileOption> hqlQueryBuilder, Map<String, Object> map) {
                    if (selectedActor != null) {
                        ActorIntegrationProfileOptionQuery query = new ActorIntegrationProfileOptionQuery();
                        hqlQueryBuilder.addRestriction(query.actorIntegrationProfile().actor().id().eqRestriction(selectedActor.getId()));
                    }
                }
            });
            this.integrationProfilesAndIntegrationProfileOptionsForActorFilter = new Filter<ActorIntegrationProfileOption>(result);
        }
        return integrationProfilesAndIntegrationProfileOptionsForActorFilter;
    }

    @Override
    public FilterDataModel<ProfileLink> getIntegrationProfilesAndTransactionsForActor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntegrationProfilesAndTransactionsForActor");
        }
        return new FilterDataModel<ProfileLink>(getProfileLinkFilter()) {
            @Override
            protected Object getId(ProfileLink profileLink) {
                return profileLink.getId();
            }
        };
    }

    private Filter<ProfileLink> getProfileLinkFilter() {
        if (integrationProfilesAndTransactionsForActorFilter == null) {
            ProfileLinkQuery query = new ProfileLinkQuery();
            HQLCriterionsForFilter<ProfileLink> result = query.getHQLCriterionsForFilter();
            result.addQueryModifier(new QueryModifier<ProfileLink>() {
                @Override
                public void modifyQuery(HQLQueryBuilder<ProfileLink> hqlQueryBuilder, Map<String, Object> map) {
                    if (selectedActor != null) {
                        ProfileLinkQuery query = new ProfileLinkQuery();
                        hqlQueryBuilder.addRestriction(query.actorIntegrationProfile().actor().id().eqRestriction(selectedActor.getId()));
                    }
                }
            });
            this.integrationProfilesAndTransactionsForActorFilter = new Filter<ProfileLink>(result);
        }
        return this.integrationProfilesAndTransactionsForActorFilter;
    }

    @Override
    public List<ActorIntegrationProfileOption> getLinkIntegrationProfileActorDependentAIPOs() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLinkIntegrationProfileActorDependentAIPOs");
        }
        return linkIntegrationProfileActorDependentAIPOs;
    }

    @Override
    public void setLinkIntegrationProfileActorDependentAIPOs(
            List<ActorIntegrationProfileOption> linkIntegrationProfileActorDependentAIPOs) {
        this.linkIntegrationProfileActorDependentAIPOs = linkIntegrationProfileActorDependentAIPOs;
    }

    @Override
    public List<ProfileLink> getLinkIntegrationProfileActorDependentPLs() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLinkIntegrationProfileActorDependentPLs");
        }
        return linkIntegrationProfileActorDependentPLs;
    }

    @Override
    public void setLinkIntegrationProfileActorDependentPLs(List<ProfileLink> linkIntegrationProfileActorDependentPLs) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setLinkIntegrationProfileActorDependentPLs");
        }
        this.linkIntegrationProfileActorDependentPLs = linkIntegrationProfileActorDependentPLs;
    }

    @Override
    public FilterDataModel<TransactionLink> getTransactionLinksForActor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTransactionLinksForActor");
        }
        return new FilterDataModel<TransactionLink>(getTransactionLinksForActorFilter()) {
            @Override
            protected Object getId(TransactionLink transactionLink) {
                return transactionLink.getId();
            }
        };
    }

    private Filter<TransactionLink> getTransactionLinksForActorFilter() {
        if (transactionLinksForActorFilter == null) {
            TransactionLinkQuery q = new TransactionLinkQuery();
            HQLCriterionsForFilter<TransactionLink> result = q.getHQLCriterionsForFilter();
            result.addQueryModifier(new QueryModifier<TransactionLink>() {
                @Override
                public void modifyQuery(HQLQueryBuilder<TransactionLink> hqlQueryBuilder, Map<String, Object> map) {
                    if (selectedActor != null) {
                        TransactionLinkQuery q = new TransactionLinkQuery();
                        hqlQueryBuilder.addRestriction(
                                HQLRestrictions.or(q.toActor().id().eqRestriction(selectedActor.getId()), q.fromActor().id().eqRestriction
                                        (selectedActor.getId())));
                    }
                }
            });
            this.transactionLinksForActorFilter = new Filter<TransactionLink>(result);
        }
        return this.transactionLinksForActorFilter;
    }

    @Override
    public boolean isDisplayLinkedActorsErrorModelPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isDisplayLinkedActorsErrorModelPanel");
        }
        return displayLinkedActorsErrorModelPanel;
    }

    @Override
    public void setDisplayLinkedActorsErrorModelPanel(boolean displayLinkedActorsErrorModelPanel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDisplayLinkedActorsErrorModelPanel");
        }
        this.displayLinkedActorsErrorModelPanel = displayLinkedActorsErrorModelPanel;
    }

    @Override
    public Map<String, String> getMessages() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMessages");
        }
        return messages;
    }

    @Override
    public void setMessages(Map<String, String> messages) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setMessages");
        }
        this.messages = messages;
    }

    @Override
    public List<Hl7MessageProfile> getHl7MessageProfilesForActor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getHl7MessageProfilesForActor");
        }
        return hl7MessageProfilesForActor;
    }

    @Override
    public void setHl7MessageProfilesForActor(List<Hl7MessageProfile> hl7MessageProfilesForActor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setHl7MessageProfilesForActor");
        }
        this.hl7MessageProfilesForActor = hl7MessageProfilesForActor;
    }

    @Override
    public List<IntegrationProfile> getOriginalLinkedProfiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOriginalLinkedProfiles");
        }
        return originalLinkedProfiles;
    }

    @Override
    public void setOriginalLinkedProfiles(List<IntegrationProfile> originalLinkedProfiles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setOriginalLinkedProfiles");
        }
        this.originalLinkedProfiles = originalLinkedProfiles;
    }

    @Override
    public List<ActorIntegrationProfile> getActorIntegrationProfileList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorIntegrationProfileList");
        }
        if (selectedActor != null) {
            ActorIntegrationProfileQuery q = new ActorIntegrationProfileQuery();
            q.actor().id().eq(selectedActor.getId());
            setActorIntegrationProfileList(q.getListDistinct());
        }
        return actorIntegrationProfileList;
    }

    @Override
    public void setActorIntegrationProfileList(List<ActorIntegrationProfile> actorIntegrationProfileList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setActorIntegrationProfileList");
        }
        this.actorIntegrationProfileList = actorIntegrationProfileList;
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
    public void setIpCount(Integer ipCount) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setIpCount");
        }
        this.ipCount = ipCount;
    }

    @Override
    public Integer getIpIpoCount() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIpIpoCount");
        }
        return ipIpoCount;
    }

    @Override
    public void setIpIpoCount(Integer ipIpoCount) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setIpIpoCount");
        }
        this.ipIpoCount = ipIpoCount;
    }

    @Override
    public Integer getIpTransCount() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIpTransCount");
        }
        return ipTransCount;
    }

    @Override
    public void setIpTransCount(Integer ipTransCount) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setIpTransCount");
        }
        this.ipTransCount = ipTransCount;
    }

    @Override
    public Integer getAllTransactionCount() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllTransactionCount");
        }
        return allTransactionCount;
    }

    @Override
    public void setAllTransactionCount(Integer allTransactionCount) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setAllTransactionCount");
        }
        this.allTransactionCount = allTransactionCount;
    }

    @Override
    public List<IntegrationProfile> getLinkedProfiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLinkedProfiles");
        }
        return linkedProfiles;
    }

    @Override
    public void setLinkedProfiles(List<IntegrationProfile> ips) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setLinkedProfiles");
        }
        linkedProfiles = ips;
    }

    @Override
    public List<IntegrationProfile> getUnlinkedProfiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUnlinkedProfiles");
        }
        IntegrationProfileQuery q = new IntegrationProfileQuery();
        q.keyword().order(true);
        List<IntegrationProfile> unlinkedProfiles = q.getListDistinct();
        Collections.sort(unlinkedProfiles);
        return unlinkedProfiles;
    }

    @Override
    public boolean getDisplayLinkedActorsErrorPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDisplayLinkedActorsErrorPanel");
        }
        return displayLinkedActorsErrorModelPanel;
    }

    @Override
    public Integer getTlCount() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTlCount");
        }
        if (getTransactionLinksForActor().getAllItems(FacesContext.getCurrentInstance()) == null) {
            return 0;
        } else {
            return getTransactionLinksForActor().getAllItems(FacesContext.getCurrentInstance()).size();
        }
    }

    @Override
    public String getLstTitle() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLstTitle");
        }
        if (Identity.instance().hasPermission("MasterModel", "edit", null)) {
            lstTitle = "gazelle.tf.menu.ActorManagement";
        } else {
            lstTitle = "gazelle.tf.menu.ActorBrowsing";
        }
        return messages.get(lstTitle);
    }

    @Override
    public void setLstTitle(String lstTitle) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setLstTitle");
        }
        this.lstTitle = lstTitle;
    }

    @Override
    public String getEditTitle() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEditTitle");
        }
        if (selectedActor == null) {
            return "getEditTitle - actor is null";
        }

        if (selectedActor.getId() == null) {
            return (messages.get(editTitle) + " : " + messages.get("gazelle.common.New"));
        } else {
            return (messages.get(editTitle) + " : " + selectedActor.getName());
        }

    }

    @Override
    public void setEditTitle(String editTitle) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setEditTitle");
        }
        this.editTitle = editTitle;
    }

    @Override
    public String getEditSubTitle() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEditSubTitle");
        }
        return messages.get(editSubTitle);
    }

    @Override
    public void setEditSubTitle(String editSubTitle) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setEditSubTitle");
        }
        this.editSubTitle = editSubTitle;
    }

    @Override
    public String getEditDescription() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEditDescription");
        }
        return messages.get(editDescription);
    }

    // --------------------------------------------------------------------------------------------- Methods

    @Override
    public void setEditDescription(String editDescription) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setEditDescription");
        }
        this.editDescription = editDescription;
    }

    @Override
    public String getKeywordSearchString() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getKeywordSearchString");
        }
        return this.keywordSearchString;
    }

    @Override
    public void setKeywordSearchString(String value) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setKeywordSearchString");
        }
        this.keywordSearchString = value;
    }

    @Override
    public boolean isActorReadOnly() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isActorReadOnly");
        }
        return actorReadOnly;
    }

    @Override
    public void setActorReadOnly(boolean actorReadOnly) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setActorReadOnly");
        }
        this.actorReadOnly = actorReadOnly;
    }

    @Override
    public String listActors() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listActors");
        }
        return "/tf/actor/listActors.seam";
    }

    @Override
    public void findActors() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findActors");
        }
        if (this.selectedDomain != null) {
            this.actors = Actor.getActorFiltered(null, null, this.selectedDomain);
        } else if ((this.keywordSearchString == null) || (this.keywordSearchString.equals(""))) {
            this.actors = Actor.listAllActors();
        } else {
            String searchPattern = this.keywordSearchString.toLowerCase().replace('*', '%') + '%';
            Query query = entityManager.createQuery("SELECT c FROM Actor c where lower(c.keyword) like :search")
                    .setParameter("search", searchPattern);

            this.actors = query.getResultList();
        }
    }

    @Override
    public void resetListFind() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetListFind");
        }
        this.keywordSearchString = null;
    }

    @Override
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public String addActor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addActor");
        }
        if (!isKeywordAvailable()) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "An actor with this keyword already exists");
            return null;
        }
        if (!isNameAvailable()) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "An actor with this name already exists");
            return null;
        }
        if (selectedActor == null) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Error selected actor is null ");

        } else {
            selectedActor = entityManager.merge(selectedActor);
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Actor #{actor.name} updated");
        }
        return "/tf/actor/listActors.seam";

    }

    @Override
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public void updateActor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("updateActor");
        }

        entityManager.merge(selectedActor);
    }

    @Override
    public String showActorDeleteSideEffects(Actor inSelectedActor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showActorDeleteSideEffects");
        }
        try {
            selectedActor = entityManager.find(Actor.class, inSelectedActor.getId());

            ipIpoCount = this.getIntegrationProfilesAndIntegrationProfileOptionsForActor().getAllItems(FacesContext.getCurrentInstance()).size();
            ipTransCount = this.getIntegrationProfilesAndTransactionsForActor().getAllItems(FacesContext.getCurrentInstance()).size();
            allTransactionCount = this.getAllTransactionsForActor().getAllItems(FacesContext.getCurrentInstance()).size();
            hl7MessageProfilesForActor = entityManager.createQuery(
                    "select mp from Hl7MessageProfile mp where mp.actor.id = " + selectedActor.getId()
                            + " order by mp.messageType").getResultList();

        } catch (Exception e) {
            ExceptionLogging.logException(e, LOG);
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Error : " + e.getMessage());
        }
        return "/tf/actor/actorDeleteSideEffects.seam";
    }

    /**
     * delete the selected actor.
     */

    @Override
    @Transactional
    public String deleteActor() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteActor");
        }
        if (selectedActor != null) {
            try {

                // Definition de l'agenda 21 !
                Query q = entityManager
                        .createQuery("SELECT actor from Actor actor fetch all properties where actor = :inActor");
                q.setParameter("inActor", selectedActor);

                selectedActor = (Actor) q.getSingleResult();

                for (TransactionLink tl : getTransactionLinksForActor().getAllItems(FacesContext.getCurrentInstance())) {
                    entityManager.remove(entityManager.merge(tl));
                }
                for (Hl7MessageProfile mp : hl7MessageProfilesForActor) {
                    entityManager.remove(entityManager.merge(mp));
                }
                for (ProfileLink pl : getIntegrationProfilesAndTransactionsForActor().getAllItems(FacesContext.getCurrentInstance())) {
                    entityManager.remove(entityManager.merge(pl));
                }
                for (ActorIntegrationProfileOption aipo : getIntegrationProfilesAndIntegrationProfileOptionsForActor().getAllItems(FacesContext
                        .getCurrentInstance())) {
                    entityManager.remove(entityManager.merge(aipo));
                }
                for (ActorIntegrationProfile aip : getActorIntegrationProfileList()) {
                    entityManager.remove(entityManager.merge(aip));
                }
                entityManager.remove(entityManager.merge(selectedActor));
                entityManager.flush();
                StatusMessages.instance().add(StatusMessage.Severity.INFO,
                        "Actor (" + selectedActor.getKeyword() + " - " + selectedActor.getName()
                                + ") has been successfully deleted");

            } catch (Exception e) {
                StatusMessages.instance().add(StatusMessage.Severity.ERROR, "Error deleting actor : " + e.getMessage());
                LOG.error("", e);
            }

            return listActors();
        } else {
            StatusMessages.instance().add(StatusMessage.Severity.ERROR, "Error deleting actor : selected actor is null");
            return listActors();
        }

    }

    @Override
    public String editActor(final Actor inSelectedActor, boolean e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editActor");
        }
        edit = e;

        selectedActor = entityManager.find(Actor.class, inSelectedActor.getId());

        ipCount = this.getIntegrationProfilesForActor().getAllItems(FacesContext.getCurrentInstance()).size();
        ipIpoCount = this.getIntegrationProfilesAndIntegrationProfileOptionsForActor().getAllItems(FacesContext.getCurrentInstance()).size();
        ipTransCount = this.getIntegrationProfilesAndTransactionsForActor().getAllItems(FacesContext.getCurrentInstance()).size();

        allTransactionCount = this.getAllTransactionsForActor().getAllItems(FacesContext.getCurrentInstance()).size();

        // ** set edit/view titles ** //
        if (edit) {
            editTitle = "gazelle.tf.Actor";
            editSubTitle = "gazelle.tf.actor.EditActorInformationHeader";
            editDescription = "gazelle.tf.actor.EditActorInformationDescription";
        } else {
            editTitle = "gazelle.tf.Actor";
            editSubTitle = "gazelle.tf.actor.ShowActorInformationHeader";
            editDescription = "gazelle.tf.actor.ShowActorInformationDescription";
        }
        return "/tf/actor/editActor.seam";
    }

    @Override
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public void createActor(Actor c) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createActor");
        }

        entityManager.persist(c);
        entityManager.flush();
    }

    @Override
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public String duplicateActor(Actor actorToDuplicate) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("duplicateActor");
        }
        actorToDuplicate = entityManager.find(Actor.class, actorToDuplicate.getId());
        setSelectedActor(actorToDuplicate);

        // Creation of a new actor
        Actor newActor = Actor.duplicateActor(actorToDuplicate);

        List<TransactionLink> transactionLinks = new ArrayList<TransactionLink>();
        List<ActorIntegrationProfileOption> actorIntegrationProfileOption = new ArrayList<ActorIntegrationProfileOption>();
        newActor.setTransactionLinksWhereActingAsSource(transactionLinks);
        newActor.setTransactionLinksWhereActingAsReceiver(transactionLinks);
        newActor.setActorIntegrationProfileOption(actorIntegrationProfileOption);
        newActor.setLastChanged(new Date());
        newActor.setLastModifierId(User.loggedInUser().getUsername());
        newActor.setToDisplay(actorToDuplicate.isToDisplay());

        newActor.saveOrMerge(entityManager);

        // Link actor and profile
        List<IntegrationProfile> lip = new ArrayList<IntegrationProfile>();

        for (ActorIntegrationProfile aip : getActorIntegrationProfileList()) {
            IntegrationProfile ip = aip.getIntegrationProfile();
            lip.add(ip);
        }

        setOriginalLinkedProfiles(new ArrayList<IntegrationProfile>());
        setLinkedProfiles(lip);
        saveActorProfiles(newActor);

        // Add aipo
        List<IntegrationProfileOption> lipo = new ArrayList<IntegrationProfileOption>();
        for (ActorIntegrationProfileOption aipo : ActorIntegrationProfileOption.getIntegrationProfilesOptionsForActor(actorToDuplicate)) {
            IntegrationProfileOption ipo = aipo.getIntegrationProfileOption();
            lipo.add(ipo);

            // Create links Profile - options
            List<IntegrationProfileOption> lipo2 = new ArrayList<IntegrationProfileOption>();
            lipo2.add(ipo);
            List<IntegrationProfile> lip2 = new ArrayList<IntegrationProfile>();
            lip2.add(aipo.getActorIntegrationProfile().getIntegrationProfile());
            AipoManager am = new AipoManager();
            List<Actor> asa = new ArrayList<Actor>();
            asa.add(newActor);
            am.createLinks(asa, lip2, lipo2);
        }

        // Create link Profile - Transactions
        for (ProfileLink pl : ProfileLink.getProfileLinksForActor(actorToDuplicate)) {
            List<IntegrationProfile> lip2 = new ArrayList<IntegrationProfile>();
            lip2.add(pl.getActorIntegrationProfile().getIntegrationProfile());
            List<Transaction> lt = new ArrayList<Transaction>();
            lt.add(pl.getTransaction());

            List<Actor> asa = new ArrayList<Actor>();
            asa.add(newActor);
            ProfileLinksManager plm = new ProfileLinksManager();
            plm.createLinks(asa, lip2, lt, pl.getTransactionOptionType());
        }

        // Create link Actor - Transactions
        for (TransactionLink tl : TransactionLink.getTransactionLinkForActor(actorToDuplicate)) {
            TransactionLinkManager tlm = new TransactionLinkManager();
            List<Transaction> lt = new ArrayList<Transaction>();
            lt.add(tl.getTransaction());
            tlm.createLinks(newActor, tl.getToActor(), lt);
        }
        FacesMessages.instance().clearGlobalMessages();
        FacesMessages.instance().add(StatusMessage.Severity.INFO, newActor.getName() + " is copied");

        return "/tf/actor/listActors.seam";
    }

    @Override
    public List<Actor> getAllActors() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllActors");
        }

        List<Actor> allFoundActors;

        allFoundActors = entityManager.createQuery("select distinct a from Actor a ").getResultList();
        return allFoundActors;
    }

    /**
     * Search all the Actors corresponding to the beginning of a String (filled in the actor list box). Eg. If we search the String 'A' we will
     * find the actor : Adt, ... This operation is allowed for
     * some granted users (check the security.drl)
     *
     * @param event : Key up event caught from the JSF
     * @return List of Actor objects : List of all actors to display into the list box
     */

    @Override
    public List<Actor> actorAutoComplete(Object event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("actorAutoComplete");
        }

        List<Actor> returnList = null;
        Session s = (Session) entityManager.getDelegate();
        Criteria c = s.createCriteria(Actor.class);

        String searchedString = event.toString();

        if (searchedString.trim().equals("*")) {
            c.addOrder(Order.asc("name"));
            returnList = c.list();
            Actor fakeAllActor = new Actor();
            fakeAllActor.setName("*");
            returnList.add(0, fakeAllActor);
        } else {
            c.add(Restrictions.or(Restrictions.ilike("name", "%" + searchedString + "%"),
                    Restrictions.ilike("keyword", "%" + searchedString + "%")));

            c.addOrder(Order.asc("name"));
            returnList = c.list();

        }
        return returnList;
    }

    @Override
    public Actor getActor(String keyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActor");
        }

        Actor tmp = (Actor) entityManager
                .createQuery("select distinct a from Actor a where keyword = '" + keyword + "'").getResultList().get(0);
        return tmp;
    }

    /**
     * When Add new actor button is clicked, the method called to redirect to editActor page to add new actor
     *
     * @return String of web page to display
     */
    @Override
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public String addNewActorButton() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNewActorButton");
        }
        selectedActor = new Actor();
        this.edit = true;
        ipCount = 0;
        ipIpoCount = 0;
        ipTransCount = 0;
        allTransactionCount = 0;

        editTitle = "gazelle.tf.Actor";
        editSubTitle = "gazelle.tf.actor.AddActorInformationHeader";
        editDescription = "gazelle.tf.actor.AddActorInformationDescription";
        return "/tf/actor/editActor.seam";
    }

    /**
     * Action method for "link actor profiles" button Sets up data for listShuttle to maintain actor - The profile currently being displayed
     * linkedprofile - IntegrationProfiles currently linked to
     * selected Actor unlinkedProfiles - IntegrationProfiles NOT currently linked to selected Actor
     *
     * @return String of web page to display (listShuttle page)
     */
    @Override
    public String linkActorProfiles(Actor selectedActor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("linkActorProfiles");
        }
        linkIntegrationProfileActorDependentAIPOs = null;
        linkIntegrationProfileActorDependentPLs = null;
        displayLinkedActorsErrorModelPanel = false;
        actorReadOnly = true;

        if (selectedActor.getId() != null) {
            selectedActor = entityManager.find(Actor.class, selectedActor.getId());
            originalLinkedProfiles = linkedProfiles = entityManager.createQuery(
                    "select ip from IntegrationProfile ip join ip.actorIntegrationProfiles aip where aip.actor.id = "
                            + selectedActor.getId() + " order by ip.keyword").getResultList();
        }
        return "/tf/actorIntegrationProfile/linkIntegrationProfilesForActor.seam";

    }

    /**
     * Save changes to actors linked to this integration profile
     */
    @Override
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public String saveActorProfiles(Actor selectedActor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveActorProfiles");
        }
        selectedActor = entityManager.find(Actor.class, selectedActor.getId());
        // ** find changes between originalLinkedProfiles and linkedProfiles ** //
        List<IntegrationProfile> profilesToLink = new ArrayList<IntegrationProfile>();
        for (IntegrationProfile ip : linkedProfiles) {
            if (!originalLinkedProfiles.contains(ip)) {
                profilesToLink.add(ip);
            }
        }
        List<IntegrationProfile> profilesToUnlink = new ArrayList<IntegrationProfile>();
        for (IntegrationProfile ip : originalLinkedProfiles) {
            if (!linkedProfiles.contains(ip)) {
                profilesToUnlink.add(ip);
            }
        }
        // ** check that actors can be unlinked without invalidating AIPO or ProfileLink entries ** //
        linkIntegrationProfileActorDependentAIPOs = new ArrayList<ActorIntegrationProfileOption>();
        for (IntegrationProfile ip : profilesToUnlink) {
            List<ActorIntegrationProfileOption> aipos = entityManager.createQuery(
                    "select aipo from ActorIntegrationProfileOption aipo where aipo.actorIntegrationProfile.actor.id = "
                            + selectedActor.getId() + " and aipo.actorIntegrationProfile.integrationProfile.id = "
                            + ip.getId()).getResultList();
            linkIntegrationProfileActorDependentAIPOs.addAll(aipos);
        }
        linkIntegrationProfileActorDependentPLs = new ArrayList<ProfileLink>();
        for (IntegrationProfile ip : profilesToUnlink) {
            List<ProfileLink> pls = entityManager.createQuery(
                    "select pl from ProfileLink pl where pl.actorIntegrationProfile.actor.id = "
                            + selectedActor.getId() + " and pl.actorIntegrationProfile.integrationProfile.id = "
                            + ip.getId()).getResultList();
            linkIntegrationProfileActorDependentPLs.addAll(pls);
        }

        if ((linkIntegrationProfileActorDependentAIPOs.size() > 0)
                || (linkIntegrationProfileActorDependentPLs.size() > 0)) {
            displayLinkedActorsErrorModelPanel = true;
            return "/tf/actorIntegrationProfile/linkIntegrationProfilesForActorSideEffects.seam";
        } else {
            // ** create new AIPs ** //
            for (IntegrationProfile ip : profilesToLink) {
                ActorIntegrationProfile aip = new ActorIntegrationProfile();
                aip.setIntegrationProfile(ip);
                aip.setActor(selectedActor);
                entityManager.persist(aip);
            }
            // ** remove unlinked AIPs ** //
            entityManager.flush();
            for (IntegrationProfile ip : profilesToUnlink) {
                List<ActorIntegrationProfile> aipUnlinkList = entityManager.createQuery(
                        "select aip from ActorIntegrationProfile aip where aip.actor.id = " + selectedActor.getId()
                                + " and aip.integrationProfile.id = " + ip.getId()).getResultList();
                for (ActorIntegrationProfile aip : aipUnlinkList) {
                    entityManager.remove(aip);
                }
            }
            linkIntegrationProfileActorDependentAIPOs = null;
            linkIntegrationProfileActorDependentPLs = null;
            entityManager.flush();

            return editActor(selectedActor, edit);
        }

    }

    @Override
    public String deleteAllDependentAIPOsAndPLs(Actor selectedActor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteAllDependentAIPOsAndPLs");
        }
        if (this.linkIntegrationProfileActorDependentAIPOs != null) {
            AipoManagerLocal aipom = (AipoManagerLocal) Component.getInstance("aipoManager");
            for (ActorIntegrationProfileOption aipo : this.linkIntegrationProfileActorDependentAIPOs) {
                aipom.delaipo(null, aipo);
            }
        }

        if (this.linkIntegrationProfileActorDependentPLs != null) {
            ProfileLinksManagerLocal pll = (ProfileLinksManagerLocal) Component.getInstance("profileLinksManager");
            for (ProfileLink pl : this.linkIntegrationProfileActorDependentPLs) {
                pll.delProfileLink(null, pl);
            }
        }

        return saveActorProfiles(selectedActor);
    }

    /**
     * Get actor from keyword got in URL Change selected actor with actor returned Load the viewing actor Method used by redirection link
     */
    @Override
    public void getSpecificActorFromKeyword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSpecificActorFromKeyword");
        }
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String actorkeyword = params.get("keyword");
        if (actorkeyword.isEmpty()) {
            LOG.error("no keyword was specified");
        }

        selectedActor = Actor.findActorWithKeyword(actorkeyword);
        this.editActor(selectedActor, false);
    }

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }
    }

    @Override
    public int countAipoWithoutDocSectionLinkedToThisActor(Actor selectedActor) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("countAipoWithoutDocSectionLinkedToThisActor");
        }
        Actor actor = entityManager.find(Actor.class, selectedActor.getId());
        ActorIntegrationProfileOptionQuery query = new ActorIntegrationProfileOptionQuery();
        query.actorIntegrationProfile().actor().id().eq(actor.getId());
        query.documentSection().isEmpty();
        query.integrationProfileOption().keyword().neq("NONE");
        return query.getListDistinct().size();
    }


    public boolean isNameAvailable() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isNameAvailable");
        }

        ActorQuery query = new ActorQuery();
        if (selectedActor.getId() != null) {
            query.id().neq(selectedActor.getId());
        }
        query.name().eq(selectedActor.getName());

        if (query.getCount() == 0) {

            return true;

        } else {
            StatusMessages.instance().addToControlFromResourceBundleOrDefault("name", StatusMessage.Severity.WARN,
                    "An Actor with this name already exists",
                    "An Actor with this name already exists");
            return false;
        }
    }

    public boolean isKeywordAvailable() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isKeywordAvailable");
        }

        boolean res = false;

        ActorQuery query = new ActorQuery();
        if (selectedActor.getId() != null) {
            query.id().neq(selectedActor.getId());
        }
        query.keyword().eq(selectedActor.getKeyword());

        if (query.getCount() == 0) {
            res = true;
        } else {
            StatusMessages.instance().addToControlFromResourceBundleOrDefault("keyword", StatusMessage.Severity.WARN,
                    "An Actor with this keyword already exists",
                    "An Actor with this keyword already exists");
            res = false;
        }

        if (selectedActor.getKeyword().length() < 32) {
            res = true;
        } else {
            StatusMessages.instance().addToControlFromResourceBundleOrDefault("keyword", StatusMessage.Severity.WARN,
                    "This keyword is too long (>32)",
                    "This keyword is too long (>32)");
            res = false;
        }
        return res;
    }
}
