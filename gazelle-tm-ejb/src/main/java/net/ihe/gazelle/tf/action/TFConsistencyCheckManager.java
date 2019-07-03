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

import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.common.filter.list.GazelleListDataModel;
import net.ihe.gazelle.common.interfacegenerator.GenerateInterface;
import net.ihe.gazelle.documents.BackgroundUrlChecker;
import net.ihe.gazelle.documents.DocumentFilter;
import net.ihe.gazelle.documents.DocumentURLStatusType;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.HQLRestriction;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.tf.model.*;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Name("tfConsistencyCheckManager")
@Scope(ScopeType.SESSION)
@GenerateInterface("TFConsistencyCheckManagerLocal")
public class TFConsistencyCheckManager implements Serializable, TFConsistencyCheckManagerLocal {

    /**
     * Serial ID version of this object
     */
    private static final long serialVersionUID = -1357012043456235100L;
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TFConsistencyCheckManager.class);
    @Out
    String currentDisplayPanelLabel;
    @In
    private EntityManager entityManager;
    private DocumentFilter docWithoutSectionsFilter;
    private FilterDataModel<Document> docWithoutSections;

    @Override
    public void refreshLists() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("refreshLists");
        }

        getDomainsNoProfiles();
        getIntegrationProfilesNoDomain();
        getIntegrationProfilesGreaterThanOneDomain();
        getIntegrationProfilesNoActors();
        getActorIntegrationProfilesNoTransactions();
        getActorIntegrationProfilesNullTransactions();
        getActorIntegrationProfilesNoAssignedOptions();
        getActorIntegrationProfilesAnyNoneOptions();
        getActorIntegrationProfilesWithoutNoneOptions();
        getActorsNoProfiles();
        getTransactionsNoAIP();
        getIpOptionsNoTfReference();
        getIpOptionsNoAIP();
        getDocWithbrokenURL();
        getDomainWithoutDocument();
        findDocumentsWithoutDocumentsSection();
        getTransactionWithoutDocumentsSection();
        getTransactionsWithoutReferencedStandard();
        getIntegrationProfileWithoutDocumentSection();
        getAipoWithoutDocumentSection();
    }

    @Override
    public void SetCurrentDisplayPanelLabel(String value) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("SetCurrentDisplayPanelLabel");
        }
        currentDisplayPanelLabel = value;
    }

    @Override
    public String GetCurrentDisplayPanelLabel() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("GetCurrentDisplayPanelLabel");
        }
        return currentDisplayPanelLabel;
    }

    @Override
    public GazelleListDataModel<Domain> getDomainsNoProfiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDomainsNoProfiles");
        }
        DomainQuery q = new DomainQuery();
        q.integrationProfilesForDP().isEmpty();
        List<Domain> res = q.getList();
        GazelleListDataModel<Domain> dm = new GazelleListDataModel<Domain>(res);
        return dm;
    }

    @Override
    public GazelleListDataModel<IntegrationProfile> getIntegrationProfilesNoDomain() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntegrationProfilesNoDomain");
        }
        IntegrationProfileQuery q = new IntegrationProfileQuery();
        q.domainsForDP().isEmpty();
        List<IntegrationProfile> res = q.getList();
        GazelleListDataModel<IntegrationProfile> dm = new GazelleListDataModel<IntegrationProfile>(res);
        return dm;
    }


    @Override
    public GazelleListDataModel<IntegrationProfile> getIntegrationProfilesGreaterThanOneDomain() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntegrationProfilesGreaterThanOneDomain");
        }
        Query q = entityManager.createQuery("SELECT p FROM IntegrationProfile p where p.domainsForDP.size > 1");
        List<IntegrationProfile> res = q.getResultList();
        GazelleListDataModel<IntegrationProfile> dm = new GazelleListDataModel<IntegrationProfile>(res);
        return dm;
    }

    @Override
    public GazelleListDataModel<IntegrationProfile> getIntegrationProfilesNoActors() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntegrationProfilesNoActors");
        }
        IntegrationProfileQuery q = new IntegrationProfileQuery();
        q.actorIntegrationProfiles().isEmpty();
        List<IntegrationProfile> res = q.getList();
        GazelleListDataModel<IntegrationProfile> dm = new GazelleListDataModel<IntegrationProfile>(res);
        return dm;
    }

    @Override
    public GazelleListDataModel<ActorIntegrationProfile> getActorIntegrationProfilesNoTransactions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorIntegrationProfilesNoTransactions");
        }
        ActorIntegrationProfileQuery q = new ActorIntegrationProfileQuery();
        q.profileLinks().isEmpty();
        List<ActorIntegrationProfile> res = q.getList();
        GazelleListDataModel<ActorIntegrationProfile> dm = new GazelleListDataModel<ActorIntegrationProfile>(res);
        return dm;
    }

    @Override
    public GazelleListDataModel<ActorIntegrationProfile> getActorIntegrationProfilesNullTransactions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorIntegrationProfilesNullTransactions");
        }
        ProfileLinkQuery q = new ProfileLinkQuery();
        q.transaction().keyword().eq("NULL");
        List<ActorIntegrationProfile> res = q.actorIntegrationProfile().getListDistinct();
        GazelleListDataModel<ActorIntegrationProfile> dm = new GazelleListDataModel<ActorIntegrationProfile>(res);
        return dm;
    }

    // AIPs with no entries in AIPO table
    @Override
    public GazelleListDataModel<ActorIntegrationProfile> getActorIntegrationProfilesNoAssignedOptions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorIntegrationProfilesNoAssignedOptions");
        }
        ActorIntegrationProfileQuery q = new ActorIntegrationProfileQuery();
        q.listOfActorIntegrationProfileOption().isEmpty();
        List<ActorIntegrationProfile> res = q.getListDistinct();
        GazelleListDataModel<ActorIntegrationProfile> dm = new GazelleListDataModel<ActorIntegrationProfile>(res);
        return dm;
    }

    // AIPs with any "None" entry in AIPO table
    @Override
    public GazelleListDataModel<ActorIntegrationProfile> getActorIntegrationProfilesAnyNoneOptions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorIntegrationProfilesAnyNoneOptions");
        }
        ActorIntegrationProfileOptionQuery q = new ActorIntegrationProfileOptionQuery();
        q.integrationProfileOption().keyword().eq("NONE");
        List<ActorIntegrationProfile> res = q.actorIntegrationProfile().getListDistinct();
        GazelleListDataModel<ActorIntegrationProfile> dm = new GazelleListDataModel<ActorIntegrationProfile>(res);
        return dm;
    }

    // AIPs without "None" entry in AIPO table
    @Override
    public GazelleListDataModel<ActorIntegrationProfile> getActorIntegrationProfilesWithoutNoneOptions() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorIntegrationProfilesWithoutNoneOptions");
        }
        ActorIntegrationProfileQuery q = new ActorIntegrationProfileQuery();
        List<ActorIntegrationProfile> actorIntegrationProfileList = q.getList();
        List<ActorIntegrationProfile> res = new ArrayList<>();
        for(ActorIntegrationProfile actorIntegrationProfile : actorIntegrationProfileList){
            boolean none = false;
            for(ActorIntegrationProfileOption actorIntegrationProfileOption : actorIntegrationProfile.getActorIntegrationProfileOption()){
                if(actorIntegrationProfileOption.getIntegrationProfileOption().getKeyword().equals("NONE")){
                    none = true;
                }
            }
            if(!none){
                if(actorIntegrationProfile.getActorIntegrationProfileOption().size() > 0) {
                    res.add(actorIntegrationProfile);
                }
            }
        }
        GazelleListDataModel<ActorIntegrationProfile> dm = new GazelleListDataModel<ActorIntegrationProfile>(res);
        return dm;
    }

    @Override
    public GazelleListDataModel<Actor> getActorsNoProfiles() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getActorsNoProfiles");
        }
        ActorQuery q = new ActorQuery();
        q.actorIntegrationProfiles().isEmpty();
        List<Actor> res = q.getListDistinct();
        GazelleListDataModel<Actor> dm = new GazelleListDataModel<Actor>(res);
        return dm;
    }

    @Override
    public GazelleListDataModel<Transaction> getTransactionsNoAIP() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTransactionsNoAIP");
        }
        TransactionQuery q = new TransactionQuery();
        q.profileLinks().isEmpty();
        List<Transaction> res = q.getListDistinct();
        GazelleListDataModel<Transaction> dm = new GazelleListDataModel<Transaction>(res);
        return dm;
    }

    public GazelleListDataModel<Transaction> getTransactionsWithoutReferencedStandard() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTransactionsWithoutReferencedStandard");
        }
        TransactionQuery q = new TransactionQuery();
        q.standards().isEmpty();
        q.transactionStatusType().keyword().neq("DEPRECATED");
        List<Transaction> res = q.getListDistinct();
        GazelleListDataModel<Transaction> dm = new GazelleListDataModel<Transaction>(res);
        return dm;
    }

    @Override
    public GazelleListDataModel<IntegrationProfileOption> getIpOptionsNoTfReference() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIpOptionsNoTfReference");
        }
        IntegrationProfileOptionQuery q = new IntegrationProfileOptionQuery();
        q.reference().eq("");
        List<IntegrationProfileOption> res = q.getListDistinct();
        GazelleListDataModel<IntegrationProfileOption> dm = new GazelleListDataModel<IntegrationProfileOption>(res);
        return dm;
    }

    @Override
    public GazelleListDataModel<IntegrationProfileOption> getIpOptionsNoAIP() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIpOptionsNoAIP");
        }
        IntegrationProfileOptionQuery q = new IntegrationProfileOptionQuery();
        q.listOfActorIntegrationProfileOption().isEmpty();
        List<IntegrationProfileOption> res = q.getListDistinct();
        GazelleListDataModel<IntegrationProfileOption> dm = new GazelleListDataModel<IntegrationProfileOption>(res);
        return dm;
    }

    @Override
    public GazelleListDataModel<Document> getDocWithbrokenURL() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDocWithbrokenURL");
        }
        DocumentQuery query = new DocumentQuery();
        query.linkStatusDescription().neq(DocumentURLStatusType.THIS_URL_IS_POINTING_TO_A_PDF.getFriendlyName());
        List<Document> res = query.getListDistinct();
        GazelleListDataModel<Document> dm = new GazelleListDataModel<Document>(res);
        return dm;
    }

    @Override
    public GazelleListDataModel<Domain> getDomainWithoutDocument() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDomainWithoutDocument");
        }
        DomainQuery query = new DomainQuery();
        query.documents().isEmpty();
        List<Domain> res = query.getListDistinct();
        GazelleListDataModel<Domain> dm = new GazelleListDataModel<Domain>(res);
        return dm;
    }

    @Override
    public void performCheckDocumentsURL() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("performCheckDocumentsURL");
        }
        BackgroundUrlChecker buc = new BackgroundUrlChecker();
        buc.setEntityManager(entityManager);
        buc.checkDocumentsUrlPointsToPDF();
        getDocWithbrokenURL();
    }

    @Override
    public void findDocumentsWithoutDocumentsSection() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findDocumentsWithoutDocumentsSection");
        }
        getDocWithoutSections();
        docWithoutSectionsFilter.modified();
    }

    @Override
    @SuppressWarnings("unchecked")
    public DocumentFilter getDocWithoutSectionsFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDocWithoutSectionsFilter");
        }
        if (docWithoutSectionsFilter == null) {
            final FacesContext fc = FacesContext.getCurrentInstance();
            final Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();

            QueryModifier<Document> queryWithoutSection = new QueryModifier<Document>() {
                /**
                 *
                 */
                private static final long serialVersionUID = 3449604199604412964L;

                @Override
                public void modifyQuery(HQLQueryBuilder<Document> arg0, Map<String, Object> filterValuesApplied) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("modifyQuery");
                    }
                    new DocumentQuery(arg0).documentSection().isEmpty();
                }
            };
            docWithoutSectionsFilter = new DocumentFilter(requestParameterMap, queryWithoutSection);
        }
        return docWithoutSectionsFilter;
    }

    @Override
    public FilterDataModel<Document> getDocWithoutSections() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getDocWithoutSections");
        }
        if (docWithoutSections == null) {
            docWithoutSections = new FilterDataModel<Document>(getDocWithoutSectionsFilter()) {
                @Override
                protected Object getId(Document t) {
                    return t.getId();
                }
            };
        }
        return docWithoutSections;
    }

    @Override
    public GazelleListDataModel<Transaction> getTransactionWithoutDocumentsSection() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getTransactionWithoutDocumentsSection");
        }
        TransactionQuery query = new TransactionQuery();
        query.documentSection().isEmpty();
        List<Transaction> res = query.getList();
        GazelleListDataModel<Transaction> dm = new GazelleListDataModel<Transaction>(res);
        return dm;
    }

    @Override
    public GazelleListDataModel<IntegrationProfile> getIntegrationProfileWithoutDocumentSection() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getIntegrationProfileWithoutDocumentSection");
        }
        IntegrationProfileQuery query = new IntegrationProfileQuery();
        query.documentSection().isEmpty();
        List<IntegrationProfile> res = query.getList();
        GazelleListDataModel<IntegrationProfile> dm = new GazelleListDataModel<IntegrationProfile>(res);
        return dm;
    }

    @Override
    @Create
    public void initialize() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initialize");
        }
        currentDisplayPanelLabel = "";
    }

    @Override
    @Destroy
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    @Override
    public DocumentFilter getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        return null;
    }

    @Override
    public GazelleListDataModel<ActorIntegrationProfileOption> getAipoWithoutDocumentSection() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAipoWithoutDocumentSection");
        }
        ActorIntegrationProfileOptionQuery query = new ActorIntegrationProfileOptionQuery();
        query.documentSection().isEmpty();
        query.integrationProfileOption().keyword().neq("NONE");
        List<ActorIntegrationProfileOption> res = query.getListDistinct();
        GazelleListDataModel<ActorIntegrationProfileOption> dm = new GazelleListDataModel<ActorIntegrationProfileOption>(res);
        return dm;
    }
}
