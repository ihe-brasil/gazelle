package net.ihe.gazelle.tm.datamodel;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.HQLRestriction;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOption;
import net.ihe.gazelle.tf.model.ActorIntegrationProfileOptionQuery;
import net.ihe.gazelle.tf.model.Domain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AIPOForCheckDataModel extends FilterDataModel<ActorIntegrationProfileOption> {

    private static final Logger LOG = LoggerFactory.getLogger(AIPOForCheckDataModel.class);

    private static final long serialVersionUID = 4655237415079137457L;
    private List<Integer> listRestriction;
    private Domain domain;

    public AIPOForCheckDataModel() {
        super(new Filter<ActorIntegrationProfileOption>(
                new ActorIntegrationProfileOptionQuery().getHQLCriterionsForFilter()));
    }

    public List<Integer> getListRestriction() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getListRestriction");
        }
        return listRestriction;
    }

    public void setListRestriction(List<Integer> listRestriction) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setListRestriction");
        }
        this.listRestriction = listRestriction;
    }

    public void setDomainRestriction(Domain domain) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setDomainRestriction");
        }
        this.domain = domain;
    }

    @Override
    public void appendFiltersFields(HQLQueryBuilder<ActorIntegrationProfileOption> queryBuilder) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("appendFiltersFields");
        }
        if ((this.listRestriction != null) && (this.listRestriction.size() > 0)) {
            queryBuilder.addRestriction(HQLRestrictions.nin("id", this.listRestriction));
            if (domain != null) {
                ActorIntegrationProfileOptionQuery query = new ActorIntegrationProfileOptionQuery();
                HQLRestriction domainRestriction = query.actorIntegrationProfile().integrationProfile()
                        .inRestriction(domain.getIntegrationProfilesForDP());
                queryBuilder.addRestriction(domainRestriction);
            }
        }
    }

    @Override
    protected Object getId(ActorIntegrationProfileOption t) {
        // TODO Auto-generated method stub
        return t.getId();
    }
}
