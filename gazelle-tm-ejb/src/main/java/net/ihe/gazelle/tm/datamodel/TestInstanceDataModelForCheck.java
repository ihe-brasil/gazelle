package net.ihe.gazelle.tm.datamodel;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestType;
import net.ihe.gazelle.tm.gazelletest.model.instance.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestInstanceDataModelForCheck extends FilterDataModel<TestInstance> {

    private static final Logger LOG = LoggerFactory.getLogger(TestInstanceDataModelForCheck.class);

    private static final long serialVersionUID = 4744071894717283270L;
    private boolean testingSessionIsNull = false;

    public TestInstanceDataModelForCheck(Filter<TestInstance> filter) {
        super(filter);
    }

    @Override
    public void appendFiltersFields(HQLQueryBuilder<TestInstance> queryBuilder) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("appendFiltersFields");
        }
        super.appendFiltersFields(queryBuilder);
        if (testingSessionIsNull) {
            queryBuilder.addRestriction(HQLRestrictions.isNull("testingSession"));
        }
        queryBuilder.addIn("test.testType", TestType.getTestTypesWithoutMESA());
    }

    public boolean isTestingSessionIsNull() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isTestingSessionIsNull");
        }
        return testingSessionIsNull;
    }

    public void setTestingSessionIsNull(boolean testingSessionIsNull) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setTestingSessionIsNull");
        }
        this.testingSessionIsNull = testingSessionIsNull;
    }

    @Override
    protected Object getId(TestInstance t) {
        // TODO Auto-generated method stub
        return t.getId();
    }
}
