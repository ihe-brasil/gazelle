package net.ihe.gazelle.tm.datamodel;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.tm.filter.TMCriterions;
import net.ihe.gazelle.tm.gazelletest.model.definition.MetaTest;
import net.ihe.gazelle.tm.gazelletest.model.definition.MetaTestQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaTestDataModel extends FilterDataModel<MetaTest> {

    private static final Logger LOG = LoggerFactory.getLogger(MetaTestDataModel.class);
    private static final long serialVersionUID = 947693451422532138L;
    private String searchedText; // part of the keyword or description of the
    // meta test

    public MetaTestDataModel() {
        this(null);
    }

    public MetaTestDataModel(String searchedText) {
        super(new Filter<MetaTest>(getMetaTestCriterions()));
        setSearchedText(searchedText);
    }

    private static HQLCriterionsForFilter<MetaTest> getMetaTestCriterions() {
        MetaTestQuery query = new MetaTestQuery();
        HQLCriterionsForFilter<MetaTest> result = query.getHQLCriterionsForFilter();
        TMCriterions.addAIPOCriterionsUsingTest(result, query.testRolesList().test(), null);
        TMCriterions.addTestCriterions(result, query.testRolesList().test(), false);
        return result;
    }

    @Override
    public void appendFiltersFields(HQLQueryBuilder<MetaTest> queryBuilder) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("appendFiltersFields");
        }
        if ((searchedText != null) && !searchedText.isEmpty()) {
            queryBuilder.addRestriction(HQLRestrictions.or(HQLRestrictions.like("keyword", searchedText),
                    HQLRestrictions.like("description", searchedText)));
        }
    }

    public String getSearchedText() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSearchedText");
        }
        return searchedText;
    }

    public void setSearchedText(String searchedText) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSearchedText");
        }
        if ((searchedText != null) && !searchedText.isEmpty()) {
            this.searchedText = "%" + searchedText + "%";
        } else {
            this.searchedText = null;
        }
        resetCache();
    }

    @Override
    protected Object getId(MetaTest t) {
        // TODO Auto-generated method stub
        return t.getId();
    }
}
