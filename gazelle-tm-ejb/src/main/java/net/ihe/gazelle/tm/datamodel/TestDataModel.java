package net.ihe.gazelle.tm.datamodel;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.hql.HQLQueryBuilder;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.hql.restrictions.HQLRestrictions;
import net.ihe.gazelle.tm.filter.TMCriterions;
import net.ihe.gazelle.tm.gazelletest.model.definition.Test;
import net.ihe.gazelle.tm.gazelletest.model.definition.TestQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import java.util.Map;

public class TestDataModel extends FilterDataModel<Test> {

    private static final Logger LOG = LoggerFactory.getLogger(TestDataModel.class);

    private static final long serialVersionUID = -4871674159294704471L;
    private String searchedText; // part of the keyword or name of the test

    public TestDataModel() {
        this(null);
    }

    public TestDataModel(String searchedText) {
        super(createFilter());
        setSearchedText(searchedText);
    }

    public TestDataModel(String searchedText, Filter<Test> filter) {
        super(filter);
        setSearchedText(searchedText);
    }

    public static Filter<Test> createFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("createFilter");
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, String> requestParameterMap = fc.getExternalContext().getRequestParameterMap();
        return new Filter<Test>(getCriterionList(), requestParameterMap);
    }

    private static HQLCriterionsForFilter<Test> getCriterionList() {
        TestQuery query = new TestQuery();

        HQLCriterionsForFilter<Test> result = query.getHQLCriterionsForFilter();

        TMCriterions.addTestCriterions(result, query, true);
        TMCriterions.addTestCriterion(result, query, true);
        TMCriterions.addAIPOCriterionsUsingTest(result, query, null);

        return result;
    }

    @Override
    public void appendFiltersFields(HQLQueryBuilder<Test> queryBuilder) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("appendFiltersFields");
        }
        if ((searchedText != null) && !searchedText.isEmpty()) {
            queryBuilder.addRestriction(HQLRestrictions.or(HQLRestrictions.like("keyword", searchedText),
                    HQLRestrictions.like("name", searchedText)));
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
            this.searchedText = searchedText;
        } else {
            this.searchedText = null;
        }
        resetCache();
    }

    @Override
    protected Object getId(Test t) {
        // TODO Auto-generated method stub
        return t.getId();
    }
}
