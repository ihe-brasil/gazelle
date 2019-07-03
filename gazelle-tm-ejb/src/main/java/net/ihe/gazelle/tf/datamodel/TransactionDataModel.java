package net.ihe.gazelle.tf.datamodel;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.tf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionDataModel extends FilterDataModel<Transaction> {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionDataModel.class);

    private static final long serialVersionUID = -321013614374100735L;

    private Domain selectedDomain;

    private IntegrationProfile selectedIntegrationProfile;

    private Actor selectedActorFrom;

    private Actor selectedActorTo;

    public TransactionDataModel() {
        this(null, null, null, null);
    }

    public TransactionDataModel(Domain dom, IntegrationProfile ip, Actor actFrom, Actor actTo) {
        super(new Filter<Transaction>(getCriterions()));
        this.selectedActorFrom = actFrom;
        this.selectedDomain = dom;
        this.selectedIntegrationProfile = ip;
    }

    private static HQLCriterionsForFilter<Transaction> getCriterions() {
        TransactionQuery query = new TransactionQuery();
        return query.getHQLCriterionsForFilter();
    }

    @Override
    public void appendFiltersFields(HQLQueryBuilder<Transaction> queryBuilder) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("appendFiltersFields");
        }
        if (selectedIntegrationProfile != null) {
            queryBuilder.addEq("profileLinks.actorIntegrationProfile.integrationProfile", selectedIntegrationProfile);
        }
        if (selectedDomain != null) {
            queryBuilder.addEq("profileLinks.actorIntegrationProfile.integrationProfile.domainsForDP.keyword",
                    selectedDomain.getKeyword());
        }
        if (selectedActorFrom != null) {
            queryBuilder.addEq("transactionLinks.fromActor.keyword", selectedActorFrom.getKeyword());
        }
        if (selectedActorTo != null) {
            queryBuilder.addEq("transactionLinks.toActor.keyword", selectedActorTo.getKeyword());
        }

    }

    public Domain getSelectedDomain() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedDomain");
        }
        return selectedDomain;
    }

    public void setSelectedDomain(Domain selectedDomain) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedDomain");
        }
        this.selectedDomain = selectedDomain;
        resetCache();
    }

    public IntegrationProfile getSelectedIntegrationProfile() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedIntegrationProfile");
        }
        return selectedIntegrationProfile;
    }

    public void setSelectedIntegrationProfile(IntegrationProfile selectedIntegrationProfile) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedIntegrationProfile");
        }
        this.selectedIntegrationProfile = selectedIntegrationProfile;
        resetCache();
    }

    public Actor getSelectedActorFrom() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedActorFrom");
        }
        return selectedActorFrom;
    }

    public void setSelectedActorFrom(Actor selectedActorFrom) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedActorFrom");
        }
        this.selectedActorFrom = selectedActorFrom;
        resetCache();
    }

    public Actor getSelectedActorTo() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSelectedActorTo");
        }
        return selectedActorTo;
    }

    public void setSelectedActorTo(Actor selectedActorTo) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSelectedActorTo");
        }
        this.selectedActorTo = selectedActorTo;
        resetCache();
    }

    @Override
    protected Object getId(Transaction t) {
        // TODO Auto-generated method stub
        return t.getId();
    }
}
