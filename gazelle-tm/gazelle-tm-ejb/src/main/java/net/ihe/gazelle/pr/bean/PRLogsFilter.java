package net.ihe.gazelle.pr.bean;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.pr.action.PRLogs;
import net.ihe.gazelle.tm.systems.model.System;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PRLogsFilter extends Filter<System> {

    private static final long serialVersionUID = 8517909947482144026L;
    private static final Logger LOG = LoggerFactory.getLogger(PRLogsFilter.class);
    private PRLogs prLogs;

    public PRLogsFilter(HQLCriterionsForFilter<System> criterions, Map<String, String> requestParameterMap,
                        PRLogs prLogs) {
        super(criterions, requestParameterMap);
        this.prLogs = prLogs;
    }

    @Override
    public boolean isPossibleFiltered() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isPossibleFiltered");
        }
        return true;
    }

    @Override
    public boolean isCountEnabled() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("isCountEnabled");
        }
        return false;
    }

    @Override
    public String getUrlParameters() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUrlParameters");
        }
        return super.getUrlParameters() + "&interval=" + prLogs.getInterval().name() + "&type="
                + prLogs.getType().name() + "&mode=" + prLogs.getMode().name();
    }

}
