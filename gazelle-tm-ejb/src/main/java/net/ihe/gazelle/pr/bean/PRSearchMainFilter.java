package net.ihe.gazelle.pr.bean;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.pr.action.PRSearch;
import net.ihe.gazelle.tm.systems.model.System;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PRSearchMainFilter extends Filter<System> {

    private static final long serialVersionUID = -4548101991236814123L;
    private static final Logger LOG = LoggerFactory.getLogger(PRSearchMainFilter.class);
    private PRSearch prSearch;

    public PRSearchMainFilter(HQLCriterionsForFilter<System> criterions, Map<String, String> requestParameterMap,
                              PRSearch prSearch) {
        super(criterions, requestParameterMap);
        this.prSearch = prSearch;
    }

    @Override
    public void modified() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modified");
        }
        super.modified();
        if (prSearch != null) {
            prSearch.setSystems(null);
        }
    }

}
