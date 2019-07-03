package net.ihe.gazelle.tm.datamodel;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.tm.gazelletest.model.instance.Status;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class TestInstanceDataModel extends FilterDataModel<TestInstance> {

    private static final Logger LOG = LoggerFactory.getLogger(TestInstanceDataModel.class);
    private static final long serialVersionUID = 2275736769856443605L;
    private Collection<Status> statuses;

    public TestInstanceDataModel(Filter<TestInstance> filter, Collection<Status> statuses) {
        super(filter);
        this.statuses = statuses;
    }

    @Override
    public void appendFiltersFields(HQLQueryBuilder<TestInstance> queryBuilder) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("appendFiltersFields");
        }
        super.appendFiltersFields(queryBuilder);
        if ((statuses != null) && (statuses.size() > 0)) {
            queryBuilder.addIn("lastStatus", statuses);
        }
    }

    @Override
    protected Object getId(TestInstance t) {
        // TODO Auto-generated method stub
        return t.getId();
    }
}
