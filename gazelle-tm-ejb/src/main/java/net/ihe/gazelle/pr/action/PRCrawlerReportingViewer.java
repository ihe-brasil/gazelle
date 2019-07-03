package net.ihe.gazelle.pr.action;

import net.ihe.gazelle.common.filter.Filter;
import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.hql.criterion.HQLCriterionsForFilter;
import net.ihe.gazelle.pr.systems.model.CrawlerReporting;
import net.ihe.gazelle.pr.systems.model.CrawlerReportingQuery;
import net.ihe.gazelle.tm.systems.model.System;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Remove;
import java.io.Serializable;
import java.util.List;

@Name("prCrawlerReportingViewer")
@Scope(ScopeType.PAGE)
public class PRCrawlerReportingViewer implements Serializable {

    private static final long serialVersionUID = 4438258032995904883L;
    private static final Logger LOG = LoggerFactory.getLogger(PRCrawlerReportingViewer.class);
    private Filter<CrawlerReporting> filter;
    private FilterDataModel<CrawlerReporting> crawlerReportings;
    private List<System> systemList;

    @Create
    public void create() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("create");
        }
    }

    @Remove
    @Destroy
    public void destroy() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("destroy");
        }

    }

    public Filter<CrawlerReporting> getFilter() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getFilter");
        }
        if (filter == null) {
            HQLCriterionsForFilter<CrawlerReporting> hqlCriterions = new CrawlerReportingQuery()
                    .getHQLCriterionsForFilter();
            filter = new Filter<CrawlerReporting>(hqlCriterions);
        }
        return filter;
    }

    public FilterDataModel<CrawlerReporting> getCrawlerReportings() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getCrawlerReportings");
        }
        if (crawlerReportings == null) {
            crawlerReportings = new FilterDataModel<CrawlerReporting>(getFilter()) {
                @Override
                protected Object getId(CrawlerReporting t) {
                    // TODO Auto-generated method stub
                    return t.getId();
                }
            };
        }
        return crawlerReportings;
    }

    private CrawlerReporting reload(CrawlerReporting crawlerReporting) {
        CrawlerReportingQuery crawlerReportingQuery = new CrawlerReportingQuery();
        crawlerReportingQuery.id().eq(crawlerReporting.getId());
        CrawlerReporting freshCrawlerReporting = crawlerReportingQuery.getUniqueResult();
        return freshCrawlerReporting;
    }

    public void viewUnmatchingSystems(CrawlerReporting crawlerReporting) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewUnmatchingSystems");
        }
        CrawlerReporting freshCrawlerReporting = reload(crawlerReporting);
        systemList = freshCrawlerReporting.getUnmatchingHashcodeForIntegrationStatements();
    }

    public void viewUnreachableSystems(CrawlerReporting crawlerReporting) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("viewUnreachableSystems");
        }
        CrawlerReporting freshCrawlerReporting = reload(crawlerReporting);
        systemList = freshCrawlerReporting.getUnreachableIntegrationStatements();
    }

    public List<System> getSystemList() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSystemList");
        }
        return systemList;
    }

}
