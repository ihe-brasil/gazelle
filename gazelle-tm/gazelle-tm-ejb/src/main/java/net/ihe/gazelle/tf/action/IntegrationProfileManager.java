// $codepro.audit.disable logExceptions
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

import net.ihe.gazelle.common.application.action.ApplicationPreferenceManager;
import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.common.log.ExceptionLogging;
import net.ihe.gazelle.documents.DocumentSectionsFilter;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.tf.model.*;
import net.ihe.gazelle.users.model.User;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.kohsuke.graphviz.Edge;
import org.kohsuke.graphviz.Graph;
import org.kohsuke.graphviz.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.*;


@Name("integrationProfileManager")
@Scope(ScopeType.SESSION)
@GenerateInterface("IntegrationProfileManagerLocal")
public class IntegrationProfileManager implements Serializable, IntegrationProfileManagerLocal, QueryModifier<IntegrationProfile> {

    private static final String FOUND = "Found ";

    private static final String TF_INTEGRATION_PROFILE_LIST_INTEGRATION_PROFILES_SEAM = "/tf/integrationProfile/listIntegrationProfiles.seam";

    private static final long serialVersionUID = -1357012043456235656L;

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationProfileManager.class);
    private final List<ActorIntegrationProfile> actorIntegrationProfileList = null;
    @In
    private EntityManager entityManager;
    private IntegrationProfile selectedIntegrationProfile;
    private List<IntegrationProfile> listOfIntegrationProfiles;
    private List<Actor> linkedActors = new ArrayList<Actor>();
    private List<Actor> originalLinkedActors = new ArrayList<Actor>();
    private boolean displayLinkedActorsErrorModelPanel = false;
    private Domain selectedDomain;
    private List<ActorIntegrationProfileOption> linkActorIntegrationProfileDependentAIPOs;
    private List<ProfileLink> linkActorIntegrationProfileDependentPLs;
    private List<Domain> listOfDomains;
    private List<Actor> listOfActors;
    private List<IntegrationProfileOption> listOfIntegrationProfileOptions;
    private List<Transaction> listOfTransactions;
    private int scrollerPage;
    private Boolean edit = false;
    private List<Domain> domainsTarget = new ArrayList<Domain>();
    private boolean viewing;
    private String showDomains = "true";
    private String selectedTab = "actorsTab";
    private ArrayList<Actor> actorForintegrationProfiles;
    private List<ProfileLink> actorsAndTransactionsForIntegrationProfile;
    private List<ActorIntegrationProfileOption> actorsIntegrationProfileOptionsForIntegrationProfile;
    private List<ActorIntegrationProfile> integrationActorProfileList;
    private DocumentSectionsFilter filter;
    private Filter<Actor> actorFilter;
    private Filter<ProfileLink> profileLinksForIntegrationProfileFilter;
    private Filter<ActorIntegrationProfileOption> aipoListFilter;
    private FilterDataModel<DocumentSection> documentSections;
    private Filter<IntegrationProfile> filterForIP;
    private String callerPage;

    public static Graph generateGraphForGivenListOfTransactionLinks(List<TransactionLink> tlList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("generateGraphForGivenListOfTransactionLinks");
        }

        Collections.sort(tlList);

        HashMap<String, Node> nodesAdded = new HashMap<String, Node>();
        HashMap<String, String> actorPair = new HashMap<String, String>();

        Graph g = new Graph();
        for (int i = 0; i < tlList.size(); i++) {
            StringBuilder transactionKeywords = new StringBuilder();
            transactionKeywords.append(tlList.get(i).getTransaction().getKeyword());

            for (int j = 0; j < tlList.size(); j++) {
                if ((tlList.get(j).getFromActor().getKeyword().equals(tlList.get(i).getFromActor().getKeyword())) && (
                        tlList.get(j).getToActor().getKeyword().equals(tlList.get(i).getToActor().getKeyword())) && (!
                        tlList.get(j).getTransaction().getKeyword().equals(tlList.get(i).getTransaction().getKeyword()))
                        && !actorPair.containsKey(
                        tlList.get(i).getFromActor().getKeyword() + "-" + tlList.get(i).getToActor().getKeyword())) {
                    transactionKeywords.append("\n").append(tlList.get(j).getTransaction().getKeyword());
                }
            }

            if (!actorPair.containsKey(
                    tlList.get(i).getFromActor().getKeyword() + "-" + tlList.get(i).getToActor().getKeyword())) {
                actorPair.put(tlList.get(i).getFromActor().getKeyword() + "-" + tlList.get(i).getToActor().getKeyword(),
                        transactionKeywords.toString());
            }

            if (actorPair.containsKey(
                    tlList.get(i).getFromActor().getKeyword() + "-" + tlList.get(i).getToActor().getKeyword()) && (
                    actorPair.get(tlList.get(i).getFromActor().getKeyword() + "-" + tlList.get(i).getToActor()
                            .getKeyword()).equals(transactionKeywords.toString()))) {
                Node src, dst;

                if (!nodesAdded.containsKey(tlList.get(i).getFromActor().getKeyword())) {
                    src = new Node();

                    src.attr("shape", "box");
                    src.attr("style", "filled");
                    src.attr("fillcolor", "#9FBDEB");
                    src.attr("pencolor", "black");
                    src.attr("URL", ApplicationPreferenceManager.instance().getApplicationUrl() + "actor.seam?keyword="
                            + tlList.get(i).getFromActor().getKeyword());
                    src.attr("label", (tlList.get(i).getFromActor().getName().replace(" ", "\n")));
                    src.attr("tooltip", tlList.get(i).getFromActor().getName());

                    nodesAdded.put(tlList.get(i).getFromActor().getKeyword(), src);
                    g.node(src);
                } else {
                    src = nodesAdded.get(tlList.get(i).getFromActor().getKeyword());
                }

                if (!nodesAdded.containsKey(tlList.get(i).getToActor().getKeyword())) {
                    dst = new Node();

                    dst.attr("shape", "box");
                    dst.attr("style", "filled");
                    dst.attr("fillcolor", "#9FBDEB");
                    dst.attr("pencolor", "black");
                    dst.attr("URL", ApplicationPreferenceManager.instance().getApplicationUrl() + "actor.seam?keyword="
                            + tlList.get(i).getToActor().getKeyword());
                    dst.attr("label", (tlList.get(i).getToActor().getName().replace(" ", "\n")));
                    dst.attr("tooltip", tlList.get(i).getToActor().getName());

                    nodesAdded.put(tlList.get(i).getToActor().getKeyword(), dst);
                    g.node(dst);
                } else {
                    dst = nodesAdded.get(tlList.get(i).getToActor().getKeyword());
                }

                Edge e = new Edge(src, dst);
                e.attr("label", transactionKeywords.toString());
                e.attr("weight", "1");

                g.edge(e);
            }
        }

        return g;
    }

    public String getSelectedTab() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedTab");
        }
        return selectedTab;
    }

    public void setSelectedTab(String selectedTab) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedTab");
        }
        this.selectedTab = selectedTab;
    }

    @Override
    public List<Actor> getOriginalLinkedActors() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getOriginalLinkedActors");
        }
        return originalLinkedActors;
    }

    @Override
    public void setOriginalLinkedActors(List<Actor> originalLinkedActors) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setOriginalLinkedActors");
        }
        this.originalLinkedActors = originalLinkedActors;
    }

    @Override
    public String getShowDomains() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getShowDomains");
        }
        return showDomains;
    }

    @Override
    public void setShowDomains(String s) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setShowDomains");
        }
        showDomains = s;
    }

    @Override
    public String renderLinkDomains() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("renderLinkDomains");
        }
        return showDomains;
    }

    @Override
    public String renderDelete() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("renderDelete");
        }
        return showDomains;
    }

    public List<Domain> getDomainsTarget() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDomainsTarget");
        }
        return domainsTarget;
    }

    public void setDomainsTarget(List<Domain> domainsTarget) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDomainsTarget");
        }
        this.domainsTarget = domainsTarget;
    }

    public List<Domain> getDomainsSource() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDomainsSource");
        }
        List<Domain> domainsSource = Domain.getPossibleDomains();
        Collections.sort(domainsSource);
        return domainsSource;
    }

    /**
     * Field filter.
     */
    @Override
    public Boolean getEdit() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getEdit");
        }
        return edit;
    }

    @Override
    public Boolean getView() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getView");
        }
        return !edit;
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
        scrollerPage = 0;
        this.selectedDomain = selectedDomain;
    }

    @Override
    public FilterDataModel<Actor> getActorsForProfile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorsForProfile");
        }
        return new FilterDataModel<Actor>(getActorFilter()) {
            @Override
            protected Object getId(Actor actor) {
                return actor.getId();
            }
        };
    }

    private Filter<Actor> getActorFilter() {
        if (actorFilter == null) {
            ActorQuery query = new ActorQuery();
            HQLCriterionsForFilter<Actor> result = query.getHQLCriterionsForFilter();
            result.addQueryModifier(new QueryModifier<Actor>() {
                @Override
                public void modifyQuery(HQLQueryBuilder<Actor> hqlQueryBuilder, Map<String, Object> map) {
                    if (selectedIntegrationProfile != null) {
                        ActorQuery query = new ActorQuery();
                        hqlQueryBuilder.addRestriction(query.actorIntegrationProfiles().integrationProfile().id().eqRestriction
                                (selectedIntegrationProfile.getId()));
                    }
                }
            });
            this.actorFilter = new Filter<Actor>(result);
        }
        return this.actorFilter;

    }

    @Override
    public FilterDataModel<ProfileLink> getProfileLinksForIntegrationProfile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getProfileLinksForIntegrationProfile");
        }
        return new FilterDataModel<ProfileLink>(getProfileLinkFilter()) {
            @Override
            protected Object getId(ProfileLink profileLink) {
                return profileLink.getId();
            }
        };
    }

    private Filter<ProfileLink> getProfileLinkFilter() {
        if (profileLinksForIntegrationProfileFilter == null) {
            ProfileLinkQuery query = new ProfileLinkQuery();
            HQLCriterionsForFilter<ProfileLink> result = query.getHQLCriterionsForFilter();
            result.addQueryModifier(new QueryModifier<ProfileLink>() {
                @Override
                public void modifyQuery(HQLQueryBuilder<ProfileLink> hqlQueryBuilder, Map<String, Object> map) {
                    if (selectedIntegrationProfile != null) {
                        ProfileLinkQuery query = new ProfileLinkQuery();
                        hqlQueryBuilder.addRestriction(query.actorIntegrationProfile().integrationProfile().id().eqRestriction
                                (selectedIntegrationProfile.getId()));
                    }
                }
            });
            this.profileLinksForIntegrationProfileFilter = new Filter<ProfileLink>(result);
        }
        return this.profileLinksForIntegrationProfileFilter;
    }

    @Override
    public FilterDataModel<ActorIntegrationProfileOption> getAipoList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAipoList");
        }
        return new FilterDataModel<ActorIntegrationProfileOption>(getAipoListFilter()) {
            @Override
            protected Object getId(ActorIntegrationProfileOption actorIntegrationProfileOption) {
                return actorIntegrationProfileOption.getId();
            }
        };
    }

    private Filter<ActorIntegrationProfileOption> getAipoListFilter() {
        if (aipoListFilter == null) {
            ActorIntegrationProfileOptionQuery query = new ActorIntegrationProfileOptionQuery();
            HQLCriterionsForFilter<ActorIntegrationProfileOption> result = query.getHQLCriterionsForFilter();
            result.addQueryModifier(new QueryModifier<ActorIntegrationProfileOption>() {
                @Override
                public void modifyQuery(HQLQueryBuilder<ActorIntegrationProfileOption> hqlQueryBuilder, Map<String, Object> map) {
                    if (selectedIntegrationProfile != null) {
                        ActorIntegrationProfileOptionQuery query = new ActorIntegrationProfileOptionQuery();
                        hqlQueryBuilder.addRestriction(query.actorIntegrationProfile().integrationProfile().id().eqRestriction
                                (selectedIntegrationProfile.getId()));
                    }
                }
            });
            this.aipoListFilter = new Filter<ActorIntegrationProfileOption>(result);
        }
        return this.aipoListFilter;
    }

    public Filter<IntegrationProfile> getFilterForIP() {
        if (filterForIP == null) {
            IntegrationProfileQuery q = new IntegrationProfileQuery();
            HQLCriterionsForFilter<IntegrationProfile> criterionsForFilter = q.getHQLCriterionsForFilter();
            criterionsForFilter.addQueryModifier(this);
            filterForIP = new Filter<IntegrationProfile>(criterionsForFilter);
        }
        return filterForIP;
    }

    @Override
    public FilterDataModel<IntegrationProfile> listOfIntegrationProfilesDataModel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listOfIntegrationProfilesDataModel");
        }
        return new FilterDataModel<IntegrationProfile>(getFilterForIP()) {
            @Override
            protected Object getId(IntegrationProfile integrationProfile) {
                return integrationProfile.getId();
            }
        };
    }

    @Override
    public List<IntegrationProfile> getListOfIntegrationProfiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfIntegrationProfiles");
        }
        return listOfIntegrationProfiles;
    }

    @Override
    public void setListOfIntegrationProfiles(List<IntegrationProfile> listOfIntegrationProfiles) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setListOfIntegrationProfiles");
        }
        this.listOfIntegrationProfiles = listOfIntegrationProfiles;
    }

    @Override
    public List<Domain> getListOfDomains() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfDomains");
        }
        return listOfDomains;
    }

    @Override
    public List<Actor> getListOfActors() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfActors");
        }
        return listOfActors;
    }

    @Override
    public List<IntegrationProfileOption> getListOfIntegrationProfileOptions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfIntegrationProfileOptions");
        }
        return listOfIntegrationProfileOptions;
    }

    @Override
    public List<Transaction> getListOfTransactions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListOfTransactions");
        }
        return listOfTransactions;
    }

    @Override
    public void findIntegrationProfiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findIntegrationProfiles");
        }

        listOfIntegrationProfiles = IntegrationProfile.listAllIntegrationProfiles();
    }

    @Override
    public void findIntegrationProfilesByDomain() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findIntegrationProfilesByDomain");
        }
        if (this.selectedDomain == null) {
            listOfIntegrationProfiles = IntegrationProfile.listAllIntegrationProfiles();
        } else {
            listOfIntegrationProfiles = IntegrationProfile.getPossibleIntegrationProfiles(selectedDomain);
        }
    }

    @Override
    public String listIntegrationProfiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("listIntegrationProfiles");
        }

        return TF_INTEGRATION_PROFILE_LIST_INTEGRATION_PROFILES_SEAM;
    }

    @Override
    public List<ActorIntegrationProfileOption> getLinkActorIntegrationProfileDependentAIPOs() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLinkActorIntegrationProfileDependentAIPOs");
        }
        return linkActorIntegrationProfileDependentAIPOs;
    }

    @Override
    public List<ProfileLink> getLinkActorIntegrationProfileDependentPLs() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLinkActorIntegrationProfileDependentPLs");
        }
        return linkActorIntegrationProfileDependentPLs;
    }

    @Override
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public void saveIntegrationProfile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveIntegrationProfile");
        }

        if (!isKeywordAvailable()) {
            return;
        }
        if (!isNameAvailable()) {
            return;
        }

        if ((selectedIntegrationProfile != null) && (selectedIntegrationProfile.getIntegrationProfileStatusType()
                == null)) {
            FacesMessages.instance().addFromResourceBundle(StatusMessage.Severity.ERROR, "gazelle.tf.setIntegrationprofileStatus");
            return;
        }

        selectedIntegrationProfile = entityManager.merge(selectedIntegrationProfile);

        FacesMessages.instance().add(StatusMessage.Severity.INFO, "IntegrationProfile #{integrationProfile.name} updated");
    }

    @Override
    public String addNewIntegrationProfileButton() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNewIntegrationProfileButton");
        }
        edit = true;

        selectedIntegrationProfile = new IntegrationProfile();
        selectedIntegrationProfile.setIntegrationProfileTypes(new ArrayList<IntegrationProfileType>());
        selectedIntegrationProfile.setDomainsForDP(new ArrayList<Domain>());
        selectedIntegrationProfile
                .setListOfActorIntegrationProfileOption(new ArrayList<ActorIntegrationProfileOption>());

        return "/tf/integrationProfile/editIntegrationProfile.seam";
    }

    @Override
    public String showIntegrationProfileDeleteSideEffects(IntegrationProfile inSelectedProfile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showIntegrationProfileDeleteSideEffects");
        }
        try {

            Query q = entityManager.createQuery(
                    "SELECT profile from IntegrationProfile profile fetch all properties where profile = :inProfile");
            q.setParameter("inProfile", inSelectedProfile);
            selectedIntegrationProfile = (IntegrationProfile) q.getSingleResult();

            HashSet<Domain> setOfDomains = new HashSet<Domain>(selectedIntegrationProfile.getDomainsForDP());
            listOfDomains = new ArrayList<Domain>(setOfDomains);
            setOfDomains = null;

            // Associated actors

            HashSet<Actor> setOfActors = new HashSet<Actor>(selectedIntegrationProfile.getActors());
            listOfActors = new ArrayList<Actor>(setOfActors);
            setOfActors = null;

            // Associated Integration Profile Options
            listOfIntegrationProfileOptions = ActorIntegrationProfileOption
                    .getIntegrationProfileOptionFromAIPOFiltered(null, selectedIntegrationProfile, null, null);

            List<ProfileLink> associatedTransactionsWithToDeleteIP = ProfileLink
                    .FindProfileLinksForIntegrationProfile(selectedIntegrationProfile);
            listOfTransactions = new ArrayList<Transaction>();
            if (associatedTransactionsWithToDeleteIP != null) {
                for (ProfileLink pl : associatedTransactionsWithToDeleteIP) {
                    Transaction transaction = pl.getTransaction();
                    listOfTransactions.add(transaction);
                }
            }
        } catch (Exception e) {
            ExceptionLogging.logException(e, LOG);
            StackTraceElement[] output = e.getStackTrace();
            for (int i = 0; i < output.length; i++) {

            }
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Error : " + e.getMessage());
        }

        return "/tf/integrationProfile/integrationProfileDeleteSideEffects.seam";
    }

    @Override
    public void LoadForActorIntegrationProfiles(IntegrationProfile ip) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("LoadForActorIntegrationProfiles");
        }
        String queryString = "SELECT ip FROM ActorIntegrationProfile ip WHERE ip.integrationProfile.id = " + ip.getId();
        Query query = entityManager.createQuery(queryString);
        integrationActorProfileList = query.getResultList();
        this.actorForintegrationProfiles = new ArrayList<Actor>();
        Iterator<ActorIntegrationProfile> it = integrationActorProfileList.iterator();
        while (it.hasNext()) {
            ActorIntegrationProfile aip = it.next();
            Actor a = aip.getActor();
            actorForintegrationProfiles.add(a);
        }

    }

    @Override
    public void LoadActorsAndTransactionsForIntegrationProfile(IntegrationProfile ip) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("LoadActorsAndTransactionsForIntegrationProfile");
        }
        String queryString =
                "SELECT pl FROM ProfileLink pl WHERE pl.actorIntegrationProfile.integrationProfile.id = " + ip.getId();
        Query query = entityManager.createQuery(queryString);
        this.actorsAndTransactionsForIntegrationProfile = query.getResultList();
    }

    @Override
    public void LoadActorsIntegrationProfileOptionsForIntegrationProfile(IntegrationProfile ip) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("LoadActorsIntegrationProfileOptionsForIntegrationProfile");
        }
        String queryString =
                "SELECT aipo FROM ActorIntegrationProfileOption aipo WHERE aipo.actorIntegrationProfile.integrationProfile.id = "
                        + ip.getId();
        Query query = entityManager.createQuery(queryString);
        this.actorsIntegrationProfileOptionsForIntegrationProfile = query.getResultList();
    }

    @Override
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public String duplicateProfile(IntegrationProfile profilToDuplicate) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("duplicateProfile");
        }
        profilToDuplicate = entityManager.find(IntegrationProfile.class, profilToDuplicate.getId());

        // Creation of a new Integration profile
        IntegrationProfile ip = IntegrationProfile.duplicateIntegrationProfile(profilToDuplicate);
        ip.setLastChanged(new Date());
        ip.setLastModifierId(User.loggedInUser().getUsername());
        ip.setIntegrationProfileStatusType(profilToDuplicate.getIntegrationProfileStatusType());

        createIntegrationProfile(ip);

        // Link actor and profile
        LoadForActorIntegrationProfiles(profilToDuplicate);
        setOriginalLinkedActors(new ArrayList<Actor>());
        setLinkedActors(actorForintegrationProfiles);
        saveProfileActors(ip);

        // Create link Actor - Transactions
        LoadActorsAndTransactionsForIntegrationProfile(profilToDuplicate);
        for (ProfileLink pl : actorsAndTransactionsForIntegrationProfile) {
            List<Actor> la2 = new ArrayList<Actor>();
            la2.add(pl.getActorIntegrationProfile().getActor());
            List<Transaction> lt = new ArrayList<Transaction>();
            lt.add(pl.getTransaction());

            List<IntegrationProfile> lip = new ArrayList<IntegrationProfile>();
            lip.add(ip);
            ProfileLinksManager plm = new ProfileLinksManager();
            plm.createLinks(la2, lip, lt, pl.getTransactionOptionType());
        }

        // Create link Actors - Options
        LoadActorsIntegrationProfileOptionsForIntegrationProfile(profilToDuplicate);
        for (ActorIntegrationProfileOption aipo : actorsIntegrationProfileOptionsForIntegrationProfile) {
            List<IntegrationProfileOption> lipo = new ArrayList<IntegrationProfileOption>();
            IntegrationProfileOption ipo = aipo.getIntegrationProfileOption();
            lipo.add(ipo);
            List<IntegrationProfile> lip2 = new ArrayList<IntegrationProfile>();
            lip2.add(ip);
            List<Actor> asa = new ArrayList<Actor>();
            asa.add(aipo.getActorIntegrationProfile().getActor());
            AipoManager am = new AipoManager();
            am.createLinks(asa, lip2, lipo);
        }

        // Create link Profile - domains
        setSelectedIntegrationProfile(ip);
        linkProfileDomains(profilToDuplicate.getDomainsForDP());
        saveProfileDomains();

        FacesMessages.instance().clearGlobalMessages();
        FacesMessages.instance().add(StatusMessage.Severity.INFO, ip.getName() + " is copied");

        return "/tf/integrationProfile/listIntegrationProfiles.seam";
    }

    @Override
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public String deleteIntegrationProfile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteIntegrationProfile");
        }

        if (selectedIntegrationProfile != null) {
            try {
                Query q = entityManager.createQuery(
                        "SELECT profile FROM IntegrationProfile profile fetch all properties where profile = :inProfile");
                q.setParameter("inProfile", selectedIntegrationProfile);

                selectedIntegrationProfile = (IntegrationProfile) q.getSingleResult();

                // ProfileLinks: the links to actors/transactions
                List<ProfileLink> profileLinksToDelete = ProfileLink
                        .FindProfileLinksForIntegrationProfile(selectedIntegrationProfile);
                if (profileLinksToDelete != null) {
                    for (ProfileLink pl : profileLinksToDelete) {
                        entityManager.remove(pl);
                        // Domains
                    }
                }
                selectedIntegrationProfile.setDomainsForDP(null);

                // AIPO
                List<ActorIntegrationProfileOption> actorIntegrationProfileOptionsToDelete = ActorIntegrationProfileOption
                        .getActorIntegrationProfileOptionFiltered(null, selectedIntegrationProfile, null, null);
                if (actorIntegrationProfileOptionsToDelete != null) {
                    for (ActorIntegrationProfileOption aipo : actorIntegrationProfileOptionsToDelete) {
                        entityManager.remove(aipo);
                    }
                }
                if (selectedIntegrationProfile.getActorIntegrationProfiles() != null) {
                    for (ActorIntegrationProfile aip : selectedIntegrationProfile.getActorIntegrationProfiles()) {
                        entityManager.remove(aip);
                    }
                }
                entityManager.remove(selectedIntegrationProfile);
                entityManager.flush();
                StatusMessages.instance().add("Integration Profile (" + selectedIntegrationProfile.getKeyword() + " - "
                        + selectedIntegrationProfile.getName() + ") has been successfully deleted");

                return TF_INTEGRATION_PROFILE_LIST_INTEGRATION_PROFILES_SEAM;
            } catch (Exception e) {
                StatusMessages.instance().add("Error deleting integration profile : " + e.getMessage());
                return TF_INTEGRATION_PROFILE_LIST_INTEGRATION_PROFILES_SEAM;
            }
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Error deleting profile : profile selected is null ");
            return TF_INTEGRATION_PROFILE_LIST_INTEGRATION_PROFILES_SEAM;
        }
    }

    @Override
    public String viewIntegrationProfile(IntegrationProfile selectedIntegrationProfile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewIntegrationProfile");
        }
        return editIntegrationProfile(selectedIntegrationProfile, false);
    }

    @Override
    public String editIntegrationProfile(IntegrationProfile selectedIntegrationProfile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editIntegrationProfile");
        }
        return editIntegrationProfile(selectedIntegrationProfile, true);
    }

    @Override
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public void createIntegrationProfile(IntegrationProfile c) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createIntegrationProfile");
        }

        entityManager.persist(c);

        entityManager.flush();
    }

    @Override
    public String showIntegrationProfile(IntegrationProfile inSelectedIntegrationProfile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("showIntegrationProfile");
        }
        return editIntegrationProfile(inSelectedIntegrationProfile, false);
    }

    @Override
    public String editIntegrationProfile(IntegrationProfile inSelectedIntegrationProfile, boolean e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("editIntegrationProfile");
        }
        this.edit = e;
        selectedIntegrationProfile = entityManager.find(IntegrationProfile.class, inSelectedIntegrationProfile.getId());

        if (edit) {

            return "/tf/integrationProfile/editIntegrationProfile.seam";
        } else {

            return "/tf/integrationProfile/showIntegrationProfile.seam";
        }

    }

    /**
     * Search all the IntegrationProfiles corresponding to the beginning of a String (filled in the integrationProfile list box). Eg. If we search
     * the String 'C' we will find the integrationProfile :
     * sCheduled workflow, ... This operation is allowed for some granted users (check the security.drl)
     *
     * @param event : Key up event caught from the JSF
     * @return List of IntegrationProfile objects : List of all integrationProfiles to display into the list box
     */

    @Override
    public List<IntegrationProfile> integrationProfileAutoComplete(Object event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("integrationProfileAutoComplete");
        }
        List<IntegrationProfile> returnList = null;
        Session s = (Session) entityManager.getDelegate();
        Criteria c = s.createCriteria(IntegrationProfile.class);
        String searchedString = event.toString();

        if (searchedString.trim().equals("*")) {
            c.addOrder(Order.asc("name"));
            returnList = c.list();
            IntegrationProfile fakeAllIntegrationProfile = new IntegrationProfile();
            fakeAllIntegrationProfile.setName("*");
            returnList.add(0, fakeAllIntegrationProfile);
        } else {
            c.add(Restrictions.or(Restrictions.ilike("name", "%" + searchedString + "%"),
                    Restrictions.ilike("keyword", "%" + searchedString + "%")));
            c.addOrder(Order.asc("name"));
            returnList = c.list();
        }
        return returnList;
    }

    @Override
    public String linkProfileDomains(List<Domain> domains) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("linkProfileDomains");
        }
        domainsTarget = new LinkedList<Domain>(domains);
        Collections.sort(domainsTarget);

        return "/tf/profileLink/linkProfileDomains.seam";
    }

    @Override
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public String saveProfileDomains() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveProfileDomains");
        }

        selectedIntegrationProfile.setDomainsForDP(domainsTarget);
        entityManager.merge(selectedIntegrationProfile);
        entityManager.flush();

        if (viewing) {
            return "/tf/integrationProfile/showIntegrationProfile.seam";
        }
        return "/tf/integrationProfile/editIntegrationProfile.seam";
    }

    @Override
    public String cancelProfileDomains() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelProfileDomains");
        }

        if (viewing) {
            return "/tf/integrationProfile/showIntegrationProfile.seam";
        }
        return "/tf/integrationProfile/editIntegrationProfile.seam";
    }

    /**
     * Action method for "link profile actors" button Sets up data for listShuttle to maintain integrationProfile - The profile currently being
     * displayed linkedActors - Actors currently linked to
     * selected Integration Profile unlinkedActors - Actors NOT currently linked to selected Integration Profile
     *
     * @return String of web page to display (listShuttle page)
     */
    @Override
    public String linkProfileActors() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("linkProfileActors");
        }

        if (selectedIntegrationProfile == null) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Selected integrationProfile is null");
            return null;
        }
        linkActorIntegrationProfileDependentAIPOs = null;
        linkActorIntegrationProfileDependentPLs = null;
        displayLinkedActorsErrorModelPanel = false;

        Query q = entityManager.createQuery(
                "SELECT profile FROM IntegrationProfile profile fetch all properties where profile = :inProfile");
        q.setParameter("inProfile", selectedIntegrationProfile);

        selectedIntegrationProfile = (IntegrationProfile) q.getSingleResult();

        originalLinkedActors = ActorIntegrationProfile.getActorByIntegrationProfile(selectedIntegrationProfile);

        if (originalLinkedActors == null) {
            originalLinkedActors = new ArrayList<Actor>();
        }
        linkedActors = originalLinkedActors;
        Collections.sort(linkedActors);

        return "/tf/actorIntegrationProfile/linkActorIntegrationProfile.seam";

    }

    /**
     * Save changes to actors linked to this integration profile
     */
    @Override
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public String saveProfileActors(IntegrationProfile ip) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveProfileActors");
        }
        selectedIntegrationProfile = entityManager.find(IntegrationProfile.class, ip.getId());
        return saveProfileActors();
    }

    @Override
    @Restrict("#{s:hasPermission('MasterModel', 'edit', null)}")
    public String saveProfileActors() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("saveProfileActors");
        }
        if (selectedIntegrationProfile == null) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Selected profile is null ");

            return null;
        }

        // ** find changes between originalLinkedActors and linkedActors ** //
        List<Actor> actorsToLink = new ArrayList<Actor>();
        for (Actor a : linkedActors) {
            if (!originalLinkedActors.contains(a)) {
                actorsToLink.add(a);
            }
        }
        List<Actor> actorsToUnlink = new ArrayList<Actor>();
        for (Actor a : originalLinkedActors) {
            if (!linkedActors.contains(a)) {
                actorsToUnlink.add(a);
            }
        }
        // ** check that actors can be unlinked without invalidating AIPO or ProfileLink entries ** //
        linkActorIntegrationProfileDependentAIPOs = new ArrayList<ActorIntegrationProfileOption>();
        for (Actor a : actorsToUnlink) {
            List<ActorIntegrationProfileOption> aipos = entityManager.createQuery(
                    "select aipo from ActorIntegrationProfileOption aipo where aipo.actorIntegrationProfile.actor.id = "
                            + a.getId() + " and aipo.actorIntegrationProfile.integrationProfile.id = "
                            + selectedIntegrationProfile.getId()).getResultList();
            linkActorIntegrationProfileDependentAIPOs.addAll(aipos);
        }

        linkActorIntegrationProfileDependentPLs = new ArrayList<ProfileLink>();
        for (Actor a : actorsToUnlink) {
            List<ProfileLink> pls = entityManager.createQuery(
                    "select pl from ProfileLink pl where pl.actorIntegrationProfile.actor.id = " + a.getId()
                            + " and pl.actorIntegrationProfile.integrationProfile.id = " + selectedIntegrationProfile
                            .getId()).getResultList();
            linkActorIntegrationProfileDependentPLs.addAll(pls);
        }

        if ((linkActorIntegrationProfileDependentAIPOs.size() > 0) || (linkActorIntegrationProfileDependentPLs.size()
                > 0)) {

            displayLinkedActorsErrorModelPanel = true;
            return "/tf/actorIntegrationProfile/linkActorIntegrationProfileSideEffects.seam";
        } else {

            // ** create new AIPs ** //
            for (Actor a : actorsToLink) {
                ActorIntegrationProfile aip = new ActorIntegrationProfile();
                aip.setActor(a);
                aip.setIntegrationProfile(selectedIntegrationProfile);
                aip = entityManager.merge(aip);

                ActorIntegrationProfileOption aipo = new ActorIntegrationProfileOption();
                aipo.setActorIntegrationProfile(aip);
                aipo.setIntegrationProfileOption(IntegrationProfileOption.getNoneOption());

                entityManager.persist(aipo);

            }
            // ** remove unlinked AIPs ** //
            entityManager.flush();
            for (Actor a : actorsToUnlink) {
                List<ActorIntegrationProfile> aipUnlinkList = entityManager.createQuery(
                        "select aip from ActorIntegrationProfile aip where aip.actor.id = " + a.getId()
                                + " and aip.integrationProfile.id = " + selectedIntegrationProfile.getId())
                        .getResultList();
                for (ActorIntegrationProfile aip : aipUnlinkList) {
                    entityManager.remove(aip);
                    ActorIntegrationProfileOption aipo = ActorIntegrationProfileOption
                            .getActorIntegrationProfileOption(aip.getActor(), aip.getIntegrationProfile(),
                                    IntegrationProfileOption.getNoneOption());
                    if (aipo != null) {
                        entityManager.remove(aipo);
                    }
                }
            }
            linkActorIntegrationProfileDependentAIPOs = null;
            linkActorIntegrationProfileDependentPLs = null;
            entityManager.flush();

            if (edit) {
                return editIntegrationProfile(selectedIntegrationProfile);
            } else {
                return viewIntegrationProfile(selectedIntegrationProfile);
            }
        }

    }

    @Override
    public String deleteAllDependentAIPOsAndPLs(IntegrationProfile inSelectedIntegrationProfile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteAllDependentAIPOsAndPLs");
        }
        if (this.linkActorIntegrationProfileDependentAIPOs != null) {
            AipoManagerLocal aipom = (AipoManagerLocal) Component.getInstance("aipoManager");
            for (ActorIntegrationProfileOption aipo : this.linkActorIntegrationProfileDependentAIPOs) {
                aipom.delaipo(null, aipo);
            }
        }

        if (this.linkActorIntegrationProfileDependentPLs != null) {
            ProfileLinksManagerLocal pll = (ProfileLinksManagerLocal) Component.getInstance("profileLinksManager");
            for (ProfileLink pl : this.linkActorIntegrationProfileDependentPLs) {
                pll.delProfileLink(null, pl);
            }
        }
        selectedIntegrationProfile = inSelectedIntegrationProfile;
        return saveProfileActors();
    }

    @Override
    public List<Actor> getLinkedActors() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getLinkedActors");
        }

        return linkedActors;
    }

    @Override
    public void setLinkedActors(List<Actor> a) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setLinkedActors");
        }

        linkedActors = a;
    }

    @Override
    public List<Actor> getUnlinkedActors() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUnlinkedActors");
        }
        List<Actor> unlinkedActors = Actor.listAllActors();
        return unlinkedActors;
    }

    @Override
    public boolean getDisplayLinkedActorsErrorPanel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDisplayLinkedActorsErrorPanel");
        }
        return displayLinkedActorsErrorModelPanel;
    }

    /**
     * Get IP from keyword got in URL Change selected IP with IP returned Load the viewing IP Method used by redirection link
     */
    @Override
    public void getSpecificIntegrationProfileFromKeyword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSpecificIntegrationProfileFromKeyword");
        }
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String ipkeyword = params.get("keyword");

        selectedIntegrationProfile = IntegrationProfile.findIntegrationProfileWithKeyword(ipkeyword);
        this.editIntegrationProfile(selectedIntegrationProfile, false);
    }

    @Override
    @Destroy

    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    @Override
    public String addSectionPage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addSectionPage");
        }

        DomainQuery domainQuery = new DomainQuery();
        domainQuery.integrationProfilesForDP().id().eq(selectedIntegrationProfile.getId());
        List<Domain> domain = domainQuery.getListDistinct();

        filter = null;
        documentSections = null;
        getFilter();

        filter.setDomainRestriction(domain);
        return "/tf/integrationProfile/addDocumentSection.xhtml";
    }

    @Override
    public String addSectionPage(IntegrationProfile ip, String callerPage) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addSectionPage");
        }

        selectedIntegrationProfile = ip;
        this.callerPage = callerPage;

        return addSectionPage();
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
        selectedIntegrationProfile.setDocumentSection(documentSection);
        try {
            entityManager.merge(selectedIntegrationProfile);
            entityManager.flush();
        } catch (Throwable e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Cannot link this document section");

        }
        return getReturnPage();
    }

    private String getReturnPage() {
        String pageToReturn = null;
        if (callerPage != null) {
            if (callerPage.equals("integrationProfileList")) {
                pageToReturn = TF_INTEGRATION_PROFILE_LIST_INTEGRATION_PROFILES_SEAM;
            } else if (callerPage.equals("consistencyCheck")) {
                pageToReturn = "/tf/utilities/tfConsistencyCheckList.seam";
            } else {
                pageToReturn = "/tf/integrationProfile/editIntegrationProfile.seam";
            }
        } else {
            pageToReturn = "/tf/integrationProfile/editIntegrationProfile.seam";
        }
        callerPage = null;
        return pageToReturn;
    }

    @Override
    public void deleteSection(IntegrationProfile ip) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteSection");
        }

        selectedIntegrationProfile = ip;
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

        selectedIntegrationProfile.setDocumentSection(null);
        try {
            entityManager.merge(selectedIntegrationProfile);
            entityManager.flush();
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Section removed");
        } catch (Throwable e) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "This section cannot be deleted");

        }
    }

    @Override
    public String cancelAddDocumentSection(IntegrationProfile profile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("cancelAddDocumentSection");
        }
        selectedIntegrationProfile = profile;
        return getReturnPage();
    }

    @Override
    public int getScrollerPage() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getScrollerPage");
        }
        return this.scrollerPage;
    }

    @Override
    public void setScrollerPage(int scrollerPage) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setScrollerPage");
        }
        this.scrollerPage = scrollerPage;
    }

    @Override
    public void modifyQuery(HQLQueryBuilder<IntegrationProfile> hqlQueryBuilder, Map<String, Object> map) {
        IntegrationProfileQuery query = new IntegrationProfileQuery();
        if (selectedDomain != null) {
            hqlQueryBuilder.addRestriction(query.domainsForDP().id().eqRestriction(selectedDomain.getId()));
        }
    }


    public boolean isNameAvailable() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isNameAvailable");
        }

        IntegrationProfileQuery query = new IntegrationProfileQuery();
        if (selectedIntegrationProfile.getId() != null) {
            query.id().neq(selectedIntegrationProfile.getId());
        }
        query.name().eq(selectedIntegrationProfile.getName());

        if (query.getCount() == 0) {

            return true;

        } else {
            StatusMessages.instance().addToControlFromResourceBundleOrDefault("name", StatusMessage.Severity.WARN,
                    "An Integration profile with this name already exists",
                    "An Integration profile with this name already exists");
            return false;
        }
    }

    public boolean isKeywordAvailable() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isKeywordAvailable");
        }

        IntegrationProfileQuery query = new IntegrationProfileQuery();
        if (selectedIntegrationProfile.getId() != null) {
            query.id().neq(selectedIntegrationProfile.getId());
        }
        query.keyword().eq(selectedIntegrationProfile.getKeyword());

        if (query.getCount() == 0) {

            return true;

        } else {
            StatusMessages.instance().addToControlFromResourceBundleOrDefault("keyword", StatusMessage.Severity.WARN,
                    "An Integration profile with this keyword already exists",
                    "An Integration profile with this keyword already exists");
            return false;
        }
    }
}
