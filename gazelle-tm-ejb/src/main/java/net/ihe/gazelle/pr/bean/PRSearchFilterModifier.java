package net.ihe.gazelle.pr.bean;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.hql.criterion.HQLCriterion;
import net.ihe.gazelle.hql.criterion.QueryModifier;
import net.ihe.gazelle.pr.action.PRSearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PRSearchFilterModifier<E> extends Filter<E> {

    private static final long serialVersionUID = 5516918399594294277L;
    private static final Logger LOG = LoggerFactory.getLogger(PRSearchFilterModifier.class);
    private PRSearch prSearch;
    private boolean countEnabled = true;

    public PRSearchFilterModifier(Class<E> entityClass, List<QueryModifier<E>> queryModifiers,
                                  List<HQLCriterion<E, ?>> criterionsList, PRSearch prSearch, boolean countEnabled) {
        super(entityClass, queryModifiers, criterionsList, null, null);
        this.prSearch = prSearch;
        this.countEnabled = countEnabled;
    }

    @Override
    public void modified() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modified");
        }
        super.modified();
        if (prSearch != null) {
            prSearch.getFilter().modified();
        }
    }

    @Override
    public boolean isCountEnabled() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isCountEnabled");
        }
        return countEnabled;
    }

}
