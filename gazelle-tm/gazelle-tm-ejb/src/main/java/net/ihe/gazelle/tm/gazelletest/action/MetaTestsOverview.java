package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.tm.datamodel.MetaTestDataModel;
import net.ihe.gazelle.tm.gazelletest.model.definition.MetaTest;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Remove;
import java.io.Serializable;

@Name("metaTestsOverview")
@Scope(ScopeType.PAGE)
public class MetaTestsOverview implements Serializable {
    private static final Logger LOG = LoggerFactory.getLogger(MetaTestsOverview.class);
    private static final long serialVersionUID = 4770287587320877870L;

    private String searchKeyword;

    private MetaTestDataModel foundMetaTests;

    @Create
    public void create() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("create");
        }
        searchKeyword = null;
    }

    @Remove
    @Destroy
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    public Filter<MetaTest> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        return getFoundMetaTests().getFilter();
    }

    public MetaTestDataModel getFoundMetaTests() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFoundMetaTests");
        }
        if (foundMetaTests == null) {
            foundMetaTests = new MetaTestDataModel(searchKeyword);
        }
        return foundMetaTests;
    }

    public String getSearchKeyword() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSearchKeyword");
        }
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("setSearchKeyword");
        }
        this.searchKeyword = searchKeyword;
        getFoundMetaTests().setSearchedText(searchKeyword);
    }

    public void resetFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("resetFilter");
        }
        getFoundMetaTests().getFilter().clear();
        setSearchKeyword(null);
    }

}
